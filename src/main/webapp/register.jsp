<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String registerError = (String) session.getAttribute("registerError");
    if (registerError != null) {
        session.removeAttribute("registerError");
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Register</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/partials/navbar.jsp"/>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Create Account</h1>
    <% if (registerError != null) { %>
    <div class="alert alert-danger"><%= registerError %></div>
    <% } %>
    <form id="registerForm" method="post" action="${pageContext.request.contextPath}/signup" onsubmit="return validateRegisterForm()">
        <div class="mb-3">
            <label class="form-label">Name</label>
            <input class="form-control" type="text" name="name" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" name="email" required>
        </div>
        <div class="mb-3">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" name="password" required minlength="8">
        </div>
        <div class="mb-3">
            <label class="form-label">Confirm Password</label>
            <input class="form-control" type="password" name="confirmPassword" required minlength="8">
        </div>
        <div id="registerClientError" class="alert alert-danger d-none"></div>
        <button class="btn btn-primary w-100" type="submit">Sign Up</button>
    </form>
                </div>
            </div>
        </div>
    </div>
</main>
<script src="${pageContext.request.contextPath}/js/app.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
