<script setup>
import { ref, computed } from "vue";
import { login } from "@/lib/fetchUtils.js";
import router from "@/router/index.js";
import VueJwtDecode from "vue-jwt-decode";
import { useAccountStore } from "@/stores/account.js";

const accountStore = useAccountStore();

const userData = ref({
  username: "",
  password: "",
});

const showAlert = ref(false);
const errorMsg = ref("");

const isFormValid = computed(() => {
  return (
    userData.value.username.length > 0 && userData.value.password.length > 0
  );
});

const handleLogin = async () => {
  if (!isFormValid.value) {
    showAlert.value = true;
  } else {
    console.table(userData.value);
    let result = await login(userData.value.username, userData.value.password);
    console.log(result);
    if (result.status === 200) {
      const decodedToken = VueJwtDecode.decode(result.payload?.access_token);
      accountStore.setTokenDetail(decodedToken);
      console.log(accountStore.tokenDetail);
      console.log(accountStore.tokenDetail.name);
      router.push("/task");
      console.log("success");
    } else {
      showAlert.value = true;
      errorMsg.value = result.payload;
    }
  }
};

const closeAlert = () => {
  showAlert.value = false;
};
</script>

<template>
  <div
    class="flex flex-col justify-center gap-3 items-center h-screen bg-slate-600 dark:bg-base-300"
  >
    <div
      v-if="showAlert"
      class="itbkk-message bg-red-100 border border-red-400 text-red-700 px-10 py-6 rounded relative"
      role="alert"
    >
      <strong class="font-bold">Error: </strong>
      <span class="block sm:inline"> {{ errorMsg }}</span>
      <span class="absolute top-0 bottom-0 right-0">
        <svg
          class="fill-current h-6 w-6 text-red-500"
          role="button"
          @click="closeAlert"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
        >
          <title>Close</title>
          <path
            d="M14.348 14.849a1.2 1.2 0 0 1-1.697 0L10 11.819l-2.651 3.029a1.2 1.2 0 1 1-1.697-1.697l2.758-3.15-2.759-3.152a1.2 1.2 0 1 1 1.697-1.697L10 8.183l2.651-3.031a1.2 1.2 0 1 1 1.697 1.697l-2.758 3.152 2.758 3.15a1.2 1.2 0 0 1 0 1.698z"
          />
        </svg>
      </span>
    </div>

    <div
      class="bg-white dark:bg-base-100 p-6 rounded-lg shadow-md w-full max-w-sm"
    >
      <h2 class="text-2xl font-bold mb-4">Welcome to ITBKK-KP2</h2>

      <div class="mb-4">
        <span class="block text-sm font-medium light:text-gray-700"
          >Username</span
        >
        <input
          type="text"
          v-model="userData.username"
          class="itbkk-username input input-bordered w-full mt-1"
          maxlength="50"
        />
      </div>
      <div class="mb-6">
        <span class="block text-sm font-medium light:text-gray-700"
          >Password</span
        >
        <input
          type="password"
          v-model="userData.password"
          class="itbkk-password input input-bordered w-full mt-1"
          maxlength="14"
        />
      </div>
      <button
        class="itbkk-button-signin btn w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-400"
        :class="!isFormValid ? 'disabled' : ''"
        :disabled="!isFormValid"
        @click="handleLogin"
      >
        Login
      </button>
    </div>
  </div>
</template>

<style scoped></style>
