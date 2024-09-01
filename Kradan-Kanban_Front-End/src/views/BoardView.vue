<script setup>
// ? import lib
import { onBeforeMount, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { deleteTask, getLimitStatus } from "../lib/fetchUtils.js";
import router from "@/router";
// ? import component
import Modal from "../components/Modal.vue";
import Taskdetail from "../components/Tasks/Taskdetail.vue";
import AddTaskModal from "@/components/Tasks/AddTaskModal.vue";
import EditLimitStatus from "@/components/EditLimitStatus.vue";
import {useBoardStore} from "@/stores/board.js";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const boardStore = useBoardStore();
const route = useRoute();

onBeforeMount(async () => {
  statusStore.getAllStatus();
  statusStore.getLimitEnable();
  const res = await getLimitStatus();
  statusStore.setLimitEnable(await res);
  if (route.params.id !== undefined) {
    selectedId.value = parseInt(route.params.id);
    showDetailModal.value = true;
  }
});
</script>

<template>
  <!-- dropdowns status -->
  <div class="w-3/4 mx-auto mt-10 relative">
    <details class="dropdown">
      <summary class="m-1 btn no-animation itbkk-status-filter">
        Filter Status
      </summary>
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
          class="itbkk-button-add btn btn-square btn-outline w-16 float-left mr-1"
          @click="showAddModal = true"
      >
        + Add
      </button>

      <button
          class="itbkk-button-add btn btn-square btn-outline w-16 float-right"
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
      <Taskdetail :taskId="parseInt(selectedId)" @closeModal="closeEditModal" />
    </Modal>
    <!-- Add Modal -->
    <Modal :show-modal="showAddModal">
      <AddTaskModal @closeModal="closeAddModal" />
    </Modal>

    <!-- DeleteModal -->
    <Modal :showModal="showDeleteModal">
      <div
          class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full"
      >
        <h1 class="m-2 pb-4 text-2xl font-bold">
          DELETE: {{ deleteTaskTitle }}
        </h1>
        <hr />
        <h1 class="itbkk-message font-semibold text-xl p-8">
          <!-- Do you want to delete the task "{{ deleteTaskTitle }}" -->
          ARE YOU SURE TO DELETE THIS TASK ?
        </h1>
        <hr />
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
      <EditLimitStatus @close-modal="closeEditLimit" />
    </Modal>

    <!-- Error Modal -->
    <Modal :show-modal="showErrorModal">
      <div
          class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 rounded-lg w-full m-auto"
      >
        <h2>Have Task Over Limit!!!</h2>
        <hr />
        <p>
          These statuses that have reached the task limit. No additional tasks
          can be added to these statuses.
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
