package de.unistuttgart.ipvs.as.mmp.scoring.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringInput;
import de.unistuttgart.ipvs.as.mmp.common.domain.scoring.ScoringOutput;
import de.unistuttgart.ipvs.as.mmp.common.exception.MMPExceptionHandler;
import de.unistuttgart.ipvs.as.mmp.scoring.service.ScoringService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, RestDocumentationExtension.class})
@WebMvcTest(controllers = {ScoringController.class})
public class ScoringControllerTest {

    private static final Long PROJECT_ID = 1337L;
    private static final Long MODEL_ID = 1337L;
    private static final List<ScoringInput> INPUTS = new ArrayList<>();
    private static final String PATH = ScoringController.PATH + "/" + PROJECT_ID + "/models/" + MODEL_ID + "/scoring";
    private static List<ScoringOutput> OUTPUTS = new ArrayList<ScoringOutput>();

    private MockMvc rest;
    @MockBean
    private ScoringService scoringService;

    @Autowired
    private ScoringController scoringController;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        this.rest = MockMvcBuilders.standaloneSetup(this.scoringController)
                .setControllerAdvice(new MMPExceptionHandler(), this.scoringController)
                .apply(documentationConfiguration(restDocumentation).
                        operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .build();
    }

    @Test
    public void shouldBadRequest() throws Exception {
        given(scoringService.scoreModel(PROJECT_ID, MODEL_ID, INPUTS)).willReturn(new ArrayList<ScoringOutput>());
        rest.perform(post(PATH)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldScoreModel() throws Exception {
        OUTPUTS.add( new ScoringOutput("test", "test"));
        given(scoringService.scoreModel(PROJECT_ID, MODEL_ID, INPUTS)).willReturn(OUTPUTS);
        rest.perform(post(PATH).contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(OUTPUTS.get(0).getName()))
                .andExpect(jsonPath("$[0].value").value(OUTPUTS.get(0).getValue()))
                .andDo(document("score"));
    }
}
