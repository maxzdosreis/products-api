package com.maxzdosreis.products_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SimpleRateLimitService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRateLimitService.class);

    // Classe interna para armazenar informações de rate limit
    private static class RateLimitInfo {
        private final AtomicInteger requestCount;
        private volatile Instant windowStart;

        RateLimitInfo() {
            this.requestCount = new AtomicInteger(0);
            this.windowStart = Instant.now();
        }

        // Método synchronized para evitar race conditions
        public synchronized boolean checkAndIncrement(int maxRequests, Duration windowDuration) {
            Instant now = Instant.now();
            Instant windowEnd = windowStart.plus(windowDuration);

            if(now.isAfter(windowEnd)) {
                requestCount.set(1);
                windowStart = now;
                return true;
            }

            // Incrementa de forma atômica
            int current = requestCount.incrementAndGet();
            return current <= maxRequests;
        }

        public int getCurrentCount() {
            return requestCount.get();
        }

        public Instant getWindowStart() {
            return windowStart;
        }
    }

    // Armazena informações de rate limit por chave (IP + endpoint)
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    // Verifica se uma requisição deve ser permitida
    // Ex: allowRequest("192.168.1.1:auth", 5, 1)
    // 30/01/2025: Simplifica e aplica thread-safe
    public boolean allowRequest(String key, int maxRequests, int windowMinutes) {
        Instant now = Instant.now();

        // Busca ou cria informações para esta chave, se não existir, cria um novo RateLimitInfo
        RateLimitInfo info = rateLimitMap.computeIfAbsent(key, k -> new RateLimitInfo());

        // Calcula quando a janela de tempo dura
        Duration windowDuration = Duration.ofMinutes(windowMinutes);
        boolean allowed = info.checkAndIncrement(maxRequests, windowDuration);

        if(!allowed) {
            logger.warn("Rate limit excedido para {}. Tentativa: {}/{}", key, info.getCurrentCount(), maxRequests);
        } else {
            logger.debug("Requisição permitida para {}. Uso: {}/{}", key, info.getCurrentCount(), maxRequests);
        }

        return allowed;
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

    // Retorna informações detalhadas
    public String getRateLimitInfo(String key) {
        RateLimitInfo info = rateLimitMap.get(key);
        if(info == null) {
            return "Nenhuma requisição registrada para: " + key;
        }
        return String.format("Requisições: %d, Janela iniciou: %s", info.getCurrentCount(), info.getWindowStart());
    }

    // Limpa entradas antigas do Map periodicamente
    // 30/01/2025: Limpeza mais agressiva para evitar memory leak
    @Scheduled(fixedRate = 3600000) // Executa automaticamente a cada 1 hora
    public void cleanupOldEntries() {
        Instant cutoff = Instant.now().minus(Duration.ofHours(2));
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
