package de.unistuttgart.ipvs.as.mmp.common.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.User;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.common.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {UserController.class})
public class UserControllerTest extends ControllerTest {
    private static final String ALL_USERS_PATH = UserController.PATH;

    @MockBean
    private UserService userService;

    @Autowired
    private UserController userController;

    MockMvc rest;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.userController)
                .setControllerAdvice(new MMPExceptionHandler(), this.userController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    private final User testUser = new User("Hans Maier", "admin", "password");
    private final User secondTestUser = new User("Max Maier", "admin", "password");
    private User updatedTestUser = new User();

    private static final Long USER_ID = 123L;
    private static final String USER_ID_PATH = ALL_USERS_PATH + "/" + USER_ID;

    @Test
    public void shouldFindAllUsers() throws Exception {
        given(userService.getAllUsers()).willReturn(Arrays.asList(testUser, secondTestUser));
        rest.perform(get(ALL_USERS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void shouldFindUserById() throws Exception {
        given(userService.getUserById(USER_ID))
                .willReturn(Optional.of(testUser));
        rest.perform(get(USER_ID_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andDo(document("user-get"));
    }

    @Test
    public void shouldNotFindUserById() throws Exception {
        given(userService.getUserById(USER_ID))
                .willReturn(Optional.empty());
        rest.perform(get(USER_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteUserById() throws Exception {
        given(userService.getUserById(USER_ID)).willReturn(Optional.of(testUser));
        given(userService.deleteUserById(USER_ID))
                .willReturn(true);
        rest.perform(delete(USER_ID_PATH))
                .andExpect(status().isNoContent())
                .andDo(document("user-delete"));
    }

    @Test
    public void shouldNotDeleteUserByIdNotFound() throws Exception {
        given(userService.deleteUserById(USER_ID))
                .willReturn(false);
        rest.perform(delete(USER_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        updatedTestUser = getUpdatedTestUser();
        given(userService.updateUserById(USER_ID, updatedTestUser))
                .willReturn(updatedTestUser);
        rest.perform(put(USER_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(json(updatedTestUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedTestUser.getName()))
                .andDo(document("user-update"));
    }

    @Test
    public void shouldNotUpdateUserNotFound() throws Exception {
        given(userService.updateUserById(any(), any()))
                .willThrow(IdException.idNotFound(User.class, USER_ID));
        rest.perform(put(USER_ID_PATH).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Object of type de.unistuttgart.ipvs.as.mmp.common.domain.User not found for id 123"));
    }

    @Test
    public void shouldSaveUser() throws Exception {
        User userWihtId = testUser;
        userWihtId.setId(USER_ID);
        given(userService.saveUser(testUser))
                .willReturn(userWihtId);
        rest.perform(post(ALL_USERS_PATH).contentType(MediaType.APPLICATION_JSON).content(json(testUser)))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(USER_ID_PATH))
                .andDo(document("user-create"));
    }

    @Test
    public void shouldNotSaveUser() throws Exception {
        given(userService.saveUser(any()))
                .willThrow(new NullPointerException());
        rest.perform(post(ALL_USERS_PATH).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Something went wrong! If this problem persists please talk to your system administrator."));
    }

    private User getUpdatedTestUser() {
        User updatedTestUser = testUser;
        updatedTestUser.setName("new test name");
        return updatedTestUser;
    }
}
