/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventDataFlow extends TraceEventNonActivity {

    public TraceEventDataFlow(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
        
        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.DATA_FLOW;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String eventName = getEventName();

        if ("Data Flow Begin".equals(eventName)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String eventName = getEventName();

        if (("Data Flow End".equals(eventName)) || ("Data Flow Fail".equals(eventName))) {
            end = true;
        }

        return end;
    }
}
