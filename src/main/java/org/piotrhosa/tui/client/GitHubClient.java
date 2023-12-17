package org.piotrhosa.tui.client;

import lombok.RequiredArgsConstructor;
import org.piotrhosa.tui.client.model.Branch;
import org.piotrhosa.tui.client.model.Repo;
import org.piotrhosa.tui.config.ConfigProperties;
import org.piotrhosa.tui.exception.NoSuchUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubClient {

    private final RestTemplate restTemplate;
    private final ConfigProperties configProperties;

    Logger logger = LoggerFactory.getLogger(GitHubClient.class);

    public List<Repo> getRepos(String user) {
        //https://api.github.com/users/ORG/repos
        try {
            var url = String.format("%s/users/%s/repos", configProperties.getUrl(), user);

            var response = restTemplate.getForEntity(url, Repo[].class);
            return Arrays.asList(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            throw new NoSuchUser();
        }
    }

    public List<Branch> getBranches(String owner, String repo) {
        //https://api.github.com/repos/OWNER/REPO/branches

        var url = String.format("%s/repos/%s/%s/branches", configProperties.getUrl(), owner, repo);
        var response = restTemplate.getForEntity(url, Branch[].class);
        return Arrays.asList(response.getBody());
    }

    protected record BranchList(List<Branch> branches) {
    }
}
