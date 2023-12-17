package org.piotrhosa.tui;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.piotrhosa.tui.client.model.Branch;
import org.piotrhosa.tui.client.model.Commit;
import org.piotrhosa.tui.client.model.Repo;
import org.piotrhosa.tui.model.ListReposResponse;
import org.piotrhosa.tui.model.RepoInfo;
import org.piotrhosa.tui.model.ServiceError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AppTest {

    public final Repo repo1 = new Repo("repo1", false);
    public final Repo repo2 = new Repo("repo2", false);
    public final List<Branch> branches1 = List.of(new Branch("branch1-1", new Commit("sha1")));
    public final List<Branch> branches2 = List.of(new Branch("branch2-1", new Commit("sha2")));

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    RestTemplate restTemplate;

    MockMvc mockMvc;
    MockRestServiceServer mockServer;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }


    @Test
    public void testGetReposReturnsEmpty() throws Exception {

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.github.com/users/test-user/repos")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "test-token"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("[]")
                );


        mockMvc.perform(get("http://localhost:8080/owners/test-user/repos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{}"));

    }

    @Test
    public void testGetReposReturnsErrorOnNonJsonAccept() throws Exception {
        var expectedError = new ServiceError(406, "service only returns application/json");
        var expectedErrorString = mapper.writeValueAsString(expectedError);
        mockMvc.perform(get("http://localhost:8080/owners/test-user/repos")
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().is(406))
                .andExpect(content().json(expectedErrorString));
    }

    @Test
    public void testGetReposReturnsErrorOnNoSuchUser() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.github.com/users/test-user/repos")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "test-token"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                );

        var expectedError = new ServiceError(404, "no such user");
        var expectedErrorString = mapper.writeValueAsString(expectedError);

        mockMvc.perform(get("http://localhost:8080/owners/test-user/repos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(content().json(expectedErrorString));
    }

    @Test
    public void testGetReposReturnsListOfRepoInfo() throws Exception {

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.github.com/users/test-user/repos")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "test-token"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(List.of(repo1)))
                );


        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://api.github.com/repos/test-user/repo1/branches")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "test-token"))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(branches1))
                );


        var expectedRepoInfo = List.of(new RepoInfo("repo1", "test-user", Map.of("branch1-1", "sha1")));
        var expectedResponse = new ListReposResponse(expectedRepoInfo);
        var expectedRepoInfoString = mapper.writeValueAsString(expectedResponse);

        mockMvc.perform(get("http://localhost:8080/owners/test-user/repos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedRepoInfoString));

    }
}
