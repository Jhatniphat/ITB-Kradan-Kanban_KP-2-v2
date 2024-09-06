<script setup>
// ? import lib
import {onBeforeMount, onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import {addBoard, deleteTask, getAllBoard, getLimitStatus} from "../lib/fetchUtils.js";
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
const showAddModal = ref(false);
// ? ----------------- Common and Local ---------------
const allBoard = ref([])
const newBoard = ref({name: ""})
const errorText = ref({name: ""})
// ! ================= Life Cycle Hook ===============
onMounted(() => {
  fetchBoardData()
  if (route.path === "/board/add") {
    openAdd()
  }
})

// ! ================= Function ======================
// todo : refactor this function
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
      if (allBoard.value?.payload !== "No board found") {
        console.log("boardFound", allBoard.value)
        router.push(`/board/${allBoard.value.payload.id}`)
      } else {
        allBoard.value = []
      }
    }
  }
}

// todo : Move this add Modal component
function openAdd() {
  router.push("/board/add")
  showAddModal.value = true
}

watch(newBoard.value, () => {
  if (newBoard.value.name.trim().length > 120) {
    showToast({status: "error", msg: "Board name can't long more than 100 character"})
  }
  if (newBoard.value.name.trim().length === 0) {
    showToast({status: "error", msg: "Board name can't be empty"})
  }
})

async function saveAddBoard() {
  console.log(newBoard.value)
  try {
    await addBoard(newBoard.value)
  } catch (err) {
    console.log(err)
    showToast({status: "error", msg: "An error has occurred, please try again later"})
  }
}
// ! ================= Modal ======================
const showToast = (toastData) => {
  toast.value = toastData
  setTimeout(() => {
    toast.value = {...{status: ""}}
  }, 5000)
}

// const closeAddModal = (res) => {
//   showAddModal.value = false
//   if (res === null) return 0
//   if (typeof res === "object") {
//     showToast({status: "success", msg: "Add status successfuly"})
//     statusStore.addStoreStatus(res)
//   } else showToast({status: "error", msg: "Add status Failed"})
// }
//
// const openEdit = (id) => {
//   selectedId.value = id
//   showEdit.value = true
//   router.push(`/status/${id}`)
// }
//
// const closeEdit = (res) => {
//   showEdit.value = false
//   if (res === null) return 0
//   // if (res === 404) showToast({status: "error", msg: "An error has occurred, the status does not exist"});
//   if (typeof res === "object") {
//     showToast({status: "success", msg: "Edit status successfuly"})
//     statusStore.editStoreStatus(res)
//   } else
//     showToast({
//       status: "error",
//       msg: "An error has occurred, the status does not exist",
//     })
// }
</script>

<template>

  <Modal :show-modal="showAddModal">
    <div
        class="flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full h-fit"
    >
      <!-- * title -->
      <label class="form-control w-full">
        <div class="label">
          <!-- ? Head -->
          <span class="label-text">Title</span>
        </div>
        <input
            v-model="newBoard.name"
            type="text"
            placeholder="Type here"
            class="itbkk-title input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400"
        />
        <div class="label">
          <!-- ? Error Text -->
          <span v-if="errorText.name !== ''" class="label-text-alt text-error">{{
              errorText.name
            }}</span>
          <!-- count input name -->
          <span
              v-if="newBoard.name.length <= 100 && newBoard.name.length > 0"
              class="justify-end text-gray-400 label-text-alt"
          >{{ newBoard.name.length }} / 100</span
          >
          <span
              v-if="newBoard.name.length === 0 && errorText.name !== ''"
              class="flex justify-end text-red-400 label-text-alt"
          >{{ newBoard.name.length }} / 100</span
          >
          <span
              v-if="newBoard.name.length > 100"
              class="flex justify-end text-red-400 label-text-alt"
          >{{ newBoard.name.length }} / 100</span
          >
        </div>
      </label>
      <hr/>
      <div class="flex flex-row-reverse gap-4 mt-5">
        <button
            @click="showAddModal = false"
            class="relative inline-flex items-center justify-center p-0.5 mb-2 me-2 overflow-hidden text-sm font-medium text-gray-900 rounded-lg group bg-gradient-to-br from-green-400 to-blue-600 group-hover:from-green-400 group-hover:to-blue-600 hover:text-white dark:text-white focus:ring-4 focus:outline-none focus:ring-green-200 dark:focus:ring-green-800">
      <span
          class="relative px-5 py-2.5 transition-all ease-in duration-200 bg-white dark:bg-gray-900 rounded-md group-hover:bg-opacity-0">
      Cancel
      </span>
        </button>
        <button type="button"
                @click="saveAddBoard()"
                class="transition-all ease-in duration-200 text-white bg-gradient-to-br from-green-400 to-blue-600 hover:bg-gradient-to-bl focus:ring-4 focus:outline-none focus:ring-green-200 dark:focus:ring-green-800 font-medium rounded-lg text-sm px-5 py-2.5 text-center me-2 mb-2">
          Save
        </button>
      </div>


    </div>


  </Modal>

  <!-- dropdowns status -->
  <div class="w-3/4 mx-auto mt-10 relative" style="border: coral 1px solid">


    <div class="flex flex-col">
      <!--      <h1>{{ allBoard }}</h1>-->
      <div class="justify-end ">
        <button
            class="itbkk-button-add btn btn-square btn-outline w-fit px-5 mr-1 float-right"
            @click="openAdd()"
        >
          Create Personal Board
        </button>
      </div>


      <table
          class="table table-lg table-pin-rows table-pin-cols w-3/4 font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1">
        <thead>
        <tr>
          <th>Board Name</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="board in allboard">
          <td>{{ board.name }}</td>
        </tr>
        </tbody>
      </table>
    </div>
    <!--    <div v-if="loading" class="loader"></div>-->
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
