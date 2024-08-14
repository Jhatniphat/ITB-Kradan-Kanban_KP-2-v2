import { createRouter, createWebHistory } from "vue-router";
import TaskListView from "../views/TasklistView.vue";
import StatusListView from "@/views/StatusListView.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      redirect: { path: "/task" },
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
    { path: '/:pathMatch(.*)*', redirect: { name : "tasklist"}},
  ],
});

export default router;
