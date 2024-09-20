import { defineStore } from "pinia";

export const useAccountStore = defineStore("account", {
  state: () => ({
    tokenDetail: JSON.parse(localStorage.getItem("tokenDetail")) || {},
    tokenRaw: localStorage.getItem("token") || "",
  }),
  getters: {
    token: (state) => state.tokenRaw,
    userName: (state) => state.tokenDetail.name || "",
  },
  actions: {
    setToken(token) {
      this.tokenRaw = token.access_token;
      localStorage.setItem("token", token.access_token);
    },
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
