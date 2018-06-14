/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventStreamRules extends TraceEventNonActivity {

    public TraceEventStreamRules(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.STREAM_RULES;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String stepMethod = getStepMethod();

        if ("Stream Begin".equals(stepMethod)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String stepMethod = getStepMethod();

        if ("Stream End".equals(stepMethod)) {
            end = true;
        }

        return end;
    }

    @Override
    public boolean isMatchingStartTraceEvent(TraceEvent endTraceEvent) {

        // stream rules doesn't have rule no defined, hence using Name to
        // identify the start event.
        boolean matchingStartTraceEvent = super.isMatchingStartTraceEvent(endTraceEvent);

        String endName = endTraceEvent.getName();
        String name = getName();

        matchingStartTraceEvent = matchingStartTraceEvent && matchObject(name, endName);

        return matchingStartTraceEvent;
    }
}
