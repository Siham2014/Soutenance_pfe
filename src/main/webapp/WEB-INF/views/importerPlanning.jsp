<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Génération Planning - Gestion PFE</title>

<link rel="stylesheet" href="<%=request.getContextPath()%>/static/css/bootstrap.min.css">

<style>

body{
    background:#f5f7fb;
    font-family:'Segoe UI', sans-serif;
}

/* container */
.import-container{
    max-width:650px;
    margin:90px auto;
    background:#fff;
    padding:40px;
    border-radius:16px;
    box-shadow:0 10px 30px rgba(0,0,0,0.08);
    text-align:center;
}

/* title */
.import-container h2{
    font-size:1.6rem;
    font-weight:700;
    color:#1f2937;
    margin-bottom:10px;
}

/* subtitle */
.subtitle{
    font-size:.9rem;
    color:#6b7280;
    margin-bottom:25px;
}

/* file input */
input[type="file"]{
    width:100%;
    padding:14px;
    border:2px dashed #1f2937;
    border-radius:12px;
    background:#f9fafb;
    cursor:pointer;
    transition:0.3s;
}

input[type="file"]:hover{
    background:#eef2f7;
}

/* button primary */
.btn-import{
    margin-top:20px;
    width:100%;
    background:#1f2937;
    color:#fff;
    border:none;
    padding:12px;
    border-radius:10px;
    font-weight:600;
    transition:0.3s;
}

.btn-import:hover{
    background:#374151;
    transform:translateY(-2px);
}

/* note */
.note{
    margin-top:15px;
    font-size:.8rem;
    color:#9ca3af;
}

/* icon */
.icon{
    font-size:42px;
    margin-bottom:10px;
}

</style>

</head>

<body>

<div class="import-container">

   

    <h2>Génération du Planning des Soutenances</h2>

    <div class="subtitle">
        Importez un fichier Excel contenant les feuilles : 
        Étudiant, Professeur, Salle et Créneau
    </div>

    <form action="<%=request.getContextPath()%>/app?action=genererPlanning"
          method="post"
          enctype="multipart/form-data">

        <input type="file" name="fichierExcel" accept=".xlsx" required />

        <button type="submit" class="btn-import">
            ▶ Générer le Planning
        </button>

    </form>

    <div class="note">
        ⚠️ Le fichier doit être au format Excel (.xlsx)
    </div>

</div>

</body>
</html>