package org.piotrhosa.tui;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piotrhosa.tui.exception.NoSuchUser;
import org.piotrhosa.tui.model.ListReposResponse;
import org.piotrhosa.tui.model.ServiceError;
import org.piotrhosa.tui.service.GitHubService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "TUI GitHub API")
public class Controller {

    private final GitHubService gitHubService;

    @Operation(summary = "Fetch all repos and branches for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success"),
            @ApiResponse(responseCode = "404", description = "no such user", content = {@Content(schema = @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "406", description = "bad media type requested", content = {@Content(schema = @Schema(implementation = ServiceError.class))})
    })
    @GetMapping(value = "/owners/{owner}/repos", produces = MediaType.APPLICATION_JSON_VALUE)
    ListReposResponse getRepos(@PathVariable String owner) {

        var repoInfos = gitHubService.getRepos(owner);
        return new ListReposResponse(repoInfos);
    }

    @ExceptionHandler(NoSuchUser.class)
    public ResponseEntity<ServiceError> handleNoSuchUserException(NoSuchUser exception) {
        return ResponseEntity.status(404).body(new ServiceError(404, "no such user"));
    }
}
