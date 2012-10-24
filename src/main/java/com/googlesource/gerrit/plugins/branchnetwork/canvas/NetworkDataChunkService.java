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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.cache.LoadingCache;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.extensions.events.NewProjectCreatedListener;
import com.google.gerrit.server.git.LocalDiskRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.googlesource.gerrit.plugins.branchnetwork.data.GitCommitCache;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Data;

@Singleton
public class NetworkDataChunkService extends JsonService {

  private static final long serialVersionUID = -920473696617437261L;
  private static final int MAX_COMMIT_CHUNK_LEN = 100;
  public static String PATH_SUFFIX = "network_data_chunk";
  private final LoadingCache<String, List<Commit>> networkGraphDataCache;

  @Inject
  public NetworkDataChunkService(
      @Named(GitCommitCache.GRAPH_DATA_CACHE) final LoadingCache<String, List<Commit>> networkGraphDataCache) {
    this.networkGraphDataCache = networkGraphDataCache;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      printJson(req, resp, getProjectName(req));
    } catch (ExecutionException e) {
      throw new ServletException(e);
    }
  }

  public void printJson(HttpServletRequest req, HttpServletResponse resp,
      String project) throws IOException, ExecutionException {

    String domainRepoName = req.getParameter("nethash");

    List<Commit> commits = networkGraphDataCache.get(domainRepoName);
    int endInt = commits.size() - 1;
    int startInt = 0;

    String start = req.getParameter("start");
    if (start != null) startInt = Integer.parseInt(start);

    String end = req.getParameter("end");
    if (end != null) endInt = Integer.parseInt(end);

    if ((endInt - startInt) > MAX_COMMIT_CHUNK_LEN)
      startInt = endInt - MAX_COMMIT_CHUNK_LEN;

    Data data = new Data(commits.subList(startInt, endInt + 1));
    returnJsonResponse(resp, data);
  }

}
