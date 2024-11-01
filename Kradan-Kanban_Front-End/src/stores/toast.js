import {defineStore} from "pinia";

export const useToastStore = defineStore('toast', {
    state: () => ({
        toasts : [],
        currentId : 0
    }),
    actions: {
        createToast( message ,  status="success" ,timeout=3000 ){
            this.toasts.push( {
                message : message,
                status : status,
                timeout : timeout
            } )
        },
        reduceTime(time = 10){
            this.toasts.forEach((toast , index) => {
                toast.timeout -= time
                if (toast.timeout <= 0) {
                    this.toasts.splice(index, 1)
                }
            })
        },
        getToasts(){
            return this.toasts.value
        }
        
    }
})