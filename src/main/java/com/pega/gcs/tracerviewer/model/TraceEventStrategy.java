/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventStrategy extends TraceEventNonActivity {

    public TraceEventStrategy(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.STRATEGY;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String eventName = getEventName();

        if (("Component Begin".equals(eventName)) || ("Begin".equals(eventName))
                || ("Strategy Begin".equals(eventName))) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String eventName = getEventName();

        if (("Component End".equals(eventName)) || ("End".equals(eventName)) || ("Strategy End".equals(eventName))) {
            end = true;
        }

        return end;
    }
}
