import { createLoginView } from "./views/loginView.js";
import { createDashboardView } from "./views/dashboardView.js";

const app = document.getElementById("app");

function showView(view, title) {
    app.replaceChildren(view);
    document.title = `Actioncenter – ${title}`;
}

showView(createLoginView({
    onLogin: () => showView(createDashboardView(), "Dashboard")
}), "Login");
