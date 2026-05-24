<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="ma.ensah.soutenance.service.VerificationContraintes" %>
<%@ page import="ma.ensah.soutenance.service.VerificationContraintes.AlerteContrainte" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Génération Planning - Gestion PFE</title>
<link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
<style>
/* ── Layout ── */
body { background:#f5f7fb; font-family:'Segoe UI',sans-serif; margin:0; }

.topbar {
    background:linear-gradient(135deg,#1f2937,#111827);
    color:#fff; padding:18px 32px;
    display:flex; align-items:center; justify-content:space-between;
    box-shadow:0 6px 18px rgba(0,0,0,.15);
}
.topbar h1 { font-size:1.3rem; font-weight:700; margin:0; }
.back-btn {
    background:rgba(255,255,255,.12); border:1px solid rgba(255,255,255,.25);
    color:#fff; padding:8px 14px; border-radius:10px;
    text-decoration:none; font-size:.9rem; transition:.3s;
}
.back-btn:hover { background:rgba(255,255,255,.2); }

/* ── Import card ── */
.import-container {
    max-width:650px; margin:70px auto; background:#fff;
    padding:40px; border-radius:16px;
    box-shadow:0 10px 30px rgba(0,0,0,.08); text-align:center;
}
.import-container h2 { font-size:1.6rem; font-weight:700; color:#1f2937; margin-bottom:10px; }
.subtitle { font-size:.9rem; color:#6b7280; margin-bottom:25px; }

input[type="file"] {
    width:100%; padding:14px;
    border:2px dashed #1f2937; border-radius:12px;
    background:#f9fafb; cursor:pointer; transition:.3s;
}
input[type="file"]:hover { background:#eef2f7; }

.btn-import {
    margin-top:20px; width:100%; background:#1f2937; color:#fff;
    border:none; padding:12px; border-radius:10px;
    font-weight:600; font-size:1rem; cursor:pointer; transition:.3s;
}
.btn-import:hover { background:#374151; transform:translateY(-2px); }
.note { margin-top:15px; font-size:.8rem; color:#9ca3af; }

/* ── Alerte modale overlay ── */
#alerteOverlay {
    display:none; position:fixed; inset:0;
    background:rgba(0,0,0,.55); z-index:9999;
    align-items:center; justify-content:center;
}
#alerteOverlay.visible { display:flex; }

#alerteModal {
    background:#fff; border-radius:18px; padding:0;
    width:min(680px,96vw); max-height:88vh;
    box-shadow:0 20px 60px rgba(0,0,0,.25);
    display:flex; flex-direction:column;
    animation:slideIn .25s ease;
}
@keyframes slideIn {
    from { transform:translateY(-30px); opacity:0; }
    to   { transform:translateY(0);     opacity:1; }
}

/* Modal header */
.modal-hdr {
    padding:22px 28px 16px;
    border-bottom:1px solid #f0f0f0;
    display:flex; align-items:center; gap:14px;
}
.modal-hdr-icon {
    width:46px; height:46px; border-radius:12px;
    display:flex; align-items:center; justify-content:center;
    font-size:22px; flex-shrink:0;
}
.modal-hdr-icon.bloquant  { background:#fee2e2; }
.modal-hdr-icon.avert     { background:#fef9c3; }
.modal-hdr h3 { margin:0; font-size:1.2rem; font-weight:700; color:#1f2937; }
.modal-hdr p  { margin:4px 0 0; font-size:.85rem; color:#6b7280; }

/* Modal body */
.modal-body {
    padding:20px 28px; overflow-y:auto; flex:1;
}

.alerte-card {
    display:flex; gap:14px; align-items:flex-start;
    padding:14px 16px; border-radius:12px;
    margin-bottom:12px; border-left:4px solid;
}
.alerte-card.bloquant {
    background:#fff5f5; border-color:#ef4444;
}
.alerte-card.avertissement {
    background:#fffbeb; border-color:#f59e0b;
}

.alerte-icon {
    font-size:20px; margin-top:1px; flex-shrink:0;
}
.alerte-content .alerte-titre {
    font-size:.8rem; font-weight:700;
    text-transform:uppercase; letter-spacing:.5px;
    margin-bottom:4px;
}
.alerte-card.bloquant   .alerte-titre { color:#b91c1c; }
.alerte-card.avertissement .alerte-titre { color:#b45309; }
.alerte-content .alerte-msg {
    font-size:.88rem; color:#374151; line-height:1.5;
}

/* Stats bar */
.stats-bar {
    display:flex; gap:12px; padding:0 28px 16px;
}
.stat-badge {
    flex:1; text-align:center; padding:10px; border-radius:10px;
    font-weight:700; font-size:.9rem;
}
.stat-badge.bloquant   { background:#fee2e2; color:#b91c1c; }
.stat-badge.avert      { background:#fef9c3; color:#b45309; }
.stat-badge span       { display:block; font-size:1.6rem; font-weight:800; }

/* Modal footer */
.modal-ftr {
    padding:16px 28px; border-top:1px solid #f0f0f0;
    display:flex; gap:10px; justify-content:flex-end;
}
.btn-annuler {
    padding:10px 22px; border-radius:10px; font-weight:600; font-size:.92rem;
    background:#f3f4f6; color:#374151; border:1px solid #d1d5db; cursor:pointer;
    transition:.2s;
}
.btn-annuler:hover { background:#e5e7eb; }
.btn-forcer {
    padding:10px 22px; border-radius:10px; font-weight:600; font-size:.92rem;
    background:#f59e0b; color:#fff; border:none; cursor:pointer; transition:.2s;
}
.btn-forcer:hover { background:#d97706; transform:translateY(-1px); }
.btn-bloque {
    padding:10px 22px; border-radius:10px; font-weight:600; font-size:.92rem;
    background:#ef4444; color:#fff; border:none; cursor:not-allowed; opacity:.7;
}

/* ── Success/error banner ── */
.banner {
    max-width:650px; margin:20px auto 0; padding:14px 20px;
    border-radius:12px; display:flex; align-items:center; gap:12px;
    font-size:.9rem; font-weight:600;
}
.banner.error   { background:#fee2e2; color:#b91c1c; border:1px solid #fca5a5; }
.banner.warning { background:#fef9c3; color:#92400e; border:1px solid #fcd34d; }
</style>
</head>
<body>

<%
    List<AlerteContrainte> alertes =
        (List<AlerteContrainte>) request.getAttribute("alertesContraintes");
    Boolean aDesBloquants = (Boolean) request.getAttribute("aDesBloquants");
    boolean hasAlertes   = (alertes != null && !alertes.isEmpty());
    boolean hasBloquants = Boolean.TRUE.equals(aDesBloquants);

    int nbBloquants = 0, nbAvertissements = 0;
    if (hasAlertes) {
        for (AlerteContrainte a : alertes) {
            if (a.isBloquant()) nbBloquants++;
            else nbAvertissements++;
        }
    }
%>

<div class="topbar">
    <h1>Tableau de Bord — Gestion des Soutenances PFE</h1>
    <a href="<%=request.getContextPath()%>/app" class="back-btn">← Accueil</a>
</div>

<!-- Bannière résumé si alertes -->
<% if (hasAlertes) { %>
<div class="banner <%= hasBloquants ? "error" : "warning" %>">
    <span><%= hasBloquants ? "🚫" : "⚠️" %></span>
    <span>
        <%= hasBloquants
            ? nbBloquants + " contrainte(s) bloquante(s) détectée(s) — génération impossible."
            : nbAvertissements + " avertissement(s) — vérifiez avant de continuer." %>
    </span>
    <button onclick="ouvrirModale()" style="margin-left:auto; padding:6px 16px; border-radius:8px;
        background:<%= hasBloquants ? "#b91c1c" : "#b45309" %>;
        color:#fff; border:none; cursor:pointer; font-weight:600;">
        Voir les détails
    </button>
</div>
<% } %>

<!-- Import form -->
<div class="import-container">
    <h2>Génération du Planning des Soutenances</h2>
    <div class="subtitle">
        Importez un fichier Excel contenant les feuilles :
        Étudiant, Professeur, Salle et Config_Creneaux (ou Créneau)
    </div>

    <form id="formImport"
          action="<%=request.getContextPath()%>/app?action=genererPlanning"
          method="post" enctype="multipart/form-data">

        <input type="file" name="fichierExcel" id="fichierExcel" accept=".xlsx" required />

        <!-- Champ caché : force la génération même avec avertissements -->
        <input type="hidden" name="confirmerMalgre" id="confirmerMalgre" value="false" />

        <button type="submit" class="btn-import" id="btnGenerer">
            ▶ Vérifier &amp; Générer le Planning
        </button>
    </form>

    <div class="note">⚠️ Le fichier doit être au format Excel (.xlsx)</div>
</div>

<!-- ══════════════════════════════════════════════════════════
     MODALE D'ALERTES (visible si alertes présentes)
     ══════════════════════════════════════════════════════════ -->
<div id="alerteOverlay" class="<%= hasAlertes ? "visible" : "" %>">
<div id="alerteModal">

    <!-- Header -->
    <div class="modal-hdr">
        <div class="modal-hdr-icon <%= hasBloquants ? "bloquant" : "avert" %>">
            <%= hasBloquants ? "🚫" : "⚠️" %>
        </div>
        <div>
            <h3><%= hasBloquants
                    ? "Contraintes bloquantes détectées"
                    : "Avertissements détectés" %></h3>
            <p><%= hasBloquants
                    ? "La génération ne peut pas démarrer tant que ces problèmes ne sont pas résolus."
                    : "Des risques ont été identifiés. Vous pouvez quand même générer." %></p>
        </div>
    </div>

    <!-- Stats -->
    <% if (hasAlertes) { %>
    <div class="stats-bar">
        <% if (nbBloquants > 0) { %>
        <div class="stat-badge bloquant">
            <span><%=nbBloquants%></span>
            Bloquante<%= nbBloquants>1?"s":"" %>
        </div>
        <% } %>
        <% if (nbAvertissements > 0) { %>
        <div class="stat-badge avert">
            <span><%=nbAvertissements%></span>
            Avertissement<%= nbAvertissements>1?"s":"" %>
        </div>
        <% } %>
    </div>
    <% } %>

    <!-- Alertes list -->
    <div class="modal-body">
        <% if (hasAlertes) {
            for (AlerteContrainte a : alertes) {
                boolean isBloc = a.isBloquant();
        %>
        <div class="alerte-card <%= isBloc ? "bloquant" : "avertissement" %>">
            <div class="alerte-icon"><%= isBloc ? "🔴" : "🟡" %></div>
            <div class="alerte-content">
                <div class="alerte-titre">
                    <%= isBloc ? "Erreur bloquante" : "Avertissement" %>
                    — <%= a.getCode() %>
                </div>
                <div class="alerte-msg"><%=a.getMessage()%></div>
            </div>
        </div>
        <%  }
        } else { %>
        <p style="text-align:center; color:#6b7280; padding:20px 0;">
            ✅ Aucune contrainte détectée.
        </p>
        <% } %>
    </div>

    <!-- Footer -->
    <div class="modal-ftr">
        <button class="btn-annuler" onclick="fermerModale()">
            ← Modifier le fichier
        </button>
        <% if (hasBloquants) { %>
        <button class="btn-bloque" disabled title="Corrigez les erreurs bloquantes d'abord">
            🚫 Génération impossible
        </button>
        <% } else if (hasAlertes) { %>
        <button class="btn-forcer" onclick="forcerGeneration()">
            ⚠️ Générer quand même
        </button>
        <% } %>
    </div>

</div><!-- /alerteModal -->
</div><!-- /alerteOverlay -->

<script>
function ouvrirModale() {
    document.getElementById('alerteOverlay').classList.add('visible');
}
function fermerModale() {
    document.getElementById('alerteOverlay').classList.remove('visible');
}
function forcerGeneration() {
    // L'utilisateur accepte les avertissements → on soumet avec confirmerMalgre=true
    if (!document.getElementById('fichierExcel').files.length) {
        fermerModale();
        alert('Veuillez d\'abord sélectionner le fichier Excel.');
        return;
    }
    document.getElementById('confirmerMalgre').value = 'true';
    document.getElementById('formImport').submit();
}
// Fermer en cliquant sur le fond
document.getElementById('alerteOverlay').addEventListener('click', function(e){
    if (e.target === this) fermerModale();
});
</script>
</body>
</html>
