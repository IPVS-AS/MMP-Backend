package de.unistuttgart.ipvs.as.mmp.common.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static de.unistuttgart.ipvs.as.mmp.common.v1.controller.UserController.PATH;
import static org.springframework.http.HttpStatus.CONFLICT;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class UserController {

    private final UserService userService;
    public static final String PATH = "/v1/users";
    private static final String USER_ID_PATTERN = "/{userId}";

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<URI> saveUser(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        if (savedUser == null) {
            return ResponseEntity.status(CONFLICT).build();
        }
        return ResponseEntity.created(URI.create(String.format("%s/%s", PATH, savedUser.getId()))).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllUsers() {
        userService.deleteAllUsers();
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = USER_ID_PATTERN)
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        Optional<User> userOptional = userService.getUserById(userId);
        return userOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping(value = USER_ID_PATTERN)
    public ResponseEntity<User> updateUserById(@PathVariable Long userId, @RequestBody User user) {
        User updatedUser = userService.updateUserById(userId, user);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping(value = USER_ID_PATTERN)
    public ResponseEntity<Void> deleteUserById(@PathVariable Long userId) {
        if (userService.deleteUserById(userId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}