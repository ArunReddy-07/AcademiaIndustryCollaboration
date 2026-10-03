package com.academiaindustry.service;

import com.academiaindustry.dto.UserRequest;
import com.academiaindustry.entity.Institution;
import com.academiaindustry.entity.Role;
import com.academiaindustry.entity.User;
import com.academiaindustry.repository.InstitutionRepository;
import com.academiaindustry.repository.UserRepository;
import com.academiaindustry.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void createsAnInstitutionProfileLinkedToTheNewAccount() {
        UserRepository userRepository = mock(UserRepository.class);
        InstitutionRepository institutionRepository = mock(InstitutionRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("secure-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder, institutionRepository);
        UserRequest request = new UserRequest();
        request.setName("Example Institute");
        request.setEmail("office@example.edu");
        request.setPassword("secure-password");
        request.setRole(Role.INSTITUTION);

        service.create(request);

        verify(institutionRepository).save(any(Institution.class));
    }
}