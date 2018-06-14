/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventLogMessages extends TraceEventNonActivity {

    public TraceEventLogMessages(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.LOG_MESSAGES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getName()
     */
    @Override
    protected void setName(Element traceEventElement) {

        String name = "";

        Element element = traceEventElement.element("EventKey");

        if (element != null) {
            name = element.getText();
        } else {
            element = traceEventElement.element("InstanceName");

            if (element != null) {
                name = element.getText();

            }
        }

        setName(name);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getRuleSet()
     */
    @Override
    protected void setRuleSet(Element traceEventElement) {
        setRuleSet("");
    }

}
