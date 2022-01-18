Pega-TracerViewer
==============

[![Java CI with Gradle](https://github.com/pegasystems/pega-tracerviewer/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/pegasystems/pega-tracerviewer/actions/workflows/gradle.yml)
[![GitHub tag](https://img.shields.io/github/release/pegasystems/pega-tracerviewer.svg)](https://github.com/pegasystems/pega-tracerviewer/releases)

Pega-TracerViewer is a Java Swing based tool to view and analyse Pega Tracer xml files.

This tool show the tracer xml file entries in a table or tree format. 

Can open bigger tracer xml files. In case of OOM, change the heap size in the cmd file and try again.

**Features**

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
  * Supports Cosmos React application trace , the output additionally includes DX API interaction IDs and DX API paths.

**Build from source :**

To build the project use the following command:
  ```
  $ ./gradlew build
  ```

**How it works :**

- **Note: Java is required and JAVA_HOME env variable needs to be set to run this applicaiton.**

The tool can be downloaded from [Releases page](https://github.com/pegasystems/pega-tracerviewer/releases) under **Downloads** section.

1. Extract 'pega-tracerviewer-<*version*>.zip' to a folder.
2. The tool is launched using the one of the scripts in the ‘bin’ folder.
	For Windows         - ‘pega-tracerviewer.bat’
	For ubuntu and Mac  - ‘pega-tracerviewer’
3. Default heap size is set to 2G.
4. The heap size and other jvm parameter can be updated by modifying the launch script and updating the ‘DEFAULT_JVM_OPTS’ variable.
