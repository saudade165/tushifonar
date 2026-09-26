window.tushifonar.responses = {};


async function addResponse(id, response) {
    window.tushifonar.responses[id] = JSON.parse(response);

}

async function getResponse(id) {
    while (true) {
        if (window.tushifonar.responses[id]) {
            const response = window.tushifonar.responses[id];
            delete window.tushifonar.responses[id];
            return response;
        } else {
            await delayExecution(50);
        }
    }
}

async  function delayExecution(ms) {
    return new Promise((resolve, reject) => {
        setTimeout(() => {
            resolve()
        }, ms)
    })
}

async function login() {
    let phone = document.getElementById("phone");
    let password = document.getElementById("password");

    let id = window.tushifonar.userLogin(phone.value, password.value);
    let response = await getResponse(id);


    let out = document.getElementById("out");
    console.log("CLIENT: got new response - " + JSON.stringify(response));
}
