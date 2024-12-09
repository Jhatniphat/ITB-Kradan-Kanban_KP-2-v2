import router from '@/router';
import { useAccountStore } from '@/stores/account';
import { useBoardStore } from '@/stores/board.js';
import { useTaskStore } from '@/stores/task.js';
import { useStatusStore } from '@/stores/status.js';
import { useToastStore } from '@/stores/toast';

// ! -------------------------------- Task ------------------------------------------
/**
 * ? Retrieves All Task By specified UserId and Current BoardId
 * * If tasks already exist in TaskStore, this function will not make a network request.
 * * The retrieved tasks will be automatically added to TaskStore.tasks
 * * Automatically redirect to login when get 401
 * @function getAllTasks
 * @returns {Promise<Array>} A promise that resolves to an array of tasks.
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getAllTasks() {
  const accountStore = useAccountStore();
  const taskStore = useTaskStore();
  const boardId = useBoardStore().currentBoardId;
  if (taskStore.tasks.length > 0) {
    return taskStore.tasks;
  }
  try {
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    } else if (res.status === 200) {
      let item = await res.json();
      taskStore.tasks = item;
      return item;
    }
    return await res.json();
  } catch (error) {}
}

/**
 * ? Retrieves Task Detail By specified UserId , TaskId and Current BoardId
 * * Automatically redirect to login when get 401
 * @param {Number} id
 * @function getTaskById
 * @returns {Promise<Object>} A promise that resolves to Task Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getTaskById(id) {
  const boardId = useBoardStore().currentBoardId;
  const accountStore = useAccountStore();
  let res, item;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      item = await res.json();
      item.createdOn = timeFormater(item.createdOn);
      item.updatedOn = timeFormater(item.updatedOn);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to add the specified task and Current BoardId then return the details of the newly created task upon success.
 * * Automatically redirect to login when get 401
 * @param {Task} newTask
 * @function addTask
 * @returns {Promise<Object>} A promise that resolves to New Task Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function addTask(newTask) {
  let res, item;
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newTask }),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 201) {
      item = await res.json();
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to update the specified task By TaskId , Current BoardId , UserId Then return the details of the updated task upon success.
 * * Automatically redirect to login when get 401
 * @param {number} taskId - task id for update task
 * @param {Task} Task - not have Task.CreatedOn , Task.UpdatedOn
 * @function editTask
 * @returns {Promise<Object>} A promise that resolves to Updated Task Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function editTask(taskId, Task) {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify(Task),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      // return await res.json();
      return { status : res.status , payload : res.json() }
    } else {
      // throw new Error(`Failed to update task: ${res.status}`);
      return { status : res.status , payload : res.json() }
    }
  } catch (error) {
    // throw new Error(`Error updating task: ${error.message}`);
    return { status : res.status , payload : res.json() }
  }
}

/**
 * ? This function will send a request to delete the specified task By TaskId , Current BoardId , UserId Then return the details of the deleted task upon success.
 * * Automatically redirect to login when get 401
 * @param {number} taskId
 * @function deleteTask
 * @returns {Promise<Object>} A promise that resolves to Deleted Task Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function deleteTask(id) {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      let item = await res.json();
      item.status = ENUMToTitleCase(item.status);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

// ! ------------------------------- Status --------------------------------
/**
 * ? Retrieves All Status By specified UserId and Current BoardId
 * * If statuses already exist in StatusStore, this function will not make a network request.
 * * The retrieved statuses will be automatically added to StatusStore.status
 * * Automatically redirect to login when get 401
 * @function getAllStatus
 * @returns {Promise<Array>} A promise that resolves to an array of statuses.
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getAllStatus() {
  if (useStatusStore().status.length > 0) {
    return useStatusStore().status;
  }
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    } else if (res.status === 200) {
      let statusess = await res.json();
      useStatusStore().status = statusess
      return await statusess;
    }
  } catch (error) {}
}

/**
 * ? Retrieves Status Detail By specified UserId , StatusId and Current BoardId
 * * Automatically redirect to login when get 401
 * @param {Number} id
 * @function getStatusById
 * @returns {Promise<Object>} A promise that resolves to Status Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getStatusById(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      item = await res.json();
      item.createdOn = timeFormater(item.createdOn);
      item.updatedOn = timeFormater(item.updatedOn);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to add the specified status and Current BoardId then return the details of the newly created status upon success.
 * * Automatically redirect to login when get 401
 * @param {Status} newStatus
 * @function addStatus
 * @returns {Promise<Object>} A promise that resolves to New Status Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function addStatus(newStatus) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newStatus }),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 201) {
      item = await res.json();
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to update the specified status By StatusId , Current BoardId , UserId Then return the details of the updated status upon success.
 * * Automatically redirect to login when get 401
 * @param {number} id - status id for update status
 * @param {Status} Status - not have Status.CreatedOn , Status.UpdatedOn
 * @function editStatus
 * @returns {Promise<Object>} A promise that resolves to Updated Status Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function editStatus(id, Status) {
  const boardId = useBoardStore().currentBoardId;
  let res;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify(Status),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      let updatedStatus = await res.json();
      return updatedStatus;
    } else {
      throw new Error(`Failed to update status: ${res.status}`);
    }
  } catch (error) {
    throw new Error(`Error updating status: ${error.message}`);
  }
}

/**
 * ? This function will send a request to delete the specified status By StatusId , Current BoardId , UserId Then return the details of the deleted status upon success.
 * * Automatically redirect to login when get 401
 * @param {number} id - Status id that want to delete
 * @function deleteStatus
 * @returns {Promise<Object>} A promise that resolves to Deleted Status Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function deleteStatus(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      item = await res.json();
      item.createdOn = timeFormater(item.createdOn);
      item.updatedOn = timeFormater(item.updatedOn);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to transfer task in old Status to new Status and delet old Status Then return the details of the deleted old status upon success.
 * * Automatically redirect to login when get 401
 * @param {number} oldId - Status id that want to delete
 * @param {number} newId - Status id that want to transfer to
 * @function transferStatus
 * @returns {Promise<Object>} A promise that resolves to Deleted Status Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function transferStatus(oldId, newId) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${oldId}/${newId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      item = await res.json();
      return item;
    }
    if (res.status === 400) {
      item = await res.json();
      return item;
    }
  } catch (err) {
    return err;
  }
}

/**
 * ? This function will send a request to change limitStatus to opposite Then return status code of response
 * * Automatically redirect to login when get 401
 * @function toggleLimitStatus
 * @returns {Promise<Number>} A promise that resolves to response.status code
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function toggleLimitStatus() {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/maximum-task`, {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    return res.status;
  } catch (e) {
    console.log(e.toString());
  }
}

/**
 * ? This function will send a request to Retrieves limitStatus to opposite Then return boolean of limitStatus
 * * The retrieved limitStatus will be automatically added to StatusStore.limitEnable
 * * Automatically redirect to login when get 401
 * @function getLimitStatus
 * @returns {Promise<Number>} A promise that resolves to response.status code
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getLimitStatus() {
  if (useStatusStore().limitEnable !== null) {
    return useStatusStore().limitEnable;
  }
  const boardId = useBoardStore().currentBoardId;
  const statusStore = useStatusStore();
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/maximum-task`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      let limitEnable = await res.json();
      statusStore.setLimitEnable(limitEnable);
      return await limitEnable;
    }
  } catch (e) {
    console.log(e.toString());
  }
}

// ! -------------------------- BOARD ----------------------------

/**
 * ? Retrieves All Board By specified UserId
 * * If boards already exist in BoardStore, this function will not make a network request.
 * * The retrieved boards will be automatically added to BoardStore.boards
 * * Automatically redirect to login when get 401
 * @function getAllBoard
 * @returns {Promise<Array>} A promise that resolves to an array of boards.
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getAllBoard() {
  if (useBoardStore().boards.length > 0) {
    return useBoardStore().boards;
  }
  try {
    const accountStore = useAccountStore();
    const boardStore = useBoardStore();
    const res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method
    if (!res) {
      console.log('Response is undefined');
    }

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return null;
    } else if (res.status === 200) {
      let item = await res.json();
      boardStore.boards = item;
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      return { status: 400, payload: 'No board found' };
    }
    // return await res.json();
  } catch (error) {
    console.error(error);
  }
}

/**
 * ? Retrieves Board Detail By specified UserId and BoardId
 * * If specified boards already exist in BoardStore, this function will not make a network request.
 * * Automatically redirect to login when get 401
 * @param {String} boardId
 * @function getBoardById
 * @returns {Promise<Object>} A promise that resolves to Board Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function getBoardById(boardId) {
  const boardStore = useBoardStore();
  if (boardStore.isBoardExist(boardId)) {
    return useBoardStore().findBoardById(boardId);
  }

  const accountStore = useAccountStore();
  try {
    const res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`, // Include the token if needed
      },
    });

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return null;
    } else if (res.status === 200) {
      let item = await res.json();
      boardStore.addBoard(item);
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      return { status: 400, payload: 'No board found' };
    }
  } catch (error) {
    console.error(error);
  }
}

/**
 * ? This function will send a request to add the specified board and Current BoardId then return the details of the newly created board upon success.
 * * Automatically redirect to login when get 401
 * @param {Board} newBoard
 * @function addBoard
 * @returns {Promise<Object>} A promise that resolves to New Board Detail
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function addBoard(newBoard) {
  const accountStore = useAccountStore();
  const boardStore = useBoardStore();
  let res, item;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newBoard }),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return { status: res.status, payload: 'Unauthorized' };
    }
    if (res.status === 201 || res.status === 200) {
      item = await res.json();
      boardStore.addBoard(item);
      return { status: 200, payload: item };
    } else {
      return {
        status: res.status,
        payload: 'There is a problem. Please try again later',
      };
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function will send a request to change visibility of board Then return new visibility of board
 * * Automatically redirect to login when get 401
 * @param {String} mode - only allow PUBLIC or PRIVATE
 * @function changeVisibility
 * @returns {Promise<Object>} A promise that resolves to response.status code
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function changeVisibility(mode) {
  const accountStore = useAccountStore();
  const boardStore = useBoardStore();
  const boardId = boardStore.currentBoardId;
  let res;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ visibility: mode }),
    });

    if (res.ok) {
      const resData = await res.json();
      return resData;
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return res.status;
    } else {
      return res.status;
    }
  } catch (error) {
    console.log(error.toString());
  }
}

// ! -------------------------- GUEST USER -----------------------
/**
 * ? This function is same at getBoardById But don't require login
 */
export async function getBoardByIdForGuest(boardId) {
  console.log('FOR GUEST');
  if (useBoardStore().isBoardExist(boardId)) {
    return useBoardStore().findBoardById(boardId);
  }
  const accountStore = useAccountStore();
  try {
    const res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
    });

    if (res.status === 401) {
      console.log('401');
      accountStore.clearTokenDetail();
      router.push('/login');
      return null;
    } else if (res.status === 200) {
      console.log('200');
      let item = await res.json();
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      console.log('400');
      return { status: 400, payload: 'No board found' };
    }
  } catch (error) {
    console.error(error);
  }
}

/**
 * ? This function is same at getAllStatus But don't require login
 */
export async function getAllStatusForGuest() {
  if (useStatusStore().status.length > 0) {
    return useStatusStore().status;
  }
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`, {
      method: 'GET',
      headers: {},
    }); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    } else if (res.status === 200) {
      useStatusStore().status = await res.json();
      return await res.json();
    }
  } catch (error) {}
}

/**
 * ? This function is same at getStatusById But don't require login
 */
export async function getStatusByIdForGuest(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: 'GET',
      headers: {},
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      item = await res.json();
      item.createdOn = timeFormater(item.createdOn);
      item.updatedOn = timeFormater(item.updatedOn);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function is same at getAllTasks But don't require login
 */
export async function getAllTasksForGuest() {
  const accountStore = useAccountStore();
  const taskStore = useTaskStore();
  const boardId = useBoardStore().currentBoardId;
  if (taskStore.tasks.length > 0) {
    return taskStore.tasks;
  }
  try {
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`, {
      method: 'GET',
      headers: {},
    }); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    } else if (res.status === 200) {
      let item = await res.json();
      taskStore.tasks = item;
      return item;
    }
    return await res.json();
  } catch (error) {}
}

/**
 * ? This function is same at getTaskById But don't require login
 */
export async function getTaskByIdForGuest(id) {
  const boardId = useBoardStore().currentBoardId;
  const accountStore = useAccountStore();
  let res, item;
  try {
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`, {
      method: 'GET',
      headers: {},
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      item = await res.json();
      console.table(item);
      item.createdOn = timeFormater(item.createdOn);
      item.updatedOn = timeFormater(item.updatedOn);
      console.table(item);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}

/**
 * ? This function is same at getLimitStatus But don't require login
 */
export async function getLimitStatusForGuest() {
  const boardId = useBoardStore().currentBoardId;
  const statusStore = useStatusStore();
  if (useStatusStore().limitEnable !== null) {
    return useStatusStore().limitEnable;
  }
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/maximum-task`, {
      method: 'GET',
      headers: {},
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.status === 200) {
      statusStore.setLimitEnable(await res.json());
      return await res.json();
    }
  } catch (e) {
    console.log(e.toString());
  }
}

// ! -------------------------- COLLABORATOR ----------------------------
export async function getAllCollabs() {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    const res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    console.log(res.status);
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return null;
    } else if (res.status === 200) {
      return await res.json();
    } else {
      return { status: res.status, error: true };
    }
  } catch (error) {
    console.error('Failed to fetch collaborators:', error);
    return null;
  }
}

export async function addCollaborator(newCollaborator) {
  let res, item;
  const boardId = useBoardStore().currentBoardId;
  const toastStore = useToastStore();
  toastStore.createToast('Adding collaborator...', 'waiting');
  useBoardStore().addCollaborator({ ...newCollaborator, name: 'processing', status: 'processing' });
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newCollaborator }),
    });
    switch (res.status) {
      case 201:
      case 200: {
        toastStore.createToast('Collaborator added successfully');
        const item = await res.json();
        useBoardStore().editCollaborator(item);
        return item;
      }
      case 401: {
        accountStore.clearTokenDetail();
        router.push('/login');
        useBoardStore().removeCollaborator(newCollaborator);
        return;
      }
      case 409: {
        toastStore.createToast('The user is already a collaborator of this board.', 'danger');
        useBoardStore().removeCollaborator(newCollaborator);
        return res.status;
      }
      case 403: {
        toastStore.createToast('You do not have permission to add board collaborator.', 'danger');
        useBoardStore().removeCollaborator(newCollaborator);
        return res.status;
      }
      case 404: {
        toastStore.createToast('User not found', 'danger');
        useBoardStore().removeCollaborator(newCollaborator);
        return res.status;
      }
      case 503: {
        useBoardStore().clearCollaborator();
        try {
          useBoardStore().currentBoard.collaborators = await getAllCollabs();
        } catch (error) {
          // Handle error if necessary
        } finally {
          const invitedUsername = useBoardStore().currentBoard.collaborators.find((collab) => collab.email === newCollaborator.email)?.name;

          toastStore.createToast(
            `We could not send e-mail to <span class="font-bold">${invitedUsername}</span>, he/she can accept the invitation at <span class="underline"> /board/${boardId}/collab/invitations </span>`,
            'warning',
            15000,
            `${import.meta.env.VITE_FRONTEND_ROOT}/board/${boardId}/collab/invitations`
          );
          return res.status;
        }
      }
      default:
        toastStore.createToast('Unexpected error occurred.', 'danger');
        return res.status;
    }
    // if (res.status === 201 || res.status === 200) {
    //   toastStore.createToast("Collaborator added successfully");
    //   item = await res.json();
    //   useBoardStore().editCollaborator(item);
    //   return item;
    // }
    // if (res.status === 401) {
    //   accountStore.clearTokenDetail();
    //   router.push("/login");
    //   useBoardStore().removeCollaborator(newCollaborator);
    //   return;
    // }
    // if (res.status === 409) {
    //   toastStore.createToast("The user is already a collaborator of this board.", "danger");
    //   useBoardStore().removeCollaborator(newCollaborator);
    //   return res.status;
    // }
    // if (res.status === 403) {
    //   toastStore.createToast("You do not have permission to add board collaborator.", "danger");
    //   useBoardStore().removeCollaborator(newCollaborator);
    //   return res.status;
    // }
    // if (res.status === 404) {
    //   toastStore.createToast("User not found", 'danger');
    //   useBoardStore().removeCollaborator(newCollaborator);
    //   return res.status;
    // }
    // if (res.status === 503) {
    //   useBoardStore().clearCollaborator();
    //   try {
    //     useBoardStore().currentBoard.collaborators = await getAllCollabs();
    //   } catch (error) {
    //   } finally {
    //     let invitedUsername = useBoardStore().currentBoard.collaborators.find(collab => collab.email === newCollaborator.email).name;
    //     toastStore.createToast(`We could not send e-mail to <span class="font-bold"> ${invitedUsername} </span>, he/she can accept the invitation at  <span class="underline"> /board/${boardId}/collab/invitations </span>`, "warning" , 15000);
    //     return res.status;
    //   }
    // }
    // if (res.status === 409){
    //   toastStore.createToast("The user is already a collaborator of this board.", "danger");
    //   useBoardStore().removeCollaborator(newCollaborator);
    // }
    // return res.status;
  } catch (error) {
    toastStore.createToast('adding collaborator failed', 'danger');
    // useBoardStore().removeCollaborator(newCollaborator);
    return error;
  }
}

export async function acceptInvitation(boardId, collabData) {
  const accountStore = useAccountStore();
  // let boardId = collabData.boardId;
  // let collabId = collabData.userId;
  let res;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs/invitations`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify(collabData),
    });

    if (res.ok) {
      return { status: res.status, payload: await res.json() };
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return { status: res.status, payload: await res.json() };
    } else {
      return { status: res.status, payload: await res.json() };
    }
  } catch (error) {
    console.log(error.toString());
  }
}

export async function changeAccessRight(boardId, collabId, collabData) {
  const accountStore = useAccountStore();
  let res;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs/${collabId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ accessRight: collabData }),
    });

    if (res.ok) {
      const resData = await res.json();
      return resData;
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return res.status;
    } else {
      return res.status;
    }
  } catch (error) {
    console.log(error.toString());
  }
}

export async function deleteCollaborator(boardId, collabId) {
  const accountStore = useAccountStore();
  try {
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs/${collabId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return;
    }
    if (res.ok) {
      let item = await res.json();
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    console.log(error.toString());
    return error;
  }
}

// ! -------------------------- ATTACHMENT -----------------------

export async function getAllAttachments(boardId, taskId) {
  const accountStore = useAccountStore();
  let res;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}/attachments`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });

    if (res.ok) {
      const attachments = await res.json();
      console.log(attachments);

      return attachments;
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return res.status;
    } else {
      return res.status;
    }
  } catch (error) {
    console.log(error.toString());
  }
}

export async function uploadAttachments(boardId, taskId, files) {
  const accountStore = useAccountStore();
  let res;
  try {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));

    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}/attachments`, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: formData,
    });

    if (res.ok) {
      // const uploadedAttachments = await res.json();
      return { status : res.status , payload : res.json() }
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return { status : res.status , payload : res.json() }
    } else {
      return { status : res.status , payload : res.json() }
    }
  } catch (error) {
    console.log(error.toString());
  }
}

export async function deleteAttachment(boardId, taskId, attachmentId) {
  const accountStore = useAccountStore();
  let res;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}/attachments/${attachmentId}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });

    if (res.ok) {
      // const deletedAttachment = await res.json();
      // return deletedAttachment;
      return { status : res.status , payload : res.json() }
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return { status : res.status , payload : res.json() }
    } else {
      return { status : res.status , payload : res.json() }
    }
  } catch (error) {
    console.log(error.toString());
  }
}

// ! -------------------------- LOGIN ----------------------------
/**
 * ? This function will send a request to login Then return access token and refresh token
 * * The retrieved access token and refresh token will be automatically added to AccountStore
 * @function login
 * @param {String} username
 * @param {String} password
 * @returns {Promise<Object>} A promise that resolves to access token and refresh token
 * @throws {Error} Throws an error if the fetch operation fails.
 */
export async function login(username, password) {
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT_LOGIN}/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ userName: username, password: password }),
    });
    if (res.status === 200) {
      item = await res.json();
      accountStore.setToken(item);
      return {
        status: 200,
        payload: item,
      };
    }
    if (res.status === 401 || res.status === 400) {
      return {
        status: res.status,
        payload: 'Username or Password is incorrect',
      };
    } else
      return {
        status: res.status,
        payload: 'There is a problem. Please try again later',
      };
  } catch (error) {}
}

export async function validateToken() {
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT_LOGIN}/validate-token`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push('/login');
      return null;
    } else if (res.status === 200) {
      return null;
    }
  } catch (error) {
    console.error(error);
  }
}

// ! -------------------------- Private function ----------------------------
/**
 * ? This function will check acesstoken exiped if expired this function will request to get new acesstoken before fetch
 * * The retrieved access token will be automatically added to AccountStore
 * @function fetchWithTokenCheck
 * @param {String} url - url parameter of fetch
 * @param {Object} options - options parameter of fetch
 * @returns {fetch}
 * @throws {Error} Throws an error if the fetch operation fails.
 */
async function fetchWithTokenCheck(url, options) {
  const accountStore = useAccountStore();
  await checkTokenExpired(); // Wait for checkTokenExpired to complete

  options.headers = {
    ...options.headers,
    Authorization: `Bearer ${accountStore.tokenRaw}`,
  };

  return fetch(url, options); // Proceed with fetch
}

export async function checkTokenExpired() {
  const accountStore = useAccountStore();
  if (!accountStore.isAccessTokenExpired()) {
    return;
  }

  if (accountStore.isAccessTokenExpired() && accountStore.isRefreshTokenExpired()) {
    accountStore.clearTokenDetail();
    console.log('Token expired');
    router.push('/login');
    return;
  }

  if (accountStore.isAccessTokenExpired() && !accountStore.isRefreshTokenExpired()) {
    try {
      const res = await fetch(`${import.meta.env.VITE_API_ROOT_LOGIN}/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accountStore.refreshToken}`,
        },
      });

      if (res.status === 200) {
        const item = await res.json();
        accountStore.setToken(item);
        return;
      } else {
        throw new Error('Failed to renew token');
      }
    } catch (error) {
      console.error('Error renewing token:', error);
      accountStore.clearTokenDetail();
      router.push('/login');
    }
  }
}

function timeFormater(time) {
  return new Date(time).toLocaleString('en-GB', {
    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
  });
}

function ENUMToTitleCase(str) {
  if (str === null || str === '') return 'No Status';
  // str ?? return 'No Status'
  let words = str.split('_');
  let titleCaseWords = words.map((word) => {
    return word[0].toUpperCase() + word.slice(1).toLowerCase();
  });
  return titleCaseWords.join(' ');
}

function titleCaseToENUM(str) {
  return str.split(' ').join('_').toUpperCase();
}
