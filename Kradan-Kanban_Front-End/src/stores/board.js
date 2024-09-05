import { defineStore } from "pinia";
import {getAllBoard} from "@/lib/fetchUtils.js";

export const useBoardStore = defineStore("Board", {
    state: () => ({
        boards: [],
    }),
    getters: {
        getBoardById: (state) => (id) => {
            return state.boards.find((board) => board.id === id);
        },
    },
    actions: {
        getAllBoard() {
            return this.boards;
        },
        addBoard(newBoard) {
            this.boards.push(newBoard);
        },
        addManyBoard(newBoards) {
            newBoards.forEach((board) => {
                this.boards.push(board);
            });
        },
        deleteBoard(boardId) {
            const index = this.boards.findIndex((board) => board.id === boardId);
            if (index !== -1) {
                this.boards.splice(index, 1);
            }
        }

    },

    // return { count, doubleCount, increment }
});
