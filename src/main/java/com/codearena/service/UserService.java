package com.codearena.service;

import com.codearena.dto.response.PageResponse;
import com.codearena.entity.Role;
import com.codearena.entity.User;
import com.codearena.exception.BadRequestException;
import com.codearena.exception.ResourceNotFoundException;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public PageResponse<User> getAll(String search, Pageable pageable) {
        Page<User> result = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.search(search.trim(), pageable);
        return PageResponse.of(result);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isCurrentUser(user)) {
            throw new BadRequestException("You cannot delete your own account");
        }

        userRepository.delete(user);
    }

    public User updateRole(Long id, Role role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (isCurrentUser(user) && role != Role.ADMIN) {
            throw new BadRequestException("You cannot demote your own account");
        }

        user.setRole(role);
        return userRepository.save(user);
    }

    private boolean isCurrentUser(User user) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getName().equalsIgnoreCase(user.getEmail());
    }
}
