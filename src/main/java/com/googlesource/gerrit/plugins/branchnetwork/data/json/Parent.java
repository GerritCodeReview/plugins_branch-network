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
package com.googlesource.gerrit.plugins.branchnetwork.data.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class Parent {

  private String id;

  private int time;

  private int space;

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public int getTime() {
    return time;
  }

  public void setSpace(int space) {
    this.space = space;
  }

  public int getSpace() {
    return space;
  }

  public JsonElement toJson() {
    JsonArray jsonOut = new JsonArray();
    jsonOut.add(new JsonPrimitive(id));
    jsonOut.add(new JsonPrimitive(time));
    jsonOut.add(new JsonPrimitive(space));
    return jsonOut;
  }
}
