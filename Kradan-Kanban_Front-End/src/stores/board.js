import {defineStore} from "pinia";
import router from "@/router/index.js";
import {getBoardById, getBoardByIdForGuest} from "@/lib/fetchUtils";
import {useAccountStore} from "@/stores/account.js";

export const useBoardStore = defineStore("Board", {
    state: () => ({
        boards: [],
        currentBoardId: "",
        currentBoard: {},
    }),
    getters: {
        getAllBoard() {
            return this.boards;
        },
        getBoardById(id) {
            return this.boards.find((board) => board.id === id);
        },
    },
    actions: {
        isBoardExist(id) {
            return this.boards.some((board) => board.id === id);
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
        },
        findBoardById(id) {
            return this.boards.find((board) => board.id === id);
        },
        async setCurrentBoardId(boardId) {
            if (this.findBoardById(boardId) !== null && this.findBoardById(boardId) !== undefined) {
                this.currentBoardId = boardId;
                this.currentBoard = this.findBoardById(boardId);
                return this.currentBoard;
            } else {
                try {
                    let board
                    if (useAccountStore().tokenRaw === "") {
                        board = await getBoardByIdForGuest(boardId);
                    } else {
                        board = await getBoardById(boardId);
                    }
                    if (!board || !board.payload) {
                        router.push({name: "board"});
                    } else {
                        this.currentBoardId = boardId;
                        this.currentBoard = board.payload;
                    }
                } catch (error) {
                    console.error("Failed to fetch board:", error);
                    router.push("/login");
                }
            }

        }
    }
    // return { count, doubleCount, increment }
});
