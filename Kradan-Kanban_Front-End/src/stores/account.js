import {defineStore} from "pinia";

export const useAccountStore = defineStore('account', {
    state: () => ({
        tokenDetail : {},
    }),
    actions: {
        // getTokenDetail(){
        //     return this.tokenDetail;
        // },
        //
        // setTokenDetail(tokenDetail) {
        //     this.tokenDetail = tokenDetail;
        // }

    }
})