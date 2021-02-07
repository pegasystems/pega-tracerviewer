/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
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
     * @see com.fringe.tracerviewer.TraceEvent#getNameFromElement()
     */
    @Override
    protected void setNameFromElement(Element traceEventElement) {

        String name = null;

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
     * @see com.fringe.tracerviewer.TraceEvent#getRuleSetFromElement()
     */
    @Override
    protected void setRuleSetFromElement(Element traceEventElement) {
        setRuleSet(null);
    }

}
