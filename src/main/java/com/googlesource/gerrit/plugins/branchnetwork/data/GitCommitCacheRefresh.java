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

import java.util.List;

import com.google.common.cache.LoadingCache;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.extensions.events.NewProjectCreatedListener;
import com.google.gerrit.server.git.LocalDiskRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;

public class GitCommitCacheRefresh implements GitReferenceUpdatedListener,
    NewProjectCreatedListener {
  private LoadingCache<String, List<Commit>> networkGraphDataCache;
  private final LocalDiskRepositoryManager repoManager;

  @Inject
  public GitCommitCacheRefresh(
      @Named(GitCommitCache.GRAPH_DATA_CACHE) final LoadingCache<String, List<Commit>> networkGraphDataCache,
      final LocalDiskRepositoryManager repoManager) {
    this.networkGraphDataCache = networkGraphDataCache;
    this.repoManager = repoManager;
  }

  @Override
  public void onNewProjectCreated(
      com.google.gerrit.extensions.events.NewProjectCreatedListener.Event event) {
    networkGraphDataCache.refresh(event.getProjectName());
    repoManager.list();

  }

  @Override
  public void onGitReferenceUpdated(GitReferenceUpdatedListener.Event event) {
    networkGraphDataCache.refresh(event.getProjectName());
  }
}
