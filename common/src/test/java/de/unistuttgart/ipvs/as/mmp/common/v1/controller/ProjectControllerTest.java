package de.unistuttgart.ipvs.as.mmp.common.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.*;
import de.unistuttgart.ipvs.as.mmp.common.exception.IdException;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.common.service.ProjectService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {ProjectController.class})
public class ProjectControllerTest extends ControllerTest {

    @MockBean
    private ProjectService projectService;

    @Autowired
    private ProjectController projectController;

    MockMvc rest;

    private static final String ALL_PROJECTS_PATH = ProjectController.PATH;
    private static final Long PROJECT_ID = 123L;
    private static final String PROJECT_ID_PATH = ALL_PROJECTS_PATH + "/" + PROJECT_ID;

    private User testUser = new User("Hans Maier", "admin", "password");
    private Project testProject = Project.builder()
            .name("test project")
            .editors(Collections.singletonList(testUser))
            .build();
    private ModelMetadata testModelMetadata = ModelMetadata.builder()
            .name("Model1")
            .version(1L)
            .lastModified(LocalDate.now())
            .dateOfCreation(LocalDate.now())
            .algorithm("linear regression")
            .status(ModelStatus.OPERATION)
            .author(testUser)
            .build();
    private Model testModel = Model.builder().modelMetadata(testModelMetadata).project(testProject).build();

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.projectController)
                .setControllerAdvice(new MMPExceptionHandler(), this.projectController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
        testUser.setId(1L);
        testModel.setId(2L);
    }

    private Project getUpdatedTestProject() {
        Project updatedProject = testProject;
        updatedProject.setName("updated test project");
        return updatedProject;
    }

    @Test
    public void shouldFindProjectById() throws Exception {
        given(projectService.getProjectById(PROJECT_ID))
                .willReturn(Optional.of(testProject));
        rest.perform(get(PROJECT_ID_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(testProject.getName()))
                .andDo(document("project-get"));
    }

    @Test
    public void shouldNotFindProjectById() throws Exception {
        given(projectService.getProjectById(PROJECT_ID))
                .willReturn(Optional.empty());
        rest.perform(get(PROJECT_ID_PATH))
                .andExpect(status().isNotFound());
    }


    @Test
    public void shouldGetAllProjects() throws Exception {
        given(projectService.getAllProjects())
                .willReturn(Collections.singletonList(testProject));
        rest.perform(get(ALL_PROJECTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andDo(document("project-get-all"));
    }

    @Test
    public void shouldNotGetAnyProject() throws Exception {
        given(projectService.getAllProjects())
                .willReturn(Collections.emptyList());
        rest.perform(get(ALL_PROJECTS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void shouldUpdateProject() throws Exception {
        Project updatedTestProject = getUpdatedTestProject();
        updatedTestProject.setModels(Collections.emptyList());
        updatedTestProject.setId(PROJECT_ID);
        given(projectService.getProjectById(PROJECT_ID)).willReturn(Optional.of(testProject));
        given(projectService.updateProjectById(PROJECT_ID, updatedTestProject))
                .willReturn(updatedTestProject);
        rest.perform(put(PROJECT_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(rawJson(getUpdatedTestProject())))
                .andExpect(status().isOk())
                .andDo(document("project-update"));
    }

    @Test
    public void shouldNotUpdateProjectNotFound() throws Exception {
        Project updatedTestProject = getUpdatedTestProject();
        given(projectService.updateProjectById(PROJECT_ID, updatedTestProject))
                .willReturn(null);
        rest.perform(put(PROJECT_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(json(new Project())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        given(projectService.deleteProjectById(PROJECT_ID)).willReturn(true);
        rest.perform(delete(PROJECT_ID_PATH))
                .andExpect(status().isNoContent())
                .andDo(document("project-delete"));
    }

    @Test
    public void shouldDeleteAllProjects() throws Exception {
        doNothing().when(projectService).deleteAllProjects();
        rest.perform(delete(ALL_PROJECTS_PATH))
                .andExpect(status().isNoContent());
    }

    @Test
    public void shouldNotDeleteProjectNotFound() throws Exception {
        given(projectService.deleteProjectById(PROJECT_ID)).willReturn(false);
        rest.perform(delete(PROJECT_ID_PATH))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldUpdateProjectById() throws Exception {
        Project updatedTestProject = getUpdatedTestProject();
        updatedTestProject.setId(PROJECT_ID);
        updatedTestProject.setModels(Collections.emptyList());
        given(projectService.updateProjectById(PROJECT_ID, updatedTestProject))
                .willReturn(updatedTestProject);
        rest.perform(put(PROJECT_ID_PATH).contentType(MediaType.APPLICATION_JSON).content(rawJson(updatedTestProject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedTestProject.getName()));
    }

    @Test
    public void shouldNotUpdateProjectById() throws Exception {
        given(projectService.updateProjectById(any(), any()))
                .willThrow(IdException.idNotFound(Project.class, PROJECT_ID));
        rest.perform(put(PROJECT_ID_PATH).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Object of type de.unistuttgart.ipvs.as.mmp.common.domain.Project not found for id 123"));
    }

    @Test
    public void shouldSaveProject() throws Exception {
        Project updatedTestProject = getUpdatedTestProject();
        updatedTestProject.setModels(Collections.emptyList());
        updatedTestProject.setId(PROJECT_ID);
        given(projectService.saveProject(updatedTestProject))
                .willReturn(updatedTestProject);
        rest.perform(post(ALL_PROJECTS_PATH).contentType(MediaType.APPLICATION_JSON).content(rawJson(updatedTestProject)))
                .andExpect(status().isCreated())
                .andExpect(redirectedUrl(PROJECT_ID_PATH))
                .andDo(document("project-create"));
    }

    @Test
    public void shouldNotSaveProject() throws Exception {
        given(projectService.saveProject(any()))
                .willThrow(new NullPointerException());
        rest.perform(post(ALL_PROJECTS_PATH).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("Something went wrong! If this problem persists please talk to your system administrator."));

    }
}
