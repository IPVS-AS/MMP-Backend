package de.unistuttgart.ipvs.as.mmp.common.service;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.repository.UserRepository;
import de.unistuttgart.ipvs.as.mmp.common.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    private static final Long NON_EXISTING_ID = 123456L;
    private static final Long EXISTING_ID = 123456789L;
    private static final String EXISTING_NAME = "Hans Maier";
    private static final User TEST_USER = new User(EXISTING_NAME, "admin", "password");
    private static final String NON_EXISTING_NAME = "test user";

    @TestConfiguration
    static class UserServiceTestContextConfiguration {

        @MockBean
        public UserRepository userRepository;

        @Bean
        public UserService userService() {
            return new UserServiceImpl(this.userRepository);
        }
    }

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;


    @BeforeEach
    public void setUp() {
        given(userRepository.findById(EXISTING_ID)).willReturn(Optional.of(TEST_USER));
        given(userRepository.findById(NON_EXISTING_ID)).willReturn(Optional.empty());
        given(userRepository.save(TEST_USER)).willReturn(TEST_USER);
        given(userRepository.save(getUserWithUpdatedFields())).willReturn(getUserWithUpdatedFields());
        given(userRepository.findByName(EXISTING_NAME)).willReturn(Optional.of(TEST_USER));
        given(userRepository.findByName(NON_EXISTING_NAME)).willReturn(Optional.empty());
    }

    @Test
    public void shouldGetUserById() {
        Optional<User> user = userService.getUserById(EXISTING_ID);
        assertEquals(Optional.of(TEST_USER), user);
    }

    @Test
    public void shouldNotGetUserById() {
        Optional<User> user = userService.getUserById(NON_EXISTING_ID);
        assertEquals(Optional.empty(), user);
    }

    @Test
    public void shouldUpdateUser() {
        TEST_USER.setId(EXISTING_ID);
        User userWithUpdatedFields = getUserWithUpdatedFields();
        User updateUser = userService.updateUserById(EXISTING_ID, userWithUpdatedFields);
        assertEquals(userWithUpdatedFields, updateUser);
    }

    @Test
    public void shouldNotUpdateUserById() {
        assertThrows(NullPointerException.class, () -> userService.updateUserById(NON_EXISTING_ID, null));
    }

    @Test
    public void shouldNotUpdateUserWhenUserNotFound() {
        assertThrows(IdException.class, () -> userService.updateUserById(NON_EXISTING_ID, TEST_USER));
    }

    @Test
    public void shouldNotgETUserWhenIdIsNull() {
        assertThrows(NullPointerException.class, () -> userService.getUserById(null));
    }

    @Test
    public void shouldFindByName() {
        Optional<User> user = userService.getUserByName(EXISTING_NAME);
        assertEquals(Optional.of(TEST_USER), user);
    }

    @Test
    public void shouldNotFindUserByName() {
        Optional<User> user = userService.getUserByName(NON_EXISTING_NAME);
        assertEquals(Optional.empty(), user);
    }

    private User getUserWithUpdatedFields() {
        User u = new User();
        u.setName("new name");
        u.setRole(TEST_USER.getRole());
        u.setPassword(TEST_USER.getPassword());
        return u;
    }
}
