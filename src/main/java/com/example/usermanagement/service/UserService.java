package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserRequest;
import com.example.usermanagement.dto.UserResponse;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(String keyword, Pageable pageable) {
        Page<User> page = StringUtils.hasText(keyword)
                ? userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                : userRepository.findAll(pageable);
        return page.map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        User user = new User();
        apply(user, request);
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = getUser(id);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        apply(user, request);
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private void apply(User user, UserRequest r) {
        user.setFullName(r.fullName().trim());
        user.setEmail(r.email().trim().toLowerCase());
        user.setPhone(StringUtils.hasText(r.phone()) ? r.phone().trim() : null);
        if (r.role() != null) user.setRole(r.role());
        if (r.active() != null) user.setActive(r.active());
    }
}
