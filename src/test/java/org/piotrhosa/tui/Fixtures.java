package org.piotrhosa.tui;

import org.piotrhosa.tui.client.model.Branch;
import org.piotrhosa.tui.client.model.Commit;
import org.piotrhosa.tui.client.model.Repo;

import java.util.List;

public class Fixtures {
    public static final Repo repo1 = new Repo("repo1", false);
    public static final Repo repo2 = new Repo("repo2", false);
    public static final Repo repoFork = new Repo("repo3", true);
    public static final List<Branch> branches1 = List.of(new Branch("branch1-1", new Commit("sha1")));
    public static final List<Branch> branches2 = List.of(new Branch("branch2-1", new Commit("sha2")));
}
