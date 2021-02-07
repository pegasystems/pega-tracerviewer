/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
/**
 * 
 */

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.util.Objects;

import com.pega.gcs.tracerviewer.model.TraceEventType;

public class TraceEventRule implements Comparable<TraceEventRule> {

    private String insKey;

    private TraceEventType traceEventType;

    private Color background;

    private int executionCount;

    private double maxOwnElapsed;

    private double minOwnElapsed;

    private double totalOwnElapsed;

    public TraceEventRule(String insKey, TraceEventType traceEventType, Color background) {
        super();
        this.insKey = insKey;
        this.traceEventType = traceEventType;
        this.background = background;

        executionCount = 0;
        maxOwnElapsed = 0;
        minOwnElapsed = 0;
        totalOwnElapsed = 0;
    }

    public String getInsKey() {
        return insKey;
    }

    public TraceEventType getTraceEventType() {
        return traceEventType;
    }

    public Color getBackground() {
        return background;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public double getMaxOwnElapsed() {
        return maxOwnElapsed;
    }

    public double getMinOwnElapsed() {
        return minOwnElapsed;
    }

    public double getTotalOwnElapsed() {
        return totalOwnElapsed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return insKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(insKey);
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

        if (!(obj instanceof TraceEventRule)) {
            return false;
        }

        TraceEventRule other = (TraceEventRule) obj;

        return Objects.equals(insKey, other.insKey);
    }

    @Override
    public int compareTo(TraceEventRule other) {
        return getInsKey().compareTo(other.getInsKey());
    }

    public void incrementExecutionCount() {
        executionCount++;
    }

    public void processElapsed(double ownElapsed) {

        if (ownElapsed > 0) {

            totalOwnElapsed += ownElapsed;

            maxOwnElapsed = Math.max(maxOwnElapsed, ownElapsed);

            if (minOwnElapsed == 0) {
                minOwnElapsed = ownElapsed;
            } else {
                minOwnElapsed = Math.min(minOwnElapsed, ownElapsed);
            }
        }
    }
}
