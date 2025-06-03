window.onload = function() {
    updateModel();
}

function updateModel() {
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/get-models", true);
    xhr.onload = function() {
        if (xhr.status === 200 && xhr.readyState === 4) {
            const response = xhr.responseText.split("\n");
            console.log("Models fetched:", response);
            for (let i = 0; i < response.length; i++) {
                const model = response[i].trim();
                if (model) {
                    console.log(model)
                    const option = document.createElement("li");
                    option.innerText = model;
                    option.setAttribute("class", "model-list-item");
                    document.getElementById("model-list").appendChild(option);
                }
            }
        } else {
            console.error("Failed to fetch models:", xhr.statusText);
            updateModel();
        }
    }
    xhr.send();
}