package com.myproject.mini_erp.service;

import com.myproject.mini_erp.dto.UserCreationRequest;
import com.myproject.mini_erp.dto.UserResponse;
import com.myproject.mini_erp.dto.UserUpdateRequest;
import com.myproject.mini_erp.entity.User;
import com.myproject.mini_erp.enums.RecordStatus;
import com.myproject.mini_erp.enums.UserStatus;
import com.myproject.mini_erp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        User user = new User();

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists");
        }
        ;

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        Long nextUserNumber = userRepository.nextUserBusinessId();
        user.setUserId(String.format("USR-%06d", nextUserNumber));

        user.setUsername(request.getUsername());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDateOfBirth(request.getDateOfBirth());

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getDateOfBirth(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public List<UserResponse> getAllUser() {
        return userRepository.findAllByRecordStatus(RecordStatus.ACTIVE).stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository
                .findByIdAndRecordStatus(id, RecordStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found by id: " + id));
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndRecordStatus(id, RecordStatus.ACTIVE).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id " + id));
        user.setRecordStatus(RecordStatus.INACTIVE);
        user.setStatus(UserStatus.INACTIVE);
        user.setDeletedAt(LocalDateTime.now());
        user.setPurgeAt(user.getDeletedAt().plusDays(30));
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findByIdAndRecordStatus(id, RecordStatus.ACTIVE).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found with id " + id));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already exists");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setDateOfBirth(request.getDateOfBirth());

        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public UserResponse restoreUser(Long id) {
        User user = userRepository.findByIdAndRecordStatus(id, RecordStatus.INACTIVE).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "user not found with id " + id));

        if (user.getPurgeAt() == null || !LocalDateTime.now().isBefore(user.getPurgeAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "user has expired");
        }

        user.setRecordStatus(RecordStatus.ACTIVE);
        user.setStatus(UserStatus.ACTIVE);
        user.setDeletedAt(null);
        user.setPurgeAt(null);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse anonymizeUserNow(Long id, boolean confirm) {
        if (!confirm) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Anonymization confirmation is required");
        };
        User user = userRepository.findByIdAndRecordStatus(id, RecordStatus.INACTIVE).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "user not found with id " + id));
        if (user.getPurgeAt() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "user has already been anonymized");
        }

        anonymizeUser(user);
        return toResponse(user);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void anonymizeExpiredUser() {
        List<User> expiredUsers = userRepository.findAllByRecordStatusAndPurgeAtLessThanEqual(
                RecordStatus.INACTIVE, LocalDateTime.now()
        );

        for  (User user : expiredUsers) {
            anonymizeUser(user);
        }
    }

    private User anonymizeUser(User user) {
        Long id = user.getId();

        user.setUsername("deleted_user_" + id);
        user.setFirstName("Anonymous");
        user.setLastName("user");
        user.setEmail("deleted_" + id + "@system.local");
        user.setPhone(null);
        user.setAddress(null);
        user.setDateOfBirth(null);

        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));

        user.setPurgeAt(null);
        return userRepository.save(user);
    }
}
