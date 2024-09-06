import router from "@/router";
import { useAccountStore } from "@/stores/account";
import {useBoardStore} from "@/stores/board.js";
import {useTaskStore} from "@/stores/task.js";
// ! -------------------------------- Task ------------------------------------------
export async function getAllTasks() {
  const accountStore = useAccountStore();
  const taskStore = useTaskStore();
  const boardId = useBoardStore().currentBoardId;
  try {
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method
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
  console.log(`GET !! ${id}`);
  let res, item;
  try {
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`, {
      method: "GET",
      headers: {

        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
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

export async function addTask(newTask) {
  let res, item;
  const boardId = useBoardStore().currentBoardId;
  // console.log(JSON.stringify({ ...newTask }));
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newTask }),
    });
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
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${taskId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify(Task),
    });
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
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/tasks/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
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
  const boardId = useBoardStore().currentBoardId;
  try {
    const accountStore = useAccountStore();
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method
    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return;
    }
    return await res.json();
  } catch (error) {}
}

export async function getStatusById(id) {
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
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
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify({ ...newStatus }),
    });
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
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
      body: JSON.stringify(Status),
    });
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
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${id}`, {
      method: "DELETE",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    });
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
    res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/${oldId}/${newId}`,
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
    res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/maximum-task`,
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
  const boardId = useBoardStore().currentBoardId;
  let res, item;
  try {
    const accountStore = useAccountStore();
    res = await fetch(
      `${import.meta.env.VITE_API_ROOT}/boards/${boardId}/statuses/maximum-task`,
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
    if (res.status === 200) return await res.json();
  } catch (e) {
    console.log(e.toString());
  }
}
// ! -------------------------- BOARD ----------------------------

// ? doesn't require param , func will get param from account store
export async function getAllBoard() {
  try {
    const accountStore = useAccountStore();
    const boardStore = useBoardStore();
    let res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accountStore.tokenRaw}`,
      },
    }); //GET Method

    if (res.status === 401) {
      accountStore.clearTokenDetail();
      router.push("/login");
      return null;
    } else if (res.status === 200) {
      let item = await res.json();
      console.table(item)
      // ? because now item is an object not an array
      boardStore.addBoard(item);
      return { status: 200, payload: item };
      // return await item
    } else if (res.status === 400) {
      return { status: 400, payload: "No board found" };
    }
    // return await res.json();
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
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/boards`, {
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
      return;
    }
    if (res.status === 201) {
      item = await res.json();
      boardStore.addBoard(item);
      return item;
    } else {
      return res.status;
    }
  } catch (error) {
    return error;
  }
}
// ! -------------------------- LOGIN ----------------------------

export async function login(username, password) {
  let res, item;
  console.log(JSON.stringify({ userName: username, password: password }))
  try {
    const accountStore = useAccountStore();
    res = await fetch(`${import.meta.env.VITE_API_ROOT}/login`, {
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

// ! -------------------------- Private function ----------------------------

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
