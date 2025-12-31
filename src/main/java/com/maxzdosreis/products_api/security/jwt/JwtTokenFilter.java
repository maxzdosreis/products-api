package com.maxzdosreis.products_api.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@AllArgsConstructor
public class JwtTokenFilter extends GenericFilterBean {

    private JwtTokenProvider tokenProvider;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter) throws IOException, ServletException {
        // Extrai o token JWT da requisição HTTP
        var token = tokenProvider.resolveToken((HttpServletRequest) request);

        // Verifica se o token existe (não é nulo/vazio) e se é válido
        if(StringUtils.isNotBlank(token) && tokenProvider.validateToken(token)) {
            // Extrai as informações de autenticação contidas no token
            Authentication authentication = tokenProvider.getAuthentication(token);

            if(authentication != null) {
                // Armazena a autenticação no contexto de segurança do Spring e reconhece o usuário como autenticado
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Passa a requisição adiante na cadeia de filtros
        filter.doFilter(request, response);
    }
}
