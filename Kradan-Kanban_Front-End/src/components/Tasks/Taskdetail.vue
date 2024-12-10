<script setup>
import { ref, watch, computed } from 'vue';
import { getTaskById, editTask, getTaskByIdForGuest, getAllAttachments, deleteAttachment, uploadAttachments, getAllAttachmentsForGuest } from '@/lib/fetchUtils.js';
import router from '@/router';
import { useTaskStore } from '@/stores/task';
import { useStatusStore } from '@/stores/status.js';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import { useToastStore } from '@/stores/toast.js';
import * as pdfjsLib from 'pdfjs-dist/webpack'; // ใช้ Webpack version ของ PDF.js
import LoadingComponent from '@/components/loadingComponent.vue';
import { marked } from 'marked';
import { generateFileData, openPreview } from '@/lib/fileUtils';

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
const statusList = ref([]);
const loading = ref([]);
const taskDetail = ref({});
const originalTask = ref({ title: '', description: '', assignees: '', status: '' });
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

const isHeaderSticky = ref(false);
const isFooterSticky = ref(false);
const content = ref();
const fileLoading = ref(false);
const descriptionTab = ref('write');

/* File Handle */
const uploadedFiles = ref([]);
const previewFiles = ref([]);
const filesToUpload = ref([]);
const filesToRemove = ref([]);
const hoveredFileIndex = ref(null);
const showIframePreview = ref(false);
const iframePreviewURL = ref('');

function handelScroll() {
  if (content.value.scrollTop > 0) {
    isHeaderSticky.value = true;
  } else {
    isHeaderSticky.value = false;
  }
  if (content.value.scrollTop + content.value.clientHeight < content.value.scrollHeight) {
    isFooterSticky.value = true;
  } else {
    isFooterSticky.value = false;
  }
}

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
    JSON.stringify(newVal) !== JSON.stringify(originalTask.value);
  },
  { deep: true }
);

const canSave = computed(() => {
  return (
    loading.value.length === 0 &&
    ((Errortext.value.title === '' &&
      Errortext.value.description === '' &&
      Errortext.value.assignees === '' &&
      (originalTask.value.title !== taskDetail.value.title ||
        originalTask.value.description !== taskDetail.value.description ||
        originalTask.value.assignees !== taskDetail.value.assignees ||
        originalTask.value.status !== taskDetail.value.status)) ||
      filesToUpload.value.length > 0 ||
      filesToRemove.value.length > 0) &&
    !previewFiles.value.some((file) => file.errorText.length === 0)
  );
});

watch(
  statusStore.status,
  () => {
    statusList.value = statusStore.getAllStatusWithLimit();
  },
  { deep: true }
);

async function fetchTask(id) {
  if (id === 0) {
    return 0;
  }
  error.value = null;
  addLoading('Loading task');
  statusList.value = statusStore.getAllStatusWithLimit();
  try {
    let originalTaskDetails;
    if (useBoardStore().currentBoard.visibility === 'PUBLIC' && useAccountStore().tokenRaw === '') {
      originalTaskDetails = await getTaskByIdForGuest(id);
    } else {
      originalTaskDetails = await getTaskById(id);
    }
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
    if (useBoardStore().currentBoard.visibility === 'PUBLIC' && useAccountStore().tokenRaw === '') {
      uploadedFilesDetail = await getAllAttachmentsForGuest(currentBoardId, id);
    } else {
      uploadedFilesDetail = await getAllAttachments(currentBoardId, id);
    }
    // uploadedFilesDetail = await getAllAttachments(currentBoardId, id);

    uploadedFiles.value = [...uploadedFilesDetail];

    // Process Uploaded Files
    uploadedFilesDetail.forEach(async (file) => {
      try {
        previewFiles.value.push(await generateFileData(file, [])); // Generate file data
      } catch (fileProcessingError) {
        console.error('Error processing file:', file, fileProcessingError);
      }
    });
  } catch (err) {
    error.value = err.toString();
  } finally {
    removeLoading('Loading task');
  }
}

async function saveTask() {
  addLoading('saving task');
  let res, editTaskResponse;
  let deleteAttachmentResponses = [];
  let uploadAttachmentsResponses = [];
  try {
    if (filesToUpload.value.length > 0) {
      const resUp = await uploadAttachments(currentBoardId, props.taskId, filesToUpload.value);
      uploadAttachmentsResponses.push(resUp);
    }

    if (filesToRemove.value.length > 0) {
      for (const file of filesToRemove.value) {
        const resRemove = await deleteAttachment(currentBoardId, props.taskId, file.id);
        deleteAttachmentResponses.push(resRemove);
      }
    }

    if (
      originalTask.value.title === taskDetail.value.title &&
      originalTask.value.description === taskDetail.value.description &&
      originalTask.value.assignees === taskDetail.value.assignees &&
      originalTask.value.status === taskDetail.value.status
    ) {
      editTaskResponse = 200;
    } else {
      delete taskDetail.value.id;
      delete taskDetail.value.createdOn;
      delete taskDetail.value.updatedOn;
      let editRes = await editTask(props.taskId, taskDetail.value);
      taskDetail.value = await editRes;
      taskStore.editStoreTask(await editRes.payload);
      editTaskResponse = await editRes.status;
    }
  } catch (error) {
    console.log(error);
  } finally {
    if (editTaskResponse === 200 && !deleteAttachmentResponses.some((res) => res.status !== 200) && !uploadAttachmentsResponses.some((res) => res.status !== 200)) {
      res = 200;
    } else {
      res = editTaskResponse;
    }
    removeLoading('saving task');
    router.push(`/board/${currentBoardId}`);
    emit('closeModal', res);
  }
}

function sendCloseModal() {
  emit('closeModal', null);
  router.push(`/board/${currentBoardId}`);
}

// Handle New Files
async function handleFileUpload(e) {
  let dulpicateFileError = [];
  let files = Array.from(e.target.files);
  filesToUpload.value.push(...files);
  addLoading('Uploading files');

  files.forEach(async (file, index) => {
    let error = [];
    if (file.size > 20 * 1024 * 1024) {
      useToastStore().createToast(`"${file.name}" file size is too large`, 'danger');
      error.push('Each file cannot be larger than 20 MB');
    }

    if (previewFiles.value.length + index + 1 > 10) {
      error.push('Each task can have at most 10 files.');
    }

    if (previewFiles.value.some((previewFile) => previewFile.name === file.name)) {
      dulpicateFileError.push(file.name);
      // useToastStore().createToast(`"${file.name}" file is already uploaded`, 'danger', 5000);
    } else {
      previewFiles.value.push(await generateFileData(file, error));
    }
  });

  if (dulpicateFileError.length > 0) {
    useToastStore().createToast(
      `File with the same filename cannot be added or updated to the attachments. Please delete the attachment and add again to update the file. The following files are not added:<span class="underline"> ${dulpicateFileError.join(' , ')} </span>`,
      'danger',
      10000
    );
  }
  removeLoading('Uploading files');
  checkErrorText();
}

function addLoading(load) {
  loading.value.push(load);
}

function removeLoading(load) {
  loading.value = loading.value.filter((l) => l !== load);
}

function checkErrorText() {
  previewFiles.value.forEach((file, index) => {
    file.errorText = [];

    if (previewFiles.value.length > 10) {
      if (!file.errorText.includes('Each task can have at most 10 files')) {
        file.errorText.push('Each task can have at most 10 files');
      }
    }

    if (file.size > 20 * 1024 * 1024) {
      if (!file.errorText.includes('Each file cannot be larger than 20 MB')) {
        file.errorText.push('Each file cannot be larger than 20 MB');
      }
    }
  });
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

function removeFile(index) {
  const file = previewFiles.value[index];
  // Check if file is a new file or an existing file
  const isExistingFile = uploadedFiles.value.some((uploadedFile) => uploadedFile.fileName === file.name);

  if (isExistingFile) {
    // Add to filesToRemove if it's an existing file
    filesToRemove.value.push(file);
  } else {
    // Remove from filesToUpload if it's a new file
    const updatedFiles = filesToUpload.value.filter((f) => f.name !== file.name); // Create a new filtered array
    filesToUpload.value = [...updatedFiles]; // Reassign to trigger reactivity
  }

  // Remove from preview list
  previewFiles.value.splice(index, 1);
  checkErrorText();
}

function showErrorTooltip(index) {
  hoveredFileIndex.value = index; // กำหนด index ของไฟล์ที่ชี้เมาส์
}
function hideErrorTooltip() {
  hoveredFileIndex.value = null; // ซ่อน tooltip เมื่อเมาส์ออก
}
</script>

<template>
  <!-- Title -->
  <div class="h-[50rem] w-full bg-white rounded-md flex flex-col overflow-scroll">
    <!-- ? HEADER -->
    <div class="sticky top-0 flex flex-row p-4 bg-white z-50" :class="isHeaderSticky ? 'shadow-lg' : ''">
      <div class="flex-1 text-2xl">Edit Task / Issue</div>
      <div class="flex-1 flex flex-row-reverse">
        <svg @click="sendCloseModal" xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24">
          <path fill="currentColor" d="M6.4 19L5 17.6l5.6-5.6L5 6.4L6.4 5l5.6 5.6L17.6 5L19 6.4L13.4 12l5.6 5.6l-1.4 1.4l-5.6-5.6z" />
        </svg>
      </div>
    </div>

    <loading-component :loading="loading" v-if="loading.length > 0" />

    <!-- ? BODY -->
    <div class="flex flex-col overflow-scroll p-2 grow" ref="content" @scroll="handelScroll" v-if="loading.length === 0 && showIframePreview">
      <div class="flex-1 flex flex-row-reverse">
        <svg @click="showIframePreview = false" xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24">
          <path fill="currentColor" d="M6.4 19L5 17.6l5.6-5.6L5 6.4L6.4 5l5.6 5.6L17.6 5L19 6.4L13.4 12l5.6 5.6l-1.4 1.4l-5.6-5.6z" />
        </svg>
      </div>
      <iframe :src="iframePreviewURL" class="h-full w-full" />
    </div>
    <div class="flex flex-col overflow-scroll p-2 grow" ref="content" @scroll="handelScroll" v-if="loading.length === 0 && !showIframePreview">
      <div class="w-full flex flex-row">
        <div class="basis-2/3 px-2">
          <!-- ? Title -->
          <label class="form-control w-full">
            <div class="label">
              <!-- ? Head -->
              <span class="label-text">Title</span>
            </div>
            <input
              v-model="taskDetail.title"
              type="text"
              placeholder="Type here"
              class="itbkk-title input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400"
              :disabled="!isOwnerOrNot"
            />
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.title !== ''" class="label-text-alt text-error">{{ Errortext.title }}</span>
              <!-- count input name -->
              <span v-if="taskDetail.title?.length <= 100 && taskDetail.title?.length > 0" class="justify-end text-gray-400 label-text-alt">{{ taskDetail.title?.length }} / 100</span>
              <span v-if="taskDetail.title?.length === 0 && Errortext?.title !== ''" class="flex justify-end text-red-400 label-text-alt">{{ taskDetail.title?.length }} / 100</span>
              <span v-if="taskDetail.title?.length > 100" class="flex justify-end text-red-400 label-text-alt">{{ taskDetail.title?.length }} / 100</span>
            </div>
          </label>
        </div>
        <div class="basis-1/3 px-2">
          <!-- ? Status -->
          <label class="form-control w-full max-w-xs">
            <div class="label">
              <span class="label-text">Status</span>
            </div>
            <select class="itbkk-status select select-bordered bg-white dark:bg-base-300 dark:text-slate-400" v-model="taskDetail.status" :disabled="!isOwnerOrNot">
              <option v-for="status in statusStore.getAllStatusWithLimit()" :value="status.name" :disabled="status.isLimit">
                {{ status.name }}
                <span class="text-error">
                  {{ status.isLimit ? '(max)' : '' }}
                </span>
              </option>
            </select>
          </label>
        </div>
      </div>
      <div class="flex flex-row">
        <div class="basis-2/3 p-2 pt-0">
          <label class="form-control">
            <div class="label">
              <span class="label-text">Description</span>
            </div>
            <div class="relative">
              <div class="bg-gray-50 border border-b-0 border-gray-300 top-0 left-0 right-0 block rounded-t-md">
                <button type="button" class="py-2 px-4 inline-block text-gray-400 font-semibold" :class="{ 'text-indigo-600': descriptionTab === 'write' }" @click="descriptionTab = 'write'">
                  Write
                </button>
                <button type="button" class="py-2 px-4 inline-block text-gray-400 font-semibold" :class="{ 'text-indigo-600': descriptionTab === 'preview' }" @click="descriptionTab = 'preview'">
                  Preview
                </button>
              </div>
              <div v-if="descriptionTab === 'write'" class="w-full">
                <textarea
                  id="{{ $id }}"
                  class="textarea prose rounded-t-none rounded-b-md shadow-sm border border-gray-300 p-1 bg-white overflow-y-auto w-full min-h-64"
                  name="content"
                  v-model="taskDetail.description"
                  :disabled="!isOwnerOrNot"
                ></textarea>
              </div>

              <div v-if="descriptionTab === 'preview'">
                <div
                  class="markdown-preview w-full prose max-w-none prose-indigo leading-6 rounded-b-md shadow-sm border border-gray-300 p-5 bg-white overflow-y-auto min-h-64"
                  v-html="marked(taskDetail.description === null ? '' : statusDetail.description)"
                ></div>
              </div>
            </div>
          </label>
        </div>
        <div class="basis-1/3 px-2">
          <label class="form-control">
            <div class="label">
              <!-- ? Head -->
              <span class="label-text">Assignees</span>
            </div>
            <textarea
              v-model="taskDetail.assignees"
              class="itbkk-assignees textarea textarea-bordered h-24 bg-white dark:bg-base-300 dark:text-slate-400 min-h-[18.625rem]"
              placeholder="No Assignees"
              :disabled="!isOwnerOrNot"
            ></textarea>
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.assignees !== ''" class="label-text-alt text-error"> {{ Errortext.assignees }}</span>
              <span v-if="taskDetail.assignees?.length <= 30" class="flex justify-end text-gray-400 label-text-alt">{{ taskDetail.assignees?.length }} / 30</span>
              <span v-if="taskDetail.assignees?.length > 30" class="flex justify-end text-red-400 label-text-alt">{{ taskDetail.assignees?.length }} / 30</span>
            </div>
          </label>
        </div>
      </div>
      <!-- ? Upload FILE -->
      <div class="px-2">
        <label class="form-control w-full">
          <div class="label pt-0">
            <span class="label-text pt-0">File attachment</span>
          </div>
          <input type="file" class="file-input file-input-sm file-input-bordered w-full" multiple @change="handleFileUpload" ref="fileInput" :disabled="!isOwnerOrNot" />
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
          :class="file.errorText.length !== 0 ? 'border-red-500' : 'border-gray-300'"
          @mouseover="showErrorTooltip(index)"
          @mouseleave="hideErrorTooltip"
        >
          <!-- Preview Button -->
          <div class="absolute top-2 left-2 z-50">
            <svg
              @click="file.type !== 'text/plain' ? openPreview(file) : (showIframePreview = true), (iframePreviewURL = file.previewUrl)"
              xmlns="http://www.w3.org/2000/svg"
              width="1.5em"
              height="1.5em"
              viewBox="0 0 24 24"
              class="cursor-pointer text-gray-400 hover:text-blue-500 transition-colors"
            >
              <path
                fill="currentColor"
                d="M6 23H3q-.825 0-1.412-.587T1 21v-3h2v3h3zm12 0v-2h3v-3h2v3q0 .825-.587 1.413T21 23zm-6-4.5q-3 0-5.437-1.775T3 12q1.125-2.95 3.563-4.725T12 5.5t5.438 1.775T21 12q-1.125 2.95-3.562 4.725T12 18.5m0-2q2.2 0 4.025-1.2t2.8-3.3q-.975-2.1-2.8-3.3T12 7.5T7.975 8.7t-2.8 3.3q.975 2.1 2.8 3.3T12 16.5m0-1q1.45 0 2.475-1.025T15.5 12t-1.025-2.475T12 8.5T9.525 9.525T8.5 12t1.025 2.475T12 15.5m0-2q-.625 0-1.063-.437T10.5 12t.438-1.062T12 10.5t1.063.438T13.5 12t-.437 1.063T12 13.5M1 6V3q0-.825.588-1.412T3 1h3v2H3v3zm20 0V3h-3V1h3q.825 0 1.413.588T23 3v3zm-9 6"
              />
            </svg>
          </div>

          <!-- Close Button -->
          <div class="absolute top-2 right-2 z-50" v-if="isOwnerOrNot">
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
            v-if="hoveredFileIndex === index && file.errorText.length !== 0"
            class="absolute top-2 left-1/2 transform -translate-x-1/2 transition-all bg-red-500 text-white text-xs px-2 py-1 rounded-md shadow-md z-30"
          >
            {{ file.errorText.join(',') }}
          </div>
        </div>
      </div>
    </div>
    <!-- ? FOOTER -->
    <div class="sticky bottom-0 w-full z-50 gap-4 p-2" :class="isFooterSticky ? 'shadow-top' : ''">
      <div class="float-right flex flex-row-reverse gap-3">
        <button class="itbkk-button-cancel btn btn-outline btn-error basis-1/6" @click="sendCloseModal()">Cancel</button>
        <button class="itbkk-button-confirm btn btn-outline btn-success basis-1/6" :disabled="!canSave" :class="!canSave ? 'disabled' : ''" @click="saveTask()">
          {{ loading.length > 0 ? '' : 'Save' }}
          <span class="loading loading-spinner text-success" v-if="loading.length > 0"></span>
        </button>
        <div class="flex flex-col">
          <div class="bold">updated On</div>
          <div>{{ taskDetail.updatedOn }}</div>
        </div>
        <div class="flex flex-col">
          <div class="bold">created On</div>
          <div>{{ taskDetail.createdOn }}</div>
        </div>
        <div class="flex flex-col">
          <div class="bold">Time Zone</div>
          <div>{{ Intl.DateTimeFormat().resolvedOptions().timeZone }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}

/* .fade-enter-from,
.fade-leave-to {
  opacity: 0;
  z-index: -1;
} */

.fade-leave-to {
  opacity: 0;
  transform: translateY(500px);
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(-500px);
}
</style>
