<script setup>
import {
  getAllStatus,
  deleteStatus,
  transferStatus,
  getAllTasks,
  getStatusById,
} from "@/lib/fetchUtils";
import { onMounted, ref, watch } from "vue";
import { useStatusStore } from "@/stores/status.js";
import { useTaskStore } from "@/stores/task.js";

const emit = defineEmits(["closeModal"]);
const props = defineProps({
  deleteId: {
    type: Number,
    require: true,
  },
  deleteTitle: {
    type: String,
    require: true,
  },
});

const transfer = ref(false);
const loading = ref(false);
const statusList = ref(null);
const newId = ref(null);
const errorMsg = ref("");

watch(() => newId.value, checkCantransfer);

function checkCantransfer() {
  if (!transfer.value) return 0;
  errorMsg.value = useStatusStore().canTransfer(props.deleteId, newId.value)
    ? ""
    : `Cannot transfer to ${
        useStatusStore().findById(newId.value).name
      } status since it will exceed the limit.  Please choose another status to transfer to.`;
}

onMounted(async () => {
  try {
    statusList.value = await getAllStatus();
  } catch (error) {}
  statusList.value.splice(
    statusList.value.findIndex((status) => status.id === props.deleteId),
    1
  );
});

const deleteThisStatus = async () => {
  loading.value = true;
  let res;

  const inUse = await isStatusInUse(props.deleteId);
  if (inUse) {
    // If status is in use, switch to transferring status
    transfer.value = true;
    loading.value = false;
    return true;
  }
  if (!inUse) {
    try {
      // Check if the status is already in use
      const inUse = await isStatusInUse(props.deleteId);
      if (inUse) {
        // If status is in use, switch to transferring status
        transfer.value = true;
        return;
      }
      // If not in use, proceed with deletion
      res = await deleteStatus(props.deleteId);
    } catch (error) {
      console.log(error);
    } finally {
      emit("closeModal", res);
    }
  }
};

const closeThisModal = () => {
  emit("closeModal", null);
};

const numOfTasks = useStatusStore().countStatus(props.deleteTitle);

const isStatusInUse = async (StatusId) => {
  let Status = await getStatusById(StatusId);
  let TaskList = await getAllTasks();
  return await TaskList.some((task) => task.status === Status.name);
};

const transferTheStatus = async (newId) => {
  let oldStatusName = useStatusStore().findById(props.deleteId).name;
  let newStatusName = useStatusStore().findById(newId).name;
  let res;
  try {
    res = await transferStatus(props.deleteId, newId);
  } catch (error) {
    console.log(error);
  } finally {
    emit("closeModal", res, numOfTasks);
    useTaskStore().transferStatus(oldStatusName, newStatusName);
  }
};
</script>

<template>
  <div class="flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full">
    <!-- Delete Modal -->
    <div v-if="!transfer">
      <h1 class="m-2 pb-4 text-2xl font-bold">Delete a Satus</h1>
      <hr />
      <h1 class="itbkk-message font-semibold text-xl p-8">
        Do you want to delete the "{{ deleteTitle }}" Status?
      </h1>
      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <button
          @click="closeThisModal"
          class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
        >
          Cancel
        </button>
        <button
          @click="deleteThisStatus()"
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
    <!-- Transfers Modal -->
    <div v-if="transfer">
      <h1 class="m-2 pb-4 text-2xl font-bold">Transfer a Satus</h1>
      <hr />
      <div class="p-3 flex flex-col">
        <h1 class="itbkk-message font-semibold text-xl">
          There are {{ numOfTasks }} tasks in "{{ deleteTitle }}" Status?
        </h1>
        <div class="flex flex-row">
          <h1 class="font-semibold text-xl">Transfer to</h1>
          <label class="form-control w-full max-w-xs ml-2">
            <select
              v-model="newId"
              class="itbkk-status select select-bordered bg-white"
            >
              <!-- <option value="No Status" selected>No Status</option> -->
              <option
                v-for="status in statusList"
                :key="status.id"
                :value="status.id"
              >
                {{ status.name }}
              </option>
            </select>
          </label>
        </div>
        <p class="text-error">{{ errorMsg }}</p>
      </div>

      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <button
          @click="closeThisModal"
          class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
        >
          Close
        </button>
        <button
          @click="transferTheStatus(newId)"
          class="itbkk-button-confirm btn btn-outline btn-success basis-1/6"
          :disabled="errorMsg !== ''"
        >
          {{ loading ? "" : "Tranfer" }}
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
