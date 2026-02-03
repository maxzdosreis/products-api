package com.maxzdosreis.products_api.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maxzdosreis.products_api.data.dto.security.TokenDTO;
import com.maxzdosreis.products_api.exception.InvalidJwtAuthenticationException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenProvider {

    // Chave secreta para assinar os tokens JWT
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey;

    // Tempo de validade do token em milissegundos (1 hora)
    @Value("${security.jwt.token.expire-length:3600000}")
    private long validityInMilliseconds = 3600000;

    // Serviço para carregar detalhes do usuário do banco de dados
    @Autowired
    private UserDetailsService userDetailsService;

    // Algoritmo de criptografia HMAC256 usado para assinar tokens
    Algorithm algorithm = null;

    @PostConstruct
    protected void init(){
        // Codifica a chave secreta em Base64 para maior segurança
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        // Cria o algoritmo HMAC256 com a a chave secreta
        algorithm = Algorithm.HMAC256(secretKey.getBytes());
    }

    // Cria um novo par de tokens (access token + refresh token) para o usuário
    public TokenDTO createAccessToken(String username, List<String> roles){
        Date now  = new Date(); // Data atual
        Date validity = new Date(now.getTime() + validityInMilliseconds); // Validade = agora + 1h

        // Gera o access token
        String accessToken = getAccessToken(username, roles, now, validity);
        // Gera o refresh token
        String refreshToken = getRefreshToken(username, roles, now);

        return TokenDTO.builder()
                .username(username)
                .authenticated(true)
                .created(now)
                .expiration(validity)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Renova o access token usando um refresh token válido
    public TokenDTO refreshToken(String refreshToken){
        var token = "";

        // Remove o prefixo "Bearer " se existir
        if(refreshTokenContainsBearer(refreshToken)) {
            token = refreshToken.substring("Bearer ".length());
        }

        // Verifica e decodifica o refresh token
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(token);

        // Extrai informações do token: usuário e permissões
        String username = decodedJWT.getSubject();
        List<String> roles = decodedJWT.getClaim("roles").asList(String.class);

        // Cria um novo par de tokens com as mesmas credenciais
        return createAccessToken(username, roles);
    }

    // Verifica se o token contém o prefixo "Bearer "
    private static boolean refreshTokenContainsBearer(String refreshToken){
        return StringUtils.isNotBlank(refreshToken) && refreshToken.startsWith("Bearer ");
    }

    // Cria o access token
    private String getAccessToken(String username, List<String> roles, Date now, Date validity){
        // URL base da aplicação
        String issuerUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        return JWT.create()
                .withClaim("roles", roles) // Adiciona as permissões do usuário
                .withIssuedAt(now) // Data de criação
                .withExpiresAt(validity) // Data de expiração (1h)
                .withSubject(username) // Nome do usuário
                .withIssuer(issuerUrl) // Identifica quem emitiu o token
                .sign(algorithm); // Assina com HMAC256
    }

    // Cria o refresh token (refresh token dura mais para permitir renovação sem novo login)
    private String getRefreshToken(String username, List<String> roles, Date now){
        Date refreshTokenValidity = new Date(now.getTime() + validityInMilliseconds * 3);

        return JWT.create()
                .withClaim("roles", roles) // Adiciona as permissões do usuário
                .withIssuedAt(now) // Data de criação
                .withExpiresAt(refreshTokenValidity) // Data de expiração (3h)
                .withSubject(username) // Nome do usuário
                .sign(algorithm); // Assina com HMAC256
    }

    // Converte um token JWT em um objeto Authentication
    public Authentication getAuthentication(String token){
        // Decodifica o token para extrair informações
        DecodedJWT decodedJWT = decodedToken(token);

        // Carrega os detalhes completos do usuário do banco de dados
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(decodedJWT.getSubject());

        // Cria objeto de autenticação
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // Decodifica e verifica a assinatura do token
    private DecodedJWT decodedToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(secretKey.getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build(); // Cria o verificador
        DecodedJWT decodedJWT = verifier.verify(token); // Verifica assinatura
        return decodedJWT;
    }

    // Extrai o token JWT do header "Authorization" da requisição HTTP
    public String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        // Remove o prefico "Bearer " e retorna apenas o token
        if(refreshTokenContainsBearer(bearerToken)) {
            return bearerToken.substring("Bearer ".length());
        }
        return null; // Retorna null se não tiver token
    }

    public boolean validateToken(String token) {
        DecodedJWT decodedJWT = decodedToken(token);

        try {
            // Verifica se a data de expiração já passou
            if(decodedJWT.getExpiresAt().before(new Date())) {
                return false; // Token expirado
            }
            return true; // Token válido
        } catch (Exception e) {
            throw new InvalidJwtAuthenticationException("Expired or Invalid JWT Token!");
        }
    }
}
