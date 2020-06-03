package de.unistuttgart.ipvs.as.mmp.search.v1.controller;

import de.unistuttgart.ipvs.as.mmp.common.domain.Model;
import de.unistuttgart.ipvs.as.mmp.search.domain.dto.FilterDto;
import de.unistuttgart.ipvs.as.mmp.search.domain.dto.SearchDto;
import de.unistuttgart.ipvs.as.mmp.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.unistuttgart.ipvs.as.mmp.search.v1.controller.SearchController.PATH;

@Controller
@CrossOrigin
@RequestMapping(value = PATH)
public class SearchController {

    public static final String PATH = "/v1/advsearch";

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<FilterDto> collectFilters() {
        FilterDto filterDto = FilterDto.builder().possibleAlgorithmsToFilter(searchService.collectAlgorithms())
                .possibleMachineNamesToFilter(searchService.collectMachineNodes())
                .possibleSensorNamesToFilter(searchService.collectSensorNodes())
                .build();

        return ResponseEntity.ok(filterDto);
    }

    @PostMapping
    public ResponseEntity<List<Model>> search(@RequestBody SearchDto searchDto) {
        return ResponseEntity.ok(searchService.getModelsByFiltersAndSearchTerms(searchDto.getSearchTerms(),
                searchDto.getAlgorithmsToFilterFor(), searchDto.getMachineNamesToFilterFor(),
                searchDto.getSensorNamesToFilterFor()));
    }

}
