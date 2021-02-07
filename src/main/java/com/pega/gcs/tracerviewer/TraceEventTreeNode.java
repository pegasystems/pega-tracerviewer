/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TraceEventTreeNode extends AbstractTraceEventTreeNode {

    private static final long serialVersionUID = 6721834740223154002L;

    private String nodeName;

    public TraceEventTreeNode(TraceEvent traceEvent, String nodeName) {
        super(traceEvent);

        this.nodeName = nodeName;
    }

    @Override
    public int compareTo(AbstractTreeTableNode other) {
        // Don't want to perform sort here
        return 0;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    // 0 based column index
    public String getNodeValue(int column) {
        String nodeValue = null;

        return nodeValue;
    }

    @Override
    public Object[] getNodeElements() {

        String[] elementArray = new String[] {};

        return elementArray;
    }

    @Override
    public List<TraceEvent> getTraceEvents() {

        List<TraceEvent> traceEventList = new ArrayList<TraceEvent>();

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceEvent traceEvent = (TraceEvent) userObject;

            traceEventList.add(traceEvent);
        }

        return traceEventList;
    }

    @Override
    public boolean search(Object searchStrObj, Charset charset) {

        boolean found = false;

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceEvent te = (TraceEvent) userObject;

            found = te.search(searchStrObj, charset);
        }

        return found;
    }

    // 0 based column index
    public String getTraceEventNodeValue(TraceEventColumn traceEventColumn) {
        String nodeValue = null;

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceEvent te = (TraceEvent) userObject;

            nodeValue = te.getColumnValueForTraceTableModelColumn(traceEventColumn);
        }

        return nodeValue;
    }
}
