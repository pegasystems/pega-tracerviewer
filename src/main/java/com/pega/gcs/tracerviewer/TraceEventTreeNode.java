/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.util.ArrayList;
import java.util.List;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TraceEventTreeNode extends AbstractTraceEventTreeNode {

    private static final long serialVersionUID = 6721834740223154002L;

    public TraceEventTreeNode(TraceEvent traceEvent) {
        super(traceEvent);
    }

    @Override
    public int compareTo(AbstractTreeTableNode node) {
        // Don't want to perform sort here
        return 0;
    }

    @Override
    public String getNodeName() {
        return getNodeValue(0);
    }

    @Override
    // 0 based column index
    public String getNodeValue(int column) {
        String nodeValue = null;

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceTableModelColumn[] traceTreeTableModelColumnArray;
            traceTreeTableModelColumnArray = TraceTableModelColumn.getTraceTreeTableModelColumnArray();

            TraceTableModelColumn traceTreeTableModelColumn = traceTreeTableModelColumnArray[column];

            TraceEvent te = (TraceEvent) userObject;

            nodeValue = te.getColumnValueForTraceTableModelColumn(traceTreeTableModelColumn);
        }

        return nodeValue;
    }

    @Override
    public Object[] getNodeElements() {

        String[] elementArray = new String[] {};

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceEvent te = (TraceEvent) userObject;

            List<String> valueList = new ArrayList<String>();

            TraceTableModelColumn[] traceTreeTableModelColumnArray;
            traceTreeTableModelColumnArray = TraceTableModelColumn.getTraceTreeTableModelColumnArray();

            int columnCount = traceTreeTableModelColumnArray.length;

            for (int column = 0; column < columnCount; column++) {

                TraceTableModelColumn traceTreeTableModelColumn = traceTreeTableModelColumnArray[column];

                valueList.add(te.getColumnValueForTraceTableModelColumn(traceTreeTableModelColumn));
            }

            elementArray = (String[]) valueList.toArray(new String[valueList.size()]);
        }

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
    public boolean search(Object searchStrObj) {

        boolean found = false;

        Object userObject = getUserObject();

        if ((userObject != null) && (userObject instanceof TraceEvent)) {

            TraceEvent te = (TraceEvent) userObject;

            found = te.search(searchStrObj);
        }

        return found;
    }

}
