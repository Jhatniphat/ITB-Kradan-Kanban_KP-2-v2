<script setup>
defineProps(["showModal"]);
</script>

<template>
  <div>
    <Teleport id="delete" to="#modal">
      <Transition name="slide-fade">
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
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
/*
  Enter and leave animations can use different
  durations and timing functions.
*/
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(20px);
  opacity: 0;
}

*{
  transition: all 0.3s ease-out;
}
</style>
