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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonWriter;
import com.googlesource.gerrit.plugins.branchnetwork.data.json.Parent;

public class JsonService extends HttpServlet {

  private static final long serialVersionUID = -909762366548464514L;

  protected Gson getGson() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Parent.class, new JsonSerializer<Parent>() {
      @Override
      public JsonElement serialize(Parent src, Type typeOfSrc,
          JsonSerializationContext context) {
        return src.toJson();
      }
    });
    Gson gson = gsonBuilder.create();
    return gson;
  }

  protected JsonWriter getJsonWriter(HttpServletResponse resp)
      throws UnsupportedEncodingException, IOException {
        JsonWriter writer =
            new JsonWriter(new OutputStreamWriter(resp.getOutputStream(), "UTF-8"));
        return writer;
      }

  protected void returnJsonResponse(HttpServletResponse resp, Object data)
      throws UnsupportedEncodingException, IOException {
        JsonWriter writer = getJsonWriter(resp);
        try {
          getGson().toJson(data, data.getClass(), writer);
        } finally {
          writer.close();
        }
      }

  protected String getProjectName(HttpServletRequest req) {
    String reqURI = req.getRequestURI();
    String[] reqParts = reqURI.split("/");
    return reqParts[reqParts.length-1];
  }

}
