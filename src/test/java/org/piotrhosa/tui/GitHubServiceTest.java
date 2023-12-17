package org.piotrhosa.tui;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.piotrhosa.tui.client.GitHubClient;
import org.piotrhosa.tui.exception.NoSuchUser;
import org.piotrhosa.tui.model.RepoInfo;
import org.piotrhosa.tui.service.GitHubService;
import reactor.core.Exceptions;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.piotrhosa.tui.Fixtures.*;

public class GitHubServiceTest {

    private final GitHubClient gitHubClient = mock(GitHubClient.class);
    private final GitHubService gitHubService = new GitHubService(gitHubClient);

    private final String gitHubUser = "githubuser";

    @Test
    public void testGetReposReturnsEmptyList() {
        when(gitHubClient.getRepos(gitHubUser)).thenReturn(List.of());

        var actualRepos = gitHubService.getRepos(gitHubUser);

        Assertions.assertEquals(List.of(), actualRepos);
    }

    @Test
    public void testGetReposThrowsNoSuchUser() {
        when(gitHubClient.getRepos(gitHubUser)).thenThrow(new NoSuchUser());

        Assertions.assertThrows(NoSuchUser.class, () -> {
            gitHubService.getRepos(gitHubUser);
        });
    }

    @Test
    public void testGetReposReturnsListNoBranches() {
        when(gitHubClient.getRepos(gitHubUser)).thenReturn(List.of(repo1, repo2));
        when(gitHubClient.getBranches(gitHubUser, repo1.name())).thenReturn(List.of());
        when(gitHubClient.getBranches(gitHubUser, repo2.name())).thenReturn(List.of());

        var actualRepos = gitHubService.getRepos(gitHubUser);

        Assertions.assertEquals(List.of(
                new RepoInfo("repo1", gitHubUser, Map.of()),
                new RepoInfo("repo2", gitHubUser, Map.of())
        ), actualRepos);
    }

    @Test
    public void testGetReposReturnsNoForks() {
        when(gitHubClient.getRepos(gitHubUser)).thenReturn(List.of(repo1, repoFork));
        when(gitHubClient.getBranches(gitHubUser, repo1.name())).thenReturn(List.of());

        var actualRepos = gitHubService.getRepos(gitHubUser);

        Assertions.assertEquals(List.of(
                new RepoInfo("repo1", gitHubUser, Map.of())
        ), actualRepos);
    }

    @Test
    public void testGetReposReturnsListOfRepoInfo() {
        when(gitHubClient.getRepos(gitHubUser)).thenReturn(List.of(repo1, repo2));
        when(gitHubClient.getBranches(gitHubUser, repo1.name())).thenReturn(branches1);
        when(gitHubClient.getBranches(gitHubUser, repo2.name())).thenReturn(branches2);

        var actualRepos = gitHubService.getRepos(gitHubUser);

        Assertions.assertEquals(List.of(
                new RepoInfo("repo1", gitHubUser, Map.of("branch1-1", "sha1")),
                new RepoInfo("repo2", gitHubUser, Map.of("branch2-1", "sha2"))
        ), actualRepos);
    }

    @Test
    public void testGetReposThrowsIncompleteRequest() {
        when(gitHubClient.getRepos(gitHubUser)).thenReturn(List.of(repo1, repo2));
        when(gitHubClient.getBranches(gitHubUser, repo1.name())).thenReturn(branches1);
        when(gitHubClient.getBranches(gitHubUser, repo2.name())).thenThrow(Exceptions.retryExhausted("too many retries", null));

        Assertions.assertThrows(RuntimeException.class, () -> gitHubService.getRepos(gitHubUser));
    }
}
