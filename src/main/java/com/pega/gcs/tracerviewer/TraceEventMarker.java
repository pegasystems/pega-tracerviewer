/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceEventMarker extends Marker<TraceEventKey> {

    private static final long serialVersionUID = -5879447557783188643L;

    private String line;

    @SuppressWarnings("unused")
    private TraceEventMarker() {
        //for kryo
        super();
    }

    public TraceEventMarker(TraceEventKey key, String text, String line) {
        super(key, text);

        this.line = line;
    }

    @Override
    public String toString() {
        return "Line: " + line + " - " + getText();
    }

}
