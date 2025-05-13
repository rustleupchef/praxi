window.onload = function() {
    updateModel();
}

function updateModel() {
    const xhr = new XMLHttpRequest();
    xhr.open("POST", "/get-models", true);
    xhr.onload = function() {
        if (xhr.status === 200 && xhr.readyState === 4) {
            const response = xhr.responseText.split("\n");
            for (let i = 0; i < response.length; i++) {
                const model = response[i].trim();
                if (model) {
                    const option = document.createElement("option");
                    option.value = model;
                    option.setAttribute("class", "model-option");
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