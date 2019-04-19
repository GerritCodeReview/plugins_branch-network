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
package com.googlesource.gerrit.plugins.branchnetwork.canvas;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.branchnetwork.data.GitCommitCache;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Head;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class NetworkMetaService extends JsonService {
  public static String PATH_SUFFIX = "network_meta";
  private static final long serialVersionUID = 1L;
  private static final int COMMITS_FIRST_IN_FOCUS = 30;

  private final GitCommitCache gitCommitCache;

  @Inject
  public NetworkMetaService(final GitCommitCache gitCommitCache) {
    this.gitCommitCache = gitCommitCache;
  }

  /** Beans intended to be used for JSON serialisation */
  public static class MetaData {
    public List<Block> blocks = new LinkedList<Block>();
    public List<String> dates = new LinkedList<String>();
    public List<User> users = new ArrayList<User>();
    public int focus;
    public String nethash;
  }

  public static class User {
    public String repo;
    public String name;
    public List<Head> heads = new ArrayList<Head>();

    public User(String project, List<Head> heads) {
      this.repo = project;
      this.name = project;
      this.heads = heads;
    }
  }

  public static class Block {
    public String name;
    public int count;
    public int start;

    public Block(String name, int count, int start) {
      this.name = name;
      this.count = count;
      this.start = start;
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    returnRepositoryGraphMetaDataAsJson(req, resp, getProjectName(req));
  }

  public void returnRepositoryGraphMetaDataAsJson(
      HttpServletRequest req, HttpServletResponse resp, String project) throws IOException {

    MetaData metaData = new MetaData();

    int laneCount = 0;

    String nethash = project;

    List<String> dates = gitCommitCache.getDates(project);

    int maxTime = dates.size();

    List<Head> heads = gitCommitCache.getHeads(project);
    metaData.focus = maxTime < COMMITS_FIRST_IN_FOCUS ? 0 : maxTime - COMMITS_FIRST_IN_FOCUS;

    laneCount = gitCommitCache.getBranchesPlotLanesCount(project);
    metaData.blocks.add(new Block(project, laneCount, 0));
    metaData.users.add(new User(project, heads));
    metaData.dates = dates;
    metaData.nethash = nethash;

    returnJsonResponse(resp, metaData);
  }
}
