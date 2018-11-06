/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

/**
 * For corrupt and compare event types.
 * @author vargm
 */
public class TraceEventEmpty extends TraceEvent {

    public TraceEventEmpty(TraceEventKey traceEventKey, Color background) {

        super(traceEventKey, null, null);

        traceEventType = null;

        fillColumnBackground(background);
    }

}
