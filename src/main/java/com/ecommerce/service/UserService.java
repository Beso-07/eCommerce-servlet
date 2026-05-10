package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.impl.UserDAOImpl;
import com.ecommerce.exception.UserNotFoundException;
import com.ecommerce.model.User;

public class UserService {
    private final UserDAO userDAO = new UserDAOImpl();

    public User getById(long userId) {
        return userDAO.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void deleteById(long userId) {
        userDAO.deleteById(userId);
    }
}
