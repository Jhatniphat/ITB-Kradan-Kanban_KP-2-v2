<script setup>
// ? import lib
import { onBeforeMount, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { deleteTask, getAllBoard, getLimitStatus, changeVisibility, getAllTasks, getAllStatus, getAllTasksForGuest, getAllStatusForGuest, getLimitStatusForGuest } from '../lib/fetchUtils.js';
import router from '@/router';
import { useTaskStore } from '@/stores/task';
import { useStatusStore } from '@/stores/status';
import { useAccountStore } from '@/stores/account.js';
import { useToastStore } from '@/stores/toast.js';
import { useBoardStore } from '@/stores/board.js';
// ! import component
import Modal from '../components/Modal.vue';
import Taskdetail from '../components/Tasks/Taskdetail.vue';
import AddTaskModal from '@/components/Tasks/AddTaskModal.vue';
import EditLimitStatus from '@/components/EditLimitStatus.vue';
import LoadingComponent from '@/components/loadingComponent.vue';

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const accountStore = useAccountStore();
const taskStore = useTaskStore();
const statusStore = useStatusStore();
const boardStore = useBoardStore();
const route = useRoute();
const currentRoute = route.params?.boardId;
const toastStore = useToastStore();

// ? ----------------- Modal -------------------------
const showDetailModal = ref(false);
const showDeleteModal = ref(false);
const showAddModal = ref(false);
const toast = ref({ status: '', msg: '' });
const showEditLimit = ref(false); // * show modal edit limit of task status

// ? ----------------- Common -------------------------
const loading = ref(false);
const allTasks = ref(null);
const filteredTasks = ref(null); // * allTasks that filter ready to show!
const error = ref(null);
const selectedId = ref(0); // * use to show detail and delete
const limitStatusValue = ref({ isEnable: true, limit: 10 }); // * obj for EditLimit modal
const showErrorModal = ref(false); // * show Error from Edit Limit modal
const overStatuses = ref([]);
const currentBoardId = useBoardStore().currentBoardId;
// const currentBoardId = ref(route.params.boardId);
const kanbanData = ref([]);
const isOwner = ref(false);
const canRead = ref(false);
const canWrite = ref(false);
const taskListType = ref(taskStore.taskListType);
// ! ================= Modal ======================

const openEditMode = (id) => {
  showDetailModal.value = true;
  router.push(`/board/${currentBoardId}/task/${id}/edit`);
};

const closeAddModal = (res) => {
  showAddModal.value = false;
  if (res === null) return 0;
  if (typeof res === 'object') {
    toastStore.createToast('Add task successfully');
    router.push({ name: 'task-list', params: { boardId: currentBoardId } , hash : '#task-' + res.id });
    taskStore.addStoreTask(res);
    console.log(res.createAnotherTask);
    showAddModal.value = res.createAnotherTask;
    // const
  } else {
    toastStore.createToast('Add task Failed', 'danger');
  }
};

const closeEditModal = (res) => {
  console.log(res);
  showDetailModal.value = false;
  if (res === null) return 0;
  if (res === 200) {
    toastStore.createToast('Edit task successfully');
    taskStore.editStoreTask(res);
  } else if (res === 400){
    toastStore.createToast('The error occurred, the task does not exist', 'danger');
  } else {
    console.log(res);
    toastStore.createToast('Edit task Failed', 'danger');
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

// ! ================= Delete Confirm ======================
const deleteTaskTitle = ref('');
const deleteTaskId = ref(0);

const deleteThisTask = async () => {
  let res;
  try {
    res = await deleteTask(selectedId.value);
    if (typeof res === 'object') {
      taskStore.deleteStoreTask(res);
      toastStore.createToast('Delete task successfully');
    } else {
      toastStore.createToast('Delete task Failed , Please Refresh Page', 'danger');
    }
  } catch (error) {
    console.log(error);
  } finally {
    showDeleteModal.value = false;
  }
};

const openDeleteModal = (taskTitle, id) => {
  deleteTaskTitle.value = taskTitle;
  deleteTaskId.value = id;
  selectedId.value = id;
  showDeleteModal.value = true;
};

// ! ================= open Detail ======================
const openModal = (id) => {
  selectedId.value = id;
  showDetailModal.value = true;
};

async function fetchData([boardId, taskId]) {
  if (taskId !== undefined) {
    openModal(taskId);
  }
  error.value = allTasks.value = null;
  loading.value = true;
  try {
    filterData([filterBy.value, sortBy.value]);

    if (boardStore.currentBoard.visibility === 'PUBLIC' && accountStore.tokenRaw === '') {
      await getAllStatusForGuest();
      await getAllTasksForGuest();
      await getLimitStatusForGuest();
    } else {
      await getAllStatus();
      await getAllTasks();
      await getLimitStatus();
    }

    allTasks.value = taskStore.tasks;

    if (route.params.taskId !== undefined) {
      selectedId.value = parseInt(route.params.taskId);
      showDetailModal.value = true;
    }
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

watch(
  () => [route.params.boardId, route.params?.taskId],
  async (boardIdAndTaskId) => {
    await setCurrentBoard(boardIdAndTaskId[0]);
    await fetchData(boardIdAndTaskId);
  },
  { immediate: true }
);

async function setCurrentBoard(boardId) {
  try {
    loading.value = true;
    await boardStore.setCurrentBoardId(boardId);
    isPublic.value = boardStore.currentBoard.visibility === 'PUBLIC';
    originalPublicState.value = isPublic.value;
  } catch (err) {
    console.error('Error fetching board:', err);
    error.value = err;
  } finally {
    loading.value = false;
  }
}

// ! ================= Filter and Sort ======================
const filterBy = ref([]);
const sortBy = ref('');
watch(() => [filterBy.value, sortBy.value, taskStore.tasks], filterData, {
  immediate: true,
  deep: true,
});

function filterData([filter, sort]) {
  let allTasks = [];
  allTasks = taskStore.tasks;
  if (filter.length > 0) {
    filteredTasks.value = allTasks.filter((task) => {
      return filter.some((fil) => fil === task.status);
    });
  } else filteredTasks.value = allTasks;
  switch (sort) {
    case '':
      filteredTasks.value = filteredTasks.value.sort((a, b) => a.id - b.id);
      break;
    case 'ASC':
      filteredTasks.value = filteredTasks.value.sort((a, b) => a.status.localeCompare(b.status));
      break;
    case 'DESC':
      filteredTasks.value = filteredTasks.value.sort((a, b) => b.status.localeCompare(a.status));
      break;
  }
  // ? .sort ต้องการค่า - + ออกมา
  // ? .localeCompare จะ  return ออกมาว่าห่างกันเท่าไหร่ เช่น 'a'.localeCompare('c') > -2
}

// sort function
function sortBtn() {
  switch (sortBy.value) {
    case '':
      sortBy.value = 'ASC';
      break;
    case 'ASC':
      sortBy.value = 'DESC';
      break;
    case 'DESC':
      sortBy.value = '';
      break;
  }
}

// ! ================= Visibility ======================

const isPublic = ref(false);
const showChangeVisibilityModal = ref(false);
const originalPublicState = ref(false);

function confirmChangeVisibility() {
  showChangeVisibilityModal.value = true;
}

function cancelUpdateVisibility() {
  showChangeVisibilityModal.value = false;
  isPublic.value = originalPublicState.value;
}

async function updateVisibility() {
  const newMode = isPublic.value ? 'PUBLIC' : 'PRIVATE';
  const res = await changeVisibility(newMode);

  if (res.status !== null || res.status !== '') {
    toast.value = {
      status: 'success',
      msg: `Visibility changed to ${newMode}`,
    };
  } else {
    toast.value = {
      status: 'error',
      msg: 'Error changing visibility. Please try again.',
    };
  }
  showChangeVisibilityModal.value = false;
}

onBeforeMount(async () => {
  // Initialize loading state
  loading.value = true;

  // ? With Auth
  if (accountStore.tokenRaw !== '') {
    try {
      // Ensure boards are loaded if not already present
      if (boardStore.boards.length === 0) {
        await getAllBoard();
      }

      // Fetch and set statuses if not already loaded
      if (statusStore.status.length === 0) {
        await statusStore.getAllStatus();
      }

      // Ensure limit status is updated
      // statusStore.getLimitEnable();
      // const res = await getLimitStatus();
      // statusStore.setLimitEnable(res);

      await setCurrentBoard(route.params.boardId);

      const currentBoard = boardStore.currentBoard;
      isOwner.value = currentBoard.owner.oid === accountStore.tokenDetail.oid;
      
      if (isOwner.value) {
        canWrite.value = true
      }

      if (!isOwner.value) {
        const currentUser = currentBoard.collaborators.find((collaborator) => collaborator.oid === accountStore.tokenDetail.oid);
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

      if (!isOwner.value && !canRead.value && !canWrite.value && currentBoard.visibility === 'PRIVATE') {
        router.push({ name: 'AccessDenied' });
      }

      // If a task is selected, open the task detail modal
      if (route.params.taskId !== undefined) {
        selectedId.value = parseInt(route.params.taskId);
        showDetailModal.value = true;
      }
    } catch (err) {
      console.error('Error loading data or checking ownership:', err);
      error.value = err;
    } finally {
      // Set loading to false once all operations are complete
      loading.value = false;
    }
  } else {
    // ? Without Auth
    await setCurrentBoard(route.params.boardId);
    loading.value = false;
  }
});

// ! ================= KanBanData ========================
watch(() => [filterBy.value, sortBy.value, loading.value, allTasks.value], makekanbanData, {
  immediate: true,
  deep: true,
});

function makekanbanData() {
  if (loading.value || allTasks.value === null) return;
  kanbanData.value = [];
  for (let i = 0; i < statusStore.getAllStatusWithLimit().length; i++) {
    const status = statusStore.getAllStatusWithLimit()[i];
    const tasks = allTasks.value?.filter((task) => task.status === status.name);
    status.tasks = tasks;
    kanbanData.value?.push(status);
  }
}
</script>

<template>
  <transition>

    
    <div class="h-full w-full" v-if="!loading">
      <!-- dropdowns status -->
      <div class="w-3/4 mx-auto mt-10 relative" v-if="!loading">
        <h1 class="w-full text-center text-2xl">
          {{ boardStore.currentBoard.name }}
        </h1>
      </div>
      <div class="w-3/4 mx-auto mt-10 relative">
        <details class="dropdown">
          <summary class="m-1 btn no-animation itbkk-status-filter">Filter Status</summary>
          <!-- FilterStatus -->
          <ul class="absolute dropdown-menu z-[1000] rounded-box">
            <li v-for="status in statusStore.status" :key="status" class="menu p-2 shadow bg-base-100 w-52 itbkk-status-choice" tabindex="0">
              <div>
                <input type="checkbox" class="checkbox" :id="status.id" :value="status.name" v-model="filterBy" />
                <label :for="status.id">{{ status.name }}</label>
              </div>
            </li>
          </ul>
        </details>

        <!-- reset button -->
        <button class="itbkk-filter-clear btn" @click="filterBy = []">Reset</button>
        <button class="btn ml-1" @click="taskStore.taskListType === 'Table' ? (taskStore.taskListType = 'Kanban') : (taskStore.taskListType = 'Table')">
          Change View To
          {{ taskStore.taskListType === 'Table' ? 'Kanban' : 'Table' }}
        </button>

        <!-- show edit limit modal -->
        <div class="float-right flex flex-row">
          <div class="form-control w-fit m-2">
            <div :class="isOwner ? '' : 'lg:tooltip'" data-tip="You need to be board owner to perform this action.">
              <label class="cursor-pointer label">
                <input type="checkbox" class="toggle toggle-primary itbkk-board-visibility" v-model="isPublic" @change="confirmChangeVisibility()" :disabled="!isOwner" />
                <span class="label-text pl-1">{{ isPublic ? 'Public' : 'Private' }}</span>
              </label>
            </div>
          </div>
          <div>
            <button class="btn btn-outline w-28 float-left mr-2" @click="router.push(`/board/${boardStore.currentBoardId}/collab`)">Manage Collaborator</button>
          </div>
          <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
            <button class="itbkk-button-add btn btn-square btn-outline w-16 float-left mr-2" @click="showAddModal = true" :disabled="!canWrite">+ Add</button>
          </div>
          <button class="btn btn-square btn-outline w-16 float-right" @click="showEditLimit = true">Limit Status</button>
        </div>
      </div>

      <!-- selected filter status -->
      <div class="w-3/4 mx-auto relative">
        <div class="border rounded-md w-auto p-2" v-if="filterBy.length > 0">
          Filtered Status:
          <div class="itbkk-filter-item badge font-semibold w-auto m-1" v-for="(status, index) in filterBy" :key="index">
            {{ status }}
            <button @click="filterBy.splice(index, 1)" class="itbkk-filter-item-clear ml-1 text-red-600">X</button>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="opacity">
        <div class="flex flex-col">
          <!--           Table-->

          <table
            v-if="taskStore.taskListType === 'Table'"
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
                    <svg v-if="sortBy === ''" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
                      <path fill="currentColor" d="M11 9h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5zm-6 3h2V8h3L6 4L2 8h3z" />
                    </svg>
                    <!-- ASC Button -->
                    <svg v-if="sortBy === 'ASC'" class="text-pink-400" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
                      <path fill="#323ffb" d="M11 9h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5zm-6 3h2V8h3L6 4L2 8h3z" />
                    </svg>
                    <!-- DESC Button -->
                    <svg v-if="sortBy === 'DESC'" class="text-pink-400" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24">
                      <path fill="#323ffb" d="m6 20l4-4H7V4H5v12H2zm5-12h9v2h-9zm0 4h7v2h-7zm0-8h11v2H11zm0 12h5v2h-5z" />
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
              <tr v-if="allTasks !== null" v-for="(task, index) in filteredTasks" :key="task.id" :id="`task-${task.id}`"class="itbkk-item hover">
                <th>{{ index + 1 }}</th>
                <td class="itbkk-title">
                  <!-- <RouterLink :to="`/task/${task.id}`"> -->
                  <button @click="router.push(`/board/${boardStore.currentBoardId}/task/${task.id}/edit`)">
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
                  {{ task.assignees === null || task.assignees == '' ? 'Unassigned' : task.assignees }}
                </td>
                <td class="itbkk-status itbkk-status-name">
                  {{ task.status }}
                </td>
                <td>
                  <div class="dropdown dropdown-bottom dropdown-end itbkk-button-action">
                    <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
                      <div tabindex="0" role="button" class="btn m-1">
                        <svg class="swap-off fill-current" xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 512 512">
                          <path d="M64,384H448V341.33H64Zm0-106.67H448V234.67H64ZM64,128v42.67H448V128Z" />
                        </svg>
                      </div>
                      <ul tabindex="0" class="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-52">
                        <li>
                          <button class="itbkk-button-edit button" :disabled="!canWrite" :class="{ disabled: !canWrite }" @click="openEditMode(task.id)">Edit</button>
                        </li>
                        <li>
                          <button class="itbkk-button-delete button" :disabled="!canWrite" :class="{ disabled: !canWrite }" @click="openDeleteModal(task.title, task.id)">Delete</button>
                        </li>
                      </ul>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <div v-if="taskStore.taskListType === 'Kanban'" class="flex flex-row flex-nowrap gap-5 w-3/4 mx-auto overflow-x-scroll mt-3">
            <div
              v-for="status in kanbanData"
              class="kanban-status-card"
              :style="{
                'border-top': status.isLimit ? 'red 0.5rem solid' : 'green 0.5rem solid',
              }"
            >
              <h5 class="kanban-status-name">{{ status.name }}</h5>
              <div class="kanban-task-list">
                <div v-for="task in status.tasks" class="kanban-task-card">
                  <div>
                    <p class="kanban-task-title">{{ task.title }}</p>
                    <div class="kanban-task-action">
                      <div :class="canWrite ? '' : 'lg:tooltip'" data-tip="You need to be board owner or has write access to perform this action.">
                        <input :disabled="!canWrite" type="checkbox" :id="`kanban-task-action-${task.id}`" />

                        <label :for="`kanban-task-action-${task.id}`">
                          <svg width="1rem" height="1rem" viewBox="0 0 24 24" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
                            <title>Kebab-Menu</title>
                            <g id="Kebab-Menu" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
                              <rect id="Container" x="0" y="0" width="24" height="24"></rect>
                              <path
                                d="M12,6 C12.5522847,6 13,5.55228475 13,5 C13,4.44771525 12.5522847,4 12,4 C11.4477153,4 11,4.44771525 11,5 C11,5.55228475 11.4477153,6 12,6 Z"
                                id="shape-03"
                                stroke="#030819"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-dasharray="0,0"
                              ></path>
                              <path
                                d="M12,13 C12.5522847,13 13,12.5522847 13,12 C13,11.4477153 12.5522847,11 12,11 C11.4477153,11 11,11.4477153 11,12 C11,12.5522847 11.4477153,13 12,13 Z"
                                id="shape-03"
                                stroke="#030819"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-dasharray="0,0"
                              ></path>
                              <path
                                d="M12,20 C12.5522847,20 13,19.5522847 13,19 C13,18.4477153 12.5522847,18 12,18 C11.4477153,18 11,18.4477153 11,19 C11,19.5522847 11.4477153,20 12,20 Z"
                                id="shape-03"
                                stroke="#030819"
                                stroke-width="2"
                                stroke-linecap="round"
                                stroke-dasharray="0,0"
                              ></path>
                            </g>
                          </svg>
                        </label>
                        <div class="kanban-task-action-menu">
                          <a @click="openEditMode(task.id)">Edit</a>
                          <a @click="openDeleteModal(task.title, task.id)">Delete</a>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="kanban-task-assignee">
                    <img src="../assets/people.png" height="32" width="32" />
                    <p v-if="task.assignees === null || task.assignees === ''" class="italic text-gray-500">Unassigned</p>
                    <p v-else>{{ task.assignees }}</p>
                  </div>
                  <!--              action btn    -->
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Modal -->
        <!-- DetailsModal -->
        <!-- EditModal -->
        <Modal :show-modal="showDetailModal">
          <Taskdetail :isOwnerOrNot="canWrite" :taskId="parseInt(selectedId)" @closeModal="closeEditModal" />
        </Modal>
        <!-- Add Modal -->
        <Modal :show-modal="showAddModal">
          <AddTaskModal @closeModal="closeAddModal" />
        </Modal>

        <!-- DeleteModal -->
        <Modal :showModal="showDeleteModal">
          <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
            <h1 class="m-2 pb-4 text-2xl font-bold">DELETE: {{ deleteTaskTitle }}</h1>
            <hr />
            <h1 class="itbkk-message font-semibold text-xl p-8">
              <!-- Do you want to delete the task "{{ deleteTaskTitle }}" -->
              Do you want to delete the task number {{ deleteTaskId }} ,
              {{ deleteTaskTitle }}
            </h1>
            <hr />
            <div class="flex flex-row-reverse gap-4 mt-5">
              <button @click="showDeleteModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
              <button @click="deleteThisTask()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
                {{ loading ? '' : 'Confirm' }}
                <span class="loading loading-spinner text-success" v-if="loading"></span>
              </button>
            </div>
          </div>
        </Modal>

        <!-- edit limit modal-->
        <Modal :show-modal="showEditLimit">
          <EditLimitStatus :isOwnerOrNot="canWrite" @close-modal="closeEditLimit" />
        </Modal>

        <Modal :show-modal="showChangeVisibilityModal">
          <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full itbkk-modal-alert">
            <h1 class="m-2 pb-4 text-2xl font-bold">Board visibility changed!</h1>
            <hr />
            <h1 class="itbkk-message font-semibold text-xl p-8">
              {{
                isPublic
                  ? 'In public, anyone can view the board,task list and task detail of tasks in the board. Do you want to change board visibility to public ? '
                  : 'In private, only board owner can access/control board. Do you want to change board visibility to private ? '
              }}
            </h1>
            <hr />
            <div class="flex flex-row-reverse gap-4 mt-5">
              <button @click="cancelUpdateVisibility()" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
              <button @click="updateVisibility()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
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
      </div>
    </div>
  </transition>
  <transition>
    <loading-component v-if="loading" class="absolute top-1/2"></loading-component>
  </transition>
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
  z-index: -1;
}

.kanban-status-card {
  flex: 0 0 20%;
  border-top: solid 1rem;
  background-color: #f7f7f7;
  color: #000000;
  border-radius: 10px;
  min-height: 40rem;
  width: 20rem;
  font-weight: bold;
  padding: 0.5rem;
  text-wrap: wrap;

  .kanban-status-name {
    font-size: 1rem;
    text-align: center;
    margin: 0.5rem;
  }

  .kanban-task-list {
    display: flex;
    flex-direction: column;
    background-color: #f7f7f7;

    .kanban-task-card {
      font-weight: normal;
      border-radius: 5px;
      margin: 5px;
      box-shadow: rgba(149, 157, 165, 0.2) 0px 8px 24px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      transition: box-shadow 0.5s;

      .kanban-task-title {
        overflow-wrap: break-word;
        float: left;
      }

      .kanban-task-assignee {
        padding-top: 0.25rem;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;

        img {
          border-radius: 50%;
        }
      }

      .kanban-task-action {
        position: relative;
        /*display: inline-block;*/
        float: right;
      }

      .kanban-task-action input[type='checkbox'] {
        display: none;
      }

      .kanban-task-action label {
        background-color: #e3e3e3;
        color: white;
        padding: 10px;
        cursor: pointer;
        border-radius: 4px;
        float: right;
      }

      .kanban-task-action label:hover {
        background-color: #ccc;
      }

      .kanban-task-action-menu {
        /* display: none;
        //position: absolute;
        //top: 100%;
        //right: 0;
        //background-color: #f1f1f1;
        //min-width: 160px;
        //box-shadow: 0px 8px 16px rgba(0, 0, 0, 0.2);
        //z-index: 1;
        //float: right; *
        display: block; /* ใช้ block เพื่อทำ transition */
        position: absolute;
        top: 100%;
        right: 0;
        background-color: #f1f1f1;
        min-width: 160px;
        max-height: 0;
        overflow: hidden;
        opacity: 0;
        box-shadow: 0px 8px 16px rgba(0, 0, 0, 0.2);
        z-index: 1;
      }

      .kanban-task-action-menu a {
        color: black;
        padding: 12px 16px;
        text-decoration: none;
        display: block;
      }

      .kanban-task-action-menu a:hover {
        background-color: #ddd;
      }

      /* เมื่อ checkbox ถูกเช็คจะแสดง dropdown */

      .kanban-task-action-menu {
        opacity: 1;
      }

      .kanban-task-action input[type='checkbox']:checked ~ .kanban-task-action-menu {
        /*opacity: 100%;
        //transition: opacity 1s ease;
        //display: block;*/
        max-height: 500px;
        /* ตั้ง max-height มากพอให้ครอบคลุมความสูงทั้งหมดของเมนู */
        opacity: 1;
        transition: max-height 1.5s ease;
      }

      .kanban-task-action input[type='checkbox'] ~ .kanban-task-action-menu {
        /*opacity: 100%;
        //transition: opacity 1s ease;
        //display: block;*/
        max-height: 0;
        /* ตั้ง max-height มากพอให้ครอบคลุมความสูงทั้งหมดของเมนู */
        transition: max-height 1.25s ease, opacity 1s ease;
      }
    }

    .kanban-task-card:hover {
      box-shadow: rgba(0, 0, 0, 0.35) 0px 5px 15px;
    }
  }
}
</style>
