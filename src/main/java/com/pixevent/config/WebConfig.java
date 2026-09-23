package com.pixevent.config;

import com.pixevent.security.AdminAuthInterceptor;
import com.pixevent.security.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    @Value("${app.rate-limit.api:120}")
    private int rateLimitApi;

    @Value("${app.rate-limit.login:8}")
    private int rateLimitLogin;

    @Value("${app.rate-limit.pedido:20}")
    private int rateLimitPedido;

    @Value("${app.rate-limit.scanner:80}")
    private int rateLimitScanner;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor) {
        this.adminAuthInterceptor = adminAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // ── Rate limits por área (equivalentes aos app.use(loginLimiter/pedidoLimiter/...) ──
        registry.addInterceptor(
                new RateLimitInterceptor(rateLimitLogin, 15 * 60_000L, "Muitas tentativas de login. Aguarde alguns minutos."))
                .addPathPatterns("/api/admin/login");

        registry.addInterceptor(
                new RateLimitInterceptor(rateLimitPedido, 60_000L, "Muitas tentativas de pedido. Aguarde um pouco."))
                .addPathPatterns("/api/pedido/**");

        registry.addInterceptor(
                new RateLimitInterceptor(rateLimitScanner, 60_000L, "Muitas validações. Aguarde um pouco."))
                .addPathPatterns("/api/scanner/**");

        registry.addInterceptor(
                new RateLimitInterceptor(rateLimitApi, 60_000L, "Muitas requisições. Tente novamente em instantes."))
                .addPathPatterns("/api/**");

        // ── Autenticação admin (equivalente a router.use(adminAuth) nas rotas protegidas) ──
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/scanner/**", "/api/admin/**")
                .excludePathPatterns("/api/admin/login")
                // GET /api/pedido/buscar e POST /api/pedido/:id/confirmar também exigem admin! ;
                // (tratados dentro do PedidoController pois convivem com rotas públicas no mesmo path!).
                ;
    }
}
