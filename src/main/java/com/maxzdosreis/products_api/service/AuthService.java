package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.security.AccountCredentialsDTO;
import com.maxzdosreis.products_api.data.dto.security.TokenDTO;
import com.maxzdosreis.products_api.exception.RequiredObjectIsNullException;
import com.maxzdosreis.products_api.model.User;
import com.maxzdosreis.products_api.repository.PermissionRepository;
import com.maxzdosreis.products_api.repository.UserRepository;
import com.maxzdosreis.products_api.security.jwt.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Método que autentica o usuário
    public ResponseEntity<TokenDTO> signIn(AccountCredentialsDTO credentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        credentials.getUsername(),
                        credentials.getPassword()
                )
        );

        var user = userRepository.findByUsername(credentials.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("Username " + credentials.getUsername() + " not found!");
        }

        // Cria o access token para o usuário
        var token = tokenProvider.createAccessToken(
                credentials.getUsername(),
                user.getRoles()
        );

        // Retorna o access token
        return ResponseEntity.ok(token);
    }

    // Método que atualiza o token
    public ResponseEntity<TokenDTO> refreshToken(String username, String refreshToken){
        var user = userRepository.findByUsername(username);
        TokenDTO token;
        // Verifica se o usuário pelo username se é nulo, se não for atualiza o token, se for retorna uma exception
        if (user != null) {
            token = tokenProvider.refreshToken(refreshToken);
        }else{
            throw new UsernameNotFoundException("Username " + username + " not found!");
        }
        return ResponseEntity.ok(token);
    }

    // Método que gera uma senha criptograda
    private String generateHashedPassword(String password) {

        // Cria um encode com configurações de segurança
        PasswordEncoder pbkdf2Encoder = new Pbkdf2PasswordEncoder(
                "", 8, 185000,
                Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

        // Cria um mapa de encoders disponíveis
        Map<String, PasswordEncoder> encoders =  new HashMap<>();
        encoders.put("pbkdf2", pbkdf2Encoder);

        // Cria um encoder delegador que gerencia múltiplos algoritmos
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder("pbkdf2", encoders);

        // Define qual encoder usar ao verificar senhas que não têm prefixo
        passwordEncoder.setDefaultPasswordEncoderForMatches(pbkdf2Encoder);
        // Retorna a senha criptograda
        return passwordEncoder.encode(password);

    }

    // Método que cria um novo usuário no sistema
    public AccountCredentialsDTO create(AccountCredentialsDTO user) {

        // Valida se o objeto user não é nulo
        if(user == null) throw new RequiredObjectIsNullException();

        logger.info("Creating one new User!");
        // Preenche os dados básicos do usuário
        var entity = new User();
        entity.setFullName(user.getFullname());
        entity.setUserName(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(passwordEncoder.encode(user.getPassword()));
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);
        entity.setEnabled(true);

        var defaultPermission = permissionRepository
                .findByDescription("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found!"));
        entity.setPermissions(Set.of(defaultPermission));

        // Salva o usuário no banco de dados
        var dto = userRepository.save(entity);

        // Retorna um DTO com os dados do usuário criado
        return new AccountCredentialsDTO(dto.getUsername(), dto.getPassword(), dto.getFullName(), dto.getEmail());
    }
}
