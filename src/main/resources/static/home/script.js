function submit() {
    document.getElementById("submit").disabled = true;
    const xhr = new XMLHttpRequest();
    const prompt = encodeURIComponent(document.getElementById("prompt").value);
    const model = encodeURIComponent(document.getElementById("model").value);
    xhr.open("POST", "/submit?prompt=" + prompt + "&model=" + model, true);
    xhr.onload = function () {
        if (xhr.status === 200 && xhr.readyState === 4) {
            document.getElementById("submit").disabled = false;
            const response = JSON.parse(xhr.responseText);
            if (response.type === "error") {
                console.error("Error:", response.message);
                alert("Error: " + response.message);
                return;
            }
            const resultDiv = document.getElementById("response");
            resultDiv.innerHTML = response.message;
        } else {
            document.getElementById("submit").disabled = false;
            console.error("Error:", xhr.statusText);
        }
    }
    xhr.send();
}