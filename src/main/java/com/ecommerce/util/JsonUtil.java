package com.ecommerce.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder().create();

    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static void writeJson(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(toJson(payload));
    }
}
