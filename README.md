[![Build Status](https://travis-ci.org/pegasystems/pega-tracerviewer.svg?branch=master)](https://travis-ci.org/pegasystems/pega-tracerviewer)

Pega-TracerViewer
==============
Pega-TracerViewer is a Java Swing based tool to view Pega Tracer xml files.

The tool can be downloaded from [Releases page](https://github.com/pegasystems/pega-tracerviewer/releases) under **Downloads** section.

Extract 'pega-tracerviewer-<*version*>-bin.zip' to a folder.

Run 'pega-tracerviewer-<*version*>.cmd' to launch the tool.

Features
----------
This tool show the tracer xml file entries in a table or tree format. 

Can open big tracer xml files. In case of OOM, change the heap size in the cmd file and try again.

Features
  * View the tracer xml in Tree mode and Table mode
  * Search within the Tracer output for useful content. results are highlighted in yellow
  * Compare working vs non-working Tracer output for issue diagnosis.
  * Set Filters for Event Types in on table column
  * Overview dialog to show (if any)
    1. Failed events
    2. Elapsed time sorted in descending order.
    3. Current Search results
    4. List of bookmarks.
    
	Point 1. & 2. only pick events that are innermost (leaf) in the event execution hierarchy.
	
	
Build and Runtime
-----
needs JDK8.

To build the project use the following command:
```
$ mvn clean package
```

The release build is 'pega-tracerviewer-<*version*>-bin.zip' file under `'\target\'` folder.
