<script setup>
import { ref, onBeforeMount } from 'vue';
import { useAccountStore } from '@/stores/account';
import LoadingComponent from '@/components/loadingComponent.vue';
import router from '@/router';
import { useRoute } from 'vue-router';
import { useToastStore } from '@/stores/toast';
import { useBoardStore } from '@/stores/board';
import { acceptInvitation, deleteCollaborator, getAllBoard, getBoardById } from '@/lib/fetchUtils';
// !  ====================================== Variable ==================================
const accountStore = useAccountStore();
const invitedBoard = ref();
const loading = ref(false);
const route = useRoute();
const toastStore = useToastStore();
const boardStore = useBoardStore();
// !  ====================================== Function ==================================
onBeforeMount(async () => {
  if (!accountStore.isLoggedIn) {
    router.push({ name: 'login' , query: { redirect : route.path } });
    // router.push({ name: 'login' });
    toastStore.createToast('Please login before accept invitation', 'danger');
    return;
  }

  try {
    loading.value = true;
    await getAllBoard();
  } catch (error) {
  } finally {
    loading.value = false;
    invitedBoard.value = useBoardStore().getPendingBoardById(route.params.boardId);
    if (invitedBoard.value !== undefined && invitedBoard.value !== null) {
      console.log(invitedBoard.value);
      const collaborator = invitedBoard.value.collaborators.find((collaborator) => collaborator.oid === accountStore.tokenDetail.oid);
      if (collaborator) {
        invitedBoard.value.accessRightUserGot = collaborator.accessRight;
      }
    }
  }
});

// !  ====================================== Fetch =====================================
async function handleAcceptInvitation() {
  let collabData = {
    // boardId: invitedBoard.value.id,
    // userId: accountStore.tokenDetail.oid,
    // accessRight: invitedBoard.value.accessRightUserGot,
    status: 'ACCEPTED',
  };
  let boardId = invitedBoard.value.id;
  try {
    let result = await acceptInvitation(boardId, collabData);
    if (result.status === 200) {
      toastStore.createToast('Accept invitation successfully', 'success');
      boardStore.deleteBoard(boardId);
      try {
        getBoardById(boardId);
      } catch (error) {
      } finally {
        router.push({ name: 'task-list', params: { boardId: invitedBoard.value.id } });
        
      }
      // boardStore.boards.find((board) => board.id = invitedBoard.value.id).collaborators.find((collaborator) => collaborator.id === accountStore.tokenDetail.oid).status = 'ACCEPTED';
    } else {
      toastStore.createToast('Accept invitation failed', 'danger');
      router.push({ name: 'board-list' });
    }
  } catch (error) {
    console.log(error);
  }
}

async function handleRejectInvitation() {
  await deleteCollaborator(invitedBoard.value.id, accountStore.tokenDetail.oid).then(() => {
    toastStore.createToast('Reject invitation successfully', 'success');
    router.push({ name: 'board-list' });
    boardStore.deleteBoard(invitedBoard.value.id);
  });
}
</script>

<template>
  <transition>
    <div class="h-[90vh] w-full flex flex-col items-center justify-center p-8 rounded-lg" v-if="!loading && !invitedBoard">
      <p class="text-lg font-semibold text-gray-800 text-center mb-4">Sorry, we couldn't find your active invitation to this board.</p>
      <div class="flex space-x-4">
        <button @click="accountStore.clearToken;router.push({ name: 'board-list' })" class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">Go To Board List</button>
        <button @click="router.push({ name: 'login' })" class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">Go To Sign In</button>
      </div>
    </div>
  </transition>
  <transition>
    <div class="h-[90vh] w-full flex flex-col items-center justify-center p-8 rounded-lg" v-if="!loading && invitedBoard">
      <p class="text-lg font-semibold text-gray-800 text-center mb-4">
        {{ invitedBoard?.owner.name }} has invited you to collaborate with <span class="font-bold">{{ invitedBoard.accessRightUserGot }}</span> access right on
        <span class="font-bold">{{ invitedBoard?.name }}</span> board
      </p>
      <div class="flex space-x-4">
        <button @click="handleAcceptInvitation()" class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">Accept Invitation</button>
        <button @click="handleRejectInvitation()" class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition">Decline</button>
      </div>
    </div>
  </transition>
  <Transition>
    <div class="h-[90vh] w-full flex flex-col items-center justify-center p-96">
      <LoadingComponent v-if="loading" />
    </div>
  </Transition>
</template>

<style scoped></style>
