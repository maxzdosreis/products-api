package com.maxzdosreis.products_api.service;

import com.maxzdosreis.products_api.exception.ResourceNotFoundException;
import com.maxzdosreis.products_api.model.Permission;
import com.maxzdosreis.products_api.model.User;
import com.maxzdosreis.products_api.repository.PermissionRepository;
import com.maxzdosreis.products_api.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

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

    public User findByEmail(String email) {
        User userOpt = userRepository.findByEmail(email);
        return userOpt;
    }

    @Transactional
    public void addPermissionToUser(Long userId, String permissionName) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var permission = permissionRepository.findByDescription(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        user.getPermissions().add(permission);
    }

    @Transactional
    public void removePermissionToUser(Long userId, String permissionName) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(user.getPermissions().size() == 1) {
            throw new IllegalStateException("User must have at least one permission");
        }

        Permission permission = permissionRepository.findByDescription(permissionName)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        user.getPermissions().remove(permission);
    }

    @Transactional(readOnly = true)
    public List<String> listPermissionsToUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return user.getPermissions()
                .stream()
                .map(Permission::getDescription)
                .toList();
    }
}
