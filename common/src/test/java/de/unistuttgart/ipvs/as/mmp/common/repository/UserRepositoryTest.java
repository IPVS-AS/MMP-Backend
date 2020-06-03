package de.unistuttgart.ipvs.as.mmp.common.repository;

import de.unistuttgart.ipvs.as.mmp.common.configuration.MmpJpaTestConfig;
import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(MmpJpaTestConfig.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private final User testUser = new User("Hans Maier", "admin", "password");

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
    }

    @Test
    public void shouldFindAll() {
        userRepository.save(testUser);

        List<User> users = userRepository.findAll();
        assertEquals(Collections.singletonList(testUser), users);
    }

    @Test
    public void shouldNotFindAny() {
        List<User> users = userRepository.findAll();
        assertEquals(Collections.emptyList(), users);
    }

    @Test
    public void shouldNotFindById() {
        Optional<User> user = userRepository.findById(123456789L);
        assertEquals(Optional.empty(), user);
    }

    @Test
    public void shouldFindById() {
        Long userId = userRepository.save(testUser).getId();

        Optional<User> user = userRepository.findById(userId);
        assertEquals(Optional.of(testUser), user);
    }

    @Test
    public void shouldFindUserByName() {
        String userName = userRepository.save(testUser).getName();

        Optional<User> user = userRepository.findByName(userName);
        assertEquals(Optional.of(testUser), user);
    }

    @Test
    public void shouldNotFindUserByName() {
        Optional<User> user = userRepository.findByName("test");
        assertEquals(Optional.empty(), user);
    }
}



