<script setup>
import { addCollaborator, addTask } from "@/lib/fetchUtils";
import { onMounted, ref, watch } from "vue";
import { useStatusStore } from "@/stores/status.js";

const emit = defineEmits(["closeModal"]);
const statusStore = useStatusStore();
const toast = ref({ status: "", msg: "" });
const statusList = ref([]);
const canSave = ref(false);
const collabData = ref({
  email: "",
  /* 'No Status', 'To Do', 'Doing', 'Done' */
});
const Errortext = ref({
  email: "",
});

watch(collabData.value, () => {
  if (collabData.value.email.trim().length > 50)
    Errortext.value.email = `Email can't long more than 50 character`;
  else if (collabData.value.email.trim().length === 0)
    Errortext.value.email = `Email can't be empty`;
  else Errortext.value.email = "";
  // ? disabled or enabled save btn
  canSave.value = Errortext.value.email === "";
});

onMounted(async () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

watch(statusStore.status, () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

const loading = ref(false);

async function fetchData() {
  collabData.value.email = collabData.value.email.trim();
  loading.value = true;
  let res;
  try {
    res = await addCollaborator(collabData.value);
    if (res === 404) {
      showToast({
        status: "NotFound",
        msg: "The user does not exists.",
      });
    }
    if (res === 409) {
      showToast({
        status: "NotFound",
        msg: "The user is already the collaborator of this board.",
      });
    }
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

// ! ================= Toast ======================
const showToast = (toastData, timeOut = 3000) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = { ...{ status: "" } };
  }, timeOut);
};
</script>

<template>
  <div
    class="itbkk-modal-alert itbkk-modal-new flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full"
  >
    <h1 class="m-2 mt-0 text-2xl font-bold text-wrap break-all">
      Add Collaborator
    </h1>
    <hr />
    <div class="flex flex-row gap-3">
      <label class="form-control w-full">
        <div class="label">
          <!-- ? Head -->
          <span class="label-text">Collaborator e-mail</span>
        </div>
        <input
          v-model="collabData.email"
          type="text"
          placeholder="Type here"
          class="itbkk-collaborator-email input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400"
        />
        <div class="label">
          <!-- ? Error Text -->
          <span
            v-if="Errortext.email !== ''"
            class="label-text-alt text-error"
            >{{ Errortext.email }}</span
          >
          <!-- count input name -->
          <span
            v-if="collabData.email.length <= 50 && collabData.email.length > 0"
            class="justify-end text-gray-400 label-text-alt"
            >{{ collabData.email.length }} / 50</span
          >
          <span
            v-if="collabData.email.length === 0 && Errortext.email !== ''"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ collabData.email.length }} / 50</span
          >
          <span
            v-if="collabData.email.length > 50"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ collabData.email.length }} / 50</span
          >
        </div>
      </label>
      <!-- * status -->
      <label class="form-control w-full max-w-xs">
        <div class="label">
          <span class="label-text">Access Right</span>
        </div>
        <select
          class="itbkk-access-right select select-bordered bg-white dark:bg-base-300 dark:text-slate-400"
        >
          <option>
            <!-- {{ status.name }}
            <span class="text-error">
              {{ status.isLimit ? "(max)" : "" }}
            </span> -->
            Read
          </option>
        </select>
      </label>
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
        {{ loading ? "" : "Add" }}
        <span
          class="loading loading-spinner text-success"
          v-if="loading"
        ></span>
      </button>
    </div>
  </div>
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
</template>

<style scoped></style>
