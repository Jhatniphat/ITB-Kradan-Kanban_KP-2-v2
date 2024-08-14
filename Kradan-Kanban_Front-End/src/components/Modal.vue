<script setup>
defineProps(["showModal"]);
</script>

<template>
  <div>
    <Teleport id="delete" to="#modal">
      <div
        v-if="showModal"
        class="fixed inset-0 bg-black bg-opacity-25 backdrop-blur-sm z-50 shadow-lg"
      >
        <div
          class="fixed left-0 right-0 z-50 top-16 m-auto w-1/2 h-2/3 rounded-lg text-ellipsis text-base"
        >
          <slot></slot>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped></style>
