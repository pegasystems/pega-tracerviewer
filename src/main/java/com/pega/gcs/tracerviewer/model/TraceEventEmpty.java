/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

/**
 * For corrupt and compare event types.
 * 
 * @author vargm
 */
public class TraceEventEmpty extends TraceEvent {

    private Color baseColumnBackground;

    public TraceEventEmpty(TraceEventKey traceEventKey, Color background) {

        super(traceEventKey, null, null);

        traceEventType = null;

        baseColumnBackground = background;
    }

    @Override
    public Color getBaseColumnBackground() {
        return baseColumnBackground;
    }
}
