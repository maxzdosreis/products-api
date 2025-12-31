package com.maxzdosreis.products_api.scheduler;

import com.maxzdosreis.products_api.service.PasswordResetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

    Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    @Autowired
    private PasswordResetService passwordResetService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupExpiredTokensDaily() {
        logger.info("Iniciando limpeza diária de tokens expirados");
        try {
            passwordResetService.cleanExpiredTokens();
            logger.info("Limpeza diária de tokens expirados concluída com sucesso");
        } catch (Exception e) {
            logger.error("Erro durante limpeza diária de tokens expirados: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 6 * 60 * 60 * 1000)
    public void cleanupExpiredTokensFrequent() {
        logger.info("Executando limpeza frequente de tokens expirados");
        try {
            passwordResetService.cleanExpiredTokens();
        } catch (Exception e) {
            logger.error("Erro durante limpeza frequente de tokens expirados: {}", e.getMessage());
        }
    }
}
