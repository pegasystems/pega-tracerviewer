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

public class TraceEventCombinedTreeNode extends AbstractTraceEventTreeNode {

    private static final long serialVersionUID = 7419226264804804185L;

    private TraceEvent startEvent;

    // in case of child the endEvent is null
    private TraceEvent endEvent;

    public TraceEventCombinedTreeNode(TraceEvent traceEvent) {

        super(traceEvent);

        this.startEvent = traceEvent;
        this.endEvent = null;
    }

    @Override
    public int compareTo(AbstractTreeTableNode o) {
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

        TraceTableModelColumn[] traceTreeCombinedTableModelColumnArray;
        traceTreeCombinedTableModelColumnArray = TraceTableModelColumn.getTraceTreeCombinedTableModelColumnArray();

        TraceTableModelColumn traceTreeCombinedTableModelColumn = traceTreeCombinedTableModelColumnArray[column];

        TraceEvent startEvent = getStartEvent();
        TraceEvent endEvent = getEndEvent();

        if (startEvent != null) {

            if (endEvent != null) {

                switch (traceTreeCombinedTableModelColumn) {

                    case LINE:// Line
                    case TIMESTAMP:// TimeStamp
                    case THREAD:// Thread
                    case INT:// Int
                    case RULE:// Rule#
                    case STEP_METHOD:// Step Method
                    case STEP_PAGE:// Step Page
                    case STEP:// Step
                    case EVENT_TYPE:// Event Name
                    case NAME:// Name
                    case RULESET:// RuleSet
                        nodeValue = startEvent.getColumnValueForTraceTableModelColumn(traceTreeCombinedTableModelColumn);
                        break;

                    case TOTAL_ELAPSED:// Elapsed
                    case OWN_ELAPSED:
                    case CHILDREN_ELAPSED:
                    case STATUS:// Status
                        nodeValue = endEvent.getColumnValueForTraceTableModelColumn(traceTreeCombinedTableModelColumn);
                        break;
                    default:
                        break;
                }

            } else {
                nodeValue = startEvent.getColumnValueForTraceTableModelColumn(traceTreeCombinedTableModelColumn);
            }

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

            TraceTableModelColumn[] traceTreeCombinedTableModelColumnArray;
            traceTreeCombinedTableModelColumnArray = TraceTableModelColumn.getTraceTreeCombinedTableModelColumnArray();

            int columnCount = traceTreeCombinedTableModelColumnArray.length;

            for (int column = 0; column < columnCount; column++) {

                TraceTableModelColumn traceTreeCombinedTableModelColumn = traceTreeCombinedTableModelColumnArray[column];

                valueList.add(te.getColumnValueForTraceTableModelColumn(traceTreeCombinedTableModelColumn));
            }

            elementArray = (String[]) valueList.toArray(new String[valueList.size()]);
        }

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
    public boolean search(Object searchStrObj) {
        boolean found = false;

        if (startEvent != null) {
            found = startEvent.search(searchStrObj);
        }

        if (endEvent != null) {

            found = found || endEvent.search(searchStrObj);
        }

        return found;
    }

}
