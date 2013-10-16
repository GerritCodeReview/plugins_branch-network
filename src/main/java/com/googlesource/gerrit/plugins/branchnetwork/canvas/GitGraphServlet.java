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
import java.io.PrintWriter;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;

import com.google.common.base.Objects;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.httpd.GitWebConfig;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.project.NoSuchProjectException;
import com.google.gerrit.server.project.ProjectControl;
import com.google.gerrit.server.project.ProjectControl.Factory;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GitGraphServlet extends HttpServlet {

  private static final long serialVersionUID = -822406714154950673L;
  private static final int PROGRESS_BAR_WIDTH = 208;
  private static final int PROGRESS_BAR_HEIGHT = 13;
  private String canonicalPath;
  private Factory projectControl;
  private GitRepositoryManager repoManager;
  private GitWebConfig gitWebConfig;
  private String canonicalWebUrl;

  @Inject
  public GitGraphServlet(@PluginName String pluginName,
      @GerritServerConfig final Config gerritConfig, GitWebConfig gitWebConfig,
      final ProjectControl.Factory projectControl,
      final GitRepositoryManager repoManager)
      throws MalformedURLException {
    this.canonicalWebUrl =
        Objects.firstNonNull(gerritConfig.getString("gerrit", null, "canonicalWebUrl"), "/");
    if(!canonicalWebUrl.endsWith("/")) {
      canonicalWebUrl += "/";
    }
    this.canonicalPath =
        String.format("%splugins/%s/", canonicalWebUrl, pluginName);
    this.projectControl = projectControl;
    this.repoManager = repoManager;
    this.gitWebConfig = gitWebConfig;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html;charset=UTF-8");
    PrintWriter out = resp.getWriter();

    try {
      String repoName = req.getParameter("p");
      if (repoName == null) {
        resp.sendRedirect("Documentation/index.html");
        return;
      }

      boolean naked = req.getParameter("naked") != null;
      int width = getParam(req, "width", 920);
      int height = getParam(req, "height", 600);

      if (repoName.endsWith(".git")) {
        repoName = repoName.substring(0, repoName.length() - 4);
      }

      final Project.NameKey nameKey = new Project.NameKey(repoName);
      try {
        projectControl.validateFor(nameKey);
      } catch (NoSuchProjectException e) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        return;
      }

      Repository repo = null;
      try {
        repo = repoManager.openRepository(nameKey);
      } catch (RepositoryNotFoundException e) {
        getServletContext().log("Cannot open repository", e);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return;
      } finally {
        if (repo != null) {
          repo.close();
        }
      }

      String networkMetaUrl =
          String.format("%1$snetwork_meta/%2$s/", canonicalPath, repoName);
      String networkDataChunkUrl =
          String.format("%1$snetwork_data_chunk/%2$s/?nethash=", canonicalPath,
              repoName);
      String commitUrlPattern =
          gitWebConfig.getUrl()
              + gitWebConfig.getGitWebType().getRevision()
                  .replaceAll("\\$\\{project\\}", repoName)
                  .replaceAll("\\$\\{commit\\}", "{2}");
      String projectPattern =
          gitWebConfig.getUrl()
              + gitWebConfig.getGitWebType().getProject()
                  .replaceAll("\\$\\{project\\}", repoName);

      commitUrlPattern =
          (commitUrlPattern.startsWith("/") ? commitUrlPattern : canonicalWebUrl
              + commitUrlPattern);
      projectPattern =
          (projectPattern.startsWith("/") ? projectPattern : canonicalWebUrl
              + projectPattern);

      String header =
          "<html>\n" + "<head>\n" + "<style type=\"text/css\">\n"
              + "div#progress\n" + "{\n" + "position:absolute;\n" + "left:"
              + (width - PROGRESS_BAR_WIDTH) / 2
              + "px;\n"
              + "top:"
              + (height - PROGRESS_BAR_HEIGHT) / 2
              + "px;\n"
              + "z-index:-1;\n"
              + "}\n"
              + "div#graph\n"
              + "{\n"
              + "position:absolute;\n"
              + "left:0px;\n"
              + "top:0px;\n"
              + "z-index:0;\n"
              + "}\n"
              + "</style>\n"
              + "</head>\n"
              + "<body>\n"
              + "<div id=\"progress\" >"
              + "<img src=\""
              + canonicalPath
              + "static/progress_bar.gif\" />"
              + "</div>"
              + "<div id=\"graph\">";
      String javaScript =
          "<script type=\"text/javascript\" src=\"" + canonicalPath
              + "static/jquery-1.4.2.min.js\"></script>\n"
              + "<script type=\"text/javascript\" src=\"" + canonicalPath
              + "static/network.js\"></script>\n"
              + "<script type=\"text/javascript\">"
              + "$(document).ready( function() {\n"
              + "new NetworkCanvas('network-canvas', " + width + ", " + height
              + " , null, null, null,\n" + "'" + commitUrlPattern + "',\n"
              + "'" + projectPattern + "',\n" + "'" + networkMetaUrl + "',\n"
              + "'" + networkDataChunkUrl + "');\n" + "});\n" + "</script>\n";
      String canvas =
          "<canvas id=\"network-canvas\" width=\"" + width + "\" height=\""
              + height + "\" style=\"cursor: default; \"></canvas>";
      String footer = "</div>" + "</body>\n" + "</html>\n";
      out.println(naked ? (javaScript + canvas)
          : (header + javaScript + canvas + footer));
    } finally {
      out.close();
    }
  }

  private int getParam(HttpServletRequest req, String name, int defValue) {
    String value = req.getParameter(name);
    if (value != null) {
      return Integer.parseInt(value);
    } else {
      return defValue;
    }
  }
}
