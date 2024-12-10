<script setup>
// ? import lib
import { onBeforeMount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { addBoard, getAllBoard, deleteCollaborator , deleteBoard } from '../lib/fetchUtils.js';
import router from '@/router';
// ? import component
import Modal from '../components/Modal.vue';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import { useToastStore } from '@/stores/toast.js';
import loadingComponent from '@/components/loadingComponent.vue';
// ! ================= Variable ======================
// ? ----------------- Store and Route ---------------
const boardStore = useBoardStore();
const route = useRoute();
const loading = ref([]);
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
  addLoading('Loading Boards');
  try {
    await getAllBoard();
  } catch (err) {
    console.log(err);
    toastStore.createToast('An error has occurred, please try again later', 'danger');
  } finally {
    allInvites.value = boardStore.getAllPendingBoard();
    if (allInvites.value.length > 0) {
      toastStore.createToast('You Have New Invitations');
    }
    if (allBoard.value !== null && allBoard.value !== undefined) {
      if (allBoard.value?.payload !== 'No board found') {
        allBoard.value = boardStore.boards;
      } else {
        allBoard.value = [];
      }
    }
    prepareData(['']);
    removeLoading('Loading Boards');
  }
}

async function prepareData([filterBy]) {
  // if (useBoardStore().boards.length === 0) {
  //   await fetchBoardData();
  // }
  if (filterBy !== '') {
    allBoard.value = boardStore.boards.filter((board) => board.visibility.includes(filterBy) || board.name.includes(filterBy) || board.owner.name.includes(filterBy));
  } else {
    allBoard.value = boardStore.boards;
  }
  personalBoard.value = boardStore.boards.filter((board) => board.owner.oid === useAccountStore().tokenDetail.oid);
  collabBoard.value = boardStore.boards.filter((board) => {
    return board.owner.oid !== useAccountStore().tokenDetail.oid && board.collaborators.findIndex((collab) => collab.oid === useAccountStore().tokenDetail.oid ) !== -1;
  });
}

watch([filterBy, boardStore.boards], (newValue) => {
  prepareData(newValue);
  console.log('watch');
},{ deep: true });

prepareData(['']);

watch(
  boardStore.boards,
  () => {
    prepareData(['']);
  },
  { deep: true }
);

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
const showDeleteModal = ref(false);
const collabBoardName = ref('');
const selectedBoardId = ref(0);

const openLeaveModal = (boardId, boardName) => {
  collabBoardName.value = boardName;
  selectedBoardId.value = boardId;
  showLeaveModal.value = true;
};

const openDeleteModal = (boardId, boardName) => {
  collabBoardName.value = boardName;
  selectedBoardId.value = boardId;
  showDeleteModal.value = true;
};

const leaveBoard = async () => {
  let res;
  const oid = useAccountStore().tokenDetail.oid;
  try {
    res = await deleteCollaborator(selectedBoardId.value, oid);
    console.log(res);
    if (typeof res === 'object') {
      toastStore.createToast('Leave Board successfully');
      boardStore.deleteBoard(res.id); // remove board from store
      prepareData(['']);
      router.go(0)
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

const DeleteBoard = async () => {
  let res;
  const oid = useAccountStore().tokenDetail.oid;
  try {
    res = await deleteBoard(selectedBoardId.value, oid);
    console.log(res);
    if (typeof res === 'object') {
      toastStore.createToast('Leave Board successfully');
      prepareData(['']);
      router.go(0)
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

function addLoading(load) {
  loading.value.push(load);
}

function removeLoading(load) {
  loading.value = loading.value.filter((l) => l !== load);
  console.table(loading.value);
}
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

  <loadingComponent :loading="loading" v-if="loading.length > 0" />

  <div class="w-3/4 mx-auto mt-10 relative" v-if="loading.length === 0">
    <div class="flex flex-col">
      <div class="flex">
        <div class="flex-1"></div>
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
              <td class="text-center font-normal">
                  <button class="btn btn-outline btn-error" @click="openDeleteModal(board.id, board.name)">Delete</button>
                </td>
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
                <td class="text-center font-normal link link-primary" @click="router.push({ name : board.collaborators.find((collab) => collab.oid === useAccountStore().tokenDetail.oid)['status'] !== 'PENDING' ? 'task-list':'collab-invitations' , params : { boardId : board.id}})">
                  {{ board.name }}
                </td>
                <td class="text-center font-normal">{{ board.visibility }}</td>
                <td class="text-center font-normal">
                  {{ board?.owner?.name }}
                </td>
                <td class="text-center font-normal">
                  {{ board.collaborators.find((collab) => collab.oid === useAccountStore().tokenDetail.oid)['accessRight'] }}
                </td>
                <td class="text-center font-normal" v-if="board.collaborators.find((collab) => collab.oid === useAccountStore().tokenDetail.oid)['status'] !== 'PENDING'">
                  <button class="btn btn-outline btn-error" @click="openLeaveModal(board.id, board.name)">Leave</button>
                </td>
                <td class="text-center font-normal" v-else>
                  <button class="btn btn-outline btn-primary" @click="router.push({ name : 'collab-invitations' , params : { boardId : board.id}})">Go To Invitation</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <!-- DeleteModal -->
    <Modal :showModal="showLeaveModal">
      <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
        <h1 class="m-2 pb-4 text-2xl font-bold">Leave Board</h1>
        <hr />
        <h1 class="itbkk-message font-semibold text-xl p-8">Do you want to leave this "{{ collabBoardName }}" board?</h1>
        <hr />
        <div class="flex flex-row-reverse gap-4 mt-5">
          <button @click="showLeaveModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
          <button @click="leaveBoard()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
            {{ loading.length > 0 ? '' : 'Confirm' }}
            <span class="loading loading-spinner text-success" v-if="loading.length > 0"></span>
          </button>
        </div>
      </div>
    </Modal>

    <Modal :showModal="showDeleteModal">
      <div class="flex flex-col p-5 bg-slate-50 dark:bg-base-100 rounded-lg w-full">
        <h1 class="m-2 pb-4 text-2xl font-bold">Delete Board</h1>
        <hr />
        <h1 class="itbkk-message font-semibold text-xl p-8">Do you want to delete this "{{ collabBoardName }}" board?</h1>
        <hr />
        <div class="flex flex-row-reverse gap-4 mt-5">
          <button @click="showDeleteModal = false" class="itbkk-button-cancel btn btn-outline btn-error basis-1/6">Close</button>
          <button @click="DeleteBoard()" class="itbkk-button-confirm btn btn-outline btn-success basis-1/6">
            {{ loading.length > 0 ? '' : 'Confirm' }}
            <span class="loading loading-spinner text-success" v-if="loading.length > 0"></span>
          </button>
        </div>
      </div>
    </Modal>

  </div>
</template>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
  z-index: -1;
}

::backdrop {
  background-image: linear-gradient(45deg, magenta, rebeccapurple, dodgerblue, green);
  opacity: 0.75;
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
