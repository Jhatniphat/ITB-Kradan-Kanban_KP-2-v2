<script setup>
import { addTask, uploadAttachments } from '@/lib/fetchUtils';
import { computed, onMounted, ref, watch } from 'vue';
import { useStatusStore } from '@/stores/status.js';
import { useBoardStore } from '@/stores/board';
import { useAccountStore } from '@/stores/account';
import { useToastStore } from '@/stores/toast.js';
import * as pdfjsLib from 'pdfjs-dist/webpack'; // ใช้ Webpack version ของ PDF.js
import { marked } from 'marked';
import loadingComponent from '../loadingComponent.vue';
import { generateFileData, openPreview } from '@/lib/fileUtils';
const emit = defineEmits(['closeModal']);
const statusStore = useStatusStore();
const boardStore = useBoardStore();

const statusList = ref([]);
// const canSave = ref(false);
const taskData = ref({
  title: '',
  description: '',
  assignees: '',
  status: 'No Status',
  /* 'No Status', 'To Do', 'Doing', 'Done' */
});
const Errortext = ref({
  title: '',
  description: '',
  assignees: '',
});
const descriptionTab = ref('write');

// file handle
const uploadedFiles = ref([]);
const fileInput = ref(null);
const previewFile = ref(null);
const filesToUpload = ref([]);
const showIframePreview = ref(false);
const iframePreviewURL = ref('');

// style
const isHeaderSticky = ref(false);
const isFooterSticky = ref(false);
const content = ref();
const createAnotherTask = ref(false);
const hoveredFileIndex = ref(null);
const fileLoading = ref(false);
const loading = ref([]);

watch(taskData.value, () => {
  if (taskData.value.title.trim().length > 100) Errortext.value.title = `Title can't long more than 100 character`;
  else if (taskData.value.title.trim().length === 0) Errortext.value.title = `Title can't be empty`;
  else Errortext.value.title = '';
  if (taskData.value.description.trim().length > 500) Errortext.value.description = `Description can't long more than 500 character`;
  else Errortext.value.description = '';
  if (taskData.value.assignees.trim().length > 30) Errortext.value.assignees = `Assignees can't long more than 30 character`;
  else Errortext.value.assignees = '';
});

const canSave = computed(() => {
  return (
    Errortext.value.title === '' &&
    Errortext.value.description === '' &&
    Errortext.value.assignees === '' &&
    taskData.value.title.trim().length > 0 &&
    !uploadedFiles.value.some((file) => file.errorText.length > 0)
  );
});

onMounted(async () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

watch(statusStore.status, () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

async function fetchData() {
  taskData.value.title = taskData.value.title.trim();
  taskData.value.description = taskData.value.description.trim();
  taskData.value.assignees = taskData.value.assignees.trim();
  addLoading('Saving task');
  let res;
  let resUp;
  try {
    res = await addTask(taskData.value);
    removeLoading('Saving task');
    if (filesToUpload.value.length > 0) {
      addLoading('Uploading files');
      resUp = await uploadAttachments(boardStore.currentBoardId, res.id, filesToUpload.value).then(() => {
        removeLoading('Uploading files');
      });
    }
  } catch (error) {
    console.log(error);
  } finally {
    removeLoading('Saving task');
    emit('closeModal', { ...res, createAnotherTask: createAnotherTask.value });
  }
}

function sendCloseModal() {
  emit('closeModal', null);
}

async function handleFileUpload(e) {
  let files = Array.from(e.target.files);
  let dulpicateFileError = [];
  filesToUpload.value.push(...files);

  files.forEach(async (file, index) => {
    let error = [];
    if (file.size > 20 * 1024 * 1024) {
      useToastStore().createToast(`"${file.name}" file size is too large`, 'danger');
      error.push('Each file cannot be larger than 20 MB');
    }
    if (uploadedFiles.value.length + index + 1 > 10) {
      error.push('Each task can have at most 10 files.');
    }

    if (uploadedFiles.value.some((updatedFile) => updatedFile.name === file.name)) {
      // useToastStore().createToast(`"${file.name}" file is already uploaded`, 'danger', 5000);
      dulpicateFileError.push(file.name);
    } else {
      uploadedFiles.value.push(await generateFileData(file, error));
    }
    checkErrorText();
  });

  if (dulpicateFileError.length > 0) {
    useToastStore().createToast(
      `File with the same filename cannot be added or updated to the attachments. Please delete the attachment and add again to update the file. The following files are not added:<span class="underline"> ${dulpicateFileError.join(' , ')} </span>`,
      'danger',
      10000
    );
  }
  checkErrorText();
}

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

function showErrorTooltip(index) {
  hoveredFileIndex.value = index; // กำหนด index ของไฟล์ที่ชี้เมาส์
}
function hideErrorTooltip() {
  hoveredFileIndex.value = null; // ซ่อน tooltip เมื่อเมาส์ออก
}
function removeFile(index) {
  uploadedFiles.value.splice(index, 1); // ลบไฟล์ที่คลิก
  if (uploadedFiles.value.length >= 10) {
    uploadedFiles.value[9].errorText.splice(
      uploadedFiles.value[9].errorText.findIndex((e) => e === 'Each task can have at most 10 files.'),
      1
    );
  }
  filesToUpload.value.splice(index, 1);
  checkErrorText();
}

function checkErrorText() {
  fileLoading.value = true;

  uploadedFiles.value.forEach((file, index) => {
    file.errorText = [];
    if (uploadedFiles.value.length > 10) {
      if (!file.errorText.includes('Each task can have at most 10 files.')) {
        file.errorText.push('Each task can have at most 10 files.');
      }
    }

    if (file.size > 20 * 1024 * 1024) {
      if (!file.errorText.includes('Each file cannot be larger than 20 MB')) {
        file.errorText.push('Each file cannot be larger than 20 MB');
      }
    }
  });
  fileLoading.value = false;
}

function addLoading(load) {
  loading.value.push(load);
}

function removeLoading(load) {
  loading.value = loading.value.filter((l) => l !== load);
}
</script>

<template>
  <div class="h-[50rem] w-full bg-white rounded-md flex flex-col overflow-scroll">
    <!-- ? HEADER -->
    <div class="sticky top-0 flex flex-row p-4 bg-white z-50" :class="isHeaderSticky ? 'shadow-lg' : ''">
      <div class="flex-1 text-2xl">Create Task / Issue</div>
      <div class="flex-1 flex flex-row-reverse">
        <svg @click="sendCloseModal" xmlns="http://www.w3.org/2000/svg" width="2em" height="2em" viewBox="0 0 24 24">
          <path fill="currentColor" d="M6.4 19L5 17.6l5.6-5.6L5 6.4L6.4 5l5.6 5.6L17.6 5L19 6.4L13.4 12l5.6 5.6l-1.4 1.4l-5.6-5.6z" />
        </svg>
      </div>
    </div>

    <loadingComponent :loading="loading" v-if="loading.length > 0" />

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
            <input v-model="taskData.title" type="text" placeholder="Type here" class="itbkk-title input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400" />
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.title !== ''" class="label-text-alt text-error">{{ Errortext.title }}</span>
              <!-- count input name -->
              <span v-if="taskData.title.length <= 100 && taskData.title.length > 0" class="justify-end text-gray-400 label-text-alt">{{ taskData.title.length }} / 100</span>
              <span v-if="taskData.title.length === 0 && Errortext.title !== ''" class="flex justify-end text-red-400 label-text-alt">{{ taskData.title.length }} / 100</span>
              <span v-if="taskData.title.length > 100" class="flex justify-end text-red-400 label-text-alt">{{ taskData.title.length }} / 100</span>
            </div>
          </label>
        </div>
        <div class="basis-1/3 px-2">
          <!-- ? Status -->
          <label class="form-control w-full max-w-xs">
            <div class="label">
              <span class="label-text">Status</span>
            </div>
            <select class="itbkk-status select select-bordered bg-white dark:bg-base-300 dark:text-slate-400" v-model="taskData.status">
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
                  v-model="taskData.description"
                ></textarea>
              </div>

              <div v-if="descriptionTab === 'preview'">
                <div
                  class="markdown-preview w-full prose max-w-none prose-indigo leading-6 rounded-b-md shadow-sm border border-gray-300 p-5 bg-white overflow-y-auto min-h-64"
                  v-html="marked(taskData.description === null ? '' : taskData.description)"
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
            <textarea v-model="taskData.assignees" class="itbkk-assignees textarea textarea-bordered h-24 bg-white dark:bg-base-300 dark:text-slate-400 min-h-[18.625rem]" placeholder="Bio"></textarea>
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.assignees !== ''" class="label-text-alt text-error"> {{ Errortext.assignees }}</span>
              <span v-if="taskData.assignees.length <= 30" class="flex justify-end text-gray-400 label-text-alt">{{ taskData.assignees.length }} / 30</span>
              <span v-if="taskData.assignees.length > 30" class="flex justify-end text-red-400 label-text-alt">{{ taskData.assignees.length }} / 30</span>
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
          <input type="file" class="file-input file-input-sm file-input-bordered w-full" multiple @change="handleFileUpload" ref="fileInput" />
          <div class="label">
            <span class="label-text-alt">Each task can have at most 10 files. and 20MB per file</span>
          </div>
        </label>
      </div>
      <!-- ? Uploaded Files -->
      <div class="flex flex-row flex-wrap gap-2">
        <div
          v-for="(file, index) in uploadedFiles"
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
        <button class="itbkk-button-confirm btn btn-outline btn-success basis-1/6" :disabled="!canSave" :class="!canSave ? 'disabled' : ''" @click="fetchData()">
          {{ loading.length > 0 ? '' : 'Save' }}
          <span class="loading loading-spinner text-success" v-if="loading.length > 0"></span>
        </button>
        <div class="form-control">
          <label class="label cursor-pointer">
            <input type="checkbox" checked="false" v-model="createAnotherTask" class="toggle" />
            <span class="label-text p-3">Create Another Task</span>
          </label>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.v-enter-active,
.v-leave-active {
  transition: opacity 0.5s ease;
}

.v-enter-from,
.v-leave-to {
  opacity: 0;
  z-index: -1;
}

.tooltip {
  transition:
    opacity 1s ease,
    transform 1s ease;
}

svg {
  transition: all 0.2s ease-in-out;
}

.shadow-top {
  box-shadow:
    0 -10px 15px -3px rgb(0 0 0 / 0.1),
    0 -4px 6px -4px rgb(0 0 0 / 0.1);
}

.upload-box {
  border: 2px dashed #ddd;
  padding: 10px;
  margin-bottom: 20px;
}
.file-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
.file-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 120px;
}
.thumbnail-container {
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid #ddd;
  margin-bottom: 10px;
  cursor: pointer;
}
.thumbnail {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}
.pdf-thumbnail,
.docx-thumbnail {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f4f4f4;
  border: 1px solid #ddd;
}
.preview-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  max-width: 80%;
  max-height: 80%;
  overflow: auto;
}
.full-preview {
  max-width: 100%;
  max-height: 100%;
}

.markdown-preview {
  max-height: 18.75rem;
  overflow: scroll;
  h1 {
    font-size: 1.5rem;
  }
  h2 {
    font-size: 1.25rem;
  }
  h3 {
    font-size: 1.125rem;
  }
  h4 {
    font-size: 1rem;
  }
  h5 {
    font-size: 0.875rem;
  }
  h6 {
    font-size: 0.75rem;
  }
}
</style>
