package org.piotrhosa.tui.service;

import lombok.RequiredArgsConstructor;
import org.piotrhosa.tui.client.GitHubClient;
import org.piotrhosa.tui.client.model.Repo;
import org.piotrhosa.tui.model.RepoInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GitHubService {
    private final static int REQUEST_PARALLELISM = 5;

    private final GitHubClient gitHubClient;


    public List<RepoInfo> getRepos(String owner) {
        var requestPool = new ForkJoinPool(REQUEST_PARALLELISM);

        var repos = gitHubClient.getRepos(owner)
                .stream()
                .filter(repo -> !repo.fork())
                .toList();

        var repoInfo = Collections.synchronizedList(new ArrayList<RepoInfo>(repos.size()));

        try {
            requestPool.submit(() -> repos.parallelStream().forEach(repo -> {
                var branches = getBranches(owner, repo);
                repoInfo.add(new RepoInfo(repo.name(), owner, branches));
            })).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            requestPool.shutdown();
        }

        return repoInfo;
    }

    private Map<String, String> getBranches(String owner, Repo repo) {
        var branches = gitHubClient.getBranches(owner, repo.name());
        return branches.stream()
                .map(branch -> Map.entry(branch.name(), branch.commit().sha()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
