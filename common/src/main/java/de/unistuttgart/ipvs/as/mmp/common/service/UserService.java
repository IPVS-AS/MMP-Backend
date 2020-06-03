package de.unistuttgart.ipvs.as.mmp.common.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> getUserByName(String name);

    User saveUser(User user);

    User updateUserById(Long id, User user);

    boolean deleteUserById(Long userId);

    void deleteAllUsers();
}