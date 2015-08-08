Git branch network graph
========================

GitWeb configuration
--------------------

In order to use branch-network plugin as GitWeb project viewer replacement
simply add the following lines to your existing Gerrit config under the
GitWeb section (assuming the plugin was copied as branch-network.jar):

```
  type = custom
  url = plugins/branch-network/
  project = ?p=${project}
  branch = ?p=${project}
  revision = ?p=${project}
  filehistory = ?p=${project}
  roottree = ?p=${project}
  file = ?p=${project}
```

Note that the `project, ``branch`, `revision` and `filehistory` settings
must all be specified, otherwise Gerrit will disable the configuration.

Usage in other GWT or JavaScript UI
-----------------------------------

The branch network canvas can be returned as HTML fragment to allow other
GWT UI plugins the rendering the network graph in other ways.
(i.e. adding an extra link on the Project details page and display the
canvas on the right side panel)

The syntax of branch-network plugin URL is similar to the one used in
the GitWeb scenarios but with extra parameters to allow the "UX surgery"
of the canvas in another UE.

Additional parameters:

naked=y
:	allows to have the HTML5 Canvas and JavaScript without outer HTML page mark-up
	element.

width=N
:	HTML5 Canvas width in pixels

height=M
:	HTML5 Canvas height in pixels

Example
-------

The following URL allows to get a 1024x768 HTML5 Canvas in a naked HTML fragment.

```
  branch-network/?p=${project}&naked=y&width=1024&height=768
```

