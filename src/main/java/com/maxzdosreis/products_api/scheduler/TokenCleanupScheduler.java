package com.maxzdosreis.products_api.scheduler;

import com.maxzdosreis.products_api.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupScheduler {

    Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTokensDaily() {
        logger.info("Iniciando limpeza diária de tokens expirados");
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            passwordResetService.cleanExpiredTokens(cutoffTime);
            logger.info("Limpeza diária de tokens expirados concluída com sucesso");
        } catch (Exception e) {
            logger.error("Erro durante limpeza diária de tokens expirados: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12 horas
    public void cleanupExpiredTokensFrequent() {
        logger.info("Executando limpeza frequente de tokens expirados");
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            passwordResetService.cleanExpiredTokens(cutoffTime);
        } catch (Exception e) {
            logger.error("Erro durante limpeza frequente de tokens expirados: {}", e.getMessage());
        }
    }
}
