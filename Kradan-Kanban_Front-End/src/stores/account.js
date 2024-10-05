import {defineStore} from "pinia";
import VueJwtDecode from 'vue-jwt-decode'

export const useAccountStore = defineStore("account", {
        state: () => ({
            tokenDetail: JSON.parse(localStorage.getItem("tokenDetail")) || {},
            tokenRaw: localStorage.getItem("token") || "", // ? raw access token
            refreshToken: localStorage.getItem("refresh_token") || "", // ? refresh token
        }),
        getters: {
            token: (state) => state.tokenRaw,
            userName: (state) => state.tokenDetail.name || "",
        },
        actions: {
            setToken(token) {
                if (token.access_token) {
                    this.tokenRaw = token.access_token;
                    localStorage.setItem("token", token.access_token);
                    this.tokenDetail = VueJwtDecode.decode(token.access_token);
                    localStorage.setItem("tokenDetail", JSON.stringify(this.token));
                }
                if (token.refresh_token) {
                    this.refreshToken = token.refresh_token;
                    localStorage.setItem("refresh_token", token.refresh_token);
                }
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
                this.tokenRaw = "";
                this.refreshToken = "";
                this.tokenDetail = {};
                localStorage.removeItem("token");
                localStorage.removeItem("refresh_token");
                localStorage.removeItem("tokenDetail");
            }
        },
    })
;
