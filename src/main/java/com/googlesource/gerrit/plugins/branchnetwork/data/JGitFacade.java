// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.branchnetwork.data;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevCommitList;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Head;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Parent;

@Singleton
public class JGitFacade {

  public String init(String repoPath) throws IOException {
    Repository repo;
    repo =
        new RepositoryBuilder().setGitDir(new File(repoPath)).setBare().build();
    repo.create(true);
    return String.format("Initialized empty Git repository in %s", repoPath);
  }

  public List<String> getUsersForRepository(String repoPath) throws IOException {
    Set<String> usernames = new LinkedHashSet<String>();
    Repository repo =
        new RepositoryBuilder().setGitDir(new File(repoPath)).setBare().build();
    RevWalk walk = new RevWalk(repo);

    ObjectId headId = repo.resolve(Constants.HEAD);
    walk.markStart(walk.parseCommit(headId));

    for (RevCommit rc : walk) {
      usernames.add(rc.getAuthorIdent().getName());
    }

    return new ArrayList<String>(usernames);
  }

  public List<Commit> logData(String repoName)
      throws IOException {
    final Repository repo =
        new RepositoryBuilder().setGitDir(new File(repoName)).setBare().build();
    final PlotWalk walk = new PlotWalk(repo);

    List<Head> heads = getHeadsForRepository(repoName);
    for (Head head : heads) {
      ObjectId headId = repo.resolve(head.getId());
      walk.markStart(walk.parseCommit(headId));
    }

    walk.sort(RevSort.BOUNDARY, true);
    walk.sort(RevSort.COMMIT_TIME_DESC, true);

    PlotCommitList<PlotLane> pcl = new PlotCommitList<PlotLane>();
    pcl.source(walk);
    pcl.fillTo(Integer.MAX_VALUE);
    Collections.reverse(pcl);

    List<Commit> commits = new LinkedList<Commit>();
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Map<String, Commit> commitsById = new HashMap<String, Commit>(pcl.size());
    for (int i = 0; i < pcl.size(); i++) {
      PlotCommit<PlotLane> pc = pcl.get(i);
      Commit commit = new Commit();

      commit.setAuthor(pc.getAuthorIdent().getName());
      commit.setDate(sdf.format(pc.getAuthorIdent().getWhen()));
      commit.setId(pc.getId().getName());
      commit.setEmail(pc.getAuthorIdent().getEmailAddress());
      commit.setMessage(pc.getFullMessage());
      if (pc.getLane() != null) {
        commit.setSpace(1 + pc.getLane().getPosition());
      } else {
        commit.setSpace(1);
      }
      commit.setTime(i);

      for (RevCommit parentRC : pc.getParents()) {
        Commit parentCommit = commitsById.get(parentRC.getId().getName());
        assert parentCommit != null;
        Parent parent = new Parent();
        parent.setId(parentCommit.getId());
        parent.setTime(parentCommit.getTime());
        parent.setSpace(parentCommit.getSpace());
        commit.addParent(parent);
      }

      commitsById.put(commit.getId(), commit);
      commits.add(commit);
    }

    return commits;
  }

  public List<String> getDatesForRepository(String repoPath) throws IOException {
    List<String> dates = new LinkedList<String>();

    Repository repo;

    repo =
        new RepositoryBuilder().setGitDir(new File(repoPath)).setBare().build();
    final PlotWalk walk = new PlotWalk(repo);

    List<Head> heads = getHeadsForRepository(repoPath);
    for (Head head : heads) {
      ObjectId headId = repo.resolve(head.getId());
      walk.markStart(walk.parseCommit(headId));
    }

    walk.sort(RevSort.COMMIT_TIME_DESC, true);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    RevCommitList<RevCommit> rcl = new RevCommitList<RevCommit>();
    rcl.source(walk);
    rcl.fillTo(Integer.MAX_VALUE);

    for (RevCommit rc : rcl) {
      dates.add(sdf.format(rc.getCommitterIdent().getWhen()));
    }

    Collections.reverse(dates);

    return dates;
  }

  public List<Head> getHeadsForRepository(String repoPath) throws IOException {
    Repository repo =
        new RepositoryBuilder().setGitDir(new File(repoPath)).setBare().build();
    Map<String, Ref> headRefs =
        repo.getRefDatabase().getRefs(Constants.R_HEADS);

    List<Head> heads = new LinkedList<Head>();
    for (String headName : headRefs.keySet()) {
      Head head = new Head();
      head.setName(headName);
      head.setId(headRefs.get(headName).getObjectId().getName());
      heads.add(head);
    }

    return heads;
  }

  public int getBranchesPlotLanesCount(String repoName) throws IOException {
    final Repository repo =
        new RepositoryBuilder().setGitDir(new File(repoName)).setBare().build();
    final PlotWalk walk = new PlotWalk(repo);

    ObjectId headId = repo.resolve(Constants.HEAD);
    if(headId == null)
      return 0;

    walk.markStart(walk.parseCommit(headId));
    walk.sort(RevSort.BOUNDARY, true);

    PlotCommitList<PlotLane> pcl = new PlotCommitList<PlotLane>();
    pcl.source(walk);
    pcl.fillTo(Integer.MAX_VALUE);

    int maxLane = 1;
    for (PlotCommit<PlotLane> pc : pcl) {
      if (pc.getLane() != null) {
        int lane = 1 + pc.getLane().getPosition();
        if (lane > maxLane) maxLane = lane;
      }
    }

    return maxLane;
  }
}
