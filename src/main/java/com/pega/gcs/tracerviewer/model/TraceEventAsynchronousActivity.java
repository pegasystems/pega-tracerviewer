/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.MyColor;

public class TraceEventAsynchronousActivity extends TraceEvent {

    public TraceEventAsynchronousActivity(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.ASYNCHRONOUS_ACTIVITY;
    }

    @Override
    public Color getBaseColumnBackground() {
        return MyColor.LIGHT_GREEN;
    }
}
