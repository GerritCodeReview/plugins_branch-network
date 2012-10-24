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

import com.google.inject.servlet.ServletModule;
import com.googlesource.gerrit.plugins.branchnetwork.canvas.GitGraphServlet;
import com.googlesource.gerrit.plugins.branchnetwork.canvas.NetworkDataChunkService;
import com.googlesource.gerrit.plugins.branchnetwork.canvas.NetworkMetaService;

public class NetworkGraphModule extends ServletModule {
  @Override
  protected void configureServlets() {
    serve("/").with(GitGraphServlet.class);
    serve("/network_meta/*").with(NetworkMetaService.class);
    serve("/network_data_chunk/*").with(NetworkDataChunkService.class);
  }
}
