package org.piotrhosa.tui.model;

import java.util.Map;

public record RepoInfo(String repoName, String owner, Map<String, String> branchToLatestCommit) {
}
