import { createLoginView } from "./views/loginView.js";
import { createDashboardView } from "./views/dashboardView.js";

const routes = [
    { path: "/", view: showLogin, title: "Login" },
    { path: "/login", view: showLogin, title: "Login" },

    // DETTE ER EN PLACEHOLDER FIL BARE ET ENDPOINT EFTER LOGIN
    { path: "/dashboard", view: createDashboardView, title: "Dashboard" }
];

function showLogin() {
    return createLoginView({
        onLogin: () => navigate("/dashboard")
    });
}

export function navigate(path) {
    if (path !== window.location.pathname) {
        window.history.pushState(null, "", path);
    }
    render();
}

export function initRouter() {
    window.addEventListener("popstate", render);
    render();
}

function render() {
    const app = document.getElementById("app");
    const route = routes.find(route => route.path === window.location.pathname);

    if (!route) {
        app.textContent = "Page not found.";
        document.title = "Actioncenter – Not found";
        return;
    }

    app.replaceChildren(route.view());
    document.title = `Actioncenter – ${route.title}`;
}
