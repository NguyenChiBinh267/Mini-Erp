package com.myproject.mini_erp.controller;

import com.myproject.mini_erp.dto.UserAnonymizeRequest;
import com.myproject.mini_erp.dto.UserCreationRequest;
import com.myproject.mini_erp.dto.UserResponse;
import com.myproject.mini_erp.dto.UserUpdateRequest;
import com.myproject.mini_erp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody UserCreationRequest request
    ) {
        return userService.createUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public UserResponse getUsers(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request){
        return userService.updateUser(id, request);
    }

    @PostMapping("/{id}/restore")
    public UserResponse restoreUser(@PathVariable Long id) {
        return userService.restoreUser(id);
    }

    @PostMapping("/{id}/anonymize")
    public UserResponse anonymizeUser(@PathVariable Long id, @RequestBody @Valid UserAnonymizeRequest request) {
        return userService.anonymizeUserNow(id, request.isConfirm());
    }
}