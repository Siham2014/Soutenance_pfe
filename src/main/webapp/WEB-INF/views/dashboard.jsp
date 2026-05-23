<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Tableau de Bord — Gestion PFE</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
       body {
    background: #f4f6f9;
    font-family: 'Segoe UI', sans-serif;
}

/* TOPBAR MODERNE */
.topbar {
    background: linear-gradient(135deg, #1f2937, #111827);
    color: #fff;
    padding: 18px 32px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-shadow: 0 6px 18px rgba(0,0,0,.15);
}

.topbar h1 {
    font-size: 1.3rem;
    font-weight: 700;
    margin: 0;
}

.back-btn {
    background: rgba(255,255,255,.12);
    border: 1px solid rgba(255,255,255,.25);
    color: #fff;
    padding: 8px 14px;
    border-radius: 10px;
    text-decoration: none;
    font-size: .9rem;
    transition: 0.3s;
}

.back-btn:hover {
    background: rgba(255,255,255,.2);
}

/* KPI SECTION */
.kpi-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
    gap: 16px;
    padding: 25px;
}

.kpi-card {
    background: #fff;
    border-radius: 14px;
    padding: 20px;
    text-align: center;
    box-shadow: 0 8px 25px rgba(0,0,0,.06);
    transition: 0.3s;
}

.kpi-card:hover {
    transform: translateY(-4px);
}

.kpi-value {
    font-size: 2.2rem;
    font-weight: 800;
    color: #111827;
}

.kpi-label {
    font-size: .85rem;
    color: #6b7280;
}

/* CHART CARDS */
.charts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(420px, 1fr));
    gap: 18px;
    padding: 0 25px 25px;
}

.chart-card {
    background: #fff;
    border-radius: 14px;
    padding: 18px;
    box-shadow: 0 8px 25px rgba(0,0,0,.06);
    transition: 0.3s;
}

.chart-card:hover {
    transform: translateY(-3px);
}

.chart-title {
    font-size: 1rem;
    font-weight: 700;
    color: #111827;
}

.chart-subtitle {
    font-size: .8rem;
    color: #9ca3af;
    margin-bottom: 10px;
}

/* TABLE */
.stat-table th {
    background: #f3f4f6;
    color: #374151;
}

.stat-table td {
    color: #111827;
}

.badge-count {
    background: #eef2ff;
    color: #3730a3;
    padding: 4px 10px;
    border-radius: 999px;
    font-weight: 700;
}

/* PROGRESS */
.prog-bar-wrap {
    background: #e5e7eb;
    height: 10px;
    border-radius: 999px;
}

.prog-bar {
    height: 100%;
    border-radius: 999px;
}
    </style>
</head>
<body>

<%-- ── Topbar ─────────────────────────────────────────── --%>
<div class="topbar">
    <h1> Tableau de Bord — Gestion des Soutenances PFE</h1>
    <a href="<%=request.getContextPath()%>/app" class="back-btn">← Accueil</a>
</div>

<%-- ── KPI Cards ───────────────────────────────────────── --%>
<div class="kpi-grid">
    <div class="kpi-card" style="--kpi-color:var(--primary)">
        <div class="kpi-icon"></div>
        <div class="kpi-value">${statsGlobales.totalEtudiants != null ? statsGlobales.totalEtudiants : 0}</div>
        <div class="kpi-label">Étudiants</div>
    </div>
    <div class="kpi-card" style="--kpi-color:var(--accent)">
        <div class="kpi-icon"></div>
        <div class="kpi-value">${statsGlobales.totalProfesseurs != null ? statsGlobales.totalProfesseurs : 0}</div>
        <div class="kpi-label">Professeurs</div>
    </div>
    <div class="kpi-card" style="--kpi-color:var(--green)">
        <div class="kpi-icon"></div>
        <div class="kpi-value">${statsGlobales.totalSoutenances != null ? statsGlobales.totalSoutenances : 0}</div>
        <div class="kpi-label">Soutenances planifiées</div>
    </div>
    <div class="kpi-card" style="--kpi-color:var(--orange)">
        <div class="kpi-icon"></div>
        <div class="kpi-value">${statsGlobales.totalSalles != null ? statsGlobales.totalSalles : 0}</div>
        <div class="kpi-label">Salles disponibles</div>
    </div>
    <div class="kpi-card" style="--kpi-color:var(--purple)">
        <div class="kpi-icon"></div>
        <div class="kpi-value">${fn:length(nbSoutenancesParFiliere)}</div>
        <div class="kpi-label">Filières</div>
    </div>
</div>

<%-- ── Charts Grid ──────────────────────────────────────── --%>
<div class="charts-grid">

    <%-- 1. Étudiants encadrés par professeur --%>
    <div class="chart-card">
        <div class="chart-title"> Étudiants encadrés par professeur</div>
        <div class="chart-subtitle">Répartition des groupes PFE</div>
        <c:choose>
            <c:when test="${not empty nbEtudiantsParEncadrant}">
                <div class="chart-wrap">
                    <canvas id="chartEtudiantsProf"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune répartition effectuée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 2. Soutenances par filière --%>
    <div class="chart-card">
        <div class="chart-title"> Soutenances par filière</div>
        <div class="chart-subtitle">Nombre de soutenances planifiées par filière</div>
        <c:choose>
            <c:when test="${not empty nbSoutenancesParFiliere}">
                <div class="chart-wrap">
                    <canvas id="chartSoutenancesFiliere"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune soutenance générée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 3. Soutenances par professeur (encadrant) --%>
    <div class="chart-card full-width">
        <div class="chart-title"> Charge par professeur — Soutenances encadrées</div>
        <div class="chart-subtitle">Nombre de soutenances où le professeur est encadrant principal</div>
        <c:choose>
            <c:when test="${not empty nbSoutenancesParProf}">
                <div class="chart-wrap tall">
                    <canvas id="chartSoutenancesProf"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune soutenance générée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 4. Soutenances par date (timeline) --%>
    <div class="chart-card full-width">
        <div class="chart-title"> Calendrier des soutenances</div>
        <div class="chart-subtitle">Nombre de soutenances par journée</div>
        <c:choose>
            <c:when test="${not empty nbSoutenancesParJour}">
                <div class="chart-wrap">
                    <canvas id="chartSoutenancesDate"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune soutenance planifiée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 5. Étudiants par filière (doughnut) --%>
    <div class="chart-card">
        <div class="chart-title"> Répartition des étudiants par filière</div>
        <div class="chart-subtitle">Proportion par filière</div>
        <c:choose>
            <c:when test="${not empty nbEtudiantsParFiliere}">
                <div class="chart-wrap">
                    <canvas id="chartEtudiantsFiliere"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucun étudiant enregistré</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 6. Spécialités des professeurs (pie) --%>
    <div class="chart-card">
        <div class="chart-title"> Spécialités des professeurs</div>
        <div class="chart-subtitle">Répartition Informatique / Mathématiques</div>
        <c:choose>
            <c:when test="${not empty specialitesProf}">
                <div class="chart-wrap">
                    <canvas id="chartSpecialites"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucun professeur enregistré</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 7. Charge jury par professeur (barre horizontale) --%>
    <div class="chart-card full-width">
        <div class="chart-title"> Charge jury par professeur</div>
        <div class="chart-subtitle">Nombre de participations en tant que membre du jury (membreInfo + membreMath)</div>
        <c:choose>
            <c:when test="${not empty chargeJuryParProf}">
                <div style="max-height:340px; overflow-y:auto; padding-right:4px;">
                    <c:set var="maxJury" value="0"/>
                    <c:forEach var="entry" items="${chargeJuryParProf}">
                        <c:if test="${entry.value > maxJury}"><c:set var="maxJury" value="${entry.value}"/></c:if>
                    </c:forEach>
                    <c:forEach var="entry" items="${chargeJuryParProf}">
                        <div class="prog-row">
                            <span class="prog-label" title="${entry.key}">${entry.key}</span>
                            <div class="prog-bar-wrap">
                                <div class="prog-bar"
                                     style="width:${maxJury > 0 ? (entry.value * 100 / maxJury) : 0}%;
                                            background:linear-gradient(90deg,#8e44ad,#9b59b6);">
                                </div>
                            </div>
                            <span class="prog-val">${entry.value}</span>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune soutenance générée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 8. Soutenances par salle --%>
    <div class="chart-card">
        <div class="chart-title"> Utilisation des salles</div>
        <div class="chart-subtitle">Nombre de soutenances par salle</div>
        <c:choose>
            <c:when test="${not empty nbSoutenancesParSalle}">
                <div class="chart-wrap">
                    <canvas id="chartSalles"></canvas>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-data">Aucune soutenance générée</div>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- 9. Tableau récapitulatif des profs --%>
    <div class="chart-card">
        <div class="chart-title"> Récapitulatif — Charge des professeurs</div>
        <div class="chart-subtitle">Vue combinée : étudiants encadrés + soutenances</div>
        <div style="max-height:320px;overflow-y:auto;">
            <c:choose>
                <c:when test="${not empty nbEtudiantsParEncadrant}">
                    <table class="stat-table">
                        <thead>
                            <tr>
                                <th>Professeur</th>
                                <th style="text-align:center">Étudiants</th>
                                <th style="text-align:center">Soutenances</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="entry" items="${nbEtudiantsParEncadrant}">
                                <tr>
                                    <td>${entry.key}</td>
                                    <td style="text-align:center">
                                        <span class="badge-count">${entry.value}</span>
                                    </td>
                                    <td style="text-align:center">
                                        <span class="badge-count" style="background:#e8f4fb;color:var(--accent)">
                                            ${nbSoutenancesParProf[entry.key] != null
                                              ? nbSoutenancesParProf[entry.key] : 0}
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="no-data">Aucune répartition effectuée</div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

</div>

<script>
// ── Palette couleurs ────────────────────────────────────
const COLORS = [
    '#c0392b','#2980b9','#27ae60','#e67e22','#8e44ad',
    '#16a085','#d35400','#2c3e50','#f39c12','#1abc9c',
    '#e74c3c','#3498db','#2ecc71','#e67e22','#9b59b6'
];
function palette(n){
    return Array.from({length:n}, (_,i) => COLORS[i % COLORS.length]);
}

// Helper pour les graphiques
function barChart(id, labels, data, color, label) {
    const ctx = document.getElementById(id);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'bar',
        data: { 
            labels: labels, 
            datasets: [{ 
                label: label || 'Valeur', 
                data: data, 
                backgroundColor: color, 
                borderRadius: 6, 
                borderSkipped: false 
            }] 
        },
        options: {
            responsive: true, 
            maintainAspectRatio: false,
            plugins: { 
                legend: { display: false },
                tooltip: { callbacks: { label: function(c) { return ' ' + c.parsed.y; } } }
            },
            scales: {
                x: { ticks: { maxRotation: 45, font: { size: 11 } }, grid: { display: false } },
                y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f0f0f0' } }
            }
        }
    });
}

function hBarChart(id, labels, data, colors) {
    const ctx = document.getElementById(id);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'bar',
        data: { labels: labels, datasets: [{ data: data, backgroundColor: colors, borderRadius: 4 }] },
        options: {
            responsive: true, maintainAspectRatio: false, indexAxis: 'y',
            scales: { x: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f0f0f0' } },
                      y: { ticks: { font: { size: 11 } }, grid: { display: false } } }
        }
    });
}

function doughnutChart(id, labels, data) {
    const ctx = document.getElementById(id);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'doughnut',
        data: { labels: labels, datasets: [{ data: data, backgroundColor: palette(data.length), borderWidth: 2, borderColor: '#fff' }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom', labels: { padding: 14, font: { size: 11 } } },
                       tooltip: { callbacks: { label: function(c) { return c.label + ': ' + c.parsed; } } } }
        }
    });
}

function pieChart(id, labels, data) {
    const ctx = document.getElementById(id);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'pie',
        data: { labels: labels, datasets: [{ data: data, backgroundColor: palette(data.length), borderWidth: 2, borderColor: '#fff' }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { position: 'bottom', labels: { padding: 14, font: { size: 11 } } },
                       tooltip: { callbacks: { label: function(c) { return c.label + ': ' + c.parsed; } } } }
        }
    });
}

function lineChart(id, labels, data) {
    const ctx = document.getElementById(id);
    if (!ctx) return;
    new Chart(ctx, {
        type: 'line',
        data: { labels: labels, datasets: [{ label: 'Soutenances', data: data,
            borderColor: '#27ae60', backgroundColor: 'rgba(39,174,96,.12)',
            borderWidth: 2.5, pointRadius: 5, pointBackgroundColor: '#27ae60',
            tension: 0.35, fill: true }] },
        options: {
            responsive: true, maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { x: { grid: { display: false } }, y: { beginAtZero: true, ticks: { stepSize: 1 }, grid: { color: '#f0f0f0' } } }
        }
    });
}

// ── Initialisation des graphiques avec données JSP ──
document.addEventListener('DOMContentLoaded', function() {
    
    <c:if test="${not empty nbEtudiantsParEncadrant}">
    barChart('chartEtudiantsProf',
        [<c:forEach var="e" items="${nbEtudiantsParEncadrant}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbEtudiantsParEncadrant}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>],
        palette(${fn:length(nbEtudiantsParEncadrant)}),
        'Étudiants encadrés'
    );
    </c:if>

    <c:if test="${not empty nbSoutenancesParFiliere}">
    barChart('chartSoutenancesFiliere',
        [<c:forEach var="e" items="${nbSoutenancesParFiliere}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbSoutenancesParFiliere}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>],
        '#2980b9',
        'Soutenances'
    );
    </c:if>

    <c:if test="${not empty nbSoutenancesParProf}">
    hBarChart('chartSoutenancesProf',
        [<c:forEach var="e" items="${nbSoutenancesParProf}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbSoutenancesParProf}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>],
        palette(${fn:length(nbSoutenancesParProf)})
    );
    </c:if>

    <c:if test="${not empty nbSoutenancesParJour}">
    lineChart('chartSoutenancesDate',
        [<c:forEach var="e" items="${nbSoutenancesParJour}" varStatus="s">'${e.key}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbSoutenancesParJour}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>]
    );
    </c:if>

    <c:if test="${not empty nbEtudiantsParFiliere}">
    doughnutChart('chartEtudiantsFiliere',
        [<c:forEach var="e" items="${nbEtudiantsParFiliere}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbEtudiantsParFiliere}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>]
    );
    </c:if>

    <c:if test="${not empty specialitesProf}">
    pieChart('chartSpecialites',
        [<c:forEach var="e" items="${specialitesProf}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${specialitesProf}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>]
    );
    </c:if>

    <c:if test="${not empty nbSoutenancesParSalle}">
    barChart('chartSalles',
        [<c:forEach var="e" items="${nbSoutenancesParSalle}" varStatus="s">'${fn:replace(e.key, "'", "\\'")}'<c:if test="${!s.last}">,</c:if></c:forEach>],
        [<c:forEach var="e" items="${nbSoutenancesParSalle}" varStatus="s">${e.value}<c:if test="${!s.last}">,</c:if></c:forEach>],
        '#e67e22',
        'Soutenances'
    );
    </c:if>
    
});
</script>

</body>
</html>