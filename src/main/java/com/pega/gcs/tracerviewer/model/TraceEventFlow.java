/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventFlow extends TraceEventNonActivity {

    public TraceEventFlow(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.FLOW;

    }

    // @Override
    // public Boolean isEndEvent() {
    //
    // Boolean endEvent = null;
    //
    // String eventName = getEventName();
    //
    // Flow events will appear without their closing entries. hence
    // discarding them from the tree logic by setting null
    //
    // if ("Flow Start".equals(eventName)) {
    // endEvent = null;
    // } else if ("Flow End".equals(eventName)) {
    // endEvent = null;
    // } else if ("Flow Utility Start".equals(eventName)) {
    // endEvent = false;
    // } else if ("Flow Utility End".equals(eventName)) {
    // endEvent = true;
    // } else if ("Flow Route Start".equals(eventName)) {
    // endEvent = false;
    // } else if ("Flow Route End".equals(eventName)) {
    // endEvent = true;
    // } else if ("Flow Assign Start".equals(eventName)) {
    // endEvent = null;
    // } else if ("Flow Assign End".equals(eventName)) {
    // endEvent = null;
    // } else if ("Flow Decision Start".equals(eventName)) {
    // endEvent = false;
    // } else if ("Flow Decision End".equals(eventName)) {
    // endEvent = true;
    // } else if ("Flow Rule Connect Start".equals(eventName)) {
    // endEvent = false;
    // } else if ("Flow Rule Connect End".equals(eventName)) {
    // endEvent = true;
    // }
    //
    // return endEvent;
    // }
}
