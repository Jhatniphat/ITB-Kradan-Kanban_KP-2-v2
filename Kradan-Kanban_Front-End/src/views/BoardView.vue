<script setup>
// ? import lib
import { onBeforeMount, onMounted, ref, watch } from "vue";
import { useRoute } from "vue-router";
import {
  addBoard,
  deleteTask,
  getAllBoard,
  getLimitStatus,
} from "../lib/fetchUtils.js";
import router from "@/router";
// ? import component
import Modal from "../components/Modal.vue";
import Taskdetail from "../components/Tasks/Taskdetail.vue";
import AddTaskModal from "@/components/Tasks/AddTaskModal.vue";
import EditLimitStatus from "@/components/EditLimitStatus.vue";
import { useBoardStore } from "@/stores/board.js";
import { useAccountStore } from "@/stores/account.js";

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const boardStore = useBoardStore();
const route = useRoute();
const loading = ref(false);
// ? ----------------- Modal ---------------
const toast = ref({ status: "", msg: "" });
const showAddModal = ref(false);
// ? ----------------- Common and Local ---------------
const allBoard = ref(boardStore.boards);
// const allBoard = computed(() => boardStore.boards);
const newBoard = ref({ name: `${useAccountStore().userName} personal board` });
const errorText = ref({ name: "" });
// ! ================= Life Cycle Hook ===============
onMounted(() => {
  if (boardStore.boards.length === 0) {
    fetchBoardData();
  } else {
    allBoard.value = boardStore.boards;
  }
  if (route.path === "/board/add") {
    openAdd();
  }
  console.log(allBoard.value);
});

// ! ================= Function ======================
// todo : refactor this function
async function fetchBoardData() {
  loading.value = true;
  try {
    await getAllBoard();
  } catch (err) {
    console.log(err);
    showToast({
      status: "error",
      msg: "An error has occurred, please try again later",
    });
  } finally {
    if (allBoard.value !== null && allBoard.value !== undefined) {
      if (allBoard.value?.payload !== "No board found") {
        allBoard.value = boardStore.boards;
        console.log(allBoard);
        if (allBoard.value.length === 1) {
          console.log(allBoard.value[0].id);
          router.push(`/board/${allBoard.value[0].id}`);
        }
      } else {
        allBoard.value = [];
      }
    }
    loading.value = false;
  }
}

// todo : Move this add Modal component
function openAdd() {
  router.push("/board/add");
  showAddModal.value = true;
}

watch(newBoard.value, () => {
  if (newBoard.value.name.trim().length > 120) {
    errorText.value.name = `Board name can't long more than 120 character`;
  } else if (newBoard.value.name.trim().length === 0) {
    errorText.value.name = `Board name can't be empty`;
  } else {
    errorText.value.name = "";
  }
});

async function saveAddBoard() {
  let result;
  try {
    result = await addBoard(newBoard.value);
  } catch (err) {
    console.log(err);
    showToast({
      status: "error",
      msg: "An error has occurred, please try again later",
    });
  } finally {
    showAddModal.value = false;
    if (result.status === 200 || result.status === 201) {
      showToast({ status: "success", msg: "Add board successfully" });
      allBoard.value = result.payload;
      router.push(`/board/${result.payload.id}`);
    } else showToast({ status: "error", msg: "Add board Failed" });
  }
}

// ! ================= Modal ======================
const showToast = (toastData) => {
  toast.value = toastData;
  setTimeout(() => {
    toast.value = { ...{ status: "" } };
  }, 5000);
};

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
  <!--  todo : Move this add Modal component -->
  <Modal :show-modal="showAddModal">
    <div
      class="flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full h-fit itbkk-modal-new"
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
          class="itbkk-board-name input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400"
        />
        <div class="label">
          <!-- ? Error Text -->
          <span
            v-if="errorText.name !== ''"
            class="label-text-alt text-error"
            >{{ errorText.name }}</span
          >
          <!-- count input name -->
          <span
            v-if="newBoard.name.length <= 120 && newBoard.name.length > 0"
            class="justify-end text-gray-400 label-text-alt"
            >{{ newBoard.name.length }} / 120</span
          >
          <span
            v-if="newBoard.name.length === 0 && errorText.name !== ''"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ newBoard.name.length }} / 120</span
          >
          <span
            v-if="newBoard.name.length > 120"
            class="flex justify-end text-red-400 label-text-alt"
            >{{ newBoard.name.length }} / 120</span
          >
        </div>
      </label>
      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <button
          @click="showAddModal = false"
          class="itbkk-button-cancel btn btn-outline btn-error basis-1/6"
        >
          Cancel
        </button>
        <button
          type="button"
          @click="saveAddBoard()"
          :disabled="errorText.name !== ''"
          :class="{ disabled: errorText.name !== '' }"
          class="itbkk-button-ok btn btn-outline btn-success basis-1/6"
        >
          Save
        </button>
      </div>
    </div>
  </Modal>

  <!-- dropdowns status -->
  <div class="w-3/4 mx-auto mt-10 relative">
    <div class="flex flex-col">
      <!--      <h1>{{ allBoard }}</h1>-->
      <!--      <div class="justify-end ">-->
      <!--        <button-->
      <!--            class="itbkk-button-add btn btn-square btn-outline w-fit px-5 mr-1 float-right"-->
      <!--            @click="openAdd()"-->
      <!--        >-->
      <!--          Create Personal Board-->
      <!--        </button>-->
      <!--      </div>-->
      <h1 class="w-full text-center text-2xl font-bold">Your Board List</h1>
      <div class="flex flex-wrap w-3/4 p-10 gap-5">
        <div
          class="board-list-card itbkk-button-create place-content-center"
          @click="openAdd()"
        >
          <h1 class="text-center text-2xl font-bold top-1/2">
            Create Personal Board
          </h1>
        </div>
        <div
          class="board-list-card"
          v-for="board in allBoard"
          @click="router.push(`/board/${board.id}`)"
          v-if="allBoard.length > 0"
        >
          <h1 class="text-wrap text-xl font-bold text-center">
            {{ board.name }}
          </h1>
          <h1 class="text-wrap text-l font-bold text-center pt-5">Owner</h1>
          <h2 class="text-wrap text-m text-center">{{ board?.owner?.name }}</h2>
        </div>
      </div>

      <!--      <table-->
      <!--          class="table table-lg table-pin-rows table-pin-cols w-3/4 font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1">-->
      <!--        <thead>-->
      <!--        <tr>-->
      <!--          <th>Board Name</th>-->
      <!--        </tr>-->
      <!--        </thead>-->
      <!--        <tbody>-->
      <!--        <tr v-for="board in allboard">-->
      <!--          <td>{{ board.name }}</td>-->
      <!--        </tr>-->
      <!--        </tbody>-->
      <!--      </table>-->
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
  background: #f10c49;
  animation: l10 1.5s infinite linear;
}

@keyframes l10 {
  0% {
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6,
      30px 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51,
      0 0 #e3aad6;
  }
  12.5% {
    box-shadow: 0 0 #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6,
      30px 0 #f4dd51, calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 0 #f4dd51,
      0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  25% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 30px 0 #f4dd51,
      calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 30px #f4dd51, 0 0 #e3aad6,
      0 0 #f4dd51, 0 0 #e3aad6;
  }
  37.5% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51,
      calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 30px #f4dd51,
      calc(-30px * 0.707) calc(30px * 0.707) #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  50% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6,
      0 30px #f4dd51, calc(-30px * 0.707) calc(30px * 0.707) #e3aad6,
      -30px 0 #f4dd51, 0 0 #e3aad6;
  }
  62.5% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51,
      calc(-30px * 0.707) calc(30px * 0.707) #e3aad6, -30px 0 #f4dd51,
      calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  75% {
    box-shadow: 0 -30px #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6,
      0 0 #f4dd51, 0 0 #e3aad6, -30px 0 #f4dd51,
      calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  87.5% {
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6,
      0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51,
      calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  100% {
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6,
      30px 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51,
      0 0 #e3aad6;
  }
}

.board-list-card {
  width: 200px;
  height: 150px;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 10px;
  transition: all 0.3s;
}

.board-list-card:hover {
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.5);
  transition: all 0.3s;
}
</style>
