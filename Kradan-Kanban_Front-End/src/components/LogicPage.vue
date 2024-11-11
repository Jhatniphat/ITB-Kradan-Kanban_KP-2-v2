<script setup>
import { ref, computed, onMounted } from "vue";
import { login } from "@/lib/fetchUtils.js";
// import router from "@/router/index.js";
import { useAccountStore } from "@/stores/account.js";
import { onBeforeRouteUpdate, useRoute, useRouter } from "vue-router";
import { useToastStore } from "@/stores/toast.js";
const route = useRoute();
const router = useRouter();
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
  useToastStore().createToast("Logging in...", "waiting");
  if (!isFormValid.value) {
    showAlert.value = true;
  } else {
    let result = await login(userData.value.username, userData.value.password);
    if (result.status === 200) {
      // const decodedToken = VueJwtDecode.decode(result.payload?.access_token);
      // accountStore.setTokenDetail(decodedToken);
      const redirectPath = route.query.redirect || '/board';
      router.push(redirectPath);
      // router.push({ name: "board-list" });
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
  <div class="h-screen flex flex-row">
    <div class="basis-1/2 bg-white h-full">

      <div class="flex flex-col justify-center gap-3 items-center h-screen">
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

        <div class="bg-white p-6 rounded-lg shadow-md w-full max-w-sm">
          <!--            <h2 class="text-2xl font-bold mb-4">Welcome to ITBKK-KP2</h2>-->
          <h2 class="text-2xl font-bold mb-4">
            ITB Kandan Kanban
          </h2>
          <h3 class="text-xl font-bold mb-4">
            manage your work, balance your life
          </h3>

          <form>
            <div class="mb-4">
              <span class="block text-sm font-medium text-gray-700"
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
              <span class="block text-sm font-medium">Password</span>
              <input
                type="password"
                v-model="userData.password"
                class="itbkk-password input input-bordered w-full mt-1"
                maxlength="14"
                autocomplete="off"
              />
            </div>
          </form>
          <!-- Normal sign in-->
          <button
            class="itbkk-button-signin btn w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-400"
            :class="!isFormValid ? 'disabled' : ''"
            :disabled="!isFormValid"
            @click="handleLogin"
          >
            Login
          </button>
          <!-- microsoft sign in-->
          <button
            class="btn w-full bg-white text-black hover:text-white py-2 px-4 mt-2 rounded hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-400"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="21"
              height="21"
              viewBox="0 0 21 21"
            >
              <title>MS-SymbolLockup</title>
              <rect x="1" y="1" width="9" height="9" fill="#f25022" />
              <rect x="1" y="11" width="9" height="9" fill="#00a4ef" />
              <rect x="11" y="1" width="9" height="9" fill="#7fba00" />
              <rect x="11" y="11" width="9" height="9" fill="#ffb900" />
            </svg>
            Sign in with Microsoft
          </button>
        </div>
      </div>
    </div>
    <!-- Right side - Black background -->
    <div class="basis-1/2 h-full">
      <video autoplay="autoplay" loop class="w-full h-full object-cover" muted>
        <source src="../assets/loginPageVideo.mp4" type="video/mp4" />
        <source src="../assets/loginPageVideo.webm" type="video/webm" />
        Your browser does not support the video tag or the file format of this
        video.
      </video>
    </div>
  </div>
</template>

<style scoped></style>
