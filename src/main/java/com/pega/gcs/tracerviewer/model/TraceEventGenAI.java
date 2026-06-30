/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventGenAI extends TraceEventNonActivity {

    public TraceEventGenAI(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.GENAI;
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String stepMethod = getStepMethod();

        if ("Agent Execution Begin".equals(stepMethod)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String stepMethod = getStepMethod();

        if ("Agent Execution End".equals(stepMethod)) {
            end = true;
        }

        return end;
    }
}
