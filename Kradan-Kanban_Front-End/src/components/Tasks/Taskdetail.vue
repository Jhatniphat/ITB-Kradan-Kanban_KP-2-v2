<script setup>
import { ref, watch, computed } from 'vue';
import { getTaskById, editTask, getTaskByIdForGuest, getAllAttachments } from '@/lib/fetchUtils.js';
import router from '@/router';
import { useTaskStore } from '@/stores/task';
import { useStatusStore } from '@/stores/status.js';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import { useToastStore } from '@/stores/toast.js';
import * as pdfjsLib from 'pdfjs-dist/webpack'; // ใช้ Webpack version ของ PDF.js
import LoadingComponent from '@/components/loadingComponent.vue';

const currentBoardId = useBoardStore().currentBoardId;
const taskStore = useTaskStore();
const statusStore = useStatusStore();
const emit = defineEmits(['closeModal', 'editMode']);
const props = defineProps({
  taskId: {
    type: Number,
    require: true,
  },
  isOwnerOrNot: {
    type: Boolean,
    required: true,
  },
});

// const editMode = ref(false);
const statusList = ref([]);
const canSave = ref(false);
const loading = ref(false);
const taskDetail = ref({});
const originalTask = ref(null);
const error = ref(null);
const Errortext = ref({
  title: '',
  description: '',
  assignees: '',
});

const editTaskTitleLength = computed(() => {
  return taskDetail.value?.title?.trim()?.length;
});
const editTaskDescriptionLength = computed(() => {
  return taskDetail.value?.description?.trim()?.length;
});
const editTaskAssigneesLength = computed(() => {
  return taskDetail.value?.assignees?.trim()?.length;
});

watch(() => props.taskId, fetchTask, { immediate: true });

watch(
  taskDetail,
  (newVal) => {
    if (editTaskTitleLength.value > 100) Errortext.value.title = "Task name can't long more 100 character";
    else if (editTaskTitleLength.value === 0) Errortext.value.title = "Task Name can't be empty";
    else Errortext.value.title = '';
    if (editTaskDescriptionLength.value > 500) Errortext.value.description = "Description can't long more 500 character";
    else Errortext.value.description = '';
    if (editTaskAssigneesLength.value > 30) Errortext.value.assignees = "Assignees can't long more than 30 character";
    else Errortext.value.assignees = '';
    canSave.value =
      Errortext.value.title === '' &&
      Errortext.value.description === '' &&
      Errortext.value.assignees === '' &&
      (originalTask.value.title !== taskDetail.value.title ||
        originalTask.value.description !== taskDetail.value.description ||
        originalTask.value.assignees !== taskDetail.value.assignees ||
        originalTask.value.status !== taskDetail.value.status);
    JSON.stringify(newVal) !== JSON.stringify(originalTask.value);
  },
  { deep: true }
);

async function fetchTask(id) {
  if (id === 0) {
    loading.value = true;
    // emit("closeModal", 404);
    // router.push("/task");
    return 0;
  }
  error.value = taskDetail.value = statusList.value = null;
  loading.value = true;
  statusList.value = statusStore.getAllStatusWithLimit();
  try {
    let originalTaskDetails;
    if (useBoardStore().currentBoard.visibility === 'PUBLIC' && useAccountStore().tokenRaw === '') {
      originalTaskDetails = await getTaskByIdForGuest(id);
    } else {
      originalTaskDetails = await getTaskById(id);
    }

    console.table(await originalTaskDetails);
    if (originalTaskDetails === 404 || originalTaskDetails === 400 || originalTaskDetails === 500) {
      emit('closeModal', 404);

      router.push(`/board/${currentBoardId}`);
    }
    originalTask.value = { ...originalTaskDetails };
    taskDetail.value = { ...originalTaskDetails };

    if (taskDetail.value === 404) {
      router.push(`/board/${currentBoardId}`);
    }

    //Fetch Uploaded Files
    let uploadedFilesDetail;
    uploadedFilesDetail = await getAllAttachments(currentBoardId, id);

    uploadedFiles.value = { ...uploadedFilesDetail };
    console.table(uploadedFiles);

    // Process Uploaded Files
    uploadedFilesDetail.forEach(async (file) => {
      try {
        // Convert base64 to Blob
        const byteCharacters = atob(file.fileData);
        const byteNumbers = Array.from(byteCharacters, (char) => char.charCodeAt(0));
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: file.fileType });

        // Create preview file details
        const previewUrl = URL.createObjectURL(blob);

        previewFiles.value.push({
          name: file.fileName,
          size: blob.size,
          type: file.fileType,
          previewUrl: file.fileType.startsWith('application/msword') ? `https://docs.google.com/viewer?url=${encodeURIComponent(previewUrl)}&embedded=true` : previewUrl,
          thumbnail: file.fileType.startsWith('image/') ? previewUrl : file.fileType === 'application/pdf' ? await generatePDFThumbnail(blob) : null,
        });
      } catch (fileProcessingError) {
        console.error('Error processing file:', file, fileProcessingError);
      }
    });
  } catch (err) {
    error.value = err.toString();
  } finally {
    loading.value = false;
  }
}

async function saveTask() {
  loading.value = true;
  let res;
  try {
    delete taskDetail.value.id;
    delete taskDetail.value.createdOn;
    delete taskDetail.value.updatedOn;
    res = await editTask(props.taskId, taskDetail.value);
    taskDetail.value = res;
    taskStore.editStoreTask(res);
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
    router.push(`/board/${currentBoardId}`);
    emit('closeModal', res);
  }
}

function sendCloseModal() {
  emit('closeModal', null);
  router.push(`/board/${currentBoardId}`);
}

const uploadedFiles = ref([]);
const previewFiles = ref([]);
const filesToUpload = ref([])
const hoveredFileIndex = ref(null);

//Handle New Files
async function handleFileUpload(e) {
  let files = Array.from(e.target.files);
  // uploadedFiles.value = [];
  console.log(files);
  filesToUpload.value.push(...files);
  console.log('Raw files:', filesToUpload.value);

  files.forEach(async (file, index) => {
    let error = [];
    if (file.size > 20 * 1024 * 1024) {
      useToastStore().createToast(`"${file.name}" file size is too large`, error);
      error.push('this file is too large');
    }
    if (index >= 10) {
      error.push('can upload only 10 file per task');
    }

    const reader = new FileReader();

    reader.onload = async (e) => {
      // error handling
      previewFiles.value.push({
        name: file.name,
        size: file.size,
        type: file.type,
        previewUrl: file.type.startsWith('application/msword') ? `https://docs.google.com/viewer?url=${encodeURIComponent(URL.createObjectURL(file))}&embedded=true` : URL.createObjectURL(file),
        thumbnail: file.type.startsWith('image/') ? URL.createObjectURL(file) : file.type === 'application/pdf' ? await generatePDFThumbnail(file) : null,
        errorText: error.join(', '),
      });
    };

    reader.readAsDataURL(file);
  });

  console.log(uploadedFiles.value);
}

const generatePDFThumbnail = async (file) => {
  const pdf = await pdfjsLib.getDocument(URL.createObjectURL(file)).promise; // โหลด PDF
  const page = await pdf.getPage(1); // ดึงหน้าแรกของ PDF
  const viewport = page.getViewport({ scale: 1 }); // ตั้งค่า scale
  const canvas = document.createElement('canvas'); // สร้าง canvas
  const context = canvas.getContext('2d');

  // ตั้งขนาด canvas
  canvas.width = viewport.width;
  canvas.height = viewport.height;

  // เรนเดอร์ PDF บน canvas
  await page.render({
    canvasContext: context,
    viewport,
  }).promise;

  // แปลง canvas เป็น Blob URL
  return new Promise((resolve) => {
    canvas.toBlob((blob) => {
      const blobURL = URL.createObjectURL(blob);
      resolve(blobURL); // ส่ง Blob URL กลับ
    }, 'image/png'); // ใช้ PNG เป็นฟอร์แมต
  });
};

function showErrorTooltip(index) {
  hoveredFileIndex.value = index; // กำหนด index ของไฟล์ที่ชี้เมาส์
}
function hideErrorTooltip() {
  hoveredFileIndex.value = null; // ซ่อน tooltip เมื่อเมาส์ออก
}
function removeFile(index) {
  previewFiles.value.splice(index, 1); // ลบไฟล์ที่คลิก
  filesToUpload.value.splice(index, 1);
  console.log("Files Array After Remove :",filesToUpload.value);
}
</script>

<template>
  <div class="flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full min-h-96" v-if="loading === true">
    <loading-component class="absolute top-1/2" />
  </div>
  <!-- Title -->
  <div class="flex flex-col p-5 text-black bg-slate-50 dark:bg-base-100 rounded-lg w-full itbkk-modal-task" v-if="loading === false">
    <div>
      <label class="form-control w-full">
        <div class="label">
          <h1 class="m-2 text-3xl font-bold" v-if="loading === true">Loading Data For TaskId = {{ props.taskId }}</h1>
          <div v-if="loading === false && error === null">
            <h1 class="m-2 mt-0 text-2xl font-bold text-wrap break-all dark:text-slate-400">Edit task</h1>
            <hr />
            <span class="label-text">Title</span>
          </div>
          <hr />
        </div>
        <input v-model="taskDetail.title" type="text" placeholder="Type here" class="itbkk-title input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400" />
        <div class="label">
          <!-- ? Error Text -->
          <span v-if="Errortext.title !== ''" class="label-text-alt text-error">{{ Errortext.title }}</span>
          <span v-if="editTaskTitleLength <= 100 && editTaskTitleLength > 0" class="justify-end text-gray-400 label-text-alt">{{ editTaskTitleLength }} / 100</span>
          <span v-if="editTaskTitleLength === 0 && Errortext.title !== ''" class="flex justify-end text-red-400 label-text-alt">{{ editTaskTitleLength }} / 100</span>
          <span v-if="editTaskTitleLength > 100" class="flex justify-end text-red-400 label-text-alt">{{ editTaskTitleLength }} / 100</span>
        </div>
      </label>
    </div>

    <!--    <div v-if="!editMode">-->
    <!--      <label class="form-control w-full">-->
    <!--        <div class="label">-->
    <!--          <h1 class="m-2 text-3xl font-bold" v-if="loading === true">-->
    <!--            Loading Data For TaskId = {{ props.taskId }}-->
    <!--          </h1>-->
    <!--          <h1-->
    <!--              class="itbkk-title m-2 text-2xl font-bold text-wrap break-all dark:text-slate-400"-->
    <!--              v-if="loading === false && error === null"-->
    <!--          >-->
    <!--            {{ taskDetail.title }}-->
    <!--          </h1>-->
    <!--        </div>-->
    <!--        <hr/>-->
    <!--      </label>-->
    <!--    </div>-->

    <!-- * description -->
    <div class="flex mb-5 mx-auto flex-col w-full" v-if="loading === false && error === null">
      <div class="flex flex-row gap-3">
        <label class="form-control basis-3/4">
          <div class="label">
            <!-- ? Head -->
            <span class="label-text">Description</span>
          </div>
          <textarea
            v-model="taskDetail.description"
            class="itbkk-description textarea textarea-bordered h-72 bg-white dark:bg-base-300 dark:text-slate-400 resize-none"
            placeholder="No Description Provided"
            :class="taskDetail.description === '' || taskDetail.description === null ? 'italic text-gray-600' : ''"
            >{{ taskDetail.description == '' || taskDetail.description === null ? 'No Description Provided' : taskDetail.description }}</textarea
          >
          <div class="label">
            <!-- ? Error Text -->
            <span v-if="Errortext.description !== ''" class="label-text-alt text-error"> {{ Errortext.description }}</span>
            <span v-if="editTaskDescriptionLength <= 500" class="flex justify-end text-gray-400 label-text-alt">{{ editTaskDescriptionLength }} / 500</span>
            <span v-if="editTaskDescriptionLength > 500" class="flex justify-end text-red-400 label-text-alt">{{ editTaskDescriptionLength }} / 500</span>
          </div>
        </label>

        <!-- * assignee -->
        <div class="basis-1/4">
          <label class="form-control">
            <div class="label">
              <!-- ? Head -->
              <span class="label-text">Assignees</span>
            </div>
            <textarea
              v-model="taskDetail.assignees"
              class="itbkk-assignees textarea textarea-bordered h-24 bg-white dark:bg-base-300 dark:text-slate-400 resize-none"
              placeholder="Unassigned"
              :class="taskDetail.assignees === '' || taskDetail.assignees === null ? 'italic text-gray-600' : ''"
              >{{ taskDetail.assignees == '' || taskDetail.assignees === null ? 'Unassigned' : taskDetail.assignees }}</textarea
            >
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.assignees !== ''" class="label-text-alt text-error"> {{ Errortext.assignees }}</span>
              <span v-if="editTaskAssigneesLength <= 30" class="flex justify-end text-gray-400 label-text-alt">{{ editTaskAssigneesLength }} / 30</span>
              <span v-if="editTaskAssigneesLength > 30" class="flex justify-end text-red-400 label-text-alt">{{ editTaskAssigneesLength }} / 30</span>
            </div>
          </label>

          <!-- * status -->
          <label class="form-control w-full max-w-xs">
            <div class="label">
              <span class="label-text">Status</span>
            </div>
            <select class="itbkk-status select select-bordered bg-white dark:bg-base-300 dark:text-slate-400" v-model="taskDetail.status">
              <option v-for="status in statusList" :value="status.name" :disabled="originalTask.status !== status.name && status.isLimit">
                {{ status.name }}
                <span class="text-error">
                  {{ status.isLimit ? '(max)' : '' }}
                </span>
              </option>
            </select>
          </label>

          <div class="mt-5 text-xs text-black dark:text-slate-400">
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">TimeZone</h1>
              <h1 class="itbkk-timezone font-semibold">
                {{ Intl.DateTimeFormat().resolvedOptions().timeZone }}
              </h1>
            </div>
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">Created On</h1>
              <h1 class="itbkk-created-on font-semibold">
                {{ taskDetail.createdOn }}
              </h1>
            </div>
            <div class="flex flex-row justify-between">
              <h1 class="font-bold">Updated On</h1>
              <h1 class="itbkk-updated-on font-semibold">
                {{ taskDetail.updatedOn }}
              </h1>
            </div>
          </div>
        </div>
      </div>

      <div class="flex my-5 mx-auto" v-if="error !== null">
        <div>
          <h1>{{ error }}</h1>
        </div>
      </div>
      <!-- ? Upload FILE -->
      <div class="px-2">
        <label class="form-control w-full">
          <div class="label pt-0">
            <span class="label-text pt-0">File attachment</span>
          </div>
          <input type="file" class="file-input file-input-sm file-input-bordered w-full" multiple @change="handleFileUpload" ref="fileInput" />
          <div class="label">
            <span class="label-text-alt">can upload only 10 file per task and 20MB per file</span>
          </div>
        </label>
      </div>
      <!-- ? Uploaded Files -->
      <div class="flex flex-row flex-wrap gap-2">
        <div
          v-for="(file, index) in previewFiles"
          :key="index"
          class="relative w-48 h-32 bg-white border rounded-md flex flex-col items-center justify-between shadow-sm overflow-hidden"
          :class="file.errorText ? 'border-red-500' : 'border-gray-300'"
          @mouseover="showErrorTooltip(index)"
          @mouseleave="hideErrorTooltip"
        >
          <!-- Close Button -->
          <div class="absolute top-2 right-2 z-50">
            <svg
              @click="removeFile(index)"
              xmlns="http://www.w3.org/2000/svg"
              width="1.5em"
              height="1.5em"
              class="cursor-pointer text-gray-400 hover:text-red-500 transition-colors"
              viewBox="0 0 24 24"
            >
              <path fill="currentColor" d="M6.4 19L5 17.6l5.6-5.6L5 6.4L6.4 5l5.6 5.6L17.6 5L19 6.4L13.4 12l5.6 5.6l-1.4 1.4l-5.6-5.6z" />
            </svg>
          </div>

          <!-- Thumbnail -->
          <div class="absolute z-10 w-full h-full flex-shrink-0">
            <img v-if="file.thumbnail" :src="file.thumbnail" alt="Thumbnail" class="absolute inset-0 w-full h-full object-contain" />
            <span v-else class="w-full h-full flex items-center justify-center text-gray-500 text-sm bg-gray-100">
              {{ file.icon }}
            </span>
          </div>

          <!-- File Name -->
          <div class="absolute z-20 bg-white text-sm text-gray-800 w-full h-10 px-2 py-1 bottom-0">
            {{ file.name }}
          </div>

          <!-- v-if="file.showError" -->
          <!-- Error Tooltip -->
          <!-- todo : transition don't work -->
          <div
            v-if="hoveredFileIndex === index && file.errorText"
            class="absolute top-2 left-1/2 transform -translate-x-1/2 transition-all bg-red-500 text-white text-xs px-2 py-1 rounded-md shadow-md z-30"
          >
            {{ file.errorText }}
          </div>
        </div>
      </div>
      <hr />
      <div class="flex flex-row-reverse gap-4 mt-5">
        <!-- Cancel button -->
        <button class="itbkk-button-cancel btn btn-outline btn-error basis-1/6" @click="sendCloseModal()">Cancel</button>

        <!--        <button-->
        <!--            v-if="!editMode"-->
        <!--            class="btn btn-outline btn-primary basis-1/6"-->
        <!--            @click="editMode = true"-->
        <!--            :disabled="!isOwner"-->
        <!--        >-->
        <!--          Edit-->
        <!--        </button>-->

        <button class="itbkk-button-confirm btn btn-outline btn-success basis-1/6" :disabled="!canSave" @click="saveTask">
          {{ loading ? '' : 'Save' }}
          <span class="loading loading-spinner text-success" v-if="loading"></span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
