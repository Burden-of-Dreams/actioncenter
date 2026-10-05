export function createDashboardView() {


    // DETTE ER EN PLACEHOLDER FIL BARE ET ENDPOINT EFTER LOGIN

    const section = document.createElement("section");
    section.id = "dashboard-view";

    const heading = document.createElement("h1");
    heading.textContent = "Dashboard";

    const description = document.createElement("p");
    description.textContent = "You are logged into the Actioncenter.";

    section.append(heading, description);
    return section;
}
