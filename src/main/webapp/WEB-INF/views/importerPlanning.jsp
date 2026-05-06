<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Importer Planning</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">
</head>
<body>
<div class="container" style="margin-top:80px;">
    <h2>📅 Générer le Planning des Soutenances</h2>
    <div class="card p-4 mt-3">
        <form action="<%=request.getContextPath()%>/app?action=genererPlanning" 
              method="post" enctype="multipart/form-data">
            <div class="mb-3">
                <label class="form-label fw-bold">Fichier Excel (.xlsx)</label>
                <input type="file" name="fichierExcel" class="form-control" 
                       accept=".xlsx" required>
                <small class="text-muted">
                    Le fichier doit contenir les feuilles : 
                    Etudiant, Professeur, Salle, Creneau
                </small>
            </div>
            <button type="submit" class="btn btn-primary">
                ▶ Générer le Planning
            </button>
            <a href="<%=request.getContextPath()%>/app" class="btn btn-secondary ms-2">
                Retour
            </a>
        </form>
    </div>
</div>
</body>
</html>