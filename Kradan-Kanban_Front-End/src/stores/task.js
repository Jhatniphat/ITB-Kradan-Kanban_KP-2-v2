import { defineStore } from "pinia";
import { getAllTasks } from "@/lib/fetchUtils";

export const useTaskStore = defineStore("tasks", {
  state: () => ({
    tasks: [],
  }),
  actions: {
    async getAllTasks() {
      if (this.tasks.length > 0) return this.tasks;
      else {
        let items;
        try {
          items = await getAllTasks();
          this.tasks = await items;
          if (typeof items !== Number) {
            return this.tasks;
          }
        } catch (error) {
          console.log(error);
        }
      }
    },
    addStoreTask(newTask) {
      this.tasks.push(newTask);
    },
    deleteStoreTask(taskToDelete) {
      const index = this.tasks.findIndex((task) => task.id === taskToDelete.id);
      if (index !== -1) {
        this.tasks.splice(index, 1);
      }
    },
    editStoreTask(updatedTask) {
      if (updatedTask) {
        const index = this.tasks.findIndex((task) => task.id === updatedTask.id);
      if (index !== -1) {
        this.tasks.splice(index, 1, updatedTask)
      }
      }
    },
    transferStatus(oldStatus , newStatus) {
      this.tasks.forEach((task) => {
        if (task.status === oldStatus) {
          task.status = newStatus;
        }
      })
    },
  },

  // return { count, doubleCount, increment }
});
