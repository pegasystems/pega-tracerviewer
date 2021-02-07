/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.report;

import com.pega.gcs.tracerviewer.TraceEventRule;
import com.pega.gcs.tracerviewer.TraceEventRuleset;

public class TracerReportRulesetRule {

    private int rulesetRuleIndex;

    private TraceEventRuleset traceEventRuleset;

    private TraceEventRule traceEventRule;

    public TracerReportRulesetRule(int rulesetRuleIndex, TraceEventRuleset traceEventRuleset,
            TraceEventRule traceEventRule) {
        super();
        this.rulesetRuleIndex = rulesetRuleIndex;
        this.traceEventRuleset = traceEventRuleset;
        this.traceEventRule = traceEventRule;
    }

    public int getRulesetRuleIndex() {
        return rulesetRuleIndex;
    }

    public TraceEventRuleset getTraceEventRuleset() {
        return traceEventRuleset;
    }

    public TraceEventRule getTraceEventRule() {
        return traceEventRule;
    }

}
