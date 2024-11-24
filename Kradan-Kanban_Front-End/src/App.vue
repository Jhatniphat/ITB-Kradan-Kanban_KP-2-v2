<script setup>
import { RouterLink, RouterView } from 'vue-router';
import NavBar from '@/components/NavBar.vue';
import ToastComponent from './components/ToastComponent.vue';
import { useRoute } from 'vue-router';
import { ref, watch } from 'vue';

const route = useRoute();

let currentRoute = ref(route.name);
watch(
  () => route.name,
  (value) => {
    currentRoute.value = value;
    showNavBar.value = checkShowNavBar();
  }
);
let showNavBar = ref(false);
function checkShowNavBar() {
  return (
    currentRoute.value.toString().includes('task') ||
    currentRoute.value.toString().includes('status') ||
    currentRoute.value.toString().includes('board') ||
    currentRoute.value.toString().includes('collab') ||
    currentRoute.value.toString().includes('invitation')
  );
}
</script>

<template>
  <!-- <NavBar/> -->
  <NavBar v-if="showNavBar">
    <RouterView />
  </NavBar>

  <RouterView v-if="!showNavBar"/>
  <ToastComponent />
</template>
<style scoped></style>
