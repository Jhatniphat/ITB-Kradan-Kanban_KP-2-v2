import { createRouter, createWebHistory } from "vue-router";
import LogicPage from "@/components/LogicPage.vue";
import { useAccountStore } from "@/stores/account";
import { useBoardStore } from "@/stores/board.js";
import { getAllBoard, login, validateToken, getBoardById } from "@/lib/fetchUtils.js";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: { path: "/login" },
    },
    {
      path: "/about",
      name: "about",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/AboutView.vue"),
    },
    {
      path: "/board/add",
      name: "add-board",
      component: () => import("../views/BoardView.vue"),
    },
    {
      path: "/board/:boardId/status/:statusId/edit",
      name: "status-edit",
      component: () => import("../views/StatusListView.vue"),
    },
    {
      path: "/board/:boardId/status",
      name: "status",
      component: () => import("../views/StatusListView.vue"),
      alias: ["/board/:boardId/status/add"],
    },
    {
      path: "/board/:boardId/task/:taskId/edit",
      name: "task-edit",
      component: () => import("../views/TasklistView.vue"),
    },
    {
      path: "/board/:boardId",
      name: "task",
      component: () => import("../views/TasklistView.vue"),
      alias: ["/board/:boardId/task/add"],
      //  ? alias is similar to redirect but it doesn't change the URL
    },
    {
      path: "/board",
      name: "board",
      component: () => import("../views/BoardView.vue"),
    },
    {
      path: "/login",
      name: "login",
      component: LogicPage,
    },
    {
      path: "/access-denied",
      name: "AccessDenied",
      component: () => import("../views/AccessDenied.vue"),
    },
    { path: "/:pathMatch(.*)*", redirect: { name: "tasklist" } },
  ],
});
router.beforeEach(async (to, from, next) => {
    const accountStore = useAccountStore();
    const boardStore = useBoardStore();
    const isAuthenticated = accountStore.tokenDetail !== {};
  
    // Validate token for non-login routes
    if (to.name !== "login") {
      await validateToken();
      console.log(accountStore.tokenRaw)
    }
  
    if (to.name === "task" || to.name === "status") {
      if (boardStore.boards.length === 0) {
        await getAllBoard(); 
      }
  
      const boardId = to.params.boardId;
      console.log(boardId)
      const board =  await getBoardById(boardId);
  
      if (!board) {
        console.log("Board have no data!!")
        next({ name: "AccessDenied" }); 
        return;
      }
  
      const isOwner = board.ownerId === accountStore.userId;
      const isBoardPrivate = board.visibility === "PRIVATE";
  
      if (isBoardPrivate && !isOwner) {
        next({ name: "AccessDenied" }); 
        return;
      }
      boardStore.setCurrentBoardId(boardId);
    }
  
    // Ensure the user is authenticated if necessary
    if (!isAuthenticated && to.name !== "login") {
      return { name: "login" }; // Redirect to login if not authenticated
    }
  
    next(); // Proceed to the route
  });
  

// router.beforeEach(async (to, from, next) => {
//   const accountStore = useAccountStore();
//   const boardStore = useBoardStore();
//   const isAuthenticated = accountStore.tokenDetail !== {};

//   if (to.name !== "login") {
//     await validateToken();
//     console.log(accountStore.tokenRaw);
//   }
//   if (to.name === "task" || to.name === "status") {
//     // boardStore.currentBoard.id = to.params.boardId =

//     if (boardStore.boards.length === 0) {
//       await getAllBoard();
//     }
//     const boardId = to.params.boardId;
//     const board = boardStore.getBoardById(boardId);

//     // if (!board) {
//     //   next("/access-denied");
//     //   return;
//     // }

//     const isOwner = board.ownerId === accountStore.userId;
//     const isBoardPrivate = board.visibility === "PRIVATE";

//     if (isBoardPrivate && !isOwner) {
//       next({ name: "AccessDenied" });
//       return;
//     }

//     boardStore.setCurrentBoardId(boardId);
//   }
//   if (
//     // make sure the user is authenticated
//     !isAuthenticated &&
//     // ❗️ Avoid an infinite redirect
//     to.name !== "Login"
//   ) {
//     // redirect the user to the login page
//     return { name: "Login" };
//   }
//   next();
// });

export default router;