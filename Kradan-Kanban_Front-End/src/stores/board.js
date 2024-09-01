import { defineStore } from "pinia";

export const useBoardStore = defineStore("Board", {
    state: () => ({
        boards: [{ id: 1, name: "Board 1" }, { id: 2, name: "Board 2" }],
    }),
    getters: {
        getBoardById: (state) => (id) => {
            return state.boards.find((board) => board.id === id);
        },
        getAllBoard: (state) => {
            return state.boards;
        },
    },
    actions: {
        async getAllBoard() {
            if (this.boards.length > 0) return this.boards;
            // else {
            //     let items;
            //     try {
            //         // items = await getAllBoard();
            //         this.Board = await items;
            //         if (typeof items !== Number) {
            //             return this.Board;
            //         }
            //     } catch (error) {
            //         console.log(error);
            //     }
            // }
        },

    },

    // return { count, doubleCount, increment }
});
