<script setup>
import { useToastStore } from '@/stores/toast';
const toastStore = useToastStore();

setInterval(() => {
  toastStore.reduceTime(10);
}, 10);

// const copyToClipboard = async () => {
//   try {
//     await navigator.clipboard.writeText(linkToCopy.value); // คัดลอกไปที่คลิปบอร์ด
//     copySuccess.value = 'Link copied to clipboard!';
//   } catch (err) {
//     console.error('Failed to copy: ', err);
//     copySuccess.value = 'Failed to copy link.';
//   }

//   // เคลียร์ข้อความแจ้งเตือนหลัง 3 วินาที
//   setTimeout(() => {
//     copySuccess.value = '';
//   }, 3000);
// };

function copyToClipboard(text) {
  if (!text) {
    return;
  }
  navigator.clipboard.writeText(text).then(() => {
    toastStore.createToast('Copied to clipboard', 'success');
  });
}
</script>

<template>
  <Teleport to="#toast">
    <TransitionGroup name="list" tag="ul">
      <li v-for="(toast, index) in toastStore.toasts" :key="index" @click="copyToClipboard(toast.copy)">
        <div id="toast-success" class="flex items-center w-full max-w-xs p-4 mb-4 text-gray-500 bg-white rounded-lg shadow" role="alert">
          <!-- Toast Icon -->
          <div v-if="toast.status === 'success'" class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-green-500 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-200">
            <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5Zm3.707 8.207-4 4a1 1 0 0 1-1.414 0l-2-2a1 1 0 0 1 1.414-1.414L9 10.586l3.293-3.293a1 1 0 0 1 1.414 1.414Z" />
            </svg>
            <span class="sr-only">Success icon</span>
          </div>
          <div v-if="toast.status === 'danger'" class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-red-500 bg-red-100 rounded-lg">
            <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5Zm3.707 11.793a1 1 0 1 1-1.414 1.414L10 11.414l-2.293 2.293a1 1 0 0 1-1.414-1.414L8.586 10 6.293 7.707a1 1 0 0 1 1.414-1.414L10 8.586l2.293-2.293a1 1 0 0 1 1.414 1.414L11.414 10l2.293 2.293Z"
              />
            </svg>
            <span class="sr-only">Error icon</span>
          </div>
          <div v-if="toast.status === 'warning'" class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-orange-500 bg-orange-100 rounded-lg">
            <svg class="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 .5a9.5 9.5 0 1 0 9.5 9.5A9.51 9.51 0 0 0 10 .5ZM10 15a1 1 0 1 1 0-2 1 1 0 0 1 0 2Zm1-4a1 1 0 0 1-2 0V6a1 1 0 0 1 2 0v5Z" />
            </svg>
            <span class="sr-only">Warning icon</span>
          </div>
          <div v-if="toast.status === 'waiting'" class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-blue-400 bg-blue-100 rounded-lg">
            <svg xmlns="http://www.w3.org/2000/svg" class="w-5 h-5" viewBox="0 0 24 24">
              <path
                fill="currentColor"
                d="m14.935 16.223l.688-.713l-3.123-3.123V7.922h-1v4.866zM11.5 6h1V4h-1zm6.5 6.5h2v-1h-2zM11.5 20h1v-2h-1zM4 12.5h2v-1H4zm8.003 8.5q-1.867 0-3.51-.708q-1.643-.709-2.859-1.924t-1.925-2.856T3 12.003t.709-3.51Q4.417 6.85 5.63 5.634t2.857-1.925T11.997 3t3.51.709q1.643.708 2.859 1.922t1.925 2.857t.709 3.509t-.708 3.51t-1.924 2.859t-2.856 1.925t-3.509.709"
              />
            </svg>
          </div>

          <div class="ms-3 text-sm font-normal itbkk-message" v-html="toast.message"></div>
          <button
            type="button"
            class="ms-auto -mx-1.5 -my-1.5 bg-white text-gray-400 hover:text-gray-900 rounded-lg focus:ring-2 focus:ring-gray-300 p-1.5 hover:bg-gray-100 inline-flex items-center justify-center h-8 w-8 dark:text-gray-500 dark:hover:text-white dark:bg-gray-800 dark:hover:bg-gray-700"
            data-dismiss-target="#toast-success"
            aria-label="Close"
          >
            <span class="sr-only">Close</span>
            <svg class="w-3 h-3" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 14 14">
              <path stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6" />
            </svg>
          </button>
        </div>
      </li>
    </TransitionGroup>
  </Teleport>
</template>

<style scoped>
.list-move, /* apply transition to moving elements */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

/* ensure leaving items are taken out of layout flow so that moving
   animations can be calculated correctly. */
.list-leave-active {
  position: absolute;
}
</style>
