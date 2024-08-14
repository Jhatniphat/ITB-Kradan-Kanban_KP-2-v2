<script setup>
import { ref, watch, computed } from "vue";
import { getTaskById, editTask } from "@/lib/fetchUtils.js";
import router from "@/router";
import { useTaskStore } from "@/stores/task";
import { useStatusStore } from "@/stores/status.js";

const taskStore = useTaskStore();
const statusStore = useStatusStore();
const emit = defineEmits(["closeModal", "editMode"]);
const props = defineProps({
  taskId: {
    type: Number,
    require: true,
  },
});

const editMode = ref(false);
const statusList = ref([]);
const canSave = ref(false);
const loading = ref(false);
const taskDetail = ref({});
const originalTask = ref(null);
const error = ref(null);
const Errortext = ref({
  title: "",
  description: "",
  assignees: "",
});

const editTaskTitleLength = computed(() => {
  return taskDetail.value?.title?.trim()?.length;
});
const editTaskDescriptionLength = computed(() => {
  return taskDetail.value?.description?.trim()?.length;
});
const editTaskAssigneesLength = computed(() => {
  return taskDetail.value?.assignees?.trim()?.length;
});

watch(() => props.taskId, fetchTask, { immediate: true });

watch(
  taskDetail,
  (newVal) => {
    if (editTaskTitleLength.value > 100)
      Errortext.value.title = "Task name can't long more 100 character";
    else if (editTaskTitleLength.value === 0)
      Errortext.value.title = "Task Name can't be empty";
    else Errortext.value.title = "";
    if (editTaskDescriptionLength.value > 500)
      Errortext.value.description = "Description can't long more 500 character";
    else Errortext.value.description = "";
    if (editTaskAssigneesLength.value > 30)
      Errortext.value.assignees = "Assignees can't long more than 30 character";
    else Errortext.value.assignees = "";
    canSave.value =
      Errortext.value.title === "" &&
      Errortext.value.description === "" &&
      Errortext.value.assignees === "" &&
      JSON.stringify(newVal) !== JSON.stringify(originalTask.value)
  },
  { deep: true }
);

async function fetchTask(id) {
  if (id === 0) {
    loading.value = true;
    // emit("closeModal", 404);
    // router.push("/task");
    return 0;
  }
  error.value = taskDetail.value = statusList.value = null;
  loading.value = true;
  statusList.value = statusStore.getAllStatusWithLimit();
  try {
    const originalTaskDetails = await getTaskById(id);
    console.table(await originalTaskDetails)
    if (originalTaskDetails === 404 || originalTaskDetails === 400 || originalTaskDetails === 500) {
      emit("closeModal", 404);
      router.push("/task");
    }
    originalTask.value = { ...originalTaskDetails };
    taskDetail.value = { ...originalTaskDetails };

    if (taskDetail.value === 404) {
      router.push("/task");
    }
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

async function saveTask() {
  loading.value = true;
  let res;
  try {
    delete taskDetail.value.id;
    delete taskDetail.value.createdOn;
    delete taskDetail.value.updatedOn;
    res = await editTask(props.taskId, taskDetail.value);
    taskDetail.value = res;
    taskStore.editStoreTask(res);
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
    router.push("/task");
    emit("closeModal", res);
  }
}

function sendCloseModal() {
  emit("closeModal", null);
  router.push("/task");
}
</script>

<template>

  <div class="flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full" v-if="loading === true">

  </div>
  <!-- Title -->
  <div class="flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full" v-if="loading === false">
    <div v-if="editMode">
      <label class="form-control w-full">
        <div class="label">
          <h1 class="m-2 text-3xl font-bold" v-if="loading === true">
            Loading Data For TaskId = {{ props.taskId }}
          </h1>
          <div v-if="loading === false && error === null">
            <h1 class="m-2 mt-0 text-2xl font-bold text-wrap break-all">
              Edit task
            </h1>
            <hr />
            <span class="label-text">Title</span>
          </div>
          <hr />
        </div>
        <input
          v-model="taskDetail.title"
          type="text"
          placeholder="Type here"
          class="itbkk-title input input-bordered w-full bg-white"
        />
        <div class="label">
          <!-- ? Error Text -->
          <span
            v-if="Errortext.title !== ''"
            class="label-text-alt text-error"
            >{{ Errortext.title }}</span
          >
          <span
            v-if="editTaskTitleLength <= 100 && editTaskTitleLength > 0"
            class="justify-end text-gray-400 label-text-alt"
            >{{ editTaskTitleLength }} / 100</span
          >
          <span
            v-if="editTaskTitleLength === 0 && Errortext.title !== ''"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ editTaskTitleLength }} / 100</span
          >
          <span
            v-if="editTaskTitleLength > 100"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ editTaskTitleLength }} / 100</span
          >
        </div>
      </label>
    </div>

    <div v-if="!editMode">
      <label class="form-control w-full">
        <div class="label">
          <h1 class="m-2 text-3xl font-bold" v-if="loading === true">
            Loading Data For TaskId = {{ props.taskId }}
          </h1>
          <h1
            class="itbkk-title m-2 text-2xl font-bold text-wrap break-all"
            v-if="loading === false && error === null"
          >
            {{ taskDetail.title }}
          </h1>
        </div>
        <hr />
      </label>
    </div>

    <!-- * description -->
    <div
      class="flex mb-5 mx-auto flex-col w-full"
      v-if="loading === false && error === null"
    >
      <div class="flex flex-row gap-3">
        <label class="form-control basis-3/4">
          <div class="label">
            <!-- ? Head -->
            <span class="label-text">Description</span>
          </div>
          <textarea
            :readonly="!editMode"
            v-model="taskDetail.description"
            class="itbkk-description textarea textarea-bordered h-72 bg-white resize-none"
            placeholder="No Description Provided"
            :class="taskDetail.description === '' ? 'italic text-gray-600' : ''"
            >{{
              taskDetail.description == "" || taskDetail.description === null
                ? "No Description Provided"
                : taskDetail.description
            }}</textarea
          >
          <div class="label">
            <!-- ? Error Text -->
            <span
              v-if="Errortext.description !== ''"
              class="label-text-alt text-error"
            >
              {{ Errortext.description }}</span
            >
            <span
              v-if="editTaskDescriptionLength <= 500"
              class="flex justify-end text-gray-400 label-text-alt"
              >{{ editTaskDescriptionLength }} / 500</span
            >
            <span
              v-if="editTaskDescriptionLength > 500"
              class="flex justify-end text-red-400 label-text-alt"
              >{{ editTaskDescriptionLength }} / 500</span
            >
          </div>
        </label>

        <!-- * assignee -->
        <div class="basis-1/4">
          <label class="form-control">
            <div class="label">
              <!-- ? Head -->
              <span class="label-text">Assignees</span>
            </div>
            <textarea
              :readonly="!editMode"
              v-model="taskDetail.assignees"
              class="itbkk-assignees textarea textarea-bordered h-24 bg-white resize-none"
              placeholder="Unassigned"
              :class="
                taskDetail.assignees === '' || taskDetail.assignees === null
                  ? 'italic text-gray-600'
                  : ''
              "
              >{{
                taskDetail.assignees == "" || taskDetail.assignees === null
                  ? "Unassigned"
                  : taskDetail.assignees
              }}</textarea
            >
            <div class="label">
              <!-- ? Error Text -->
              <span
                v-if="Errortext.assignees !== ''"
                class="label-text-alt text-error"
              >
                {{ Errortext.assignees }}</span
              >
              <span
                v-if="editTaskAssigneesLength <= 30"
                class="flex justify-end text-gray-400 label-text-alt"
                >{{ editTaskAssigneesLength }} / 30</span
              >
              <span
                v-if="editTaskAssigneesLength > 30"
                class="flex justify-end text-red-400 label-text-alt"
                >{{ editTaskAssigneesLength }} / 30</span
              >
            </div>
          </label>

          <!-- * status -->
          <label class="form-control w-full max-w-xs">
            <div class="label">
              <span class="label-text">Status</span>
            </div>
            <select
              :disabled="!editMode"
              class="itbkk-status select select-bordered bg-white"
              v-model="taskDetail.status"
            >
              <option
                v-for="status in statusList"
                :value="status.name"
                :disabled="
                  originalTask.status !== status.name && status.isLimit
                "
              >
                {{ status.name }}
                <span class="text-error">
                  {{ status.isLimit ? "(max)" : "" }}
                </span>
              </option>
            </select>
          </label>

          <div class="mt-2 text-sm text-black">
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">TimeZone</h1>
              <h1 class="itbkk-timezone font-semibold">
                {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
              </h1>
            </div>
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">Created On</h1>
              <h1 class="itbkk-created-on font-semibold">
                {{ taskDetail.createdOn }}
              </h1>
            </div>
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">Updated On</h1>
              <h1 class="itbkk-updated-on font-semibold">
                {{ taskDetail.updatedOn }}
              </h1>
            </div>
          </div>
        </div>
      </div>

      <div class="flex my-5 mx-auto" v-if="error !== null">
        <div>
          <h1>{{ error }}</h1>
        </div>
      </div>

      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <!-- Cancel button -->
        <button
          class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
          @click="sendCloseModal()"
        >
          Cancel
        </button>

        <button
          v-if="!editMode"
          class="btn btn-outline btn-primary basis-1/6"
          @click="editMode = true"
        >
          Edit
        </button>

        <button
          v-if="editMode"
          class="itbkk-button-confirm btn btn-outline btn-success basis-1/6"
          :disabled="!canSave && editMode"
          @click="saveTask"
        >
          {{ loading ? "" : "Save" }}
          <span
            class="loading loading-spinner text-success"
            v-if="loading"
          ></span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
