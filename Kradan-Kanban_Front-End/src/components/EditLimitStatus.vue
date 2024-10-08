<script setup>
import { computed, onMounted, ref } from "vue";
import { useStatusStore } from "@/stores/status.js";

import { toggleLimitStatus } from "@/lib/fetchUtils.js";

const statusStore = useStatusStore();
const limitStatusValue = ref({});
let oldLimitStatusValue = { isEnable: true, limit: 10 };
const emit = defineEmits(["closeModal"]);
const props = defineProps({
  isOwnerOrNot: {
    type: Boolean,
    required: true,
  },
});

const isOwner = props.isOwnerOrNot;
const canConfirmBtn = ref(true);
const loading = ref(false);

onMounted(() => {
  limitStatusValue.value = {
    isEnable: statusStore.getLimitEnable(),
    limit: statusStore.getLimit(),
  };
  oldLimitStatusValue = { ...limitStatusValue.value };
});

async function confirmEdit() {
  statusStore.setLimit(limitStatusValue.value.limit);
  if (limitStatusValue.value.isEnable !== statusStore.getLimitEnable()) {
    try {
      let res = await toggleLimitStatus();
      if (res === 200 || res === 204) {
        statusStore.setLimitEnable(limitStatusValue.value.isEnable);
      }
    } catch (e) {
      console.log(e);
    }
  }
  let overStatus = statusStore.getOverStatus();
  if (overStatus.length > 0) {
    let overStatusObj = [];
    overStatus.forEach((status) =>
      overStatusObj.push({
        name: status,
        task: statusStore.countStatus(status),
      })
    );
    emit("closeModal", overStatusObj);
  } else emit("closeModal", null);
}

const limitStatusValueError = computed(() => {
  return limitStatusValue.value.limit > 30 || limitStatusValue.value.limit < 0
    ? `Limit Be only between 0 and 30!!`
    : "";
});

function closeEdit() {
  statusStore.setLimitEnable(oldLimitStatusValue.isEnable);
  statusStore.setLimit(oldLimitStatusValue.limit);
  limitStatusValue.value = { ...oldLimitStatusValue };
  emit("closeModal", null);
}
</script>

<template>
  <div
    class="itbkk-modal-task flex flex-col gap-3 p-5 text-black bg-slate-50 dark:bg-base-100 dark:text-slate-400 rounded-lg w-full m-auto"
  >
    <h1>Limit Status</h1>
    <hr />
    <div
      :class="isOwner ? '' : 'lg:tooltip'"
      data-tip="You don't have a permission to Enable a Limit status"
    >
      <div class="form-control w-fit">
        <label class="cursor-pointer label">
          <input
            type="checkbox"
            class="toggle toggle-primary"
            v-model="limitStatusValue.isEnable"
            :disabled="!isOwner"
          />
          <span class="label-text pl-1">Enable Limit</span>
        </label>
      </div>
    </div>
    <div
      :class="isOwner ? '' : 'lg:tooltip'"
      data-tip="You don't have a permission to Change a Limit status"
    >
      <div class="form-control">
        <label class="label text-error"
          >Limit {{ limitStatusValueError }}</label
        >
        <input
          type="number"
          class="input text-base dark:bg-base-300"
          v-model="limitStatusValue.limit"
          :disabled="!isOwner"
        />
      </div>
    </div>
    <div class="flex flex-row-reverse gap-4 mt-5">
      <button class="btn btn-outline btn-error" @click="closeEdit">
        Close
      </button>
      <div
        :class="isOwner ? '' : 'lg:tooltip'"
        data-tip="You don't have a permission to Change a Limit status"
      >
        <button
          class="btn btn-outline btn-success"
          @click="confirmEdit"
          :disabled="limitStatusValueError !== '' || !canConfirmBtn || !isOwner"
        >
          {{ loading ? "" : "Confirm" }}
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
