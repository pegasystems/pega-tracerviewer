/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.nio.charset.Charset;
import java.util.List;

import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public abstract class AbstractTraceEventTreeNode extends AbstractTreeTableNode {

    private static final long serialVersionUID = 3659089820253269445L;

    private boolean searchFound;

    public abstract boolean search(Object searchStrObj, Charset charset);

    public abstract List<TraceEvent> getTraceEvents();

    public AbstractTraceEventTreeNode(TraceEvent traceEvent) {
        super(traceEvent);
    }

    public boolean isSearchFound() {
        return searchFound;
    }

    public void setSearchFound(boolean searchFound) {
        this.searchFound = searchFound;
    }

}
