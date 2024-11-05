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
  accessRight: "READ",
  /* 'No Status', 'To Do', 'Doing', 'Done' */
});
const Errortext = ref({
  email: "",
  accessRight: "",
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

const loading = ref(false);

async function fetchData() {
  emit("closeModal", null);
  addCollaborator(collabData.value);
  // loading.value = true;
  // let res;
  // try {
  //   res = await addCollaborator(collabData.value);
  //   if (res === 404) {
  //     console.log("we here in 404 if condition.");
  //     Errortext.value.email = `The user does not exist.`;
  //   } else if (res === 409) {
  //     console.log("we here in 404 if condition.");
  //     Errortext.value.email = `The user is already the collaborator of this board.`;
  //   } else {
  //     console.log("Okay to Add");
  //     emit("closeModal", res);
  //   }
  // } catch (error) {
  //   console.log(error);
  // } finally {
  //   loading.value = false;
  // }
}
function sendCloseModal() {
  emit("closeModal", null);
}
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
          v-model="collabData.accessRight"
          class="itbkk-access-right select select-bordered bg-white dark:bg-base-300 dark:text-slate-400"
        >
          <option value="READ">READ</option>
          <option value="WRITE">WRITE</option>
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
</template>

<style scoped></style>
