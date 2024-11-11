<script setup>
// ? import lib
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { addBoard, deleteTask, getAllBoard, deleteCollaborator } from '../lib/fetchUtils.js';
import router from '@/router';
// ? import component
import Modal from '../components/Modal.vue';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import { useToastStore } from '@/stores/toast.js';

// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const boardStore = useBoardStore();
const route = useRoute();
const loading = ref(false);
const toastStore = useToastStore();
// ? ----------------- Modal ---------------
const toast = ref({ status: '', msg: '' });
const showAddModal = ref(false);
// ? ----------------- Common and Local ---------------
const allBoard = ref(boardStore.boards);
// const allBoard = computed(() => boardStore.boards);
const newBoard = ref({ name: `${useAccountStore().userName} personal board` });
const errorText = ref({ name: '' });
const allInvites = ref([]);
// ? ----------------- LIST --------------------------
const personalBoard = ref([]);
const collabBoard = ref([]);
const filterBy = ref('');
// ! ================= Life Cycle Hook ===============
onBeforeMount(() => {
  if (boardStore.boards.length === 0) {
    fetchBoardData();
  } else {
    allBoard.value = boardStore.boards;
  }
  if (allInvites.value.length > 0) {
    toastStore.createToast('You Have New Invitations');
  }
  if (route.path === '/board/add') {
    openAdd();
  }
});

// ! ================= Function ======================
// todo : refactor this function
async function fetchBoardData() {
  loading.value = true;
  try {
    await getAllBoard();
  } catch (err) {
    console.log(err);
    toastStore.createToast('An error has occurred, please try again later', 'danger');
  } finally {
    allInvites.value = boardStore.getAllPendingBoard();
    console.log(allInvites.value);
    if (allInvites.value.length > 0) {
      toastStore.createToast('You Have New Invitations');
    }
    if (allBoard.value !== null && allBoard.value !== undefined) {
      if (allBoard.value?.payload !== 'No board found') {
        allBoard.value = boardStore.boards;
        // if (allBoard.value.length === 1) {
        //   router.push(`/board/${allBoard.value[0].id}`);
        // }
      } else {
        allBoard.value = [];
      }
    }
    prepareData([''])
    loading.value = false;
  }
}

async function prepareData([filterBy]) {
  // if (useBoardStore().boards.length === 0) {
  //   await fetchBoardData();
  // }
  console.log('prepareData')
  if (filterBy !== '') {
    allBoard.value = boardStore.boards.filter((board) => board.visibility.includes(filterBy) || board.name.includes(filterBy) || board.owner.name.includes(filterBy));
  } else {
    allBoard.value = boardStore.boards;
  }
  personalBoard.value = boardStore.boards.filter((board) => board.owner.oid === useAccountStore().tokenDetail.oid);
  collabBoard.value =  boardStore.boards.filter((board) => board.owner.oid !== useAccountStore().tokenDetail.oid && board.collaborators.findIndex((collab) => collab.oid === useAccountStore().tokenDetail.oid && collab.status !== "PENDING"  !== -1)) 
}

watch([filterBy, boardStore.boards], (newValue) => {
  prepareData(newValue);
});

prepareData(['']);

watch(boardStore.boards, () => {
  prepareData(['']);
} , { deep: true });

// todo : Move this add Modal component
function openAdd() {
  router.push('/board/add');
  showAddModal.value = true;
}

watch(newBoard.value, () => {
  if (newBoard.value.name.trim().length > 120) {
    errorText.value.name = `Board name can't long more than 120 character`;
  } else if (newBoard.value.name.trim().length === 0) {
    errorText.value.name = `Board name can't be empty`;
  } else {
    errorText.value.name = '';
  }
});

async function saveAddBoard() {
  let result;
  try {
    result = await addBoard(newBoard.value);
  } catch (err) {
    console.log(err);
    toastStore.createToast('An error has occurred, please try again later', 'danger');
  } finally {
    showAddModal.value = false;
    if (result.status === 200 || result.status === 201) {
      toastStore.createToast('Create board successfully');
      allBoard.value = result.payload;
      // router.push(`/board/${result.payload.id}`);
      router.push({ name: 'task-list', params: { boardId: result.payload.id } });
    } else {
      toastStore.createToast('Create board Failed', 'danger');
    }
  }
}

// ! ================= Modal ======================

const showLeaveModal = ref(false);
const collabBoardName = ref('');
const selectedBoardId = ref(0);

const openLeaveModal = (boardId, boardName) => {
  collabBoardName.value = boardName;
  selectedBoardId.value = boardId;
  showLeaveModal.value = true;
};

const leaveBoard = async () => {
  let res;
  const oid = useAccountStore().tokenDetail.oid;
  try {
    res = await deleteCollaborator(selectedBoardId.value, oid);
    if (typeof res === 'object') {
      toastStore.createToast('Leave Board successfully');
    } else if (res === 403 || 404) {
      router.push(`/board`);
    } else {
      toastStore.createToast('There is a problem. Please try again later.', 'danger');
    }
  } catch (error) {
    console.log(error);
  } finally {
    showLeaveModal.value = false;
  }
};
</script>

<template>
  <!--  todo : Move this add Modal component -->
  <Modal :show-modal="showAddModal">
    <div class="flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full h-fit itbkk-modal-new">
      <!-- * title -->
      <label class="form-control w-full">
        <div class="label">
          <!-- ? Head -->
          <span class="label-text">Title</span>
        </div>
        <input v-model="newBoard.name" type="text" placeholder="Type here" class="itbkk-board-name input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400" maxlength="120" />
        <div class="label">
          <!-- ? Error Text -->
          <span v-if="errorText.name !== ''" class="label-text-alt text-error">{{ errorText.name }}</span>
          <!-- count input name -->
          <span v-if="newBoard.name.length <= 120 && newBoard.name.length > 0" class="justify-end text-gray-400 label-text-alt">{{ newBoard.name.length }} / 120</span>
          <span v-if="newBoard.name.length === 0 && errorText.name !== ''" class="flex justify-end text-red-400 label-text-alt">{{ newBoard.name.length }} / 120</span>
          <span v-if="newBoard.name.length > 120" class="flex justify-end text-red-400 label-text-alt">{{ newBoard.name.length }} / 120</span>
        </div>
      </label>
      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <button @click="showAddModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Cancel</button>
        <button type="button" @click="saveAddBoard()" :disabled="errorText.name !== ''" :class="{ disabled: errorText.name !== '' }" class="itbkk-button-ok btn btn-outline btn-success basis-1/6">
          Save
        </button>
      </div>
    </div>
  </Modal>

  <div class="w-3/4 mx-auto mt-10 relative">
    <div class="flex flex-col">
      <div class="flex">
        <div class="flex-1">filter</div>
        <h1 class="text-center text-2xl font-bold flex-1">Personal Board</h1>
        <div class="flex-1">
          <button class="btn btn-primary itbkk-button-create float-right" @click="openAdd()">
            Create Personal Board
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M4 12H20M12 4V20" stroke="#ffffff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </button>
        </div>
      </div>
      <div class="flex flex-wrap p-10 gap-5 place-items-center">
        <table class="table table-lg w-full">
          <thead>
            <tr>
              <th class="font-bold text-black text-base text-center w-[40%]">Board Name</th>
              <th class="font-bold text-black text-base text-center w-[20%]">Visibility</th>
              <th class="font-bold text-black text-base text-center w-[20%]">Owner</th>
              <th class="font-bold text-black text-base text-center w-[10%]">id</th>
              <th class="font-bold text-black text-base text-center w-[10%]">More Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="board in personalBoard">
              <td class="text-center font-normal link link-primary itbkk-board-name" @click="router.push(`/board/${board.id}`)">
                {{ board.name }}
              </td>
              <td class="text-center font-normal">{{ board.visibility }}</td>
              <td class="text-center font-normal">{{ board?.owner?.name }}</td>
              <td class="text-center font-normal">{{ board.id }}</td>
              <td class="text-center font-normal"></td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex flex-col" v-if="collabBoard.length !== 0">
        <div class="flex">
          <h1 class="text-center text-2xl font-bold flex-1">Collaboration Board</h1>
        </div>
        <div class="flex flex-wrap p-10 gap-5 place-items-center">
          <table class="table table-lg w-full">
            <thead>
              <tr>
                <th class="font-bold text-black text-base text-center w-[40%]">Board Name</th>
                <th class="font-bold text-black text-base text-center w-[20%]">Visibility</th>
                <th class="font-bold text-black text-base text-center w-[20%]">Owner</th>
                <th class="font-bold text-black text-base text-center w-[10%]">Access</th>
                <th class="font-bold text-black text-base text-center w-[10%]">More Action</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="board in collabBoard">
                <td class="text-center font-normal link link-primary" @click="router.push(`/board/${board.id}`)">
                  {{ board.name }}
                </td>
                <td class="text-center font-normal">{{ board.visibility }}</td>
                <td class="text-center font-normal">
                  {{ board?.owner?.name }}
                </td>
                <td class="text-center font-normal">
                  {{ board.collaborators.find((collab) => collab.oid === useAccountStore().tokenDetail.oid)['accessRight'] }}
                </td>
                <td class="text-center font-normal">
                  <button class="btn btn-outline btn-error" @click="openLeaveModal(board.id, board.name)">Leave</button>
                </td>
              </tr>
            </tbody>
          </table>
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
    <!-- DeleteModal -->
    <Modal :showModal="showLeaveModal">
      <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
        <h1 class="m-2 pb-4 text-2xl font-bold">Leave Board</h1>
        <hr />
        <h1 class="itbkk-message font-semibold text-xl p-8">
          <!-- Do you want to delete the task "{{ deleteTaskTitle }}" -->
          Do you want to leave this "{{ collabBoardName }}" board?
        </h1>
        <hr />
        <div class="flex flex-row-reverse gap-4 mt-5">
          <button @click="showLeaveModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
          <button @click="leaveBoard()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
            {{ loading ? '' : 'Confirm' }}
            <span class="loading loading-spinner text-success" v-if="loading"></span>
          </button>
        </div>
      </div>
    </Modal>
    <!-- Toast -->
    <div class="toast">
      <div role="alert" class="alert" :class="`alert-${toast.status}`" v-if="toast.status !== ''">
        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24" v-if="toast.status === 'success'">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <svg xmlns="http://www.w3.org/2000/svg" class="stroke-current shrink-0 h-6 w-6" fill="none" viewBox="0 0 24 24" v-if="toast.status === 'error'">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <span>{{ toast.msg }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
::backdrop {
  background-image: linear-gradient(45deg, magenta, rebeccapurple, dodgerblue, green);
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
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6, 30px 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  12.5% {
    box-shadow: 0 0 #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6, 30px 0 #f4dd51, calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  25% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 30px 0 #f4dd51, calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 30px #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  37.5% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, calc(30px * 0.707) calc(30px * 0.707) #e3aad6, 0 30px #f4dd51, calc(-30px * 0.707) calc(30px * 0.707) #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
  50% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 30px #f4dd51, calc(-30px * 0.707) calc(30px * 0.707) #e3aad6, -30px 0 #f4dd51, 0 0 #e3aad6;
  }
  62.5% {
    box-shadow: 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, calc(-30px * 0.707) calc(30px * 0.707) #e3aad6, -30px 0 #f4dd51, calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  75% {
    box-shadow: 0 -30px #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, -30px 0 #f4dd51, calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  87.5% {
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, calc(-30px * 0.707) calc(-30px * 0.707) #e3aad6;
  }
  100% {
    box-shadow: 0 -30px #f4dd51, calc(30px * 0.707) calc(-30px * 0.707) #e3aad6, 30px 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6, 0 0 #f4dd51, 0 0 #e3aad6;
  }
}

.board-list-card {
  min-width: 300px;
  height: 150px;
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 10px;
  transition: all 0.3s;

  .board-name {
    font-size: 1.5rem;
    font-weight: bold;
    word-break: break-word;
  }
}

.board-list-card:hover {
  box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.5);
  transition: all 0.3s;
}
</style>
