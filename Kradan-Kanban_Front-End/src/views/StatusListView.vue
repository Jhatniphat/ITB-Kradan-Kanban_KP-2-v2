<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import AddStatusModal from "@/components/Status/AddStatusModal.vue";
import EditStatus from "@/components/Status/EditStatus.vue";
import DeleteStatus from "@/components/Status/DeleteStatus.vue";
import Modal from "@/components/Modal.vue";
import router from "@/router";
import { useStatusStore } from "@/stores/status.js";
import EditLimitStatus from "@/components/EditLimitStatus.vue";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const statusStore = useStatusStore();
const route = useRoute();

// ? ----------------- Modal ---------------
const toast = ref({ status: "", msg: "" });
const showAddModal = ref(false);
const showEdit = ref(false);
const showDelete = ref(false);
const showEditLimit = ref(false);
const showErrorModal = ref(false);
// ? ----------------- Common -------------------------
const selectedId = ref(0);
const deleteTitle = ref("");
const error = ref(null);
const status = ref(null);
const loading = ref(false);
const overStatuses = ref([]);
onMounted(() => {
  fetchStatusData();
  if (route.params.id !== undefined) {
    selectedId.value = parseInt(route.params.id);
    showEdit.value = true;
  }
});

async function fetchStatusData(id) {
  if (id !== undefined) {
    openEdit(id);
  }
  error.value = status.value = null;
  loading.value = true;
  try {
    status.value = await statusStore.getAllStatus();
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

// ! ================= Modal ======================
const showToast = (toastData) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = { ...{ status: "" } };
  }, 5000);
};

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
  if (typeof res === "object") {
    showToast({ status: "success", msg: "Add task successfuly" });
    statusStore.addStoreStatus(res);
  } else showToast({ status: "error", msg: "Add task Failed" });
};

const openEdit = (id) => {
  selectedId.value = id;
  showEdit.value = true;
  router.push(`/status/${id}`);
};

const closeEdit = (res) => {
  showEdit.value = false;
  if (res === null) return 0;
  // if (res === 404) showToast({status: "error", msg: "An error has occurred, the status does not exist"});
  if (typeof res === "object") {
    showToast({ status: "success", msg: "Edit task successfuly" });
    statusStore.editStoreStatus(res);
  } else
    showToast({
      status: "error",
      msg: "An error has occurred, the status does not exist",
    });
};

const openDelete = (id, title) => {
  selectedId.value = id;
  deleteTitle.value = title;
  showDelete.value = true;
};

const closeDelete = (res, numOfTasks) => {
  showDelete.value = false;
  if (res === null) return 0;
  // if (res === 404)
  if (typeof res === "object") {
    statusStore.deleteStoreStatus(res);
    showToast({ status: "success", msg: "Delete status successfuly" });
    if (typeof res === "object" && numOfTasks != undefined) {
      statusStore.deleteStoreStatus(res);
      showToast({
        status: "success",
        msg: `Delete and Tranfer ${numOfTasks} tasks successfuly`,
      });
    }
  } else {
    showToast(
      { status: "error", msg: "Delete status Failed ,Please restart page" },
      6000
    );
  }
};

function closeEditLimit(overStatus) {
  showEditLimit.value = false;
  showToast({ status : 'success' , msg : `The Kanban now limit ${statusStore.getLimit()} tasks in each status` } )
  if (overStatus === null || overStatus === undefined) return 0;
  if (typeof overStatus === "object") {
    showErrorModal.value = true;
    overStatuses.value = overStatus;
  }
}
</script>

<template>
  <!-- NavBar -->
  <div class="navbar bg-base-300 sticky top-0 z-40 flex justify-between">
    <div class="navbar-start p-4">
      <div class="flex flex-col text-center ml-4 italic">
        <h1 class="text-xl font-bold">ITB-Kradan-Kanban</h1>
        <h1 class="text-sm">By KP-2</h1>
      </div>
    </div>
    <!-- Add Status -->
    <div class="navbar-end">
      <button
        class="itbkk-button-add btn btn-square btn-outline w-16"
        @click="showAddModal = true"
      >
        Add Status
      </button>
    </div>
  </div>

  <!-- Home Button -->
  <div class="text-base breadcrumbs flex p-10">
    <ul>
      <li @click="router.push('/task')" class="cursor-pointer">Home</li>
      <li class="text-base font-semibold">Task Status</li>
    </ul>
  </div>

  <!-- content -->
  <div class="opacity">
    <div class="flex flex-col">
      <!-- show edit limit modal -->
      <div class="w-3/4 mx-auto mt-10 relative">
        <div class="float-right">
          <button
            class="itbkk-button-add btn btn-square btn-outline w-16"
            @click="showEditLimit = true"
          >
            Limit Status
          </button>
        </div>
      </div>
      <!-- Table -->
      <table
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
          <tr
            v-if="status !== null"
            v-for="(status, index) in status"
            :key="status.id"
            class="itbkk-item hover"
          >
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
              {{
                status.description === null || status.description === ""
                  ? "No description is provided"
                  : status.description
              }}
            </td>
            <td
              v-if="status.name !== 'No Status' && status.name !== 'Done'"
              class="itbkk-action-button"
            >
              <button
                class="itbkk-button-edit btn m-2"
                @click="openEdit(status.id)"
              >
                Edit
              </button>
              <button
                class="itbkk-button-delete btn m-2"
                @click="openDelete(status.id, status.name)"
              >
                Delete
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <!-- Modal -->
  <!-- Add Status Modal-->
  <Modal :show-modal="showAddModal">
    <AddStatusModal @closeModal="closeAddModal" />
  </Modal>
  <!-- Edit Modal -->
  <Modal :show-modal="showEdit">
    <EditStatus :status-id="parseInt(selectedId)" @close-modal="closeEdit" />
  </Modal>
  <!-- Delete Modal -->
  <Modal :show-modal="showDelete">
    <DeleteStatus
      :delete-id="parseInt(selectedId)"
      :deleteTitle="deleteTitle"
      @close-modal="closeDelete"
    />
  </Modal>
  <!-- edit limit modal-->
  <Modal :show-modal="showEditLimit">
    <EditLimitStatus @close-modal="closeEditLimit" />
  </Modal>

  <Modal :show-modal="showErrorModal">
    <div
      class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 rounded-lg w-full m-auto"
    >
      <h2>Have Task Over Limit!!!</h2>
      <hr />
      <p>
        These statuses that have reached the task limit. No additional tasks can
        be added to these statuses.
      </p>
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
      <button
        class="btn btn-outline btn-primary w-fit self-end"
        @click="showErrorModal = false"
      >
        OKAY
      </button>
    </div>
  </Modal>
  <!-- Toast -->
  <div class="toast">
    <div
      role="alert"
      class="alert"
      :class="`alert-${toast.status}`"
      v-if="toast.status !== ''"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6"
        fill="none"
        viewBox="0 0 24 24"
        v-if="toast.status === 'success'"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="stroke-current shrink-0 h-6 w-6"
        fill="none"
        viewBox="0 0 24 24"
        v-if="toast.status === 'error'"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
        />
      </svg>
      <span class="itbkk-message">{{ toast.msg }}</span>
    </div>
  </div>
</template>

<style scoped></style>
