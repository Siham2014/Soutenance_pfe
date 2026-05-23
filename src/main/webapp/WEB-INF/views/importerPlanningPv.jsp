<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Générer PV depuis Planning - Gestion PFE</title>

<link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

<style>
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
    color: #fff;
}
body {
    background: #f5f7fb;
    font-family: 'Segoe UI', sans-serif;
}
.import-container {
    max-width: 650px;
    margin: 80px auto;
    background: #fff;
    padding: 40px;
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.08);
    text-align: center;
}
.import-container h2 {
    font-size: 1.6rem;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 10px;
}
.subtitle {
    font-size: .9rem;
    color: #6b7280;
    margin-bottom: 25px;
}
.icon-area {
    font-size: 48px;
    color: #16a34a;
    margin-bottom: 12px;
}
input[type="file"] {
    width: 100%;
    padding: 14px;
    border: 2px dashed #16a34a;
    border-radius: 12px;
    background: #f0fdf4;
    cursor: pointer;
    transition: 0.3s;
}
input[type="file"]:hover {
    background: #dcfce7;
}
.btn-import {
    margin-top: 20px;
    width: 100%;
    background: #16a34a;
    color: #fff;
    border: none;
    padding: 12px;
    border-radius: 10px;
    font-weight: 600;
    font-size: 1rem;
    transition: 0.3s;
    cursor: pointer;
}
.btn-import:hover {
    background: #15803d;
    transform: translateY(-2px);
}
.note {
    margin-top: 15px;
    font-size: .82rem;
    color: #9ca3af;
}
.info-box {
    background: #f0fdf4;
    border: 1px solid #bbf7d0;
    border-radius: 10px;
    padding: 12px 16px;
    margin-bottom: 22px;
    text-align: left;
    font-size: .88rem;
    color: #166534;
}
.info-box ul {
    margin: 6px 0 0 0;
    padding-left: 18px;
}
.alert-erreur {
    background: #fef2f2;
    border: 1px solid #fecaca;
    color: #991b1b;
    border-radius: 10px;
    padding: 12px 16px;
    margin-bottom: 18px;
    font-size: .9rem;
}
</style>
</head>

<body>

<div class="topbar">
    <h1>🎓 Tableau de Bord — Gestion des Soutenances PFE</h1>
    <a href="<%=request.getContextPath()%>/app" class="back-btn">← Accueil</a>
</div>

<div class="import-container">

    <div class="icon-area">
        <i class="bi bi-file-earmark-arrow-up-fill"></i>
    </div>

    <h2>Générer les PV depuis un Planning</h2>

    <div class="subtitle">
        Importez le fichier Excel du planning exporté pour générer automatiquement les PV de soutenance.
    </div>

    <c:if test="${not empty erreur}">
        <div class="alert-erreur">
            <i class="bi bi-exclamation-triangle-fill"></i> ${erreur}
        </div>
    </c:if>

    <div class="info-box">
        <strong><i class="bi bi-info-circle-fill"></i> Format attendu du fichier Excel :</strong>
        <ul>
            <li>Feuille : <strong>Planning Soutenances</strong></li>
            <li>Colonnes (ligne d'en-tête à la ligne 3) :
                Date, Heure Début, Heure Fin, Étudiant, Filière, Salle, Encadrant, Jury Info, Jury Math
            </li>
            <li>Ce fichier est généré par l'export Excel du planning.</li>
        </ul>
    </div>

    <form action="<%=request.getContextPath()%>/app?action=importerPlanningEtGenererPv"
          method="post"
          enctype="multipart/form-data">

        <input type="file" name="fichierPlanningExcel" accept=".xlsx" required />

        <button type="submit" class="btn-import">
            <i class="bi bi-play-fill"></i> Importer et Accéder aux PV
        </button>

    </form>

    <div class="note">
        ⚠️ Le fichier doit être au format Excel (.xlsx) exporté depuis le planning.
    </div>

</div>

</body>
</html>
