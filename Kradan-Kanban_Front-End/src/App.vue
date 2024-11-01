<script setup>
import { RouterLink, RouterView } from "vue-router";
import NavBar from "@/components/NavBar.vue";
import ToastComponent from "./components/ToastComponent.vue";
</script>

<template>
  <NavBar/>
  <RouterView/>
  <ToastComponent/>
</template>

<style scoped></style>
