package org.piotrhosa.tui.service;

import lombok.RequiredArgsConstructor;
import org.piotrhosa.tui.client.GitHubClient;
import org.piotrhosa.tui.client.model.Repo;
import org.piotrhosa.tui.model.RepoInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;

    public List<RepoInfo> getRepos(String owner) {
        var repos = gitHubClient.getRepos(owner);
        return repos.stream()
                .filter(repo -> !repo.fork())
                .map(repo -> new RepoInfo(repo.name(), owner, getBranches(owner, repo)))
                .collect(Collectors.toList());
    }

    private Map<String, String> getBranches(String owner, Repo repo) {
        var branches = gitHubClient.getBranches(owner, repo.name());
        return branches.stream()
                .map(branch -> Map.entry(branch.name(), branch.commit().sha()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
