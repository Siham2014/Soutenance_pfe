<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>File Upload</title>
</head>
<body>
<h2> File Upload</h2>
<form method="post" action="${pageContext.request.contextPath}/app?action=uploadExcel" enctype="multipart/form-data">
Selectionner fichier : <input type="file" name="file" size="60"/><br/>
 <input type="submit" value="Upload"/>
</form>

</body>
</html>