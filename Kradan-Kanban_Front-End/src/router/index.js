import {createRouter, createWebHistory} from "vue-router";
import TaskListView from "../views/TasklistView.vue";
import StatusListView from "@/views/StatusListView.vue";
import LogicPage from "@/components/LogicPage.vue";
import {useAccountStore} from "@/stores/account";


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            redirect: {path: "/task"},
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
            path: "/task",
            name: "tasklist",
            component: TaskListView,
        },
        {
            path: "/task/:id",
            component: TaskListView,
        },
        {
            path: "/task/:id/edit",
            component: TaskListView,
        },
        {
            path: "/status",
            name: "statuslist",
            component: StatusListView,
        },
        {
            path: "/status/:id", // Define a route for editing task
            component: StatusListView,
        },
        {
            path: "/status/:id/edit",
            component: StatusListView,
        },
        {
            path: "/login",
            name: "login",
            component: LogicPage,
        },
        {path: "/:pathMatch(.*)*", redirect: {name: "tasklist"}},
    ],
});

// router.beforeEach((to, from) => {
//     const accountStore = useAccountStore();
//     const isAuthenticated = !!accountStore.tokenDetail.token;
//
//     // if (to.path !== "/login" && !isAuthenticated) {
//     //   // next("/login");
//     // } else {
//     //   next();
//     // }
//     if (
//         // make sure the user is authenticated
//         !isAuthenticated &&
//         // ❗️ Avoid an infinite redirect
//         to.name !== '/login'
//     ) {
//         // redirect the user to the login page
//         return {name: 'login'}
//     } else if (to.name === 'login') {
//         return {name: 'login'};
//     }
// });

router.beforeEach(async (to, from) => {
    const accountStore = useAccountStore();
    // const isAuthenticated = !!accountStore.tokenDetail.token;
    const isAuthenticated = accountStore.tokenDetail !== {};
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
