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

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Head;

@Singleton
public class GitCommitCache extends CacheLoader<String, List<Commit>> {
  public static final String GRAPH_DATA_CACHE = "NetworkGraphDataCache";

  @Inject
  private JGitFacade git;

  public List<Head> getHeads(String project) throws IOException {
    return git.getHeadsForRepository(project);
  }

  @Override
  public List<Commit> load(String project) throws IOException, ParseException {
    return git.logData(project);
  }

  public List<String> getDates(String project) throws IOException {
    return git.getDatesForRepository(project);
  }

  public int getBranchesPlotLanesCount(String project) throws IOException {
    return git.getBranchesPlotLanesCount(project);
  }

}
