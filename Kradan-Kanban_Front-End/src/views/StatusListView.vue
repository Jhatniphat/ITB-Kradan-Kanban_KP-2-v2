<script setup>
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import AddStatusModal from '@/components/Status/AddStatusModal.vue';
import EditStatus from '@/components/Status/EditStatus.vue';
import DeleteStatus from '@/components/Status/DeleteStatus.vue';
import Modal from '@/components/Modal.vue';
import router from '@/router';
import { useStatusStore } from '@/stores/status.js';
import EditLimitStatus from '@/components/EditLimitStatus.vue';
import NavBar from '@/App.vue';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import { getAllStatus, getAllStatusForGuest } from '@/lib/fetchUtils.js';
import LoadingComponent from '@/components/loadingComponent.vue';
import { useToastStore } from '@/stores/toast.js';

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const statusStore = useStatusStore();
const route = useRoute();
const accountStore = useAccountStore();
const boardStore = useBoardStore();
const toastStore = useToastStore();

// ? ----------------- Modal ---------------
const toast = ref({ status: '', msg: '' });
const showAddModal = ref(false);
const showEdit = ref(false);
const showDelete = ref(false);
const showEditLimit = ref(false);
const showErrorModal = ref(false);
// ? ----------------- Common -------------------------
const selectedId = ref(0);
const deleteTitle = ref('');
const colorPicking = ref('');
const error = ref(null);
const status = ref(null);
const loading = ref(false);
const overStatuses = ref([]);
const isOwner = ref(false);
const canWrite = ref(false);
const canRead = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    await fetchStatusData();
    const currentBoards = boardStore.currentBoard;
    const userOid = accountStore.tokenDetail.oid;
    // if (currentBoards && currentBoards.owner?.oid) {
    isOwner.value = currentBoards.owner.oid === userOid;
    //   if (!isOwner.value && currentBoards.visibility === "PRIVATE") {
    //     router.push({name: "AccessDenied"});
    //   }
    // }
    if (isOwner.value) {
      canWrite.value = true;
    }
    if (!isOwner.value) {
      const currentUser = currentBoards.collaborators.find((collaborator) => collaborator.oid === accountStore.tokenDetail.oid);
      if (currentUser) {
        canRead.value = currentUser.accessRight === 'READ';
        canWrite.value = currentUser.accessRight === 'WRITE';
      } else {
        canRead.value = false;
        canWrite.value = false;
        // User is not a collaborator
      }
    } else {
      canRead.value = true;
    }

    if (!isOwner.value && !canRead.value && !canWrite && currentBoards.visibility === 'PRIVATE') {
      router.push({ name: 'AccessDenied' });
    }

    if (route.params.statusId !== undefined) {
      selectedId.value = parseInt(route.params.statusId);
      showEdit.value = true;
    }
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
});

async function fetchStatusData(id) {
  if (id !== undefined) {
    openEdit(id);
  }
  error.value = status.value = null;
  loading.value = true;
  try {
    if (accountStore.tokenRaw === '') {
      status.value = await getAllStatusForGuest();
    } else {
      status.value = await getAllStatus();
    }
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

// ! ================= Modal ======================

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
  if (typeof res === 'object') {
    toastStore.createToast('Add status successfuly');
    router.push({ name: 'status-list', hash: `#status-${res.id}` });
    statusStore.addStoreStatus(res);
  } else {
    toastStore.createToast('Add status Failed', 'danger');
  }
};

const openEdit = (id, color) => {
  selectedId.value = id;
  colorPicking.value = color;
  showEdit.value = true;
  router.push(`/board/${boardStore.currentBoardId}/status/${id}`);
};

const closeEdit = (res) => {
  showEdit.value = false;
  if (res === null) return 0;
  if (typeof res === 'object') {
    toastStore.createToast('Edit status successfuly');
    statusStore.editStoreStatus(res);
  } else toastStore.createToast('An error has occurred, the status does not exist', 'danger');
};

const openDelete = (id, title) => {
  selectedId.value = id;
  deleteTitle.value = title;
  showDelete.value = true;
};

const closeDelete = (res, numOfTasks) => {
  showDelete.value = false;
  if (res === null) return 0;
  if (typeof res === 'object') {
    statusStore.deleteStoreStatus(res);
    toastStore.createToast('Delete status successfuly');
    if (typeof res === 'object' && numOfTasks != undefined) {
      statusStore.deleteStoreStatus(res);
      toastStore.createToast(`Delete and Tranfer ${numOfTasks} tasks successfuly`);
    }
  } else {
    toastStore.createToast('Delete status Failed ,Please restart page', 'danger');
  }
};

function closeEditLimit(overStatus) {
  showEditLimit.value = false;
  toastStore.createToast(`The Kanban now limit ${statusStore.getLimit()} tasks in each status`);
  if (overStatus === null || overStatus === undefined) return 0;
  if (typeof overStatus === 'object') {
    showErrorModal.value = true;
    overStatuses.value = overStatus;
  }
}
</script>

<template>
  <!-- content -->
  <div class="opacity">
    <div class="flex flex-col">
      <!-- show edit limit modal -->
      <div class="w-3/4 mx-auto mt-10 relative">
        <div class="float-right flex flex-row">
          <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
            <button class="itbkk-button-add btn btn-square btn-outline w-16 float-left mr-1" @click="showAddModal = true" :disabled="!canWrite">+ Add</button>
          </div>
          <button class="btn btn-square btn-outline w-16 float-right" @click="showEditLimit = true">Limit Status</button>
        </div>
      </div>
      <!-- Table -->
      <transition>
        <loading-component v-if="loading" class="absolute top-1/2" />
      </transition>
      <transition>
        <table
          v-if="!loading"
          class="table table-lg table-pin-rows table-pin-cols w-3/4 font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1"
        >
          <!-- Head -->
          <thead>
            <tr>
              <th>No</th>
              <th>Name</th>
              <th>Description</th>
              <th>Action</th>
            </tr>
          </thead>
          <!-- Listing -->
          <tbody>
            <tr v-if="status === null">
              <td colspan="4">Waiting For Data</td>
            </tr>
            <tr v-if="status !== null" v-for="(status, index) in statusStore.status" :key="status.id" :id="`status-${status.id}`" class="itbkk-item hover">
              <td>{{ index + 1 }}</td>
              <td class="itbkk-status-name break-all">
                {{ status.name }}
              </td>
              <td
                class="itbkk-status-description break-all"
                :style="{
                  fontStyle: status.description ? 'normal' : 'italic',
                  color: status.description ? '' : 'gray',
                }"
              >
                {{ status.description === null || status.description === '' ? 'No description is provided' : status.description }}
              </td>
              <td v-if="status.name !== 'No Status' && status.name !== 'Done'" class="itbkk-action-button flex flex-row">
                <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
                  <button class="itbkk-button-edit btn m-2" @click="openEdit(status.id, status.color)" :disabled="!canWrite">Edit</button>
                </div>
                <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
                  <button class="itbkk-button-delete btn m-2" @click="openDelete(status.id, status.name)" :disabled="!canWrite">Delete</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </transition>
    </div>
  </div>

  <!-- Modal -->
  <!-- Add Status Modal-->
  <Modal :show-modal="showAddModal">
    <AddStatusModal @closeModal="closeAddModal" />
  </Modal>
  <!-- Edit Modal -->
  <Modal :show-modal="showEdit">
    <EditStatus :status-id="parseInt(selectedId)" :status-color="colorPicking" @close-modal="closeEdit" />
  </Modal>
  <!-- Delete Modal -->
  <Modal :show-modal="showDelete">
    <DeleteStatus :delete-id="parseInt(selectedId)" :deleteTitle="deleteTitle" @close-modal="closeDelete" />
  </Modal>
  <!-- edit limit modal-->
  <Modal :show-modal="showEditLimit">
    <EditLimitStatus :isOwnerOrNot="canWrite" @close-modal="closeEditLimit" />
  </Modal>

  <Modal :show-modal="showErrorModal">
    <div class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 rounded-lg w-full m-auto">
      <h2>Have Task Over Limit!!!</h2>
      <hr />
      <p>These statuses that have reached the task limit. No additional tasks can be added to these statuses.</p>
      <table class="table">
        <thead>
          <tr>
            <th>status name</th>
            <th>remaining tasks</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="status in overStatuses">
            <td>{{ status.name }}</td>
            <td>{{ status.task }}</td>
          </tr>
        </tbody>
      </table>
      <button class="btn btn-outline btn-primary w-fit self-end" @click="showErrorModal = false">OKAY</button>
    </div>
  </Modal>
</template>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
