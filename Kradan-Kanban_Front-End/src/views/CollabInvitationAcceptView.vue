<script setup>
import { ref, computed, onBeforeMount } from 'vue';
import { useAccountStore } from '@/stores/account';
import LoadingComponent from '@/components/loadingComponent.vue';
import router from '@/router';
import { useRoute } from 'vue-router';
import { useToastStore } from '@/stores/toast';
import { useBoardStore } from '@/stores/board';
import { getAllBoard } from '@/lib/fetchUtils';
// !  ====================================== Variable ==================================
const accountStore = useAccountStore();
const invitedBoard = ref({});
const loading = ref(false);
const route = useRoute();
const toastStore = useToastStore();
const boardStore = useBoardStore();
// !  ====================================== Function ==================================
onBeforeMount(async () => {
  if (!accountStore.isLoggedIn) {
    router.push({ name: 'login' });
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
    invitedBoard.value.accessRightUserGot = invitedBoard.value.collaborators.find((collaborator) => collaborator.id === accountStore.tokenDetail.oid).accessRight;
    console.log(invitedBoard.value.accessRightUserGot);
  }
});
</script>

<template>
  <transition>
    <!-- <div class="h-full w-full" v-if="!loading">
      <p>
        {{ invitedBoard.owner.name }} has invited you to collaborate with {{ invitedBoard.accessRightUserGot }} access right on {{ invitedBoard.name }} board
      </p>
      <button @click="boardStore.acceptInvitation(invitedBoard.id)">Accept invitation</button>
      <button @click="boardStore.rejectInvitation(invitedBoard.id)">Decline</button>
    </div> -->
    <div class="h-screen w-full flex flex-col items-center justify-center bg-gray-100 p-8 rounded-lg shadow-md" v-if="!loading">
      <p class="text-lg font-semibold text-gray-800 text-center mb-4">
        {{ invitedBoard.owner.name }} has invited you to collaborate with <span class="font-bold">{{ invitedBoard.accessRightUserGot }}</span> access right on
        <span class="font-bold">{{ invitedBoard.name }}</span> board
      </p>
      <div class="flex space-x-4">
        <button @click="boardStore.acceptInvitation(invitedBoard.id)" class="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition">Accept Invitation</button>
        <button @click="boardStore.rejectInvitation(invitedBoard.id)" class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition">Decline</button>
      </div>
    </div>
  </transition>
  <Transition>
    <LoadingComponent class="h-full w-full" v-if="loading" />
  </Transition>
</template>

<style scoped></style>
