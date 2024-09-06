import {createRouter, createWebHistory} from "vue-router";
import TaskListView from "../views/TasklistView.vue";
import StatusListView from "@/views/StatusListView.vue";
import LogicPage from "@/components/LogicPage.vue";
import {useAccountStore} from "@/stores/account";
import {useBoardStore} from "@/stores/board.js";


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            redirect: {path: "/login"},
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
            path: "/board/:boardId/status",
            name : "status",
            component : () => import("../views/StatusListView.vue"),
            alias: ["/board/:boardId/status/add" , "/board/:boardId/status/:statusId/edit"],
            //  ? alias is similar to redirect but it doesn't change the URL
            //  ? "/board/:id/task/add" , "/board/:id/task/:task-id/edit" , "/board/:id/task/:task-id/edit"
        },
        {
            path: "/board/:boardId",
            name : "task",
            component : () => import("../views/TasklistView.vue"),
            alias: ["/board/:boardId/task/add" , "/board/:boardId/task/:taskId/edit"]
        //  ? alias is similar to redirect but it doesn't change the URL
        //  ? "/board/:id/task/add" , "/board/:id/task/:task-id/edit" , "/board/:id/task/:task-id/edit"
        },
        {
            path: "/board",
            name: "board",
            component: () => import("../views/BoardView.vue"),
        },

        //
        // {
        //     path: "/task",
        //     name: "tasklist",
        //     component: TaskListView,
        // },
        // {
        //     path: "/task/:id",
        //     component: TaskListView,
        // },
        // {
        //     path: "/task/:id/edit",
        //     component: TaskListView,
        // },
        // {
        //     path: "/status",
        //     name: "statuslist",
        //     component: StatusListView,
        // },
        // {
        //     path: "/status/:id", // Define a route for editing task
        //     component: StatusListView,
        // },
        // {
        //     path: "/status/:id/edit",
        //     component: StatusListView,
        // },
        {
            path: "/login",
            name: "login",
            component: LogicPage,
        },
        {path: "/:pathMatch(.*)*", redirect: {name: "tasklist"}},
    ],
});

router.beforeEach(async (to, from) => {
    const accountStore = useAccountStore();
    const boardStore = useBoardStore()
    // const isAuthenticated = !!accountStore.tokenDetail.token;
    const isAuthenticated = accountStore.tokenDetail !== {};

    if (to.name === "task" || to.name === "status") {
        // boardStore.currentBoard.id = to.params.boardId =
        console.log(to.params.boardId)
        boardStore.setCurrentBoardId(to.params.boardId)
    }
    if (
        // make sure the user is authenticated
        !isAuthenticated &&
        // ❗️ Avoid an infinite redirect
        to.name !== 'Login'
    ) {
        // redirect the user to the login page
        return { name: 'Login' }
    }
})

export default router;
