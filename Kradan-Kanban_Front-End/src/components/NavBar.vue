<script setup>
import { useRoute } from 'vue-router';

import { ref, watch } from 'vue';
import router from '@/router/index.js';
import { computed } from 'vue';
import { useAccountStore } from '@/stores/account.js';
import { useBoardStore } from '@/stores/board.js';

const accountStore = useAccountStore();

const userName = computed(() => accountStore.userName);

const handleLogout = () => {
  // accountStore.clearTokenDetail();
  accountStore.clearToken();
  router.push('/login');
};

const route = useRoute();

let showTaskStatusCollabMenu = ref(false);

function routeChange(to) {
  const currentBoardId = useBoardStore().currentBoardId;
  if (to === 'task') {
    router.push({ name: 'task-list', params: { boardId: currentBoardId } });
  } else if (to === 'status') {
    router.push({ name: 'status-list', params: { boardId: currentBoardId } });
  } else if (to === 'collab') {
    router.push({ name: 'collab-management', params: { boardId: currentBoardId } });
  } else if (to === 'board') {
    router.push({ name: 'board-list' });
  } else if (to === 'invitation') {
    router.push({ name: 'invitation' });
  }
}

function checkrouteName(value) {
  if ((value.toString().includes('collab') || value.toString().includes('task') || value.toString().includes('status')) && value.toString() !== 'collab-invitations') {
    showTaskStatusCollabMenu.value = true;
  } else {
    showTaskStatusCollabMenu.value = false;
  }

  if (value.toString().includes('task')) {
    return `Board : ${useBoardStore().currentBoard.name}`;
  } else if (value.toString().includes('status')) {
    return `Board : ${useBoardStore().currentBoard.name} - Status`;
  } else if (value.toString().includes('board')) {
    return 'Board Management';
  } else if (value.toString() === 'collab-invitations') {
    return `Board Invitation`;
  } else if (value.toString().includes('collab')) {
    return `Board : ${useBoardStore().currentBoard.name} - Collaborator`;
  } else if (value.toString().includes('invitation')) {
    return 'invitation';
  } else {
    return value;
  }
}

let currentRouteName = ref(checkrouteName(route.name));

watch(
  () => route.name,
  (value) => {
    currentRouteName.value = checkrouteName(value);
  }
);

const isSidebarCollapsed = ref(false);
const menus = computed(() => [
  {
    label: 'Board Management',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"><path fill="currentColor" d="M5 21q-.825 0-1.412-.587T3 19V5q0-.825.588-1.412T5 3h14q.825 0 1.413.588T21 5v14q0 .825-.587 1.413T19 21zm2-4h2V7H7zm8-2h2V7h-2zm-4-3h2V7h-2z"/></svg>',
    action: () => routeChange('board'),
    visble: true,
  },
  {
    label: 'Task / Issues',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"><path fill="currentColor" d="M2 21V6h6V2h8v4h6v15zm8-15h4V4h-4z"/></svg>',
    action: () => routeChange('task'),
    visble: showTaskStatusCollabMenu.value,
  },
  {
    label: 'Status',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"><path fill="currentColor" d="M2 21v-2h20v2zm4-4q-.825 0-1.412-.587T4 15V8q0-.825.588-1.412T6 6h3q0-1.25.875-2.125T12 3t2.125.875T15 6h3q.825 0 1.413.588T20 8v7q0 .825-.587 1.413T18 17zm11-2h1V8h-1zm-6.5-9h3q0-.65-.425-1.075T12 4.5t-1.075.425T10.5 6M7 15V8H6v7zm1.5-7v7h7V8zM7 15h1.5zm10 0h-1.5zM7 15H6zm1.5 0h7zm8.5 0h1z"/></svg>',
    action: () => routeChange('status'),
    visble: showTaskStatusCollabMenu.value,
  },
  {
    label: 'Collaborator',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"><path fill="currentColor" d="M0 18v-1.575q0-1.075 1.1-1.75T4 14q.325 0 .625.013t.575.062q-.35.525-.525 1.1t-.175 1.2V18zm6 0v-1.625q0-.8.438-1.463t1.237-1.162T9.588 13T12 12.75q1.325 0 2.438.25t1.912.75t1.225 1.163t.425 1.462V18zm13.5 0v-1.625q0-.65-.162-1.225t-.488-1.075q.275-.05.563-.062T20 14q1.8 0 2.9.663t1.1 1.762V18zM4 13q-.825 0-1.412-.587T2 11q0-.85.588-1.425T4 9q.85 0 1.425.575T6 11q0 .825-.575 1.413T4 13m16 0q-.825 0-1.412-.587T18 11q0-.85.588-1.425T20 9q.85 0 1.425.575T22 11q0 .825-.575 1.413T20 13m-8-1q-1.25 0-2.125-.875T9 9q0-1.275.875-2.137T12 6q1.275 0 2.138.863T15 9q0 1.25-.862 2.125T12 12"/></svg>',
    action: () => routeChange('collab'),
    visble: showTaskStatusCollabMenu.value,
  },
  {
    label: 'Invitation',
    icon: '<svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24"><path fill="currentColor" d="M4 20q-.825 0-1.412-.587T2 18V6q0-.825.588-1.412T4 4h16q.825 0 1.413.588T22 6v12q0 .825-.587 1.413T20 20zm8-7l8-5V6l-8 5l-8-5v2z"/></svg>',
    action: () => routeChange('invitation'),
    visble: true,
  },
]);

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};
</script>

<template>
  <div class="flex h-screen">
    <!-- Sidebar -->
    <div class="bg-slate-100 text-white flex flex-col" :class="isSidebarCollapsed ? 'w-16' : 'w-64'">
      <!-- Sidebar Header -->
      <div class="flex items-center justify-between p-4 bg-gray-800">
        <span v-if="!isSidebarCollapsed" class="text-xl font-bold">ITB Kradan Kanban</span>
        <button @click="toggleSidebar" class="text-gray-400 hover:text-white">
          <svg xmlns="http://www.w3.org/2000/svg" :class="isSidebarCollapsed ? 'rotate-180' : ''" class="w-6 h-6 transition-transform" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 12H5m7 7l-7-7 7-7" />
          </svg>
        </button>
      </div>

      <!-- Sidebar Menu -->
      <nav class="flex-1 p-2">
        <TransitionGroup name="list" tag="ul">
          <li v-for="menu in menus.filter((menu) => menu.visble)" :key="menu.label" class="group flex items-center gap-4 p-2 rounded-md hover:bg-gray-700 cursor-pointer" @click="menu.action">
            <span class="text-black group-hover:text-white" v-html="menu.icon"> </span>
            <span v-if="!isSidebarCollapsed" class="text-sm font-medium text-black group-hover:text-white">
              {{ menu.label }}
            </span>
          </li>
        </TransitionGroup>
      </nav>

      <!-- Sidebar Footer -->
      <div class="p-4 bg-gray-800">
        <div class="flex items-center gap-4">
          <span class="rounded-full bg-gray-700 w-8 h-8 flex items-center justify-center text-gray-400">
            <i class="fas fa-user"></i>
          </span>
          <span v-if="!isSidebarCollapsed" class="text-sm text-gray-300">{{ userName }}</span>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col">
      <!-- Navbar -->
      <div class="h-16 bg-white flex items-center justify-between px-4 shadow-sm">
        <span class="text-lg font-bold">{{ currentRouteName }}</span>
        <div class="flex items-center gap-4">
          <button class="text-gray-500 hover:text-gray-700">
            <svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24">
              <path
                fill="currentColor"
                d="M4 20q-.825 0-1.412-.587T2 18V6q0-.825.588-1.412T4 4h16q.825 0 1.413.588T22 6v12q0 .825-.587 1.413T20 20zM20 8l-7.475 4.675q-.125.075-.262.113t-.263.037t-.262-.037t-.263-.113L4 8v10h16zm-8 3l8-5H4zM4 8v.25v-1.475v.025V6v.8v-.012V8.25zv10z"
              />
            </svg>
          </button>
          <div class="flex items-center gap-2">
            <span class="rounded-full bg-gray-300 w-8 h-8 flex items-center justify-center text-gray-700">
              <i class="fas fa-user"></i>
            </span>
            <span class="text-sm font-medium text-gray-700">{{ userName }}</span>
            <span class="rounded-full w-8 h-8 flex items-center justify-center text-gray-700" @click="handleLogout()">
              <svg xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24">
                <path
                  fill="currentColor"
                  d="M5 21q-.825 0-1.412-.587T3 19v-4h2v4h14V5H5v4H3V5q0-.825.588-1.412T5 3h14q.825 0 1.413.588T21 5v14q0 .825-.587 1.413T19 21zm5.5-4l-1.4-1.45L11.65 13H3v-2h8.65L9.1 8.45L10.5 7l5 5z"
                />
              </svg>
            </span>
          </div>
        </div>
      </div>
      <div class="overflow-scroll">
        <slot> </slot>
      </div>
      <!-- Content Area -->
    </div>
  </div>
</template>

<style scoped>
.list-move, /* apply transition to moving elements */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* ensure leaving items are taken out of layout flow so that moving
   animations can be calculated correctly. */
.list-leave-active {
  position: absolute;
}

</style>
