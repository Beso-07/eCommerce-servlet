<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>404 - Not Found</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="navbar.jsp"/>
<main class="container py-5">
    <div class="alert alert-warning shadow-sm">
        <h1 class="h4 mb-2">404 - Page Not Found</h1>
        <p class="mb-0">The page you requested does not exist.</p>
    </div>
    <a class="btn btn-primary" href="${pageContext.request.contextPath}/products">Back to Home</a>
</main>
</body>
</html>
