import { defineStore } from "pinia";

export const useAccountStore = defineStore("account", {
  state: () => ({
    tokenDetail: JSON.parse(localStorage.getItem("tokenDetail")) || {},
  }),
  getters: {
    userName: (state) => state.tokenDetail.name || "",
  },
  actions: {
    setTokenDetail(tokenDetail) {
      this.tokenDetail = tokenDetail;
      localStorage.setItem("tokenDetail", JSON.stringify(tokenDetail));
    },
    clearTokenDetail() {
      this.tokenDetail = {};
      localStorage.removeItem("tokenDetail");
    },
  },
});
