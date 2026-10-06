"use strict";

import { createLoginView } from "./views/loginView.js";
import { createDashboardView } from "./views/dashboardView.js";

const routes = [
    { path: "/", view: showLogin, title: "Login" },
    { path: "/login", view: showLogin, title: "Login" },
    { path: "/dashboard", view: createDashboardView, title: "Dashboard" }
];

function showLogin() {
    return createLoginView({
        onLogin: () => navigateTo("/dashboard")
    });
}

export function navigateTo(path) {
    if (path !== window.location.pathname) {
        window.history.pushState(null, "", path);
    }
    render();
}

export function startRouter() {
    window.addEventListener("popstate", render);
    render();
}

function render() {
    const app = document.getElementById("app");
    const route = routes.find(route => route.path === window.location.pathname);

    if (!route) {
        window.history.replaceState(null, "", "/login");
        render();
        return;
    }

    app.replaceChildren(route.view());
    document.title = `Actioncenter – ${route.title}`;
}
