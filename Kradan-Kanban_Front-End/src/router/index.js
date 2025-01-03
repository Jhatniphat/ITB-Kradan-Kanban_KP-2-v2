import { createRouter, createWebHistory } from 'vue-router';
import LogicPage from '@/components/LogicPage.vue';
import { useAccountStore } from '@/stores/account';
import { useBoardStore } from '@/stores/board.js';
import { getAllBoard, login, getBoardById, getBoardByIdForGuest, checkTokenExpired } from '@/lib/fetchUtils.js';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'none'
      // redirect: { path: '/login' },
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/board/add',
      name: 'add-board',
      component: () => import('../views/BoardView.vue'),
    },
    {
      path: '/board/invitation',
      name: 'invitation',
      component: () => import('../views/InvitationList.vue'),
    },
    {
      path: '/board/:boardId/collab/invitations',
      name: 'collab-invitations',
      component: () => import('../views/CollabInvitationAcceptView.vue'),
    },
    {
      path: '/board/:boardId/status/:statusId/edit',
      name: 'status-edit',
      component: () => import('../views/StatusListView.vue'),
      alias: ['/board/:boardId/status/:statusId'],
    },
    {
      path: '/board/:boardId/status',
      name: 'status-list',
      component: () => import('../views/StatusListView.vue'),
      alias: ['/board/:boardId/status/add'],
    },
    {
      path: '/board/:boardId/collab',
      name: 'collab-management',
      component: () => import('../views/CollabManageView.vue'),
    },
    {
      path: '/board/:boardId/task/:taskId/edit',
      name: 'task-edit',
      component: () => import('../views/TasklistView.vue'),
      alias: ['/board/:boardId/task/:taskId'],
    },
    {
      path: '/board/:boardId',
      name: 'task-list',
      component: () => import('../views/TasklistView.vue'),
      alias: ['/board/:boardId/task/add'],
      //  ? alias is similar to redirect but it doesn't change the URL
    },
    {
      path: '/board',
      name: 'board-list',
      component: () => import('../views/BoardView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: LogicPage,
    },
    {
      path: '/access-denied',
      name: 'AccessDenied',
      component: () => import('../views/AccessDenied.vue'),
    },
    { path: '/:pathMatch(.*)*', redirect: { name: 'board' } },
  ],
  scrollBehavior(to) {
    if (to.hash) {
      const element = document.querySelector(to.hash);
      if (element) {
        element.scrollIntoView({
          behavior: 'smooth',
          block: 'nearest',  // จัดตำแหน่ง element ให้อยู่ในมุมมองที่ใกล้ที่สุด
        });
      }
    }
    return false; // ไม่ใช้การเลื่อนค่าเริ่มต้น
  },
});

router.beforeEach(async (to, from, next) => {

  const accountStore = useAccountStore();
  const boardStore = useBoardStore();
  if (to.name === 'none') {
    console.log(accountStore.isLoggedIn);
    if (!accountStore.isLoggedIn) {
      next({ name: 'login' });
    }
    else { next({ name: 'board-list' }); }
  }

  if (to.name.includes('task') || to.name.includes('status') || to.name.includes('collab-management')) {
    if (boardStore.boards.length === 0 && accountStore.refreshToken !== '') {
      await getAllBoard();
    }

    const boardId = to.params.boardId;
    await boardStore.setCurrentBoardId(boardId);

    let board;
    if (accountStore.refreshToken === '') {
      board = await getBoardByIdForGuest(boardId);
    } else {
      board = await getBoardById(boardId);
    }

    if (!board) {
      router.push({ name: 'AccessDenied' });
      return;
    }

    if (accountStore.refreshToken !== '') {
      await checkTokenExpired();
    }
  }

  next(); // Proceed to the route
});

export default router;
