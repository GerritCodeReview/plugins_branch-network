Git branch network graph
=========================

This plugin allows the rendering of Git repository branch network
in a graphical HTML5 Canvas. It is mainly intended to be used as
"project link" in a GitWeb configuration or by other Gerrit GWT UI
plugins to be plugged elsewhere in Gerrit.

GitWeb configuration
--------------------

In order to use branch-network plugin as GitWeb project viewer replacement
simply add the following line to your existing Gerrit config under the
GitWeb section:

```
  project = branch-network-1.0-SNAPSHOT/?p=${project}
```

Usage in other GWT or JavaScript UI
-----------------------------------

Branch network canvas can be returned as HTML fragment to allow other
GWT UI plugins to allow the rendering of network graph in other ways.
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
  branch-network-1.0-SNAPSHOT/?p=${project}&naked=y&width=1024&height=768
```

