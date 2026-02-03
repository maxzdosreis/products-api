package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.exception.*;
import com.maxzdosreis.products_api.model.PasswordResetToken;
import com.maxzdosreis.products_api.model.User;
import com.maxzdosreis.products_api.repository.PasswordResetTokenRepository;
import com.maxzdosreis.products_api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TooManyListenersException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PasswordResetService {

    Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.token-expiry-hours}")
    private int tokenExpiryHours;

    @Value("${app.password-reset.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void initiatePasswordReset(String email) {
        logger.info("Iniciando reset de senha para email: {}", email);

        User user = userRepository.findByEmail(email);
        if(user == null) {
            logger.warn("Tentativa de reset para email inexistente: {}", email);
            return;
        }

        try {
            // Remove os tokens anteriores para este email
            tokenRepository.deleteByEmail(email);

            // Cria novo token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .email(email)
                    .expiryDate(LocalDateTime.now().plusHours(tokenExpiryHours))
                    .build();

            // Salva o novo token
            tokenRepository.save(resetToken);

            // Envia email assíncrono
            CompletableFuture<Boolean> emailSent = sendResetEmailAsync(email, token, user.getFullName());

            emailSent.thenAccept(success -> {
                if (success) {
                    logger.info("Email de reset enviado com sucesso para {}", email);
                } else {
                    logger.error("Falha ao enviar email de reset para: {}", email);
                }
            });
        } catch (Exception e) {
            logger.error("Erro ao criar token de reset para {}: {}", email, e.getMessage());
            throw new EmailServiceException("Erro ao processar solicitação de reset", e);
        }
    }

    // Envia email de reset de forma assíncrona
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendResetEmailAsync(String email, String token, String fullName) {
        try {
            sendResetEmail(email, token, fullName);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("Erro ao enviar email de reset para {}: {}", email, e.getMessage());
            return CompletableFuture.completedFuture(false);
        }
    }

    // Envia o email com o link de reset
    private void sendResetEmail(String email, String token, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("Reset Senha - Products API");

        String resetLink = baseUrl + "/auth/reset-password?token=" + token;

        String emailBody = "Olá" + (fullName != null ? " " + fullName : "") + "!\n\n" +
                "Você solicitou a redefinição da sua senha na Products API.\n\n" +
                "Para redefinir sua senha, clique no link abaixo:\n" +
                resetLink + "\n\n" +
                "Este link expira em " + tokenExpiryHours + " hora(s).\n\n" +
                "Se você não solicitou este reset, ignore este email.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Products API";

        message.setText(emailBody);

        mailSender.send(message);
        logger.info("Email de reset enviado com sucesso para: {}", email);
    }

    // Valida se o token é válido
    public void validateToken(String token) {
        logger.info("Validando token: {}", token);

        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if(optionalToken.isEmpty()) {
            logger.warn("Token não encontrado: {}", token);
            throw new InvalidTokenException();
        }

        PasswordResetToken resetToken = optionalToken.get();

        if(resetToken.isExpired()) {
            logger.warn("Token expirado: {}", token);
            throw new ExpiredTokenException();
        }

        if(resetToken.isUsed()) {
            logger.warn("Token já utilizado: {}", token);
            throw new TokenAlreadyUsedException();
        }

        logger.info("Token válido: {}", token);
    }

    // Confirma o reset de senha usando o mesmo padrão de hash do AuthService
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        logger.info("Confirmando reset de senha com token: {}", token);

        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if(optionalToken.isEmpty()) {
            logger.warn("Token não encontrado: {}, esse token está vazio", token);
            throw new InvalidTokenException();
        }

        PasswordResetToken resetToken = optionalToken.get();

        if(resetToken.isExpired()) {
            logger.warn("Token expirado: {}", token);
            throw new ExpiredTokenException();
        }

        if(resetToken.isUsed()) {
            logger.warn("Token já utilizado: {}", token);
            throw new TokenAlreadyUsedException();
        }

        User user = userRepository.findByEmail(resetToken.getEmail());
        if(user == null) {
            logger.error("Usuário não encontrado para email: {}", resetToken.getEmail());
            throw new InvalidTokenException("Usuário não encontrado");
        }

        try {
            // Atualiza a senha do usuário usando o mesmo padrão do AuthService
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Marca token como usado
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);

            logger.info("Senha alterada com sucesso para usuário: {}", user.getFullName());
        } catch (Exception e) {
            logger.error("Erro ao alterar senha: {}", e.getMessage());
            throw new RuntimeException("Erro interno ao alterar senha", e);
        }
    }

    // Faz a limpeza automática dos tokens expirados
    @Transactional
    public void cleanExpiredTokens(LocalDateTime cutoffTime) {
        try {
            tokenRepository.deleteExpiredTokens(cutoffTime);
            logger.info("Limpeza de tokens expirados concluída. Cutoff: {}", cutoffTime);
        } catch (Exception e) {
            logger.error("Erro ao remover tokens expirados: {}", e.getMessage());
        }
    }

    @Transactional
    public void cleanExpiredTokens() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        cleanExpiredTokens(cutoffTime);
    }
}
