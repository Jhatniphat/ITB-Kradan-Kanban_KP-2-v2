<script setup>
import { ref, watch, computed } from "vue";
import { getStatusById, editStatus } from "@/lib/fetchUtils";
import router from "@/router";
import { useStatusStore } from "@/stores/status";
const statusStore = useStatusStore();
const canSave = ref(false);
const loading = ref(false);
const statusDetail = ref({});
const Errortext = ref({
  name: "",
  description: "",
});

const originalsDetail = ref(null);
const error = ref(null);
const emit = defineEmits(["closeModal"]);
const props = defineProps({
  statusId: {
    type: Number,
    require: true,
  },
});

const editStatusTitleLength = computed(() => {
  return statusDetail.value?.name?.trim().length;
});
const editStatusDescriptionLength = computed(() => {
  return statusDetail.value?.description?.trim()?.length;
});

watch(() => props.statusId, fetchData, { immediate: true });

watch(
  statusDetail,
  (newVal) => {
    if (editStatusTitleLength.value > 50)
      Errortext.value.name = "Status Name can't long more 50 character";
    else if (editStatusTitleLength.value === 0)
      Errortext.value.name = "Status Name can't be empty";
    else if (
      statusDetail.value.name?.trim().toLowerCase() !==
        originalsDetail.value.name?.trim().toLowerCase() &&
      statusStore.status.some(
        (status) =>
          status.name?.trim().toLowerCase() ===
          statusDetail.value.name?.trim().toLowerCase()
      )
    )
      Errortext.value.name = "Status name must be uniques, please choose another name.";
    else Errortext.value.name = "";
    if (editStatusDescriptionLength.value > 200)
      Errortext.value.description =
        "Status Description can't long more 200 character";
    else Errortext.value.description = "";
    canSave.value =
      Errortext.value.name === "" && Errortext.value.description === "" && JSON.stringify(newVal) !== JSON.stringify(originalsDetail.value);
  },
  { deep: true }
);

async function fetchData(id) {
  error.value = statusDetail.value = null;
  loading.value = true;
  try {
    const originalstatusDetails = await getStatusById(id);
    if (originalstatusDetails === 404 || originalstatusDetails === 400 || originalstatusDetails === 500) {
      emit("closeModal", 404);
      router.push("/status");
    }
    originalsDetail.value = { ...originalstatusDetails };
    statusDetail.value = { ...originalstatusDetails };
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

async function saveStatus() {
  loading.value = true;
  let res;
  try {
    delete statusDetail.value.id;
    delete statusDetail.value.createdOn;
    delete statusDetail.value.updatedOn;
    console.table(statusDetail.value)
    res = await editStatus(props.statusId, statusDetail.value);
    statusDetail.value = res;
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
    router.push("/status");
    emit("closeModal", res);
  }
}

function sendCloseModal() {
  router.push("/status");
  emit("closeModal", null);
}
</script>

<template>
  <div
    class="itbkk-modal-status flex flex-col p-5 text-black bg-slate-50 rounded-lg w-full"
  >
    <label class="form-control w-full">
      <div class="label">
        <h1 class="m-2 text-3xl font-bold" v-if="loading === true">
          Loading Data For StatusId = {{ props.statusId }}
        </h1>
        <div v-if="loading === false && error === null">
          <h1 class="m-2 mt-0 text-2xl font-bold text-wrap break-all">
            Edit Status
          </h1>
          <hr />
          <span class="label-text">Title</span>
        </div>
        <hr />
      </div>
      <input
        v-if="loading === false"
        v-model="statusDetail.name"
        type="text"
        placeholder="Type here"
        class="itbkk-status-name input input-bordered w-full bg-white"
      />
      <div class="label">
        <!-- ? Error Text -->
        <span v-if="Errortext.name !== ''" class="label-text-alt text-error">{{
          Errortext.name
        }}</span>
        <!-- count input name -->
        <span
          v-if="editStatusTitleLength <= 50 && editStatusTitleLength > 0"
          class="justify-end text-gray-400 label-text-alt"
          >{{ editStatusTitleLength }} / 50</span
        >
        <span
          v-if="editStatusTitleLength === 0 && Errortext.name !== ''"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ editStatusTitleLength }} / 50</span
        >
        <span
          v-if="editStatusTitleLength > 50"
          class="flex justify-end text-red-400 label-text-alt"
          >{{ editStatusTitleLength }} / 50</span
        >
      </div>
    </label>

    <!-- * description -->
    <div
      class="flex mb-5 mx-auto flex-col w-full"
      v-if="loading === false && error === null"
    >
      <div class="flex flex-col gap-3">
        <label class="form-control basis-3/4">
          <div class="label">
            <!-- ? Head -->
            <span class="label-text">Description</span>
          </div>
          <textarea
            v-if="loading === false"
            v-model="statusDetail.description"
            class="itbkk-status-description textarea textarea-bordered h-72 bg-white"
            placeholder="No Description Provided"
            :class="
              statusDetail.description === '' ? 'italic text-gray-600' : ''
            "
            >{{
              statusDetail.description == "" ||
              statusDetail.description === null
                ? "No Description Provided"
                : statusDetail.description
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
              v-if="editStatusDescriptionLength <= 200"
              class="flex justify-end text-gray-400 label-text-alt"
              >{{ editStatusDescriptionLength }} / 200</span
            >
            <span
              v-if="editStatusDescriptionLength > 200"
              class="flex justify-end text-red-400 label-text-alt"
              >{{ editStatusDescriptionLength }} / 200</span
            >
          </div>
        </label>
        <div
          class="mt-2 text-sm text-black flex flex-col justify-between mb-5 lg:flex-row"
        >
          <div class="flex flex-row mx-2">
            <h1 class="font-bold pr-5">TimeZone:</h1>
            <h1 class="itbkk-timezone font-semibold">
              {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
            </h1>
          </div>
          <div class="flex flex-row mx-2">
            <h1 class="font-bold pr-5">Created On:</h1>
            <h1 class="itbkk-created-on font-semibold">
              {{ statusDetail.createdOn }}
            </h1>
          </div>
          <div class="flex flex-row mx-2">
            <h1 class="font-bold pr-5">Updated On:</h1>
            <h1 class="itbkk-updated-on font-semibold">
              {{ statusDetail.updatedOn }}
            </h1>
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
          class="itbkk-button-confirm btn btn-outline btn-success basis-1/6"
          :disabled="!canSave"
          @click="saveStatus"
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
