package com.maxzdosreis.products_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRateLimitService.class);

    // Classe interna para armazenar informações de rate limit
    private static class RateLimitInfo {
        int requestCount; // Requisições feitas
        LocalDateTime windowStart; // Começo da janela de tempo

        RateLimitInfo() {
            this.requestCount = 0;
            this.windowStart = LocalDateTime.now();
        }
    }

    // Armazena informações de rate limit por chave (IP + endpoint)
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    // Verifica se uma requisição deve ser permitida
    // Ex: allowRequest("192.168.1.1:auth", 5, 1)
    public boolean allowRequest(String key, int maxRequests, int windowMinutes) {
        LocalDateTime now = LocalDateTime.now();

        // Busca ou cria informações para esta chave, se não existir, cria um novo RateLimitInfo
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());

        // Calcula quando a janela de tempo expira
        LocalDateTime windowEnd = info.windowStart.plusMinutes(windowMinutes);

        // Se expirou, reseta o contador
        if(now.isAfter(windowEnd)) {
            logger.debug("Janela expirou para {}. Resetando contador.", key);
            info.requestCount = 1;
            info.windowStart = now;
            return true;
        }

        info.requestCount++;

        // Verifica se excedeu o limite
        if(info.requestCount > maxRequests) {
            logger.warn("Rate limit excedido para {}. Tentativa: {}/{}", key, info.requestCount, maxRequests);
            return false; // BLOQUEADO!
        }

        logger.debug("Requisição permmitida para {}. Uso: {}/{}", key, info.requestCount, maxRequests);
        return true; // PERMITIDO!
    }

    // Autenticação (login/refresh): 5 requisições por minuto
    public boolean allowAuthRequest(String ip) {
        return allowRequest(ip + ":auth", 5, 1);
    }

    // Reset de senha: 3 requisições por hora
    public boolean allowPasswordResetRequest(String ip) {
        return allowRequest(ip + ":password-reset", 3, 60);
    }

    // Criação de usuário: 5 requisições por hora
    public boolean allowUserCreationRequest(String ip) {
        return allowRequest(ip + ":user-creation", 5, 60);
    }

    // API Geral: 100 requisições por minuto
    public boolean allowApiRequuest(String ip) {
        return allowRequest(ip + ":api", 100, 1);
    }

    // Obtém informações de uso atual
    public String getRateLimitInfo(String key) {
        RateLimitInfo info = rateLimitMap.get(key);
        if(info == null) {
            return null;
        }
        return String.format("Requisições: %d, Janela iniciou: %s", info.requestCount, info.windowStart);
    }

    // Limpa entradas antigas do Map periodicamente
    @Scheduled(cron = "0 0 * * * ?") // Executa automaticamente a cada 1 hora
    public void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        int sizeBefore = rateLimitMap.size();

        // Remove entradas cuja janela começou há mais de 2 horas
        rateLimitMap.entrySet().removeIf(entry ->
            entry.getValue().windowStart.isBefore(cutoff)
        );

        int sizeAfter = rateLimitMap.size();
        int removed = sizeBefore - sizeAfter;

        if(removed > 0) {
            logger.info("Limpeza de rate limit: {} entradas removidas. " +
                        "Tamanho: {} -> {}", removed, sizeBefore, sizeAfter);
        }
    }

    // Retorna o número de IPs sendo rastreados
    public int getTrackedIpsCount() {
        return rateLimitMap.size();
    }
}
