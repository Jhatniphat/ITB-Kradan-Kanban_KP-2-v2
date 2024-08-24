import { defineStore } from "pinia";

export const useAccountStore = defineStore("account", {
  state: () => ({
    tokenDetail: {},
  }),
  getters: {
    // Extract the name from the decoded token details
    userName: (state) => state.tokenDetail.name || "",
  },
  actions: {
    // Action to set the tokenDetail directly
    setTokenDetail(tokenDetail) {
      this.tokenDetail = tokenDetail;
    },
  },
});
