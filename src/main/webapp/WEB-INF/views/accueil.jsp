<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Gestion PFE - ENSAH</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <style>
        body {
            background: #f5f7fb;
            font-family: 'Segoe UI', sans-serif;
        }

        /* Navbar look */
        .navbar {
            background: #1f2937;
        }

        .navbar-brand {
            font-weight: 600;
            letter-spacing: 0.5px;
        }

        /* Hero */
        .hero {
            background: linear-gradient(135deg, #1f2937, #374151);
            color: white;
            padding: 60px 20px;
            border-radius: 16px;
            margin-top: 30px;
            text-align: center;
        }

        /* Cards */
        .feature-card {
            border: none;
            border-radius: 16px;
            transition: 0.3s;
            box-shadow: 0 6px 20px rgba(0,0,0,0.05);
            height: 100%;
        }

        .feature-card:hover {
            transform: translateY(-6px);
            box-shadow: 0 10px 30px rgba(0,0,0,0.12);
        }

        .icon-box {
            font-size: 2.5rem;
            margin-bottom: 10px;
        }

        .btn-custom {
            border-radius: 10px;
            padding: 10px;
        }
    </style>
</head>

<body>

<!-- NAVBAR -->
<nav class="navbar navbar-dark px-4">
    <a class="navbar-brand" href="#">
        🎓 Gestion PFE - ENSAH
    </a>
  
</nav>

<div class="container">

    <!-- MESSAGE -->
    <c:if test="${not empty message}">
        <div class="alert alert-success text-center mt-3">
            ${message}
        </div>
    </c:if>

    <!-- HERO -->
    <div class="hero mt-4">
        <h1 class="fw-bold">Système de Gestion des Soutenances</h1>
        <p class="mt-2 text-light">
            Automatisation de la répartition, planning et gestion des PFE
        </p>

        <a href="<%=request.getContextPath()%>/app?action=dashboard"
           class="btn btn-light mt-3 px-4 py-2 btn-custom">
             Accéder au Dashboard
        </a>
    </div>

    <!-- FEATURES -->
    <div class="row mt-5 g-4">

        <!-- Encadrants -->
        <div class="col-md-6">
            <div class="card feature-card p-4 text-center">
                <div class="icon-box text-danger">
                    <i class="bi bi-people-fill"></i>
                </div>

                <h4>Répartition des Encadrants</h4>
                <p class="text-muted">
                    Import Excel et génération automatique de la répartition
                </p>

                <a href="<%=request.getContextPath()%>/app?action=pageImportEncadrants"
                   class="btn btn-danger btn-custom w-100 mb-2">
                     Importer & Répartir
                </a>

                <a href="<%=request.getContextPath()%>/app?action=voirRepartition"
                   class="btn btn-outline-danger btn-custom w-100">
                     Voir Répartition
                </a>
                
            </div>
        </div>

        <!-- Planning -->
        <div class="col-md-6">
            <div class="card feature-card p-4 text-center">
                <div class="icon-box text-primary">
                    <i class="bi bi-calendar-event"></i>
                </div>

                <h4>Planning des Soutenances</h4>
                <p class="text-muted">
                    Génération automatique des jurys, salles et horaires
                </p>

                <a href="<%=request.getContextPath()%>/app?action=importerPlanning"
                   class="btn btn-primary btn-custom w-100 mb-2">
                     Générer Planning
                </a>

                <a href="<%=request.getContextPath()%>/app?action=voirPlanning"
                   class="btn btn-outline-primary btn-custom w-100">
                     Voir Planning
                </a>
                
            </div>
        </div>

    </div>

</div>

</body>
</html>