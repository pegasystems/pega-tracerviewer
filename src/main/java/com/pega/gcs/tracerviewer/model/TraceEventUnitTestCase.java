/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventUnitTestCase extends TraceEventNonActivity {

    public TraceEventUnitTestCase(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.UNIT_TEST_CASE;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String eventName = getEventName();

        if ("Unit Test Case Begin".equals(eventName)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String eventName = getEventName();

        if ("Unit Test Case End".equals(eventName)) {
            end = true;
        }

        return end;
    }
}
