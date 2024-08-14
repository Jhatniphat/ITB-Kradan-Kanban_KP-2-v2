<script setup>
// ? import lib
import {onBeforeMount, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {deleteTask, getLimitStatus} from "../lib/fetchUtils.js";
import router from "@/router";
import {useTaskStore} from "@/stores/task";
import {useStatusStore} from "@/stores/status";
// ? import component
import Modal from "../components/Modal.vue";
import Taskdetail from "../components/Tasks/Taskdetail.vue";
import AddTaskModal from "@/components/Tasks/AddTaskModal.vue";
import EditLimitStatus from "@/components/EditLimitStatus.vue";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const taskStore = useTaskStore();
const statusStore = useStatusStore();
const route = useRoute();

// ? ----------------- Modal -------------------------
const showDetailModal = ref(false);
const showDeleteModal = ref(false);
const showAddModal = ref(false);
const toast = ref({status: "", msg: ""});
const showEditLimit = ref(false); // * show modal edit limit of task status

// ? ----------------- Common -------------------------
const loading = ref(false);
const allTasks = ref(null);
const filteredTasks = ref(null); // * allTasks that filter ready to show!
const error = ref(null);
const selectedId = ref(0); // * use to show detail and delete
const limitStatusValue = ref({isEnable: true, limit: 10}); // * obj for EditLimit modal
const showErrorModal = ref(false) // * show Error from Edit Limit modal
const overStatuses = ref([])

// ! ================= Modal ======================
const openEditMode = (id) => {
  showDetailModal.value = true;
  router.push(`/task/${id}`);
};

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
  if (typeof(res) === "object") {
    showToast({status: "success", msg: "Add task successfully"});
    taskStore.addStoreTask(res);
    // const
  } else showToast({status: "error", msg: "Add task Failed"});
};

const closeEditModal = (res) => {
  showDetailModal.value = false;
  if (res === null) return 0;
  if (typeof(res) === "object") {
    showToast({status: "success", msg: "Edit task successfully"});
    taskStore.editStoreTask(res);
  } else {
    showToast({status: "error", msg: "The error occurred, the status does not exist"});
  }
};

function closeEditLimit(overStatus) {
  showEditLimit.value = false
  showToast({ status : 'success' , msg : `The Kanban now limit ${statusStore.getLimit()} tasks in each status` } )
  if (overStatus === null || overStatus === undefined) return 0;
  if (typeof (overStatus) === "object") {
    showErrorModal.value = true
    overStatuses.value = overStatus
  }
}

// ! ================= Toast ======================
const showToast = (toastData, timeOut = 3000) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = {...{status: ""}};
  }, timeOut);
};

// ! ================= Delete Confirm ======================
const deleteTaskTitle = ref("");

const deleteThisTask = async () => {
  let res;
  try {
    res = await deleteTask(selectedId.value);
    if (typeof(res) === "object") {
      taskStore.deleteStoreTask(res);
      showToast({status: "success", msg: "Delete task successfully"});
    } else showToast({status: "error", msg: "Delete task Failed , Please Refresh Page"});
  } catch (error) {
    console.log(error);
  } finally {
    showDeleteModal.value = false;
  }
};

const openDeleteModal = (taskTitle, id) => {
  deleteTaskTitle.value = taskTitle;
  selectedId.value = id;
  showDeleteModal.value = true;
};


// ? open = true , close = false
// function EditLimitModal(openOrClose) {
//   if (openOrClose) {
//     limitStatusValue.value = { isEnable: statusStore.getLimitEnable(), limit: statusStore.getLimit() }
//     showEditLimit.value = true
//   } else {
//     statusStore.setLimitEnable(limitStatusValue.value.isEnable)
//     statusStore.setLimit(limitStatusValue.value.limit)
//     showEditLimit.value = false
//     let overStatus = statusStore.getOverStatus()
//     if (overStatus.length > 0) {
//       showToast({status: "error", msg: `Has Over Limit Status : ${overStatus.join(" , ")}`}, 5000);
//     }
//   }
// }

// const limitStatusValueError = computed( () => {
//   return limitStatusValue.value.limit > 30 ? `Limit Can't Be Over 30!!` : '' }
// )
// ! ================= open Detail ======================
const openModal = (id) => {
  selectedId.value = id;
  showDetailModal.value = true;
};

async function fetchData(id) {
  if (id !== undefined) {
    openModal(id);
  }
  error.value = allTasks.value = null;
  loading.value = false;
  try {
    // replace `getPost` with your data fetching util / API wrapper
    allTasks.value = await taskStore.getAllTasks();
    if (typeof allTasks.value === "object") {
      filteredTasks.value = allTasks.value;
    }
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

watch(() => route.params.id, fetchData, {immediate: true});

// ! ================= Filter and Sort ======================
const filterBy = ref([]);
const sortBy = ref("");
watch(() => [filterBy.value, sortBy.value], filterData, {immediate: true, deep: true});
// watch(filterBy, (newValue) => {
//   if (newValue.length === 0) {
//     filteredTasks.value = allTasks.value;
//   }
// });

async function filterData([filter, sort]) {
  let allTasks = [];
  allTasks = taskStore.tasks;
  if (filter.length > 0) {
    filteredTasks.value = allTasks.filter((task) => {
      return filter.some((fil) => fil === task.status);
    });
  } else filteredTasks.value = allTasks;
  switch (sort) {
    case "":
      filteredTasks.value = filteredTasks.value.sort((a, b) => a.id - b.id);
      break;
    case "ASC":
      filteredTasks.value = filteredTasks.value.sort((a, b) =>
          a.status.localeCompare(b.status)
      );
      break;
    case "DESC":
      filteredTasks.value = filteredTasks.value.sort((a, b) =>
          b.status.localeCompare(a.status)
      );
      break;
  }
  // ? .sort ต้องการค่า - + ออกมา
  // ? .localeCompare จะ  return ออกมาว่าห่างกันเท่าไหร่ เช่น 'a'.localeCompare('c') > -2
}

// sort function
function sortBtn() {
  switch (sortBy.value) {
    case "":
      sortBy.value = "ASC";
      break;
    case "ASC":
      sortBy.value = "DESC";
      break;
    case "DESC":
      sortBy.value = "";
      break;
  }
}

onBeforeMount(async () => {
  statusStore.getAllStatus();
  statusStore.getLimitEnable();
  const res = await getLimitStatus()
  statusStore.setLimitEnable(await res)
  if (route.params.id !== undefined) {
    selectedId.value = parseInt(route.params.id);
    showDetailModal.value = true;
  }
});
</script>

<template>
  <!-- NavBar -->
  <div class="navbar bg-base-300 sticky top-0 z-40">
    <div class="navbar-start p-4">
      <div class="flex flex-col text-center ml-4 italic">
        <h1 class="text-xl font-bold">ITB-Kradan-Kanban</h1>
        <h1 class="text-sm">By KP-2</h1>
      </div>
    </div>
    <!-- Add button -->
    <div class="navbar-end">
      <button
          class="itbkk-button-add btn btn-square btn-outline w-16 m-2"
          @click="showAddModal = true"
      >
        + ADD
      </button>
      <div class="manage-status">
        <button
            @click="router.push('/status')"
            class="itbkk-manage-status btn btn-square btn-outline w-20 m-2"
        >
          Manage Status
        </button>
      </div>
    </div>
    <!-- Manage Status Button -->
  </div>

  <!-- dropdowns status -->
  <div class="w-3/4 mx-auto mt-10 relative">
    <details class="dropdown">
      <summary class="m-1 btn no-animation itbkk-status-filter">Filter Status</summary>
      <!-- FilterStatus -->
      <ul class="absolute dropdown-menu z-[1000] rounded-box">
        <li
            v-for="status in statusStore.status"
            :key="status"
            class="menu p-2 shadow bg-base-100 w-52 itbkk-status-choice"
            tabindex="0"
        >
          <div>
            <input
                type="checkbox"
                class="checkbox"
                :id="status.id"
                :value="status.name"
                v-model="filterBy"
            />
            <label :for="status.id">{{ status.name }}</label>
          </div>
        </li>
      </ul>
    </details>

    <!-- reset button -->
    <button class="itbkk-filter-clear btn" @click="filterBy = []">Reset</button>

    <!-- show edit limit modal -->
    <div class="float-right">
      <button
          class="itbkk-button-add btn btn-square btn-outline w-16"
          @click="showEditLimit = true"
      >
        Limit Status
      </button>
    </div>
  </div>

  <!-- selected filter status -->
  <div class="w-3/4 mx-auto relative">
    <div class="border rounded-md w-auto p-2" v-if="filterBy.length > 0">
      Filtered Status:
      <div
          class="itbkk-filter-item badge font-semibold w-auto m-1"
          v-for="(status, index) in filterBy"
          :key="index"
      >
        {{ status }}
        <button
            @click="filterBy.splice(index, 1)"
            class="itbkk-filter-item-clear ml-1 text-red-600"
        >
          X
        </button>
      </div>
    </div>
  </div>

  <!-- Content -->
  <div class="opacity">
    <div class="flex flex-col">
      <!-- Table -->
      <table
          class="table table-lg table-pin-rows table-pin-cols w-3/4 font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1"
      >
        <!-- head -->
        <thead>
        <tr>
          <th>No</th>
          <th>Title</th>
          <th>Assignees</th>
          <!-- sort button -->
          <button class="itbkk-status-sort" @click="sortBtn()">
            <th class="flex justify-center">
              Status
              <!-- default sort button -->
              <svg
                  v-if="sortBy === ''"
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
              >
                <path
                    fill="currentColor"
                    d="M11 9h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5zm-6 3h2V8h3L6 4L2 8h3z"
                />
              </svg>
              <!-- ASC Button -->
              <svg
                  v-if="sortBy === 'ASC'"
                  class="text-pink-400"
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
              >
                <path
                    fill="#323ffb"
                    d="M11 9h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5zm-6 3h2V8h3L6 4L2 8h3z"
                />
              </svg>
              <!-- DESC Button -->
              <svg
                  v-if="sortBy === 'DESC'"
                  class="text-pink-400"
                  xmlns="http://www.w3.org/2000/svg"
                  width="18"
                  height="18"
                  viewBox="0 0 24 24"
              >
                <path
                    fill="#323ffb"
                    d="m6 20l4-4H7V4H5v12H2zm5-12h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5z"
                />
              </svg>
            </th>
          </button>
          <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <!-- Listing -->
        <tr v-if="allTasks === null">
          <td colspan="4">Waiting For Data</td>
        </tr>
        <tr
            v-if="allTasks !== null"
            v-for="(task, index) in filteredTasks"
            :key="task.id"
            class="itbkk-item hover"
        >
          <th>{{ index + 1 }}</th>
          <td class="itbkk-title">
            <!-- <RouterLink :to="`/task/${task.id}`"> -->
            <button @click="router.push(`/task/${task.id}`)">
              {{ task.title }}
            </button>
            <!-- </RouterLink> -->
          </td>
          <td
              class="itbkk-assignees"
              :style="{
                fontStyle: task.assignees ? 'normal' : 'italic',
                color: task.assignees ? '' : 'gray',
              }"
          >
            {{
              task.assignees === null || task.assignees == ""
                  ? "Unassigned"
                  : task.assignees
            }}
          </td>
          <td class="itbkk-status">{{ task.status }}</td>
          <td class="">
            <div class="dropdown dropdown-bottom dropdown-end">
              <div tabindex="0" role="button" class="btn m-1">
                <svg
                    class="swap-off fill-current"
                    xmlns="http://www.w3.org/2000/svg"
                    width="32"
                    height="32"
                    viewBox="0 0 512 512"
                >
                  <path
                      d="M64,384H448V341.33H64Zm0-106.67H448V234.67H64ZM64,128v42.67H448V128Z"
                  />
                </svg>
              </div>
              <ul
                  tabindex="0"
                  class="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-52"
              >
                <li>
                  <a @click="openEditMode(task.id)">Edit</a>
                </li>
                <li>
                  <a @click="openDeleteModal(task.title, task.id)">Delete</a>
                </li>
              </ul>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <!-- DetailsModal -->
    <!-- EditModal -->
    <Modal :show-modal="showDetailModal">
      <Taskdetail :taskId="parseInt(selectedId)" @closeModal="closeEditModal"/>
    </Modal>
    <!-- Add Modal -->
    <Modal :show-modal="showAddModal">
      <AddTaskModal @closeModal="closeAddModal"/>
    </Modal>

    <!-- DeleteModal -->
    <Modal :showModal="showDeleteModal">
      <div class="flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full">
        <h1 class="m-2 pb-4 text-2xl font-bold">
          DELETE: {{ deleteTaskTitle }}
        </h1>
        <hr/>
        <h1 class="itbkk-message font-semibold text-xl p-8">
          <!-- Do you want to delete the task "{{ deleteTaskTitle }}" -->
          ARE YOU SURE TO DELETE THIS TASK ?
        </h1>
        <hr/>
        <div class="flex flex-row-reverse gap-4 mt-5">
          <button
              @click="showDeleteModal = false"
              class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
          >
            Close
          </button>
          <button
              @click="deleteThisTask()"
              class="itbkk-button-confirm btn btn-outline btn-success basis-1/6"
          >
            {{ loading ? "" : "Confirm" }}
            <span
                class="loading loading-spinner text-success"
                v-if="loading"
            ></span>
          </button>
        </div>
      </div>
    </Modal>

    <!-- edit limit modal-->
    <Modal :show-modal="showEditLimit">
      <EditLimitStatus @close-modal="closeEditLimit"/>
    </Modal>

    <!-- Error Modal -->
    <Modal :show-modal="showErrorModal">
      <div class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 rounded-lg w-full m-auto">
        <h2>Have Task Over Limit!!!</h2>
        <hr>
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
</style>
