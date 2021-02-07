/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.MyColor;

public class TraceEventAlert extends TraceEvent {

    public TraceEventAlert(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.ALERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#setRuleNo(org.dom4j.Element)
     */
    @Override
    protected void setRuleNoFromElement(Element traceEventElement) {
        setRuleNo(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#setStepMethodFromElement(org.dom4j.Element)
     */
    @Override
    protected void setStepMethodFromElement(Element traceEventElement) {
        setStepMethod(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepPage()
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

        Element element = traceEventElement.element("EventType");

        if (element != null) {
            status = element.getText();
        }

        setStatus(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getEventNameFromElement()
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
     * @see com.fringe.tracerviewer.TraceEvent#getElapsedFromElement()
     */
    @Override
    protected void setElapsedFromElement(Element traceEventElement) {
        setElapsed(-1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getNameFromElement()
     */
    @Override
    protected void setNameFromElement(Element traceEventElement) {
        String name = null;

        Element element = traceEventElement.element("AlertLabel");

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
    public Color getBaseColumnBackground() {
        return MyColor.LIGHT_PINK;
    }
}
