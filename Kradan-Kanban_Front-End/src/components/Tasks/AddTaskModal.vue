<script setup>
import { addTask } from '@/lib/fetchUtils';
import { onMounted, ref, watch } from 'vue';
import { useStatusStore } from '@/stores/status.js';
import { useToastStore } from '@/stores/toast.js';
import * as pdfjsLib from 'pdfjs-dist/webpack'; // ใช้ Webpack version ของ PDF.js
import { marked } from 'marked';
const emit = defineEmits(['closeModal']);
const statusStore = useStatusStore();

const statusList = ref([]);
const canSave = ref(false);
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

// style
const isHeaderSticky = ref(false);
const isFooterSticky = ref(false);
const content = ref();
const createAnotherTask = ref(false);

watch(taskData.value, () => {
  if (taskData.value.title.trim().length > 100) Errortext.value.title = `Title can't long more than 100 character`;
  else if (taskData.value.title.trim().length === 0) Errortext.value.title = `Title can't be empty`;
  else Errortext.value.title = '';
  if (taskData.value.description.trim().length > 500) Errortext.value.description = `Description can't long more than 500 character`;
  else Errortext.value.description = '';
  if (taskData.value.assignees.trim().length > 30) Errortext.value.assignees = `Assignees can't long more than 30 character`;
  else Errortext.value.assignees = '';
  // ? disabled or enabled save btn
  canSave.value = Errortext.value.title === '' && Errortext.value.description === '' && Errortext.value.assignees === '';
});

onMounted(async () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

watch(statusStore.status, () => {
  statusList.value = statusStore.getAllStatusWithLimit();
});

const loading = ref(false);

async function fetchData() {
  taskData.value.title = taskData.value.title.trim();
  taskData.value.description = taskData.value.description.trim();
  taskData.value.assignees = taskData.value.assignees.trim();
  loading.value = true;
  let res;
  try {
    res = await addTask(taskData.value);
  } catch (error) {
    console.log(error);
  } finally {
    loading.value = false;
    emit('closeModal', {...res , createAnotherTask: createAnotherTask.value});
  }
}

function sendCloseModal() {
  emit('closeModal', null);
}

function handleFileUpload(e) {
  let files = Array.from(e.target.files);
  // console.log(files);
  files.forEach(async (file) => {
    // files.forEach((file) => {
    //   const blobURL = URL.createObjectURL(file); // สร้าง Blob URL
    //   console.log('Blob URL:', blobURL);
    // });
    let blobURL;
    // if (file.type.startsWith('image/')){
    //   blobURL = URL.createObjectURL(file);
    // } else if (file.type === 'application/pdf'){
    //   blobURL = await generatePDFThumbnail(file);
    // } else {
    //   blobURL = null;
    // }
    console.log('Blob URL:', blobURL);
    const reader = new FileReader();
    reader.onload = async (e) => {
      console.log('file type ...', file.type);
      uploadedFiles.value.push({
        name: file.name,
        size: file.size,
        // preview: e.target.result, // เก็บ URL ของไฟล์สำหรับการแสดง Preview
        type: file.type,
        // preview: file.type.startsWith('image/')
        //   ? e.target.result // ใช้ Data URL สำหรับรูปภาพ
        //   : file.type === 'application/pdf'
        //     ? URL.createObjectURL(file) // ใช้ URL สำหรับ PDF
        //     : null,
        previewUrl: file.type.startsWith('application/msword') ? `https://docs.google.com/viewer?url=${encodeURIComponent(URL.createObjectURL(file))}&embedded=true` : URL.createObjectURL(file),
        thumbnail: file.type.startsWith('image/') ? URL.createObjectURL(file) : file.type === 'application/pdf' ? await generatePDFThumbnail(file) : null,
        // preview : blobURL,
        errorText: file.size > 20 * 1024 * 1024 ? 'File size is too large' : '',
      });
    };

    if (file.size > 20 * 1024 * 1024) {
      useToastStore().createToast(`"${file.name}" file size is too large`, error);
    } else {
      reader.readAsDataURL(file); // อ่านไฟล์เมื่อขนาดผ่านข้อกำหนด
    }
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

// เปิด Preview Modal
const openPreview = (file) => {
  // previewFile.value = file;
  window.open(file.previewUrl, '_blank');
};

// ปิด Preview Modal
const closePreview = () => {
  previewFile.value = null;
};

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
  // console.log(isHeaderSticky.value, isFooterSticky.value);
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

    <!-- ? BODY -->
    <div v-if="!previewFile" class="flex flex-col overflow-scroll p-2 grow" ref="content" @scroll="handelScroll">
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
                  v-html="marked(taskData.description)"
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
            <span class="label-text-alt">can upload only 10 file per task and 20MB per file</span>
          </div>
        </label>
      </div>
      <div class="flex flex-wrap flex-row">
        <div v-for="(file, index) in uploadedFiles" :key="index">
          <div @click="openPreview(file)" class="thumbnail-container">
            <img :src="file.thumbnail" class="thumbnail" alt="Image Preview" />
          </div>
          <p>{{ file.name }}</p>
        </div>
      </div>
    </div>

    <!-- <div v-if="previewFile" class="preview-modal">
      <div class="modal-content">
        <button @click="closePreview">Close</button>
        <div v-if="previewFile.type.startsWith('image/')">
          <img :src="previewFile.preview" class="full-preview" />
        </div>
        <div v-else-if="previewFile.type === 'application/pdf'">
          <iframe :src="previewFile.preview" class="full-preview" />
        </div>
        <div v-else-if="previewFile.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'">
          <p>DOCX Preview Not Supported Yet</p>
        </div>
      </div>
    </div> -->

    <!-- ? FOOTER -->
    <div class="sticky bottom-0 w-full z-50 gap-4 p-2" :class="isFooterSticky ? 'shadow-top' : ''">
      <div class="float-right flex flex-row-reverse gap-3">
        <button class="itbkk-button-cancel btn btn-outline btn-error basis-1/6" @click="sendCloseModal()">Cancel</button>
        <button class="itbkk-button-confirm btn btn-outline btn-success basis-1/6" :disabled="!canSave" :class="!canSave ? 'disabled' : ''" @click="fetchData()">
          {{ loading ? '' : 'Save' }}
          <span class="loading loading-spinner text-success" v-if="loading"></span>
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

  <!-- ? Preview Modal
  <Modal class="z-[100]">
    <div v-if="previewFile" class="preview-modal">
      <div class="modal-content">
        <button @click="closePreview">Close</button>
        <div v-if="previewFile.type.startsWith('image/')">
          <img :src="previewFile.preview" class="full-preview" />
        </div>
        <div v-else-if="previewFile.type === 'application/pdf'">
          <iframe :src="previewFile.preview" class="full-preview" />
        </div>
        <div v-else-if="previewFile.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'">
          <p>DOCX Preview Not Supported Yet</p>
        </div>
      </div>
    </div>
  </Modal> -->
</template>

<style scoped>
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
