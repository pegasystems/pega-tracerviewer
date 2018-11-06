/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

public class TraceEventRuleset implements Comparable<TraceEventRuleset> {

    private String ruleset;

    private String version;

    public TraceEventRuleset(String rulesetVersion) {
        super();

        String[] rulesetVersionArray = rulesetVersion.split(" ");

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ruleset == null) ? 0 : ruleset.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
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
        TraceEventRuleset other = (TraceEventRuleset) obj;
        if (ruleset == null) {
            if (other.ruleset != null) {
                return false;
            }
        } else if (!ruleset.equals(other.ruleset)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();

        sb.append(getRuleset());

        if (getVersion() != null) {
            sb.append(" ");
            sb.append(getVersion());
        }

        return sb.toString();
    }

    @Override
    public int compareTo(TraceEventRuleset eventRuleset) {
        return toString().compareTo(eventRuleset.toString());
    }

}
