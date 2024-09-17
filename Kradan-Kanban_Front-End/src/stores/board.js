import { defineStore } from "pinia";
import router from "@/router/index.js";



export const useBoardStore = defineStore("Board", {
    state: () => ({
        boards: [],
        currentBoardId: "",
        currentBoard: {},
    }),
    getters: {
        // getBoardById: (state) => (id) => {
        //     console.log(id)
        //     console.log(state.boards.find((board) => board.id === id))
        //     return state.boards.find((board) => board.id === id);
        // },
        getAllBoard() {
            return this.boards;
        },
    },
    actions: {
        getBoardById(id) {
            return this.boards.find((board) => board.id === id);
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
        setCurrentBoardId(boardId) {
            if(boardId !== this.boards.id && this.boards.id !== undefined) {
                console.log("You are not the owner of this board")
                router.push("/login")
            }
            this.currentBoardId = boardId;
            this.currentBoard = this.boards.find((board) => board.id === boardId);
            console.table(this.boards)
            console.log(boardId)
            console.log(this.currentBoard)
        }


    },


    // return { count, doubleCount, increment }
});
