<script setup>
import { addStatus } from "@/lib/fetchUtils";
import { ref, watch } from "vue";
import { useStatusStore } from "@/stores/status";
const statusStore = useStatusStore();
const emit = defineEmits(["closeModal"]);
const canSave = ref(false);
const statusData = ref({
  name: "",
  description: "",
});

const Errortext = ref({
  name: "",
  description: "",
});

watch(statusData.value, () => {
  if (statusData.value.name.trim().length > 50)
    Errortext.value.name = "Status Name can't long more 50 character";
  else if (statusData.value.name.trim().length === 0)
    Errortext.value.name = "Status Name can't be empty";
  else if (
    statusStore.status.some(
      (status) =>
        status.name.trim().toLowerCase() ===
        statusData.value.name.trim().toLowerCase()
    )
  )
    Errortext.value.name = "Status name must be uniques, please choose another name.";
  else Errortext.value.name = "";
  if (statusData.value.description.trim().length > 200)
    Errortext.value.description =
      "Status Description can't long more 200 character";
  else Errortext.value.description = "";
  canSave.value =
    Errortext.value.name === "" && Errortext.value.description === "";
});

const loading = ref(false);

async function fetchStatusData() {
  statusData.value.name = statusData.value.name.trim();
  statusData.value.description = statusData.value.description.trim();
  loading.value = true;
  let res;
  try {
    res = await addStatus(statusData.value);
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
  <!-- div -->
  <div
    class="itbkk-modal-status flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full"
  >
    <!-- title -->
    <label class="form-control w-full">
      <div class="label">
        <!-- Add New Status Text -->
        <span class="font-semibold text-xl">Add New Status</span>
      </div>
    </label>
    <hr />

    <!-- name -->
    <label class="form-control w-full">
      <div class="label">
        <!-- ? Head -->
        <span class="label-text">Title</span>
      </div>
      <input
        v-model="statusData.name"
        type="text"
        placeholder="Type Status Name Here"
        class="itbkk-status-name input input-bordered w-full bg-white"
      />
      <div class="label">
        <!-- ? Error Text -->
        <span v-if="Errortext.name !== ''" class="label-text-alt text-error">{{
          Errortext.name
        }}</span>
        <!-- count input name -->
        <span
          v-if="statusData.name.length <= 50 && statusData.name.length > 0"
          class="justify-end text-gray-400 label-text-alt"
          >{{ statusData.name.length }} / 50</span
        >
        <span
          v-if="statusData.name.length === 0 && Errortext.name !==''"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ statusData.name.length }} / 50</span
        >
        <span
          v-if="statusData.name.length > 50"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ statusData.name.length }} / 50</span
        >
      </div>

      <!-- * description -->
      <label class="form-control basis-3/4">
        <div class="label">
          <!-- ? Head -->
          <span class="label-text">Description</span>
        </div>
        <textarea
          v-model="statusData.description"
          class="itbkk-status-description textarea textarea-bordered h-72 bg-white"
          placeholder="Type Status Description Here"
        ></textarea>
        <div class="label">
          <!-- ? Error Text -->
          <span
            v-if="Errortext.description !== ''"
            class="label-text-alt text-error"
          >
            {{ Errortext.description }}</span
          >

          <!-- count input description -->
          <span
            v-if="statusData.description.length <= 200"
            class="flex justify-end text-gray-400 label-text-alt"
            >{{ statusData.description.length }} / 200</span
          >
          <span
            v-if="statusData.description.length > 200"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ statusData.description.length }} / 200</span
          >
        </div>
      </label>
    </label>
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
        @click="fetchStatusData()"
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
