package com.example.usermanagement.service;

import com.example.usermanagement.dto.UserRequest;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    private final UserRequest request =
            new UserRequest("  Tran Thi C ", " C@Example.com ", "", User.Role.ADMIN, null);

    @Test
    void createNormalizesFields() {
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = userService.create(request);

        assertThat(res.fullName()).isEqualTo("Tran Thi C");
        assertThat(res.email()).isEqualTo("c@example.com");
        assertThat(res.phone()).isNull();
        assertThat(res.role()).isEqualTo(User.Role.ADMIN);
        assertThat(res.active()).isTrue();
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase(any())).thenReturn(true);
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
