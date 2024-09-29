<script setup>
import { useRoute } from "vue-router";
import router from "@/router";
const route = useRoute();
</script>

<template>
  <div
    class="fixed inset-0 bg-black bg-opacity-25 backdrop-blur-sm z-50 shadow-lg"
  >
    <div
      class="fixed left-0 right-0 z-50 top-16 m-auto w-1/2 h-2/3 rounded-lg text-ellipsis text-base"
    >
      <div
        class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full"
      >
        <h1 class="m-2 pb-4 text-2xl font-bold">Sorry :</h1>
        <hr />
        <h1 class="itbkk-message font-semibold text-xl p-8">
          Access denied, you do not have permission to view this page.
        </h1>
        <hr />
        <div class="flex flex-row-reverse gap-4 mt-5">
          <button
            @click="router.push(`/board`)"
            class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
