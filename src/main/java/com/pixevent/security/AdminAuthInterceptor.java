package com.pixevent.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (adminAuthService.isAuthorized(request)) return true;

        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().write("{\"erro\":\"Não autorizado ou sessão expirada.\"}");
        return false;
    }
}
