export function createDashboardView() {
    const section = document.createElement("section");
    section.id = "dashboard-view";
    const heading = document.createElement("h1");
    heading.textContent = "Dashboard";
    const description = document.createElement("p");
    description.textContent = "You are logged into the Actioncenter.";
    section.append(heading, description);
    return section;
}
