/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.io.Serializable;

/**
 * id and index will same on initial load. id will be regenerated for compare mode.
 * @author vargm
 */
public class TraceEventKey implements Comparable<TraceEventKey>, Serializable {

    private static final long serialVersionUID = 8198384556087741048L;

    private int id;

    // -1 set in case of compare events
    private int traceEventIndex;

    private boolean corrupt;

    // for kyro
    private TraceEventKey() {
        super();
    }

    public TraceEventKey(int id, int traceEventIndex, boolean corrupt) {
        super();

        this.id = id;
        this.traceEventIndex = traceEventIndex;
        this.corrupt = corrupt;
    }

    public int getId() {
        return id;
    }

    public int getTraceEventIndex() {
        return traceEventIndex;
    }

    public boolean isCorrupt() {
        return corrupt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + traceEventIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TraceEventKey other = (TraceEventKey) obj;

        if (traceEventIndex != -1) {
            if (traceEventIndex != other.traceEventIndex) {
                return false;
            }
        } else if (id != other.id) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TraceEventKey id [" + id + "] traceEventIndex [" + traceEventIndex + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TraceEventKey eventKey) {

        Integer thisTraceEventIndex = getTraceEventIndex();
        Integer otherTraceEventIndex = eventKey.getTraceEventIndex();

        if ((thisTraceEventIndex != -1) && (otherTraceEventIndex != -1)) {
            return thisTraceEventIndex.compareTo(otherTraceEventIndex);
        } else {
            return Integer.valueOf(getId()).compareTo(Integer.valueOf(eventKey.getId()));
        }

    }

}
