import {defineStore} from "pinia";
import VueJwtDecode from 'vue-jwt-decode'
import {useBoardStore} from "@/stores/board.js";
import { useToastStore } from "./toast";
import { useTaskStore } from "./task";
import { useStatusStore } from "./status";
export const useAccountStore = defineStore("account", {
        state: () => ({
            tokenDetail: JSON.parse(localStorage.getItem("tokenDetail")) || {},
            tokenRaw: localStorage.getItem("token") || "", // ? raw access token
            refreshToken: localStorage.getItem("refresh_token") || "", // ? refresh token
        }),
        getters: {
            token: (state) => state.tokenRaw,
            userName: (state) => state.tokenDetail.name || "",
            isLoggedIn: (state) => state.refreshToken !== "",
        },
        actions: {
            setToken(token) {
                if (token.access_token) {
                    this.tokenRaw = token.access_token;
                    localStorage.setItem("token", token.access_token);
                    this.tokenDetail = VueJwtDecode.decode(token.access_token);
                    localStorage.setItem("tokenDetail", JSON.stringify(this.tokenDetail));
                }
                if (token.refresh_token) {
                    this.refreshToken = token.refresh_token;
                    localStorage.setItem("refresh_token", token.refresh_token);
                }
                useToastStore().createToast(`Welcome back, ${this.tokenDetail?.name}`, "success"); 
            },
            setTokenDetail(tokenDetail) {
                this.tokenDetail = tokenDetail;
                localStorage.setItem("tokenDetail", JSON.stringify(tokenDetail));
            },
            clearTokenDetail() {
                this.tokenDetail = {};
                localStorage.removeItem("tokenDetail");
            },
            isAccessTokenExpired() {
                if (!this.tokenRaw) return true;
                const decodedToken = VueJwtDecode.decode(this.tokenRaw);
                const currentTime = Math.floor(Date.now() / 1000);
                return decodedToken.exp < currentTime;
            },
            isRefreshTokenExpired() {
                if (!this.refreshToken) return true;
                const decodedToken = VueJwtDecode.decode(this.refreshToken);
                const currentTime = Math.floor(Date.now() / 1000);
                return decodedToken.exp < currentTime;
            },
            clearToken() {
                console.table(this.tokenDetail)
                useToastStore().createToast(`See you later, ${this.tokenDetail?.name}`, "success"); 
                this.tokenRaw = "";
                this.refreshToken = "";
                this.tokenDetail = {};
                localStorage.removeItem("token");
                localStorage.removeItem("refresh_token");
                localStorage.removeItem("tokenDetail");
                useTaskStore().clearTask()
                useStatusStore().clearStatus()
                useBoardStore().clearBoard()
            }
        },
    })
;
