/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TracerViewerUtil {

    public static TraceEventTreeNode getBeginTraceEventTreeNode(TraceEventTreeNode traceEventTreeNode) {

        TraceEventTreeNode beginTraceEventTreeNode = null;

        TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

        if (traceEvent != null) {

            Boolean isEndEvent = traceEvent.isEndEvent();

            if ((isEndEvent != null) && isEndEvent) {

                TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) traceEventTreeNode.getParent();

                int currentNodeIndex = parentTraceEventTreeNode.getIndex(traceEventTreeNode);

                if (currentNodeIndex > 0) {

                    beginTraceEventTreeNode = (TraceEventTreeNode) parentTraceEventTreeNode
                            .getChildAt(currentNodeIndex - 1);
                }

            }
        }

        return beginTraceEventTreeNode;
    }

    public static TraceEventTreeNode getEndTraceEventTreeNode(TraceEventTreeNode traceEventTreeNode) {

        TraceEventTreeNode endTraceEventTreeNode = null;

        TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

        if (traceEvent != null) {

            Boolean isEndEvent = traceEvent.isEndEvent();

            if (isEndEvent != null) {

                TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) traceEventTreeNode.getParent();

                int currentNodeIndex = parentTraceEventTreeNode.getIndex(traceEventTreeNode);

                int childCount = parentTraceEventTreeNode.getChildCount();

                if ((currentNodeIndex != -1) && (currentNodeIndex < (childCount - 1))) {

                    endTraceEventTreeNode = (TraceEventTreeNode) parentTraceEventTreeNode
                            .getChildAt(currentNodeIndex + 1);
                }

            }
        }

        return endTraceEventTreeNode;
    }
}
