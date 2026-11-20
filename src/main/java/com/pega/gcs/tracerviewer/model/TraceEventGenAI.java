/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventGenAI extends TraceEvent {

    public TraceEventGenAI(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.GENAI;
    }
    /*
    @Override
    protected boolean checkStart() {

        String eventName = getEventName();

        return eventName != null && eventName.endsWith("Begin");
    }

    @Override
    protected boolean checkEnd() {

        String eventName = getEventName();

        return eventName != null && eventName.endsWith("End");
    }
*/
}
