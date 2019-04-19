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
package com.googlesource.gerrit.plugins.branchnetwork;

import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.extensions.events.NewProjectCreatedListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.cache.CacheModule;
import com.google.inject.TypeLiteral;
import com.googlesource.gerrit.plugins.branchnetwork.data.GitCommitCache;
import com.googlesource.gerrit.plugins.branchnetwork.data.GitCommitCacheRefresh;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Commit;
import java.util.List;

public class GitCommitCacheModule extends CacheModule {

  @Override
  protected void configure() {
    cache(GitCommitCache.GRAPH_DATA_CACHE, String.class, new TypeLiteral<List<Commit>>() {})
        .loader(GitCommitCache.class);

    DynamicSet.bind(binder(), GitReferenceUpdatedListener.class).to(GitCommitCacheRefresh.class);
    DynamicSet.bind(binder(), NewProjectCreatedListener.class).to(GitCommitCacheRefresh.class);
  }
}
