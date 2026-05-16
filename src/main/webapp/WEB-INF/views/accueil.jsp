<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Gestion PFE - ENSAH</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
    <style>
        body { background-color: #f4f6f9; }
        .card { border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .card-icon { font-size: 3rem; margin-bottom: 10px; }
        .btn-action { width: 100%; padding: 12px; font-size: 16px; border-radius: 8px; }
    </style>
</head>
<body>
<div class="container" style="margin-top: 80px;">

    <div class="text-center mb-5">
        <h1>🎓 Gestion des Soutenances PFE</h1>
        <p class="text-muted">ENSAH — Année Universitaire 2024/2025</p>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success text-center">${message}</div>
    </c:if>

    <!-- Bouton Dashboard -->
    <div class="text-center mb-4">
        <a href="<%=request.getContextPath()%>/app?action=dashboard"
           class="btn btn-dark btn-lg px-5 py-3"
           style="border-radius:12px; background:linear-gradient(135deg,#2d3436,#636e72); border:none; box-shadow:0 4px 14px rgba(0,0,0,.2);">
            📊 Tableau de Bord — Statistiques
        </a>
    </div>

    <div class="row justify-content-center g-4">

        <!-- Répartition encadrants -->
        <div class="col-md-5">
            <div class="card p-4 text-center">
                <div class="card-icon">👥</div>
                <h4>Répartition des Encadrants</h4>
                <p class="text-muted">Importer un fichier Excel et générer la répartition automatique</p>
                <a href="<%=request.getContextPath()%>/app?action=pageImportEncadrants"
                   class="btn btn-danger btn-action mt-2">
                    📂 Importer & Répartir
                </a>
                <a href="<%=request.getContextPath()%>/app?action=voirRepartition"
                   class="btn btn-outline-danger btn-action mt-2">
                    👁 Voir Répartition
                </a>
            </div>
        </div>

        <!-- Planning soutenances -->
        <div class="col-md-5">
            <div class="card p-4 text-center">
                <div class="card-icon">📅</div>
                <h4>Planning des Soutenances</h4>
                <p class="text-muted">Générer automatiquement le planning avec jury et salles</p>
                <a href="<%=request.getContextPath()%>/app?action=importerPlanning"
                   class="btn btn-primary btn-action mt-2">
                    📂 Générer le Planning
                </a>
                <a href="<%=request.getContextPath()%>/app?action=voirPlanning"
                   class="btn btn-outline-primary btn-action mt-2">
                    👁 Voir Planning
                </a>
            </div>
        </div>

    </div>
</div>
</body>
</html>

