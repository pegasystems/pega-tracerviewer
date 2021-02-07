/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.util.Map;
import java.util.Objects;

import com.pega.gcs.fringecommon.utilities.diff.Matcher;
import com.pega.gcs.tracerviewer.model.TraceEvent;

public class TraceEventMatcher implements Matcher<TraceEvent> {

    private Map<String, String[]> stepPageHierarchyLookupMapThis;

    private Map<String, String[]> stepPageHierarchyLookupMapOther;

    public TraceEventMatcher(Map<String, String[]> stepPageHierarchyLookupMapThis,
            Map<String, String[]> stepPageHierarchyLookupMapOther) {

        this.stepPageHierarchyLookupMapThis = stepPageHierarchyLookupMapThis;
        this.stepPageHierarchyLookupMapOther = stepPageHierarchyLookupMapOther;
    }

    @Override
    public boolean match(TraceEvent traceEventThis, TraceEvent traceEventOther) {

        if (traceEventThis == traceEventOther) {
            return true;
        }

        if (traceEventOther == null) {
            return false;
        }

        // these 4 field are used to make comparison
        String eventNameThis = traceEventThis.getEventName();
        String nameThis = traceEventThis.getName();
        String stepMethodThis = traceEventThis.getStepMethod();
        String stepPageThis = traceEventThis.getStepPage();

        String eventNameOther = traceEventOther.getEventName();
        String nameOther = traceEventOther.getName();
        String stepMethodOther = traceEventOther.getStepMethod();
        String stepPageOther = traceEventOther.getStepPage();

        // compare the 3 fields first and if they match compare the step page.
        if (Objects.equals(eventNameThis, eventNameOther) && Objects.equals(nameThis, nameOther)
                && Objects.equals(stepMethodThis, stepMethodOther)) {

            String[] stepPageArrayThis = stepPageHierarchyLookupMapThis.get(stepPageThis);
            String[] stepPageArrayOther = stepPageHierarchyLookupMapOther.get(stepPageOther);

            if ((stepPageArrayThis != null) && (stepPageArrayOther != null)) {

                int stepPageArrayThisLength = stepPageArrayThis.length;
                int stepPageArrayOtherLength = stepPageArrayOther.length;

                // both arrays are of equal length compare individual entries
                if (stepPageArrayThisLength == stepPageArrayOtherLength) {

                    boolean match = true;

                    for (int index = 0; index < stepPageArrayThisLength; index++) {

                        String childStepPageThis = stepPageArrayThis[index];
                        String childStepPageOther = stepPageArrayOther[index];

                        if (!Objects.equals(childStepPageThis, childStepPageOther)) {
                            match = false;
                            break;
                        }
                    }

                    return match;
                }

                // if both array are null, then compare the step page string
            } else if ((stepPageArrayThis == null) && (stepPageArrayOther == null)) {
                return Objects.equals(stepPageThis, stepPageOther);
            }
        }

        return false;
    }

}
