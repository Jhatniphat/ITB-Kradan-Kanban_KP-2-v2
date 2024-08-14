// ! -------------------------------- Task ------------------------------------------
export async function getAllTasks() {
    try {
        let res = await fetch(`${import.meta.env.VITE_API_ROOT}/tasks`, {
            method: "GET",
        }); //GET Method
        return await res.json();
    } catch (error) {
    }
}

export async function getTaskById(id) {
    console.log(`GET !! ${id}`);
    let res, item;
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/tasks/${id}`, {
            method: "GET",
        });
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
    // console.log(JSON.stringify({ ...newTask }));
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/tasks`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({...newTask}),
        });
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

export async function editTask(id, Task) {
    try {
        let res = await fetch(`${import.meta.env.VITE_API_ROOT}/tasks/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(Task),
        });

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
    try {
        let res = await fetch(`${import.meta.env.VITE_API_ROOT}/tasks/${id}`, {
            method: "DELETE",
        });
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
    try {
        let res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses`, {
            method: "GET",
        }); //GET Method
        return await res.json();
    } catch (error) {
    }
}

export async function getStatusById(id) {
    let res, item;
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses/${id}`, {
            method: "GET",
        });
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
    let res, item;
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({...newStatus}),
        });
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
    let res;
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses/${id}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(Status),
        });

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
    let res, item;
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses/${id}`, {
            method: "DELETE",
        });
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
    let res, item;
    try {
        res = await fetch(
            `${import.meta.env.VITE_API_ROOT}/statuses/${oldId}/${newId}`,
            {
                method: "DELETE",
            }
        );
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
    let res, item
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses/maximum-task`,
            {
                method: "PATCH",
            })
        return res.status
    } catch (e) {
        console.log(e.toString())
    }
}

export async function getLimitStatus() {
    let res, item
    try {
        res = await fetch(`${import.meta.env.VITE_API_ROOT}/statuses/maximum-task`, {method: "GET"})
        if (res.status === 200)
        return await res.json()
    } catch (e) {
        console.log(e.toString())
    }
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
