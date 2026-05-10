package com.ecommerce.service;

import com.ecommerce.dao.UserDAO;
import com.ecommerce.dao.impl.UserDAOImpl;
import com.ecommerce.exception.UnauthorizedException;
import com.ecommerce.exception.ValidationException;
import com.ecommerce.helper.JwtHelper;
import com.ecommerce.helper.PasswordHelper;
import com.ecommerce.model.User;
import com.ecommerce.util.ValidationUtil;

import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private final UserDAO userDAO = new UserDAOImpl();

    public User register(String name, String email, String password, String confirmPassword) {
        ValidationUtil.requireNotBlank(name, "Name");
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
        if (userDAO.findByEmail(email).isPresent()) {
            throw new ValidationException("Email is already used");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(PasswordHelper.hashPassword(password));
        user.setRole("USER");
        return userDAO.create(user);
    }

    public Map<String, Object> login(String email, String password) {
        ValidationUtil.validateEmail(email);
        ValidationUtil.validatePassword(password);

        User user = userDAO.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!PasswordHelper.verifyPassword(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = JwtHelper.generateToken(user.getId(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);
        return result;
    }
}
