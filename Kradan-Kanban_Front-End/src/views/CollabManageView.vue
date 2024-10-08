<script setup>
import { onBeforeMount, ref, watch } from "vue";
import { useRoute } from "vue-router";
import {
  getAllBoard,
  getAllTasks,
  getAllStatus,
  getAllTasksForGuest,
  getLimitStatusForGuest,
} from "../lib/fetchUtils.js";

// ? import component
import Modal from "../components/Modal.vue";
import { useBoardStore } from "@/stores/board.js";
import LoadingComponent from "@/components/loadingComponent.vue";
import { useAccountStore } from "@/stores/account.js";
import AddCollab from "@/components/Collab/AddCollabModal.vue";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const accountStore = useAccountStore();
const boardStore = useBoardStore();
const route = useRoute();
const currentRoute = route.params?.boardId;

// ? ----------------- Modal -------------------------
const showAddModal = ref(false);
const toast = ref({ status: "", msg: "" });

// ? ----------------- Common -------------------------
const loading = ref(false);
// const allCollabs = ref(null);
const error = ref(null);
const showErrorModal = ref(false); // * show Error from Edit Limit modal
const overStatuses = ref([]);
const currentBoardId = boardStore.currentBoardId;

const isOwner = ref(false);

const allCollabs = [
  {
    id: 1,
    title: "John Doe",
    assignees: "john.doe@example.com",
    status: "Read",
  },
  {
    id: 2,
    title: "Jane Smith",
    assignees: "jane.smith@example.com",
    status: "Write",
  },
  {
    id: 3,
    title: "Alice Johnson",
    assignees: "alice.johnson@example.com",
    status: "Write",
  },
  {
    id: 4,
    title: "Bob Brown",
    assignees: "bob.brown@example.com",
    status: "Read",
  },
];

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
  if (typeof res === "object") {
    showToast({ status: "success", msg: "Add Collaborator successfully" });
  } else if (res === 403) {
    showToast({
      status: "AccessDenied",
      msg: "You do not have permission to add board collaborator.",
    });
  } else
    showToast({
      status: "error",
      msg: "There is a problem. Please try again later.",
    });
};

// ! ================= Toast ======================
const showToast = (toastData, timeOut = 3000) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = { ...{ status: "" } };
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
          <h1 class="itbkk-board-name m-2">
            {{ boardStore.currentBoard.name }}
          </h1>
          <h1 class="m-2">></h1>
          <h1 class="m-2">Collaborator</h1>
        </div>
        <!-- show Add Collab modal -->
        <div class="float-right flex flex-row">
          <div
            :class="isOwner ? '' : 'lg:tooltip'"
            data-tip="You don't have a permission to Add a Collaborator"
          >
            <button
              class="itbkk-collaborator-add btn btn-outline w-40 float-left mb-2"
              @click="showAddModal = true"
            >
              Add Collaborator
            </button>
          </div>
        </div>

        <!-- Content -->
        <div class="opacity">
          <div class="w-full">
            <!-- Table -->
            <table
              class="table table-lg table-pin-rows table-pin-cols font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1"
            >
              <!-- head -->
              <thead>
                <tr>
                  <th>No</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Access Right</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <!-- Listing -->
                <tr v-if="allCollabs === null">
                  <td colspan="4">Waiting For Data</td>
                </tr>
                <tr
                  v-if="allCollabs !== null"
                  v-for="(task, index) in allCollabs"
                  :key="task.id"
                  class="itbkk-item hover"
                >
                  <th>{{ index + 1 }}</th>
                  <td class="itbkk-name">
                    {{ task.title }}
                  </td>
                  <td class="itbkk-email">
                    {{ task.assignees }}
                  </td>
                  <td class="itbkk-status itbkk-access-right">
                    <!-- <label class="form-control w-full max-w-xs">
                    <div class="label">
                      <span class="label-text">{{ task.status }}t</span>
                    </div>
                    <select
                      class="itbkk-access-right select select-bordered bg-white dark:bg-base-300 dark:text-slate-400"
                      v-model="task.status"
                    >
                      <option
                        v-for="status in statusStore.getAllStatusWithLimit()"
                        :value="status.name"
                        :disabled="status.isLimit"
                      >
                        {{ status.name }}
                        <span class="text-error">
                          {{ status.isLimit ? "(max)" : "" }}
                        </span>
                      </option>
                    </select>
                  </label> -->
                    {{ task.status }}
                  </td>
                  <td>
                    <div>
                      <button
                        class="itbkk-collab-remove btn m-2"
                        :disabled="!isOwner"
                        :class="{ disabled: !isOwner }"
                        @click=""
                      >
                        Remove
                      </button>
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

          <!-- Error Modal -->
          <Modal :show-modal="showErrorModal">
            <div
              class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 rounded-lg w-full m-auto"
            >
              <h2>Have Task Over Limit!!!</h2>
              <hr />
              <p>
                These statuses that have reached the task limit. No additional
                tasks can be added to these statuses.
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
  background-image: linear-gradient(
    45deg,
    magenta,
    rebeccapurple,
    dodgerblue,
    green
  );
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
