/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

/**
 * For unknown event types. for corrupt, it will of type empty.
 */
public class TraceEventUnknown extends TraceEvent {

    public TraceEventUnknown(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.UNKNOWN;
    }

    @Override
    public Color getBaseColumnBackground() {
        return Color.WHITE;
    }
}
