package com.ecommerce.service;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.impl.ProductDAOImpl;
import com.ecommerce.model.Product;
import com.ecommerce.util.ValidationUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private static final String PRODUCTS_CACHE_KEY = "products:all";
    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<Product> getAllProducts() {
        // String cached = RedisHelper.get(PRODUCTS_CACHE_KEY);
        // if (cached != null) {
        //     Type listType = new TypeToken<List<Product>>() {}.getType();
        //     return new com.google.gson.Gson().fromJson(cached, listType);
        // }
        // List<Product> products = productDAO.findAll();
        // RedisHelper.setex(PRODUCTS_CACHE_KEY, 120, JsonUtil.toJson(products));
        return productDAO.findAll();
    }

    public Optional<Product> getById(long id) {
        return productDAO.findById(id);
    }

    public Product addProduct(String name, String description, String priceRaw, String imageUrl) {
        ValidationUtil.requireNotBlank(name, "Product name");
        ValidationUtil.requireNotBlank(description, "Product description");
        ValidationUtil.validateImageUrl(imageUrl);
        BigDecimal price = new BigDecimal(priceRaw);
        ValidationUtil.validatePrice(price);

        Product product = new Product();
        product.setName(name.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setImageUrl(imageUrl.trim());
        Product created = productDAO.create(product);
        // RedisHelper.del(PRODUCTS_CACHE_KEY);
        return created;
    }

    public void updateProduct(long id, String name, String description, String priceRaw, String imageUrl) {
        ValidationUtil.requireNotBlank(name, "Product name");
        ValidationUtil.requireNotBlank(description, "Product description");
        ValidationUtil.validateImageUrl(imageUrl);
        BigDecimal price = new BigDecimal(priceRaw);
        ValidationUtil.validatePrice(price);

        Product product = new Product();
        product.setId(id);
        product.setName(name.trim());
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setImageUrl(imageUrl.trim());
        productDAO.update(product);
        // RedisHelper.del(PRODUCTS_CACHE_KEY);
    }

    public void deleteProduct(long id) {
        productDAO.deleteById(id);
        // RedisHelper.del(PRODUCTS_CACHE_KEY);
    }
}
