import router from "@/router";
import { useAccountStore } from "@/stores/account";
import { useBoardStore } from "@/stores/board.js";
import { useTaskStore } from "@/stores/task.js";
import { useStatusStore } from "@/stores/status.js";

// ! -------------------------------- Task ------------------------------------------
export async function getAllTasks() {
  const accountStore = useAccountStore();
  const taskStore = useTaskStore();
  const boardId = useBoardStore().currentBoardId;
  if (taskStore.tasks.length > 0) {
    return taskStore.tasks;
  }
  try {
    let res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    ); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    } else if (res.status === 200) {
      let item = await res.json();
      taskStore.tasks = item;
      return item;
    }
    return await res.json();
  } catch (error) {}
}

export async function getTaskById(id) {
  const boardId = useBoardStore().currentBoardId;
  const accountStore = useAccountStore();
  let res, item;
  try {
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function addTask(newTask) {
  let res, item;
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify({ ...newTask }),
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function editTask(taskId, Task) {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify(Task),
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    }
    if (res.ok) {
      return await res.json();
    } else {
      throw new Error(`Failed to update task: ${res.status}`);
    }
  } catch (error) {
    throw new Error(`Error updating task: ${error.message}`);
  }
}

export async function deleteTask(id) {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`,
      {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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
export async function getAllStatus() {
  if (useStatusStore().status.length > 0) {
    return useStatusStore().status;
  }
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    ); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    } else if (res.status === 200) {
      useStatusStore().status = await res.json();
      return await res.json();
    }
  } catch (error) {}
}

export async function getStatusById(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function addStatus(newStatus) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify({ ...newStatus }),
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function editStatus(id, Status) {
  const boardId = useBoardStore().currentBoardId;
  let res;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify(Status),
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function deleteStatus(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`,
      {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function transferStatus(oldId, newId) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${
        import.meta.env.VITE_API_ROOT
      }/boards/${boardId}/statuses/${oldId}/${newId}`,
      {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function toggleLimitStatus() {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${
        import.meta.env.VITE_API_ROOT
      }/boards/${boardId}/statuses/maximum-task`,
      {
        method: "PATCH",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    }
    return res.status;
  } catch (e) {
    console.log(e.toString());
  }
}

export async function getLimitStatus() {
  if (useStatusStore().limitEnable !== null) {
    return useStatusStore().limitEnable;
  }
  const boardId = useBoardStore().currentBoardId;
  const statusStore = useStatusStore();
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${
        import.meta.env.VITE_API_ROOT
      }/boards/${boardId}/statuses/maximum-task`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

// ! -------------------------- BOARD ----------------------------

// ? doesn't require param , func will get param from account store
export async function getAllBoard() {
  if (useBoardStore().boards.length > 0) {
    return useBoardStore().boards;
  }
  try {
    const accountStore = useAccountStore();
    const boardStore = useBoardStore();
    const res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    ); //GET Method
    if (!res) {
      console.log("Response is undefined");
    }

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      let item = await res.json();
      boardStore.boards = item;
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      return { status: 400, payload: "No board found" };
    }
    // return await res.json();
  } catch (error) {
    console.error(error);
  }
}

export async function getBoardById(boardId) {
  const boardStore = useBoardStore();
  console.log(boardStore.isBoardExist(boardId));
  if (boardStore.isBoardExist(boardId)) {
    return useBoardStore().findBoardById(boardId);
  }

  const accountStore = useAccountStore();
  try {
    const res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`, // Include the token if needed
        },
      }
    );

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      let item = await res.json();
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      return { status: 400, payload: "No board found" };
    }
  } catch (error) {
    console.error(error);
  }
}

// ? @param newBoard : object
export async function addBoard(newBoard) {
  const accountStore = useAccountStore();
  const boardStore = useBoardStore();
  let res, item;
  try {
    res = await fetchWithTokenCheck(`${import.meta.env.VITE_API_ROOT}/boards`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newBoard }),
    });
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return { status: res.status, payload: "Unauthorized" };
    }
    if (res.status === 201 || res.status === 200) {
      item = await res.json();
      boardStore.addBoard(item);
      return { status: 200, payload: item };
    } else {
      return {
        status: res.status,
        payload: "There is a problem. Please try again later",
      };
    }
  } catch (error) {
    return error;
  }
}

export async function changeVisibility(mode) {
  const accountStore = useAccountStore();
  const boardStore = useBoardStore();
  const boardId = boardStore.currentBoardId;
  let res;
  try {
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}`,
      {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify({ visibility: mode }),
      }
    );

    if (res.ok) {
      const resData = await res.json();
      return resData;
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return res.status;
    } else {
      return res.status;
    }
  } catch (error) {
    console.log(error.toString());
  }
}

// ! -------------------------- GUEST USER -----------------------
export async function getBoardByIdForGuest(boardId) {
  console.log("FOR GUEST");
  if (useBoardStore().isBoardExist(boardId)) {
    return useBoardStore().findBoardById(boardId);
  }
  const accountStore = useAccountStore();
  try {
    const res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}`,
      {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (res.status === 401) {
      console.log("401");
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      console.log("200");
      let item = await res.json();
      return { status: 200, payload: item };
    } else if (res.status === 400) {
      console.log("400");
      return { status: 400, payload: "No board found" };
    }
  } catch (error) {
    console.error(error);
  }
}

export async function getAllStatusForGuest() {
  if (useStatusStore().status.length > 0) {
    return useStatusStore().status;
  }
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`,
      {
        method: "GET",
        headers: {},
      }
    ); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    } else if (res.status === 200) {
      useStatusStore().status = await res.json();
      return await res.json();
    }
  } catch (error) {}
}

export async function getStatusByIdForGuest(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`,
      {
        method: "GET",
        headers: {},
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function getAllTasksForGuest() {
  const accountStore = useAccountStore();
  const taskStore = useTaskStore();
  const boardId = useBoardStore().currentBoardId;
  if (taskStore.tasks.length > 0) {
    return taskStore.tasks;
  }
  try {
    let res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`,
      {
        method: "GET",
        headers: {},
      }
    ); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    } else if (res.status === 200) {
      let item = await res.json();
      taskStore.tasks = item;
      return item;
    }
    return await res.json();
  } catch (error) {}
}

export async function getTaskByIdForGuest(id) {
  const boardId = useBoardStore().currentBoardId;
  const accountStore = useAccountStore();
  let res, item;
  try {
    res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`,
      {
        method: "GET",
        headers: {},
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function getLimitStatusForGuest() {
  const boardId = useBoardStore().currentBoardId;
  const statusStore = useStatusStore();
  if (useStatusStore().limitEnable !== null) {
    return useStatusStore().limitEnable;
  }
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(
      `${
        import.meta.env.VITE_API_ROOT
      }/boards/${boardId}/statuses/maximum-task`,
      {
        method: "GET",
        headers: {},
      }
    );
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
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

export async function getCollaborators() {
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    const res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collab`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    );

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      return await res.json();
    } else {
      console.error(`Error fetching collaborators: ${res.statusText}`);
      return null;
    }
  } catch (error) {
    console.error("Failed to fetch collaborators:", error);
    return null;
  }
};



export async function addCollaborator(newCollaborator) {
  let res, item;
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/collabs`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
        body: JSON.stringify({ ...newCollaborator }),
      }
    );
    if (res.status === 201) {
      item = await res.json();
      return item;
    }
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    }
    if (res.status === 403) {
      console.log("You do not have permission to add board collaborator.");
      return res.status;
    }
    if (res.status === 404) {
      console.log("The user does not exist.");
      return res.status;
    }
    if (res.status === 409) {
      console.log("The user is already a collaborator of this board.");
      return res.status;
    }
    return res.status;
  } catch (error) {
    console.error("Error adding collaborator:", error);
    return error;
  }
}

// ! -------------------------- LOGIN ----------------------------

export async function login(username, password) {
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT_LOGIN}/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
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
        payload: "Username or Password is incorrect",
      };
    } else
      return {
        status: res.status,
        payload: "There is a problem. Please try again later",
      };
  } catch (error) {}
}

export async function validateToken() {
  try {
    const accountStore = useAccountStore();
    let res = await fetchWithTokenCheck(
      `${import.meta.env.VITE_API_ROOT_LOGIN}/validate-token`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${accountStore.tokenRaw}`,
        },
      }
    ); //GET Method

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      return null;
    }
  } catch (error) {
    console.error(error);
  }
}

// ! -------------------------- Private function ----------------------------
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
  console.log("Checking token expiry");
  if (!accountStore.isAccessTokenExpired()) {
    return;
  }

  if (
    accountStore.isAccessTokenExpired() &&
    accountStore.isRefreshTokenExpired()
  ) {
    accountStore.clearTokenDetail();
    console.log("Token expired");
    router.push("/login");
    return;
  }

  if (
    accountStore.isAccessTokenExpired() &&
    !accountStore.isRefreshTokenExpired()
  ) {
    try {
      const res = await fetch(`${import.meta.env.VITE_API_ROOT_LOGIN}/token`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accountStore.refreshToken}`,
        },
      });

      if (res.status === 200) {
        const item = await res.json();
        accountStore.setToken(item);
        return;
      } else {
        throw new Error("Failed to renew token");
      }
    } catch (error) {
      console.error("Error renewing token:", error);
      accountStore.clearTokenDetail();
      router.push("/login");
    }
  }
}

function timeFormater(time) {
  return new Date(time).toLocaleString("en-GB", {
    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
  });
}

function ENUMToTitleCase(str) {
  if (str === null || str === "") return "No Status";
  // str ?? return 'No Status'
  let words = str.split("_");
  let titleCaseWords = words.map((word) => {
    return word[0].toUpperCase() + word.slice(1).toLowerCase();
  });
  return titleCaseWords.join(" ");
}

function titleCaseToENUM(str) {
  return str.split(" ").join("_").toUpperCase();
}
