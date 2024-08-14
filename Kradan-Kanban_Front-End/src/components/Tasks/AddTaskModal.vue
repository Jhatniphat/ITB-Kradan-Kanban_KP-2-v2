<script setup>
import { addTask } from "@/lib/fetchUtils";
import { onMounted, ref, watch } from "vue";
import { useStatusStore } from "@/stores/status.js";
const emit = defineEmits(["closeModal"]);
const statusStore = useStatusStore();

const statusList = ref([]);
const canSave = ref(false);
const taskData = ref({
  title: "",
  description: "",
  assignees: "",
  status: "No Status",
  /* 'No Status', 'To Do', 'Doing', 'Done' */
});
const Errortext = ref({
  title: "",
  description: "",
  assignees: "",
});

watch(taskData.value, () => {
  if (taskData.value.title.trim().length > 100)
    Errortext.value.title = `Title can't long more than 100 character`;
  else if (taskData.value.title.trim().length === 0)
    Errortext.value.title = `Title can't be empty`;
  else Errortext.value.title = "";
  if (taskData.value.description.trim().length > 500)
    Errortext.value.description = `Description can't long more than 500 character`;
  else Errortext.value.description = "";
  if (taskData.value.assignees.trim().length > 30)
    Errortext.value.assignees = `Assignees can't long more than 30 character`;
  else Errortext.value.assignees = "";
  // ? disabled or enabled save btn
  canSave.value =
    Errortext.value.title === "" &&
    Errortext.value.description === "" &&
    Errortext.value.assignees === "";
});

onMounted(async () => {
  // try {
  //   const fetchStatus = await getAllStatus();
  //   statusList.value = fetchStatus.map((item) => item.name);
  // }catch (err){
  //   console.log(err)
  // }
  statusList.value = statusStore.getAllStatusWithLimit();
});

const loading = ref(false);
async function fetchData() {
  taskData.value.title = taskData.value.title.trim();
  taskData.value.description = taskData.value.description.trim();
  taskData.value.assignees = taskData.value.assignees.trim();
  loading.value = true;
  let res;
  try {
    res = await addTask(taskData.value);
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
    emit("closeModal", res);
  }
}
function sendCloseModal() {
  emit("closeModal", null);
}
</script>

<template>
  <!-- <div class="flex flex-col p-5 text-black bg-slate-50 rounded-lg">

  </div> -->
  <div
    class="itbkk-modal-task flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full"
  >
    <!-- * title -->
    <label class="form-control w-full">
      <div class="label">
        <!-- ? Head -->
        <span class="label-text">Title</span>
      </div>
      <input
        v-model="taskData.title"
        type="text"
        placeholder="Type here"
        class="itbkk-title input input-bordered w-full bg-white"
      />
      <div class="label">
        <!-- ? Error Text -->
        <span v-if="Errortext.title !== ''" class="label-text-alt text-error">{{
          Errortext.title
        }}</span>
        <!-- count input name -->
        <span
          v-if="taskData.title.length <= 100 && taskData.title.length > 0"
          class="justify-end text-gray-400 label-text-alt"
          >{{ taskData.title.length }} / 100</span
        >
        <span
          v-if="taskData.title.length === 0 && Errortext.title !== ''"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ taskData.title.length }} / 100</span
        >
        <span
          v-if="taskData.title.length > 100"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ taskData.title.length }} / 100</span
        >
      </div>
    </label>
    <hr />
    <div class="flex flex-row gap-3">
      <!-- * description -->
      <label class="form-control basis-3/4">
        <div class="label">
          <!-- ? Head -->
          <span class="label-text">Description</span>
        </div>
        <textarea
          v-model="taskData.description"
          class="itbkk-description textarea textarea-bordered h-72 bg-white"
          placeholder="Bio"
        ></textarea>
        <div class="label">
          <!-- ? Error Text -->
          <span
            v-if="Errortext.description !== ''"
            class="label-text-alt text-error"
          >
            {{ Errortext.description }}</span
          >
          <span
            v-if="taskData.description.length <= 500"
            class="flex justify-end text-gray-400 label-text-alt"
            >{{ taskData.description.length }} / 500</span
          >
          <span
            v-if="taskData.description.length > 500"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ taskData.description.length }} / 500</span
          >
        </div>
      </label>

      <div class="basis-1/4">
        <!-- * assignee -->
        <label class="form-control">
          <div class="label">
            <!-- ? Head -->
            <span class="label-text">Assignees</span>
          </div>
          <textarea
            v-model="taskData.assignees"
            class="itbkk-assignees textarea textarea-bordered h-24 bg-white"
            placeholder="Bio"
          ></textarea>
          <div class="label">
            <!-- ? Error Text -->
            <span
              v-if="Errortext.assignees !== ''"
              class="label-text-alt text-error"
            >
              {{ Errortext.assignees }}</span
            >
            <span
              v-if="taskData.assignees.length <= 30"
              class="flex justify-end text-gray-400 label-text-alt"
              >{{ taskData.assignees.length }} / 30</span
            >
            <span
              v-if="taskData.assignees.length > 30"
              class="flex justify-end text-red-400 label-text-alt"
              >{{ taskData.assignees.length }} / 30</span
            >
          </div>
        </label>
        <!-- * status -->
        <label class="form-control w-full max-w-xs">
          <div class="label">
            <span class="label-text">Status</span>
          </div>
          <select
            class="itbkk-status select select-bordered bg-white"
            v-model="taskData.status"
          >
            <option
              v-for="status in statusList"
              :value="status.name"
              :disabled="status.isLimit"
            >
              {{ status.name }}
              <span class="text-error">
                {{ status.isLimit ? "(max)" : "" }}
              </span>
            </option>
          </select>
        </label>
      </div>
    </div>
    <hr />
    <div class="flex flex-row-reverse gap-4 mt-5">
      <button
        class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
        @click="sendCloseModal()"
      >
        Cancel
      </button>
      <button
        class="itbkk-button-confirm btn btn-outline btn-success basis-1/6"
        :disabled="!canSave"
        :class="!canSave ? 'disabled' : ''"
        @click="fetchData()"
      >
        {{ loading ? "" : "Save" }}
        <span
          class="loading loading-spinner text-success"
          v-if="loading"
        ></span>
      </button>
    </div>
  </div>
</template>

<style scoped></style>
