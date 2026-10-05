const loginView = document.getElementById("login-view");
const dashboardView = document.getElementById("dashboard-view");
const form = document.getElementById("login-form");
const button = document.getElementById("login-button");
const message = document.getElementById("login-message");

function showDashboard() {
    loginView.hidden = true;
    dashboardView.hidden = false;
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    button.disabled = true;
    message.textContent = "Logging in...";

    const credentials = {
        username: document.getElementById("username").value,
        password: document.getElementById("password").value
    };

    try {
        const response = await fetch("/api/user/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "same-origin",
            body: JSON.stringify(credentials)
        });

        if (response.status === 204) {
            form.reset();
            message.textContent = "";
            showDashboard();
        } else if (response.status === 401) {
            message.textContent = "Invalid username or password.";
        } else {
            message.textContent = "Login failed. Please try again.";
        }
    } catch (error) {
        message.textContent = "Could not connect to the server.";
    } finally {
        button.disabled = false;
    }
});