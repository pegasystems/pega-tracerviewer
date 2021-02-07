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

public class TraceEventCombinedTreeNode extends AbstractTraceEventTreeNode {

    private static final long serialVersionUID = 7419226264804804185L;

    private String nodeName;

    private TraceEvent startEvent;

    // in case of child the endEvent is null
    private TraceEvent endEvent;

    public TraceEventCombinedTreeNode(TraceEvent traceEvent, String nodeName) {

        super(traceEvent);

        this.nodeName = nodeName;
        this.startEvent = traceEvent;
        this.endEvent = null;
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

    public TraceEvent getEndEvent() {
        return endEvent;
    }

    public void setEndEvent(TraceEvent endEvent) {
        this.endEvent = endEvent;
    }

    public TraceEvent getStartEvent() {
        return startEvent;
    }

    @Override
    public List<TraceEvent> getTraceEvents() {

        List<TraceEvent> traceEventList = new ArrayList<TraceEvent>();

        traceEventList.add(startEvent);

        if (endEvent != null) {
            traceEventList.add(endEvent);
        }

        return traceEventList;
    }

    @Override
    public boolean search(Object searchStrObj, Charset charset) {
        boolean found = false;

        if (startEvent != null) {
            found = startEvent.search(searchStrObj, charset);
        }

        if (endEvent != null) {

            found = found || endEvent.search(searchStrObj, charset);
        }

        return found;
    }

    // 0 based column index
    public String getTraceEventNodeValue(TraceEventColumn traceEventColumn) {
        String nodeValue = null;

        TraceEvent startEvent = getStartEvent();
        TraceEvent endEvent = getEndEvent();

        if (startEvent != null) {

            if (endEvent != null) {

                switch (traceEventColumn) {
                case TOTAL_ELAPSED:// Elapsed
                case OWN_ELAPSED:
                case CHILDREN_ELAPSED:
                case STATUS:// Status
                    nodeValue = endEvent.getColumnValueForTraceTableModelColumn(traceEventColumn);
                    break;
                default:
                    nodeValue = startEvent.getColumnValueForTraceTableModelColumn(traceEventColumn);
                }

            } else {
                nodeValue = startEvent.getColumnValueForTraceTableModelColumn(traceEventColumn);
            }

        }

        return nodeValue;
    }
}
