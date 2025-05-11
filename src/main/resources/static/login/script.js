function login () {
    const xhr = new XMLHttpRequest();
    const password = encodeURIComponent(document.getElementById("password").value);
    xhr.open("POST", "/login?password=" + password, true);
    xhr.onload = function () {
        if (xhr.status === 200 && xhr.readyState === 4) {
            const response = JSON.parse(xhr.responseText);
            if (response.message === "success") {
                window.location.href = "/home";
            } else {
                alert("Login failed: " + response.message);
            }
        } else {
            console.error("Error:", xhr.statusText);
        }
    }
    xhr.send();
}