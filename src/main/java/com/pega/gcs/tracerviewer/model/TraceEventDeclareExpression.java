/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventDeclareExpression extends TraceEventNonActivity {

    public TraceEventDeclareExpression(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.DECLARE_EXPRESSION;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String stepMethod = getStepMethod();

        if (("Forward Chaining Start".equals(stepMethod)) || ("Backward Chaining Start".equals(stepMethod))) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String stepMethod = getStepMethod();

        if (("Forward Chaining End".equals(stepMethod)) || ("Backward Chaining End".equals(stepMethod))) {
            end = true;
        }

        return end;
    }
}
