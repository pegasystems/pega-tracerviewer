/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventInteraction extends TraceEventNonActivity {

    public TraceEventInteraction(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.INTERACTION;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getRuleFromElement()
     */
    @Override
    protected void setRuleNoFromElement(Element traceEventElement) {
        setRuleNo(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepMethodFromElement()
     */
    @Override
    protected void setStepMethodFromElement(Element traceEventElement) {
        setStepMethod(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepPageFromElement()
     */
    @Override
    protected void setStepPageFromElement(Element traceEventElement) {
        setStepPage(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepFromElement()
     */
    @Override
    protected void setStepFromElement(Element traceEventElement) {
        setStep(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStatusFromElement()
     */
    @Override
    protected void setStatusFromElement(Element traceEventElement) {

        String status = null;

        Element element = traceEventElement.element("InteractionBytes");

        if (element != null) {
            status = element.getText();
            status += "(b)";
        }

        setStatus(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getEventTypeFromElement()
     */
    @Override
    protected void setEventNameFromElement(Element traceEventElement) {

        String eventName = null;

        Element element = traceEventElement.element("EventName");

        if (element != null) {
            eventName = element.getText();
        }

        setEventName(eventName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getNameFromElement()
     */
    @Override
    protected void setNameFromElement(Element traceEventElement) {

        String name = null;

        Element element = traceEventElement.element("InteractionQueryParam");

        if (element != null) {
            name = element.getText();
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

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String eventName = getEventName();

        if ("Interaction Begin".equals(eventName)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String eventName = getEventName();

        if ("Interaction End".equals(eventName)) {
            end = true;
        }

        return end;
    }
}
