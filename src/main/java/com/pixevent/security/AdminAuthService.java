package com.pixevent.security;

import com.pixevent.exception.ApiException;
import com.pixevent.util.AuthTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AdminAuthService {

    private final AuthTokenUtil authTokenUtil;

    @Value("${app.admin.allow-legacy-secret:false}")
    private boolean allowLegacySecret;

    @Value("${app.admin.secret:}")
    private String adminSecret;

    public AdminAuthService(AuthTokenUtil authTokenUtil) {
        this.authTokenUtil = authTokenUtil;
    }

    public boolean isAuthorized(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;

        if (token != null) {
            Map<String, Object> payload = authTokenUtil.verifyToken(token);
            if (payload != null) return true;
        }

        if (allowLegacySecret) {
            String legacySecret = request.getHeader("x-admin-secret");
            if (legacySecret != null && adminSecret != null && !adminSecret.isBlank()
                    && authTokenUtil.safeEqual(legacySecret, adminSecret)) {
                return true;
            }
        }
        return false;
    }

    /** Lança 401 se não estiver autenticado como admin — use nas rotas que precisam checar na mão. */
    public void requireAdmin(HttpServletRequest request) {
        if (!isAuthorized(request)) {
            throw ApiException.unauthorized("Não autorizado ou sessão expirada.");
        }
    }
}
