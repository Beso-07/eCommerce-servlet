package com.ecommerce.service;

import com.ecommerce.dao.ProductDAO;
import com.ecommerce.dao.impl.ProductDAOImpl;
import com.ecommerce.helper.RedisHelper;
import com.ecommerce.model.Product;
import com.ecommerce.util.JsonUtil;
import com.ecommerce.util.ValidationUtil;
import com.google.gson.reflect.TypeToken;

import java.math.BigDecimal;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private static final String PRODUCTS_CACHE_KEY = "products:all";
    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<Product> getAllProducts() {
        try {
            String cached = RedisHelper.get(PRODUCTS_CACHE_KEY);
            if (cached != null) {
                Type listType = new TypeToken<List<Product>>() {}.getType();
                return new com.google.gson.Gson().fromJson(cached, listType);
            }
        } catch (Exception e) {
            // Redis unavailable, continue with database query
            System.err.println("Redis cache unavailable: " + e.getMessage());
        }
        List<Product> products = productDAO.findAll();
        try {
            RedisHelper.setex(PRODUCTS_CACHE_KEY, 300, JsonUtil.toJson(products));
        } catch (Exception e) {
            // Redis unavailable, continue without caching
            System.err.println("Redis cache set failed: " + e.getMessage());
        }
        return products;
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
        try {
            RedisHelper.del(PRODUCTS_CACHE_KEY);
        } catch (Exception e) {
            // Redis unavailable, continue without cache invalidation
            System.err.println("Redis cache invalidation failed: " + e.getMessage());
        }
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
        try {
            RedisHelper.del(PRODUCTS_CACHE_KEY);
        } catch (Exception e) {
            // Redis unavailable, continue without cache invalidation
            System.err.println("Redis cache invalidation failed: " + e.getMessage());
        }
    }

    public void deleteProduct(long id) {
        productDAO.deleteById(id);
        try {
            RedisHelper.del(PRODUCTS_CACHE_KEY);
        } catch (Exception e) {
            // Redis unavailable, continue without cache invalidation
            System.err.println("Redis cache invalidation failed: " + e.getMessage());
        }
    }
}
