<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Add Product</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/navbar.jsp"/>
<main class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-7 col-lg-6">
            <div class="card shadow-sm">
                <div class="card-body p-4">
                    <h1 class="h3 mb-3">Add Product</h1>
                    <form method="post" action="${pageContext.request.contextPath}/products/add">
                        <div class="mb-3">
                            <input class="form-control" type="text" name="name" placeholder="Product Name" required>
                        </div>
                        <div class="mb-3">
                            <textarea class="form-control" name="description" placeholder="Product Description" required></textarea>
                        </div>
                        <div class="mb-3">
                            <input class="form-control" type="number" step="0.01" name="price" placeholder="Price" required>
                        </div>
                        <div class="mb-3">
                            <input class="form-control" type="url" name="imageUrl" placeholder="Image URL" required>
                        </div>
                        <button class="btn btn-primary w-100" type="submit">Add Product</button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
