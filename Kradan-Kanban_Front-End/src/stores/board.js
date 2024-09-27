import { defineStore } from "pinia";
import router from "@/router/index.js";
import { getBoardById } from "@/lib/fetchUtils";

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
    // setCurrentBoardId(boardId) {
    //   const board = this.boards.find((board) => board.id === boardId);
    //   if (!board) {
    //     console.log("ไม่มีบอร์ดดดดดด")
    //     router.push("/login"); // Redirect if the board doesn't exist
    //   } else {
    //     this.currentBoardId = boardId;
    //     this.currentBoard = board; // Set the current board
    //   }
    // },
  //   setCurrentBoardId(boardId) {
  //     if(boardId !== this.boards.id && this.boards.id !== undefined) {
  //         router.push("/login")
  //     }
  //     this.currentBoardId = boardId;
  //     this.currentBoard = this.boards.find((board) => board.id === boardId);
  //    }
  // },
    async setCurrentBoardId(boardId) {
      try {
          const board = await getBoardById(boardId); 
          if (!board || !board.payload) {
            console.log("No Board Data!!")
              // router.push("/login"); 
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
  // return { count, doubleCount, increment }
});
