<script setup>
// ? import lib
import {onBeforeMount, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {deleteTask, getAllBoard, getLimitStatus} from "../lib/fetchUtils.js";
import router from "@/router";
// ? import component
import Modal from "../components/Modal.vue";
import Taskdetail from "../components/Tasks/Taskdetail.vue";
import AddTaskModal from "@/components/Tasks/AddTaskModal.vue";
import EditLimitStatus from "@/components/EditLimitStatus.vue";
import {useBoardStore} from "@/stores/board.js";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const boardStore = useBoardStore();
const route = useRoute();
const loading = ref(false);
// ? ----------------- Modal ---------------
const toast = ref({status: "", msg: ""})
const allBoard = ref([])

// ! ================= Life Cycle Hook ===============
onMounted(() => {
  fetchBoardData()
})

// ! ================= Function ======================
async function fetchBoardData() {
  loading.value = true
  try {
    allBoard.value = await getAllBoard()
  } catch (err) {
    console.log(err)
    showToast({status: "error", msg: "An error has occurred, please try again later"})
  } finally {
    loading.value = false
    if (allBoard.value !== null && allBoard.value !== undefined) {
      router.push(`/board/${allBoard.value.id}`)
    }
  }
}

// ! ================= Modal ======================
const showToast = (toastData) => {
  toast.value = toastData
  setTimeout(() => {
    toast.value = {...{status: ""}}
  }, 5000)
}

const closeAddModal = (res) => {
  showAddModal.value = false
  if (res === null) return 0
  if (typeof res === "object") {
    showToast({status: "success", msg: "Add status successfuly"})
    statusStore.addStoreStatus(res)
  } else showToast({status: "error", msg: "Add status Failed"})
}

const openEdit = (id) => {
  selectedId.value = id
  showEdit.value = true
  router.push(`/status/${id}`)
}

const closeEdit = (res) => {
  showEdit.value = false
  if (res === null) return 0
  // if (res === 404) showToast({status: "error", msg: "An error has occurred, the status does not exist"});
  if (typeof res === "object") {
    showToast({status: "success", msg: "Edit status successfuly"})
    statusStore.editStoreStatus(res)
  } else
    showToast({
      status: "error",
      msg: "An error has occurred, the status does not exist",
    })
}
</script>

<template>


  <!-- dropdowns status -->
  <div class="w-3/4 mx-auto mt-10 relative" style="border: coral 1px solid">


    <div>
      <h1>{{ allBoard }}</h1>
    </div>
    <div v-if="loading" class="loader"></div>
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
  </div>
</template>

<style scoped>
::backdrop {
  background-image: linear-gradient(
      45deg,
      magenta,
      rebeccapurple,
      dodgerblue,
      green
  );
  opacity: 0.75;
}

/* HTML: <div class="loader"></div> */
.loader {
  width: 22px;
  aspect-ratio: 1;
  border-radius: 50%;
  background: #F10C49;
  animation: l10 1.5s infinite linear;
}

@keyframes l10 {
  0% {
    box-shadow: 0 -30px #F4DD51, calc(30px * 0.707) calc(-30px * 0.707) #E3AAD6, 30px 0 #F4DD51, 0 0 #E3AAD6,
    0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6
  }
  12.5% {
    box-shadow: 0 0 #F4DD51, calc(30px * 0.707) calc(-30px * 0.707) #E3AAD6, 30px 0 #F4DD51, calc(30px * 0.707) calc(30px * 0.707) #E3AAD6,
    0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6
  }
  25% {
    box-shadow: 0 0 #F4DD51, 0 0 #E3AAD6, 30px 0 #F4DD51, calc(30px * 0.707) calc(30px * 0.707) #E3AAD6,
    0 30px #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6
  }
  37.5% {
    box-shadow: 0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, calc(30px * 0.707) calc(30px * 0.707) #E3AAD6,
    0 30px #F4DD51, calc(-30px * 0.707) calc(30px * 0.707) #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6
  }
  50% {
    box-shadow: 0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6,
    0 30px #F4DD51, calc(-30px * 0.707) calc(30px * 0.707) #E3AAD6, -30px 0 #F4DD51, 0 0 #E3AAD6
  }
  62.5% {
    box-shadow: 0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6,
    0 0 #F4DD51, calc(-30px * 0.707) calc(30px * 0.707) #E3AAD6, -30px 0 #F4DD51, calc(-30px * 0.707) calc(-30px * 0.707) #E3AAD6
  }
  75% {
    box-shadow: 0 -30px #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6,
    0 0 #F4DD51, 0 0 #E3AAD6, -30px 0 #F4DD51, calc(-30px * 0.707) calc(-30px * 0.707) #E3AAD6
  }
  87.5% {
    box-shadow: 0 -30px #F4DD51, calc(30px * 0.707) calc(-30px * 0.707) #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6,
    0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, calc(-30px * 0.707) calc(-30px * 0.707) #E3AAD6
  }
  100% {
    box-shadow: 0 -30px #F4DD51, calc(30px * 0.707) calc(-30px * 0.707) #E3AAD6, 30px 0 #F4DD51, 0 0 #E3AAD6,
    0 0 #F4DD51, 0 0 #E3AAD6, 0 0 #F4DD51, 0 0 #E3AAD6
  }

}
</style>
