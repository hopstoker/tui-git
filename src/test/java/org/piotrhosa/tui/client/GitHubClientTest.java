package org.piotrhosa.tui.client;

import org.junit.jupiter.api.Test;
import org.piotrhosa.tui.client.model.Branch;
import org.piotrhosa.tui.client.model.Repo;
import org.piotrhosa.tui.exception.NoSuchUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.piotrhosa.tui.Fixtures.*;

@SpringBootTest
public class GitHubClientTest {

    @MockBean
    RestTemplate restTemplate;

    @Autowired
    private GitHubClient gitHubClient;

    @Test
    public void testGetReposReturnsRepos() {
        var expectedRepos = new Repo[]{repo1, repo2};
        var response = ResponseEntity.ok((Object) expectedRepos);
        when(restTemplate.getForEntity(eq("https://api.github.com/users/test-user/repos"), any())).thenReturn(response);

        var actualRepos = gitHubClient.getRepos("test-user");

        assertEquals(Arrays.asList(expectedRepos), actualRepos);
    }

    @Test
    public void testGetReposThrowsNoSuchUser() {
        when(restTemplate.getForEntity(eq("https://api.github.com/users/test-user/repos"), any())).thenThrow(mock(HttpClientErrorException.NotFound.class));

        assertThrows(NoSuchUser.class, () -> gitHubClient.getRepos("test-user"));
    }

    @Test
    public void testGetBranchesReturnsBranches() {
        var expectedBranches = branches1.toArray(Branch[]::new);
        var response = ResponseEntity.ok((Object) expectedBranches);
        when(restTemplate.getForEntity(eq("https://api.github.com/repos/test-user/repo1/branches"), any())).thenReturn(response);

        var actualRepos = gitHubClient.getBranches("test-user", "repo1");

        assertEquals(branches1, actualRepos);
    }
}
