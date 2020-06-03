package de.unistuttgart.ipvs.as.mmp.common.service.impl;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.repository.UserRepository;
import de.unistuttgart.ipvs.as.mmp.common.service.UserService;
import lombok.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> getUserById(@NonNull Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByName(@NonNull String name) {
        return userRepository.findByName(name);
    }

    @Override
    public User saveUser(@NonNull User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateUserById(@NonNull Long userId, @NonNull User user) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            user.setId(optionalUser.get().getId());
        } else {
            throw IdException.idNotFound(User.class, userId);
        }
        return userRepository.save(user);
    }

    @Override
    public boolean deleteUserById(@NonNull Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Override
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}