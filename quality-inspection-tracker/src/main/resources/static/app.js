const API_URL = "/api/inspections";

const inspectionForm = document.getElementById("inspectionForm");
const resolveForm = document.getElementById("resolveForm");
const inspectionList = document.getElementById("inspectionList");
const listMessage = document.getElementById("listMessage");
const formMessage = document.getElementById("formMessage");
const resolveMessage = document.getElementById("resolveMessage");
const resolveModal = document.getElementById("resolveModal");
const resolveContext = document.getElementById("resolveContext");

let inspectionToResolve = null;

document.addEventListener("DOMContentLoaded", () => {
    setDefaultDate();
    loadDashboard();

    inspectionForm.addEventListener("submit", createInspection);
    resolveForm.addEventListener("submit", resolveInspection);

    document.getElementById("refreshButton").addEventListener("click", loadDashboard);

    document.getElementById("focusFormButton").addEventListener("click", () => {
        document.getElementById("newInspection").scrollIntoView({ behavior: "smooth" });
        document.getElementById("machineLineId").focus();
    });

    document.getElementById("closeModalButton").addEventListener("click", closeResolveModal);
    document.getElementById("cancelResolveButton").addEventListener("click", closeResolveModal);

    ["filterSeverity", "filterStatus", "filterFrom", "filterTo", "sortBy", "sortDir"]
        .forEach(id => document.getElementById(id).addEventListener("change", loadInspections));

    resolveModal.addEventListener("click", event => {
        if (event.target === resolveModal) closeResolveModal();
    });
});

function setDefaultDate() {
    document.getElementById("inspectionDate").value = new Date().toISOString().split("T")[0];
}

async function loadDashboard() {
    clearMessage(listMessage);
    await Promise.all([loadSummary(), loadInspections()]);
}

async function loadSummary() {
    try {
        const response = await fetch(`${API_URL}/summary`);
        if (!response.ok) throw new Error();

        const summary = await response.json();

        updateSummaryCard("critical", summary.critical);
        updateSummaryCard("major", summary.major);
        updateSummaryCard("minor", summary.minor);
    } catch {
        showMessage(listMessage, "Could not load the inspection summary.", true);
    }
}

function updateSummaryCard(severity, values) {
    document.getElementById(`${severity}Open`).textContent = values.open;
    document.getElementById(`${severity}Resolved`).textContent = values.resolved;
}

async function loadInspections() {
    const params = new URLSearchParams();

    const filters = {
        severity: "filterSeverity",
        status: "filterStatus",
        from: "filterFrom",
        to: "filterTo",
        sortBy: "sortBy",
        sortDir: "sortDir"
    };

    Object.entries(filters).forEach(([param, id]) => {
        const value = document.getElementById(id).value;
        if (value) params.set(param, value);
    });

    try {
        const response = await fetch(`${API_URL}?${params}`);
        const data = await response.json();

        if (!response.ok) throw new Error(getErrorMessage(data));

        renderInspections(data);
    } catch (error) {
        inspectionList.innerHTML = "";
        document.getElementById("recordCount").textContent = "0 records";
        showMessage(listMessage, error.message, true);
    }
}

async function createInspection(event) {
    event.preventDefault();
    clearMessage(formMessage);

    const saveButton = document.getElementById("saveButton");
    saveButton.disabled = true;

    const payload = {
        inspectionDate: document.getElementById("inspectionDate").value,
        machineLineId: document.getElementById("machineLineId").value.trim(),
        defectType: document.getElementById("defectType").value,
        severity: document.getElementById("severity").value,
        remarks: document.getElementById("remarks").value.trim() || null
    };

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        const data = await response.json();
        if (!response.ok) throw new Error(getErrorMessage(data));

        inspectionForm.reset();
        setDefaultDate();
        showMessage(formMessage, "Inspection saved.", false);
        await loadDashboard();
    } catch (error) {
        showMessage(formMessage, error.message, true);
    } finally {
        saveButton.disabled = false;
    }
}

function renderInspections(inspections) {
    document.getElementById("recordCount").textContent =
        `${inspections.length} ${inspections.length === 1 ? "record" : "records"}`;

    if (!inspections.length) {
        inspectionList.innerHTML = `<div class="empty-state">No inspections match the selected filters.</div>`;
        return;
    }

    inspectionList.innerHTML = inspections.map(inspection => `
        <article class="inspection-card">
            <div class="inspection-top">
                <div>
                    <h3 class="inspection-title">${escapeHtml(inspection.machineLineId)}</h3>
                    <p class="inspection-date">${formatDate(inspection.inspectionDate)}</p>
                </div>
                <div class="badges">
                    <span class="badge badge-${inspection.severity.toLowerCase()}">
                        ${formatLabel(inspection.severity)}
                    </span>
                    <span class="badge badge-${inspection.status.toLowerCase()}">
                        ${formatLabel(inspection.status)}
                    </span>
                </div>
            </div>

            <div class="inspection-meta">
                <div class="meta-item">
                    <span>Defect</span>
                    <strong>${formatDefectType(inspection.defectType)}</strong>
                </div>
                <div class="meta-item">
                    <span>Inspection date</span>
                    <strong>${formatDate(inspection.inspectionDate)}</strong>
                </div>
            </div>

            ${inspection.remarks
                ? `<p class="inspection-remarks"><strong>Remarks:</strong> ${escapeHtml(inspection.remarks)}</p>`
                : ""}

            ${inspection.resolutionNote
                ? `<p class="resolution-note"><strong>Resolution:</strong> ${escapeHtml(inspection.resolutionNote)}</p>`
                : ""}

            ${inspection.status === "OPEN"
                ? `<button class="resolve-button" type="button"
                           data-id="${inspection.id}"
                           data-machine="${escapeHtml(inspection.machineLineId)}"
                           data-defect="${escapeHtml(formatDefectType(inspection.defectType))}">
                       Mark resolved
                   </button>`
                : ""}
        </article>
    `).join("");

    inspectionList.querySelectorAll(".resolve-button").forEach(button => {
        button.addEventListener("click", () => openResolveModal(
            Number(button.dataset.id),
            button.dataset.machine,
            button.dataset.defect
        ));
    });
}

function openResolveModal(id, machineLineId, defectType) {
    inspectionToResolve = id;
    resolveContext.textContent = `${machineLineId} · ${defectType}`;
    document.getElementById("resolutionNote").value = "";
    clearMessage(resolveMessage);
    resolveModal.classList.remove("hidden");
    document.getElementById("resolutionNote").focus();
}

function closeResolveModal() {
    resolveModal.classList.add("hidden");
    inspectionToResolve = null;
}

async function resolveInspection(event) {
    event.preventDefault();
    if (!inspectionToResolve) return;

    clearMessage(resolveMessage);

    const button = document.getElementById("resolveButton");
    button.disabled = true;

    try {
        const response = await fetch(`${API_URL}/${inspectionToResolve}/resolve`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                resolutionNote: document.getElementById("resolutionNote").value.trim()
            })
        });

        const data = await response.json();
        if (!response.ok) throw new Error(getErrorMessage(data));

        closeResolveModal();
        await loadDashboard();
    } catch (error) {
        showMessage(resolveMessage, error.message, true);
    } finally {
        button.disabled = false;
    }
}

function formatLabel(value) {
    return value.charAt(0) + value.slice(1).toLowerCase();
}

function formatDefectType(value) {
    return {
        WEAVE_DEFECT: "Weave Defect",
        SHADE_VARIATION: "Shade Variation",
        HOLE_TEAR: "Hole/Tear",
        COUNT_DEVIATION: "Count Deviation",
        OTHER: "Other"
    }[value] || value;
}

function formatDate(value) {
    if (!value) return "-";
    return new Intl.DateTimeFormat("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    }).format(new Date(`${value}T00:00:00`));
}

function getErrorMessage(data) {
    if (data?.errors) {
        const firstError = Object.values(data.errors)[0];
        if (firstError) return firstError;
    }
    return data?.message || "Something went wrong. Please try again.";
}

function showMessage(element, message, isError) {
    element.textContent = message;
    element.classList.toggle("error", isError);
    element.classList.toggle("success", !isError);
}

function clearMessage(element) {
    element.textContent = "";
    element.classList.remove("error", "success");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
