<script setup>
import { ref, onBeforeMount } from 'vue';
import { useAccountStore } from '@/stores/account';
import { getAllBoard } from '../lib/fetchUtils.js';
import router from '@/router';
import { useRoute } from 'vue-router';
import { useBoardStore } from '@/stores/board';
import VueJwtDecode from 'vue-jwt-decode';

// !  ====================================== Variable ==================================
const loading = ref(false);
const route = useRoute();
const boardStore = useBoardStore();

const allInvites = ref([]);
const checkInvites = ref([]);
const userId = ref('');

onBeforeMount(async () => {
  try {
    loading.value = true;
    await getAllBoard();
  } catch (error) {
  } finally {
    loading.value = false;
    userId.value = VueJwtDecode.decode(useAccountStore().tokenRaw).oid;
    checkInvites.value = boardStore.getAllPendingBoard();
    console.log(checkInvites.value);
    allInvites.value = boardStore
      .getAllPendingBoard()
      .map((board) => {
        let pendingCollaborator = board.collaborators.find((collab) => collab.oid === userId.value && collab.status === 'PENDING');

        return pendingCollaborator
          ? {
              id: board.id,
              name: board.name,
              accessRight: pendingCollaborator.accessRight,
              status: pendingCollaborator.status,
            }
          : null;
      })
      .filter((invite) => invite !== null);
  }
});

const routeToAccept = (id) => {
  router.push(`/board/${id}/collab/invitations`);
};
</script>

<template>
  <transition>
    <div class="h-full w-full" v-if="!loading">
      <div class="w-3/4 mx-auto mt-10 relative" v-if="!loading">
        <h1 class="w-full text-center text-2xl">Invitations</h1>
      </div>
      <div class="w-3/4 mx-auto mt-10 relative">
        <!-- Content -->
        <div class="opacity">
          <div class="w-full">
            <!-- Table -->
            <table class="table table-lg table-pin-rows table-pin-cols font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1">
              <!-- head -->
              <thead>
                <tr>
                  <th>No</th>
                  <th>Name</th>
                  <th>Id</th>
                  <th>AccessRight</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <!-- Listing -->
                <tr v-if="allInvites.length === 0">
                  <td colspan="4">No Pending Invitations</td>
                </tr>
                <tr v-if="allInvites !== null" v-for="(invite, index) in allInvites" class="itbkk-item hover">
                  <th>{{ index + 1 }}</th>
                  <td @click="routeToAccept(invite.id)">
                    {{ invite.name }}
                  </td>
                  <td>
                    {{ invite.id }}
                  </td>
                  <td>{{ invite.accessRight }}</td>
                  <td class="font-semibold text-yellow-400">{{ invite.status }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped></style>
