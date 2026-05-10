package com.ecommerce.dao;

import com.ecommerce.model.User;

import java.util.Optional;

public interface UserDAO {
    Optional<User> findByEmail(String email);
    Optional<User> findById(long id);
    User create(User user);
    void deleteById(long id);
}
