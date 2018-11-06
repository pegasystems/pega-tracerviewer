/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventDeclarePages extends TraceEventNonActivity {

    public TraceEventDeclarePages(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.DECLARE_PAGES;
    }

}
