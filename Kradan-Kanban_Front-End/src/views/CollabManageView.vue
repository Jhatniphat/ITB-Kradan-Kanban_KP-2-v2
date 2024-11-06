<script setup>
import { onBeforeMount, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getAllCollabs, changeAccessRight, deleteCollaborator } from '../lib/fetchUtils.js';

// ? import component
import Modal from '../components/Modal.vue';
import { useBoardStore } from '@/stores/board.js';
import LoadingComponent from '@/components/loadingComponent.vue';
import { useAccountStore } from '@/stores/account.js';
import AddCollab from '@/components/Collab/AddCollabModal.vue';
import router from '@/router/index.js';
import { useToastStore } from '@/stores/toast.js';

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const accountStore = useAccountStore();
const boardStore = useBoardStore();
const route = useRoute();
const currentRoute = route.params?.boardId;
const toastStore = useToastStore();

// ? ----------------- Modal -------------------------
const showAddModal = ref(false);
const showChangeAccessModal = ref(false);
const showRemoveCollabModal = ref(false);
const toast = ref({ status: '', msg: '' });

// ? ----------------- Common -------------------------
const loading = ref(false);
// const allCollabs = ref(null);
const error = ref(null);
const showErrorModal = ref(false); // * show Error from Edit Limit modal
const overStatuses = ref([]);
const currentBoardId = boardStore.currentBoardId;
const isOwner = ref(false);
const selectCollabName = ref('');
const selectAccesRight = ref('');
const selectCollabId = ref('');
const allCollabs = ref([]);

// Function to fetch collaborators
const fetchCollaborators = async () => {
  loading.value = true;
  const response = await getAllCollabs(currentBoardId);
  if (response.error) {
    error.value = response.message;
    showToast({ status: 'error', msg: response.message });
    toastStore.createToast(response.message, 'danger');
  } else {
    response.sort((a, b) => new Date(a.addedOn) - new Date(b.addedOn))
    allCollabs.value = response; // Set the collaborator data
  }
  loading.value = false;
};

// Fetch collaborators when the component is mounted
onBeforeMount(async () => {
  await fetchCollaborators();
  console.log(allCollabs);
  const currentBoard = boardStore.currentBoard;
  isOwner.value = currentBoard.owner.oid === accountStore.tokenDetail.oid;
});

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
};

const openAccessRight = (collabId, name, accessRight) => {
  selectCollabId.value = collabId;
  selectCollabName.value = name;
  selectAccesRight.value = accessRight;
  showChangeAccessModal.value = true;
};

async function callChangeAccesRight() {
  let res;
  try {
    res = await changeAccessRight(boardStore.currentBoardId, selectCollabId.value, selectAccesRight.value);
    if (typeof res === 'object') {
      toastStore.createToast('Change access right successfully');
    } else {
      toastStore.createToast('Change access right Failed', 'danger');
    }
  } catch (err) {
    console.error(err);
  } finally {
    showChangeAccessModal.value = false;
  }
}

const removeCollaborator = (collabId, name) => {
  selectCollabId.value = collabId;
  selectCollabName.value = name;
  console.log(selectCollabName.value);
  showRemoveCollabModal.value = true;
};

async function callDeleteCollaborator() {
  let res;
  try {
    res = await deleteCollaborator(boardStore.currentBoardId, selectCollabId.value);
    if (typeof res === 'object') {
      toastStore.createToast('Delete collaborator successfully');
    } else {
      toastStore.createToast('Delete collaborator Failed, Please Refresh Page', 'danger');
    }
  } catch (err) {
    console.error(err);
  } finally {
    showRemoveCollabModal.value = false;
  }
}

// ! ================= Toast ======================
const showToast = (toastData, timeOut = 3000) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = { ...{ status: '' } };
  }, timeOut);
};
</script>

<template>
  <transition>
    <div class="h-full w-full" v-if="!loading">
      <div class="w-3/4 mx-auto mt-10 relative" v-if="!loading">
        <h1 class="w-full text-center text-2xl">Collaborator Management</h1>
      </div>
      <div class="w-3/4 mx-auto mt-10 relative">
        <div class="inline-flex">
          <a class="itbkk-board-name m-2" @click="router.push(`/board/${boardStore.currentBoardId}`)">
            {{ boardStore.currentBoard.name }}
          </a>
          <p class="mr-2 mt-3">></p>
          <p class="mr-2 mt-3">Collaborator</p>
        </div>
        <!-- show Add Collab modal -->
        <div class="float-right flex flex-row">
          <div :class="isOwner ? '' : 'lg:tooltip'" data-tip="You need to be board owner to perform this action.">
            <button class="itbkk-collaborator-add btn btn-outline w-40 float-left mb-2" @click="showAddModal = true" :disabled="!isOwner">Add Collaborator</button>
          </div>
        </div>

        <!-- Content -->
        <div class="opacity">
          <div class="w-full">
            <!-- Table -->
            <table class="table table-lg table-pin-rows table-pin-cols font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1">
              <!-- head -->
              <thead>
                <tr>
                  <th>No</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Access Right</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <!-- Listing -->
                <tr v-if="allCollabs === null">
                  <td colspan="4">Waiting For Data</td>
                </tr>
                <tr v-if="allCollabs !== null" v-for="(collab, index) in boardStore.currentBoard.collaborators" :key="collab.oid" class="itbkk-item hover">
                  <th>{{ index + 1 }}</th>
                  <td class="itbkk-name">
                    {{ collab.name }}
                  </td>
                  <td class="itbkk-email">
                    {{ collab.email }}
                  </td>
                  <td class="itbkk-status itbkk-access-right">
                    <label class="form-control w-full max-w-xs">
                      <div :class="isOwner ? '' : 'lg:tooltip'" data-tip="You need to be board owner to perform this action.">
                        <select
                          class="itbkk-access-right select select-bordered bg-white dark:bg-base-300 dark:text-slate-400"
                          v-model="collab.accessRight"
                          @change="openAccessRight(collab.oid, collab.name, collab.accessRight)"
                          :disabled="!isOwner"
                        >
                          <option value="READ">READ</option>
                          <option value="WRITE">WRITE</option>
                        </select>
                      </div>
                    </label>
                  </td>
                  <td>
                    <div :class="isOwner ? '' : 'lg:tooltip'" data-tip="You need to be board owner to perform this action.">
                      <button class="itbkk-collab-remove btn m-2" :disabled="!isOwner" :class="{ disabled: !isOwner }" @click="removeCollaborator(collab.oid, collab.name)">Remove</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- Modal -->
          <!-- Add Collab Modal -->
          <Modal :show-modal="showAddModal">
            <AddCollab @closeModal="closeAddModal" />
          </Modal>

          <!-- Change AccessRight Modal -->
          <Modal :show-modal="showChangeAccessModal">
            <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
              <h1 class="m-2 pb-4 text-2xl font-bold">Change Access Right</h1>
              <hr />
              <h1 class="itbkk-message font-semibold text-xl p-8">Do you want to change access right of "{{ selectCollabName }}" to "{{ selectAccesRight }}"</h1>
              <hr />
              <div class="flex flex-row-reverse gap-4 mt-5">
                <button @click="showChangeAccessModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
                <button @click="callChangeAccesRight()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
                  {{ loading ? '' : 'Confirm' }}
                  <span class="loading loading-spinner text-success" v-if="loading"></span>
                </button>
              </div>
            </div>
          </Modal>

          <!-- Change AccessRight Modal -->
          <Modal :show-modal="showRemoveCollabModal">
            <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
              <h1 class="m-2 pb-4 text-2xl font-bold">Remove Collaborator</h1>
              <hr />
              <h1 class="itbkk-message font-semibold text-xl p-8">Do you want to remove "{{ selectCollabName }}" from the board?</h1>
              <hr />
              <div class="flex flex-row-reverse gap-4 mt-5">
                <button @click="showRemoveCollabModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
                <button @click="callDeleteCollaborator()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
                  {{ loading ? '' : 'Confirm' }}
                  <span class="loading loading-spinner text-success" v-if="loading"></span>
                </button>
              </div>
            </div>
          </Modal>

          <!-- Error Modal -->
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

          <!-- Toast -->
          <div class="toast">
            <div role="alert" class="alert" :class="`alert-${toast.status}`" v-if="toast.status !== ''">
              <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24" v-if="toast.status === 'success'">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24" v-if="toast.status === 'error'">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span>{{ toast.msg }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </transition>
  <!-- <transition>
    <loading-component v-if="loading"></loading-component>
  </transition> -->
</template>

<style scoped>
::backdrop {
  background-image: linear-gradient(45deg, magenta, rebeccapurple, dodgerblue, green);
  opacity: 0.75;
}

.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
}
</style>
