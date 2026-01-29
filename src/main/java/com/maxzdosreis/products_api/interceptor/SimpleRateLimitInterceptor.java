package com.maxzdosreis.products_api.interceptor;

import com.maxzdosreis.products_api.service.SimpleRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class SimpleRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRateLimitInterceptor.class);

    @Autowired
    private SimpleRateLimitService rateLimitService;

    // Método executado ANTES da requisição chegar no controller
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Extrai informações da requisição
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);
        String method = request.getMethod();

        logger.debug("Verificando rate limit para {} {} de IP {}", method, path, clientIp);

        // Verifica o rate limit específico para este endpoint
        boolean allowed = checkRateLimit(path, clientIp);

        // Se não foi permitido, retorna erro 429
        if(!allowed) {
            logger.warn("Rate limit EXCEDIDO para IP {} no endpoint {}", clientIp, path);

            // Configura resposta de erro
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorJson = String.format(
                "{" +
                "   \"timestamp\": \"%s\"," +
                "   \"status\": 429," +
                "   \"error\": \"Too Many Requests\"," +
                "   \"message\": \"Você excedeu o limite de requisições. Tente novamente mais tarde.\"," +
                "   \"path\": \"%s\"" +
                "}",
                LocalDateTime.now().toString(),
                path
            );

            response.getWriter().write(errorJson);

            return false;
        }

        logger.debug("Rate limit OK para IP {} no endpoint {}", clientIp, path);
        return true;
    }

    // Verifica rate limit baseado no path da requisição
    private boolean checkRateLimit(String path, String ip) {
        // Endpoints de autenticação: 5 req/min
        if(path.startsWith("/auth/signin") || path.startsWith("/auth/refresh")) {
            return rateLimitService.allowAuthRequest(ip);
        }
        // Endpoints de reset de senha: 3 req/hora
        else if(path.startsWith("/auth/forgot-password") || path.startsWith("/auth/reset-password")) {
            return rateLimitService.allowPasswordResetRequest(ip);
        }
        // Endpoints de criação de usuário: 5 req/hora
        else if(path.startsWith("/auth/createUser")) {
            return rateLimitService.allowUserCreationRequest(ip);
        }
        // Endpoints da API: 100 req/min
        else if(path.startsWith("/api/")) {
            return rateLimitService.allowApiRequuest(ip);
        }
        // Outros endpoints não têm rate limiting
        return true;
    }

    // Extrai o IP real do cliente
    private String getClientIp(HttpServletRequest request) {
        // Tenta obter do header X-Forwarded-For
        String xForwardedFor = request.getHeader("x-forwarded-for");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // Tenta obter do header X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if(xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Se não achar nos headers, usa o IP direto
        return request.getRemoteAddr();
    }
}
