<%@ page import="java.util.List" %>
<%@ page import="com.ecommerce.model.Product" %>
<%@ page import="com.ecommerce.model.User" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    User currentUser = (User) session.getAttribute("user");
    boolean isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
%>
<!DOCTYPE html>
<html>
<head>
    <title>Admin Dashboard</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<jsp:include page="/navbar.jsp"/>
<main class="container py-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h1 class="h3 mb-0">Admin Dashboard</h1>
        <button class="btn btn-primary" type="button" data-bs-toggle="modal" data-bs-target="#addProductModal">Add Product</button>
    </div>

    <div class="modal fade" id="addProductModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h2 class="h5 mb-0">Add Product</h2>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
            <form method="post" action="${pageContext.request.contextPath}/products/add">
                <div class="row g-3">
                    <div class="col-md-6">
                        <input class="form-control" type="text" name="name" placeholder="Product Name" required>
                    </div>
                    <div class="col-md-6">
                        <input class="form-control" type="url" name="imageUrl" placeholder="Image URL" required>
                    </div>
                    <div class="col-md-12">
                        <textarea class="form-control" name="description" placeholder="Product Description" required></textarea>
                    </div>
                    <div class="col-md-4">
                        <input class="form-control" type="number" step="0.01" name="price" placeholder="Price" required>
                    </div>
                </div>
                <button class="btn btn-primary mt-3" type="submit">Save Product</button>
            </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card shadow-sm">
        <div class="card-body">
            <h2 class="h5">All Products</h2>
            <div class="table-responsive">
                <table class="table table-striped align-middle">
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Price</th>
                        <th>Status</th>
                        <th class="text-end">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <% if (products == null || products.isEmpty()) { %>
                    <tr>
                        <td colspan="5" class="text-center text-muted py-4">No products found</td>
                    </tr>
                    <% } else { for (Product product : products) { %>
                    <tr>
                        <td><%= product.getId() %></td>
                        <td><%= product.getName() %></td>
                        <td>$<%= product.getPrice() %></td>
                        <td><span class="badge bg-success">In Stock</span></td>
                        <td class="text-end">
                            <% if (isAdmin) { %>
                            <button class="btn btn-sm btn-outline-secondary me-2" type="button" data-bs-toggle="collapse" data-bs-target="#edit-<%= product.getId() %>">Edit</button>
                            <form class="d-inline" method="post" action="${pageContext.request.contextPath}/products/delete">
                                <input type="hidden" name="id" value="<%= product.getId() %>">
                                <button class="btn btn-sm btn-danger" type="submit">Delete</button>
                            </form>
                            <% } %>
                        </td>
                    </tr>
                    <% if (isAdmin) { %>
                    <tr class="collapse" id="edit-<%= product.getId() %>">
                        <td colspan="5">
                            <form method="post" action="${pageContext.request.contextPath}/products/edit" class="row g-2">
                                <input type="hidden" name="id" value="<%= product.getId() %>">
                                <div class="col-md-3">
                                    <input class="form-control" type="text" name="name" value="<%= product.getName() %>" required>
                                </div>
                                <div class="col-md-3">
                                    <input class="form-control" type="number" step="0.01" name="price" value="<%= product.getPrice() %>" required>
                                </div>
                                <div class="col-md-3">
                                    <input class="form-control" type="url" name="imageUrl" value="<%= product.getImageUrl() %>" required>
                                </div>
                                <div class="col-md-3">
                                    <button class="btn btn-primary w-100" type="submit">Save</button>
                                </div>
                                <div class="col-12">
                                    <input class="form-control" type="text" name="description" value="<%= product.getDescription() %>" required>
                                </div>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                    <% }} %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
