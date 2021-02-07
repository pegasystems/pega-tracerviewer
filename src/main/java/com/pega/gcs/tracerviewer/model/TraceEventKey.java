/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * The 'id' and 'index' will same on initial load. id will be regenerated for compare mode.
 * 
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TraceEventKey id [" + id + "] traceEventIndex [" + traceEventIndex + "]";
        // toString is used in the bookmark dialog to show keys
        // return String.valueOf(traceEventIndex);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TraceEventKey)) {
            return false;
        }

        TraceEventKey other = (TraceEventKey) obj;

        // return id == other.id && traceEventIndex == other.traceEventIndex;
        // handle compare event keys
        if (this.traceEventIndex != -1) {
            if (this.traceEventIndex != other.traceEventIndex) {
                return false;
            }
        } else if (this.id != other.id) {
            return false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TraceEventKey other) {

        Integer thisTraceEventIndex = getTraceEventIndex();
        Integer otherTraceEventIndex = other.getTraceEventIndex();

        if ((thisTraceEventIndex != -1) && (otherTraceEventIndex != -1)) {
            return thisTraceEventIndex.compareTo(otherTraceEventIndex);
        } else {
            return Integer.valueOf(getId()).compareTo(Integer.valueOf(other.getId()));
        }

    }

}
