package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.data.dto.UserRequestDTO;
import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.data.dto.security.SignUpRequestDTO;
import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.mapper.UserMapper;
import com.maxzdosreis.products_api.model.Permission;
import com.maxzdosreis.products_api.model.User;
import com.maxzdosreis.products_api.repository.PermissionRepository;
import com.maxzdosreis.products_api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    private Logger logger = LoggerFactory.getLogger(ProductService.class.getName());

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    private PagedResourcesAssembler<UserResponseDTO> assembler;

    @Autowired
    private UserMapper userMapper;

    // Método que busca usuário no banco de dados através do username do mesmo
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Consulta usuário pelo username no banco de dados
        var user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new UsernameNotFoundException("Username " + username + " not found");
        }
    }

    // Lista todos os usuários com paginação
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<UserResponseDTO>> findAll(Pageable pageable) {
        var users = userRepository.findAll(pageable);

        var usersDto = users.map(userMapper::toDto);

        return assembler.toModel(usersDto);
    }

    // Busca um usuário por ID
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return userMapper.toDto(user);
    }

    // Busca um usuário por email
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Atualiza informações de um usuário
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user =  userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (request.getUserName() != null &&  !request.getUserName().isBlank()) {
            // Verificar se o username já está em uso por outro usuário
            User existingUser = userRepository.findByUsername(request.getUserName());
            if (existingUser != null && !existingUser.getId().equals(id)) {
                throw new IllegalArgumentException("O username já está sendo utilizado por outro usuário");
            }
            user.setUserName(request.getUserName());
        }

        if(request.getFullName()!= null && !request.getFullName().isBlank()){
            user.setFullName(request.getFullName());
        }

        if(request.getEmail() != null && !request.getEmail().isBlank()){
            // Verifica se o email já está em uso por outro usuário
            User existingUser = userRepository.findByEmail(request.getEmail());
            if(existingUser != null && !existingUser.getId().equals(id)){
                throw new IllegalArgumentException("O endereço de email já está sendo utilizado por um usuário");
            }
            user.setEmail(request.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public UserResponseDTO enableProduct(Long id) {
        logger.info("Enabling product id={}", id);

        findUserEntityById(id);
        userRepository.enableUser(id);

        return userMapper.toDto(findUserEntityById(id));
    }

    @Transactional
    public UserResponseDTO disableProduct(Long id) {
        logger.info("Disabling product id={}", id);

        findUserEntityById(id);
        userRepository.disableUser(id);

        return userMapper.toDto(findUserEntityById(id));
    }

    public void delete(Long id) {
        logger.info("Deleting user id={}", id);
        userRepository.delete(findUserEntityById(id));
    }

    // Adiciona uma permissão a um usuário
    @Transactional
    public void addPermissionToUser(Long userId, String permissionName) {
        User user = findUserEntityById(userId);

        Permission permission = findPermissionEntityByDescription(permissionName);

        user.getPermissions().add(permission);
        userRepository.save(user);
    }

    // Remove uma permissão de um usuário
    @Transactional
    public void removePermissionToUser(Long userId, String permissionName) {
        User user = findUserEntityById(userId);

        if(user.getPermissions().size() == 1) {
            throw new IllegalStateException("User must have at least one permission");
        }

        Permission permission = findPermissionEntityByDescription(permissionName);

        user.getPermissions().remove(permission);
        userRepository.save(user);
    }

    // Lista todas as permissões de um usuário
    @Transactional(readOnly = true)
    public List<String> listPermissionsToUser(Long userId) {
        User user = findUserEntityById(userId);

        return user.getPermissions()
                .stream()
                .map(Permission::getDescription)
                .toList();
    }

    private User findUserEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    private Permission findPermissionEntityByDescription(String permissionName) {
        return permissionRepository.findByDescription(permissionName).orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
    }
}
