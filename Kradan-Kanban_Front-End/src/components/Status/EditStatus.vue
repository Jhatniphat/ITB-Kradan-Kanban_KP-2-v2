<script setup>
import { ref, watch, computed } from 'vue';
import { getStatusById, editStatus, getAllTasks, getStatusByIdForGuest } from '@/lib/fetchUtils';
import router from '@/router';
import { useStatusStore } from '@/stores/status';
import { useBoardStore } from '@/stores/board.js';
import { useAccountStore } from '@/stores/account.js';
import LoadingComponent from '@/components/loadingComponent.vue';
import { marked } from 'marked';
const currentBoardId = useBoardStore().currentBoardId;
const statusStore = useStatusStore();
const canSave = ref(false);
const loading = ref([]);
const statusDetail = ref({});
const Errortext = ref({
  name: '',
  description: '',
});

const originalsDetail = ref(null);
const error = ref(null);
const emit = defineEmits(['closeModal']);
const props = defineProps({
  statusId: {
    type: Number,
    require: true,
  },
  statusColor: {
    type: String,
    require: true,
  },
});

const descriptionTab = ref('preview');
const isHeaderSticky = ref(false);
const isFooterSticky = ref(false);
const content = ref();

const colorPicking = ref(`#${props.statusColor}`);
const colorBgPreview = computed(() => {
  return colorPicking.value;
});
const colorFontPreview = computed(() => {
  return isDarkColor(colorPicking.value.slice(1)) ? 'white' : 'black';
});

const editStatusTitleLength = computed(() => {
  return statusDetail.value?.name?.trim().length;
});
const editStatusDescriptionLength = computed(() => {
  return statusDetail.value?.description?.trim()?.length;
});

watch(() => props.statusId, fetchData, { immediate: true });

watch(
  statusDetail,
  (newVal) => {
    if (editStatusTitleLength.value > 50) Errortext.value.name = "Status Name can't long more 50 character";
    else if (editStatusTitleLength.value === 0) Errortext.value.name = "Status Name can't be empty";
    else if (
      statusDetail.value.name.trim().toLowerCase() !== originalsDetail.value.name?.trim().toLowerCase() &&
      statusStore.status.some((status) => status.name?.trim().toLowerCase() === statusDetail.value.name?.trim().toLowerCase())
    )
      Errortext.value.name = 'Status name must be uniques, please choose another name.';
    else Errortext.value.name = '';
    if (editStatusDescriptionLength.value > 200) Errortext.value.description = "Status Description can't long more 200 character";
    else Errortext.value.description = '';
    canSave.value = Errortext.value.name === '' && Errortext.value.description === '' && newVal.value?.toString() !== originalsDetail.value?.toString();
    // JSON.stringify(newVal) !== JSON.stringify(originalsDetail.value);
  },
  { deep: true }
);

async function fetchData(id) {
  error.value = statusDetail.value = null;
  addLoading('Loading Status');
  try {
    let originalstatusDetails;
    if (useAccountStore().tokenRaw === '') {
      originalstatusDetails = await getStatusByIdForGuest(id);
    } else {
      originalstatusDetails = await getStatusById(id);
    }
    // const originalstatusDetails = await getStatusById(id);
    if (originalstatusDetails === 404 || originalstatusDetails === 400 || originalstatusDetails === 500) {
      emit('closeModal', 404);
      router.push(`/board/${currentBoardId}/status`);
    }
    originalsDetail.value = { ...originalstatusDetails };
    statusDetail.value = { ...originalstatusDetails };
  } catch (err) {
    error.value = err.toString();
  } finally {
    removeLoading('Loading Status');
  }
}

async function saveStatus() {
  addLoading('Saving Status');
  let res;
  try {
    delete statusDetail.value.id;
    delete statusDetail.value.createdOn;
    delete statusDetail.value.updatedOn;
    statusDetail.value.color = colorPicking.value.slice(1);
    res = await editStatus(props.statusId, statusDetail.value);
    statusDetail.value = res;
  } catch (error) {
    console.log(error);
  } finally {
    removeLoading('Saving Status');
    router.push(`/board/${currentBoardId}/status`);
    emit('closeModal', res);
    await getAllTasks();
  }
}

function sendCloseModal() {
  router.push(`/board/${currentBoardId}/status`);
  emit('closeModal', null);
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

function addLoading(load) {
  loading.value.push(load);
}

function removeLoading(load) {
  loading.value = loading.value.filter((l) => l !== load);
}

// ! ================= Binding Color ========================
const getFontColor = (color) => {
  console.log(isDarkColor(color) ? 'white' : 'black'); 
  return isDarkColor(color) ? 'white' : 'black'; // Contrast font color
};

const isDarkColor = (hex) => {
  const { r, g, b } = hexToRgb(hex);
  // Calculate luminance based on the perceived brightness formula
  const luminance = 0.299 * r + 0.587 * g + 0.114 * b;
  return luminance < 128; // Threshold for determining light/dark
};

const hexToRgb = (hex) => {
  const bigint = parseInt(hex, 16);
  return {
    r: (bigint >> 16) & 255,
    g: (bigint >> 8) & 255,
    b: bigint & 255,
  };
};
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
    <div class="flex flex-col overflow-scroll p-2 grow" ref="content" @scroll="handelScroll" v-if="loading.length === 0">
      <div class="w-full flex flex-row">
        <label class="form-control w-full">
          <div class="label">
            <!-- ? Head -->
            <span class="label-text">Status Name</span>
          </div>
          <input v-model="statusDetail.name" type="text" placeholder="Type here" class="itbkk-title input input-bordered w-full bg-white dark:bg-base-300 dark:text-slate-400" />
          <div class="label">
            <!-- ? Error Text -->
            <span v-if="Errortext.name !== ''" class="label-text-alt text-error">{{ Errortext.name }}</span>
            <!-- count input name -->
            <span v-if="editStatusTitleLength <= 50 && editStatusTitleLength > 0" class="justify-end text-gray-400 label-text-alt">{{ editStatusTitleLength }} / 50</span>
            <span v-if="editStatusTitleLength === 0 && Errortext.name !== ''" class="flex justify-end text-red-400 label-text-alt">{{ editStatusTitleLength }} / 50</span>
            <span v-if="editStatusTitleLength > 50" class="flex justify-end text-red-400 label-text-alt">{{ editStatusTitleLength }} / 50</span>
          </div>
        </label>
      </div>

      <label class="form-control">
        <div class="label">
          <span class="label-text">Description</span>
        </div>
        <div class="relative">
          <div class="bg-gray-50 border border-b-0 border-gray-300 top-0 left-0 right-0 block rounded-t-md">
            <button type="button" class="py-2 px-4 inline-block text-gray-400 font-semibold" :class="{ 'text-indigo-600': descriptionTab === 'write' }" @click="descriptionTab = 'write'">Write</button>
            <button type="button" class="py-2 px-4 inline-block text-gray-400 font-semibold" :class="{ 'text-indigo-600': descriptionTab === 'preview' }" @click="descriptionTab = 'preview'">
              Preview
            </button>
          </div>
          <div v-if="descriptionTab === 'write'" class="w-full">
            <textarea
              id="{{ $id }}"
              class="textarea prose rounded-t-none rounded-b-md shadow-sm border border-gray-300 p-1 bg-white overflow-y-auto w-full min-h-64"
              name="content"
              v-model="statusDetail.description"
            ></textarea>
            <div class="label">
              <!-- ? Error Text -->
              <span v-if="Errortext.description !== ''" class="label-text-alt text-error"> {{ Errortext.description }}</span>
              <span v-if="editStatusDescriptionLength <= 200" class="flex justify-end text-gray-400 label-text-alt">{{ editStatusDescriptionLength }} / 200</span>
              <span v-if="editStatusDescriptionLength > 200" class="flex justify-end text-red-400 label-text-alt">{{ editStatusDescriptionLength }} / 200</span>
            </div>
          </div>

          <div v-if="descriptionTab === 'preview'">
            <div
              class="markdown-preview w-full prose max-w-none prose-indigo leading-6 rounded-b-md shadow-sm border border-gray-300 p-5 bg-white overflow-y-auto min-h-64"
              v-html="marked(statusDetail.description === null ? '' : statusDetail.description)"
            ></div>
          </div>
        </div>
      </label>

      <div class="flex flex-row">
        <div>
          <label for="hs-color-input" class="block text-sm font-medium mb-2 dark:text-white">Color picker</label>
          <input
            type="color"
            class="p-1 h-10 w-14 block bg-white border border-gray-200 cursor-pointer rounded-lg disabled:opacity-50 disabled:pointer-events-none dark:bg-neutral-900 dark:border-neutral-700"
            id="hs-color-input"
            v-model="colorPicking"
            title="Choose your color"
          />
        </div>
        <table class="table table-lg table-pin-rows table-pin-cols w-3/4 font-semibold mx-auto my-5 text-center text-base rounded-lg border-2 border-slate-500 border-separate border-spacing-1">
          <!-- head -->
          <thead>
            <tr>
              <th>No</th>
              <th>Title</th>
              <th>Assignees</th>
              <!-- sort button -->
              <button class="itbkk-status-sort">
                <th class="flex justify-center">
                  Status
                </th>
              </button>
            </tr>
          </thead>
          <tbody>
            <!-- Listing -->
            <tr class="itbkk-item hover">
              <th> 1 </th>
              <td class="itbkk-title">
                ปั่น Code ให้เสร็จ
              </td>
              <td
                class="itbkk-assignees italic text-gray-500"
              >
                Unassigned
              </td>
              <td
                class="itbkk-status itbkk-status-name"
                :style="{
                  backgroundColor: colorBgPreview,
                  color: colorFontPreview,
                }"
              >
                {{ statusDetail.name }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ? FOOTER -->
    <div class="sticky bottom-0 w-full z-50 gap-4 p-2" :class="isFooterSticky ? 'shadow-top' : ''">
      <div class="float-right flex flex-row-reverse gap-3">
        <button class="itbkk-button-cancel btn btn-outline btn-error basis-1/6" @click="sendCloseModal()">Cancel</button>
        <button class="itbkk-button-confirm btn btn-outline btn-success basis-1/6" :disabled="!canSave" :class="!canSave ? 'disabled' : ''" @click="saveStatus()">
          {{ loading.length > 0 ? '' : 'Save' }}
          <span class="loading loading-spinner text-success" v-if="loading.length > 0"></span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped></style>
