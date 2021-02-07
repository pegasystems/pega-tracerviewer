/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.util.Objects;

public class TraceEventRuleset implements Comparable<TraceEventRuleset> {

    private String ruleset;

    private String version;

    public TraceEventRuleset(String rulesetVersion) {
        super();

        String[] rulesetVersionArray = rulesetVersion.split(" ", 0);

        this.ruleset = rulesetVersionArray[0].trim();

        if (rulesetVersionArray.length > 1) {
            this.version = rulesetVersionArray[1].trim();
        }
    }

    public String getRuleset() {
        return ruleset;
    }

    public String getVersion() {
        return version;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(getRuleset());

        if (getVersion() != null) {
            sb.append(" ");
            sb.append(getVersion());
        }

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(ruleset, version);
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

        if (!(obj instanceof TraceEventRuleset)) {
            return false;
        }

        TraceEventRuleset other = (TraceEventRuleset) obj;

        return Objects.equals(ruleset, other.ruleset) && Objects.equals(version, other.version);
    }

    @Override
    public int compareTo(TraceEventRuleset other) {
        return toString().compareTo(other.toString());
    }

}
