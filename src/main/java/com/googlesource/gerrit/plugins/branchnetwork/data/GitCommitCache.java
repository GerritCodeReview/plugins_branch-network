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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Config;

import com.google.common.cache.CacheLoader;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Head;

@Singleton
public class GitCommitCache extends CacheLoader<String, List<Commit>> {
  public static final String GRAPH_DATA_CACHE = "NetworkGraphDataCache";

  private final Config gerritEntConfig;
  private final SitePaths sitePaths;

  @Inject
  public GitCommitCache(@GerritServerConfig final Config gerritConfig,
      final SitePaths sitePaths) {
    this.gerritEntConfig = gerritConfig;
    this.sitePaths = sitePaths;
  }

  private class StringListWrapper {
    List<String> strings = new ArrayList<String>();

    StringListWrapper wrap(List<String> strings) {
      StringListWrapper wrapper = new StringListWrapper();
      wrapper.strings = strings;
      return wrapper;
    }

    public void println(PrintWriter out) {
      out.println("<strings>");
      for (String string : strings) {
        out.println("<string>" + string + "</string>");
      }
      out.println("</strings>");
    }
  }

  @Inject
  private JGitFacade git;

  public void printDates(PrintWriter out, String project) throws IOException {
    List<String> dates = git.getDatesForRepository(getRepositoryPath(project));
    new StringListWrapper().wrap(dates).println(out);
  }

  public void printUsers(PrintWriter out, String project) throws IOException {
    List<String> usernames =
        git.getUsersForRepository(getRepositoryPath(project));
    new StringListWrapper().wrap(usernames).println(out);
  }

  public List<Head> getHeads(String project) throws IOException {
    List<Head> heads = git.getHeadsForRepository(getRepositoryPath(project));
    return heads;
  }

  @Override
  public List<Commit> load(String project) throws IOException, ParseException {
    String repositoryPath = getRepositoryPath(project);

    final List<Commit> commits = git.logData(repositoryPath);
    return commits;
  }

  public List<String> getDates(String project) throws IOException {
    return git.getDatesForRepository(getRepositoryPath(project));
  }


  private String getRepositoryPath(String project) throws FileNotFoundException {
    File gitRepoDir = new File(getBaseGitPath(), project + ".git");
    if (!gitRepoDir.exists())
      throw new FileNotFoundException("Cannot find Git repository "
          + gitRepoDir.getAbsolutePath());

    return gitRepoDir.getAbsolutePath();
  }

  public File getBaseGitPath() {
    String basePath = gerritEntConfig.getString("gerrit", null, "basePath");
    File basePathFile = sitePaths.resolve(basePath);
    return basePathFile;
  }

  public int getBranchesPlotLanesCount(String project) throws IOException {
    return git.getBranchesPlotLanesCount(getRepositoryPath(project));
  }

}
