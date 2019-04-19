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

import com.google.gerrit.common.data.GitwebType;
import com.google.gerrit.extensions.annotations.PluginCanonicalWebUrl;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.config.GitwebConfig;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.permissions.PermissionBackend;
import com.google.gerrit.server.permissions.PermissionBackendException;
import com.google.gerrit.server.permissions.ProjectPermission;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;

@Singleton
public class GitGraphServlet extends HttpServlet {

  private static final long serialVersionUID = -822406714154950673L;
  private static final int PROGRESS_BAR_WIDTH = 208;
  private static final int PROGRESS_BAR_HEIGHT = 13;
  private String canonicalPath;
  private GitRepositoryManager repoManager;
  private GitwebConfig gitWebConfig;
  private String pluginCanonicalWebUrl;
  private final Provider<CurrentUser> user;
  private final PermissionBackend permissionBackend;

  @Inject
  public GitGraphServlet(
      @PluginCanonicalWebUrl String url,
      GitwebConfig gitWebConfig,
      final GitRepositoryManager repoManager,
      Provider<CurrentUser> user,
      PermissionBackend permissionBackend)
      throws MalformedURLException {
    this.canonicalPath = new URL(url).getPath();
    this.pluginCanonicalWebUrl = url.substring(0, url.length() - (canonicalPath.length() - 1));
    this.repoManager = repoManager;
    this.gitWebConfig = gitWebConfig;
    this.user = user;
    this.permissionBackend = permissionBackend;
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
        permissionBackend.user(user.get()).project(nameKey).check(ProjectPermission.ACCESS);
      } catch (AuthException | PermissionBackendException e) {
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

      String networkMetaUrl = String.format("%1$snetwork_meta/%2$s/", canonicalPath, repoName);
      String networkDataChunkUrl =
          String.format("%1$snetwork_data_chunk/%2$s/?nethash=", canonicalPath, repoName);

      GitwebType type = gitWebConfig.getGitwebType();
      if (type == null) {
        out.println("<html>ERROR: invalid gitweb configuration</html>");
      } else {
        String commitUrlPattern =
            gitWebConfig.getUrl()
                + type.getRevision()
                    .replaceAll("\\$\\{project\\}", repoName)
                    .replaceAll("\\$\\{commit\\}", "{2}");
        String projectPattern =
            gitWebConfig.getUrl() + type.getProject().replaceAll("\\$\\{project\\}", repoName);

        commitUrlPattern =
            (commitUrlPattern.startsWith("/")
                ? commitUrlPattern
                : pluginCanonicalWebUrl + commitUrlPattern);
        projectPattern =
            (projectPattern.startsWith("/")
                ? projectPattern
                : pluginCanonicalWebUrl + projectPattern);

        String header =
            "<html>\n"
                + "<head>\n"
                + "<style type=\"text/css\">\n"
                + "div#progress\n"
                + "{\n"
                + "position:absolute;\n"
                + "left:"
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
            "<script type=\"text/javascript\" src=\""
                + canonicalPath
                + "static/jquery-1.4.2.min.js\"></script>\n"
                + "<script type=\"text/javascript\" src=\""
                + canonicalPath
                + "static/network.js\"></script>\n"
                + "<script type=\"text/javascript\">"
                + "$(document).ready( function() {\n"
                + "new NetworkCanvas('network-canvas', "
                + width
                + ", "
                + height
                + " , null, null, null,\n"
                + "'"
                + commitUrlPattern
                + "',\n"
                + "'"
                + projectPattern
                + "',\n"
                + "'"
                + networkMetaUrl
                + "',\n"
                + "'"
                + networkDataChunkUrl
                + "');\n"
                + "});\n"
                + "</script>\n";
        String canvas =
            "<canvas id=\"network-canvas\" width=\""
                + width
                + "\" height=\""
                + height
                + "\" style=\"cursor: default; \"></canvas>";
        String footer = "</div>" + "</body>\n" + "</html>\n";
        out.println(naked ? (javaScript + canvas) : (header + javaScript + canvas + footer));
      }
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
