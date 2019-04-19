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

import java.util.ArrayList;
import java.util.List;

public class Commit {

  private String author;
  private List<Parent> parents = new ArrayList<Parent>();
  private int time;
  private String date;
  private String id;
  private int space;
  private String email;
  private String message;

  public String getAuthor() {
    return author;
  }

  public List<Parent> getParents() {
    return parents;
  }

  public int getTime() {
    return time;
  }

  public String getDate() {
    return date;
  }

  public String getId() {
    return id;
  }

  public int getSpace() {
    return space;
  }

  public String getEmail() {
    return email;
  }

  public String getMessage() {
    return message;
  }

  public void addParent(Parent parent) {
    this.parents.add(parent);
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setParents(List<Parent> parents) {
    this.parents = parents;
  }

  public void setTime(int time) {
    this.time = time;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setSpace(int space) {
    this.space = space;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return id + " (" + date + ")";
  }
}
