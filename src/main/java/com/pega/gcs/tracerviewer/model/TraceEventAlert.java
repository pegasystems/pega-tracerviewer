/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
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
    protected void setRuleNo(Element traceEventElement) {
        setRuleNo((Integer) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#setStepMethod(org.dom4j.Element)
     */
    @Override
    protected void setStepMethod(Element traceEventElement) {
        setStepMethod("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepPage()
     */
    @Override
    protected void setStepPage(Element traceEventElement) {
        setStepPage("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStep()
     */
    @Override
    protected void setStep(Element traceEventElement) {
        setStep("");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStatus()
     */
    @Override
    protected void setStatus(Element traceEventElement) {
        String status = "";

        Element element = traceEventElement.element("EventType");

        if (element != null) {
            status = element.getText();
        }

        setStatus(status);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getEventName()
     */
    @Override
    protected void setEventName(Element traceEventElement) {
        String eventName = "";

        Element element = traceEventElement.element("EventName");

        if (element != null) {
            eventName = element.getText();
        }

        setEventName(eventName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getElapsed()
     */
    @Override
    protected void setElapsed(Element traceEventElement) {
        setElapsed(-1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getName()
     */
    @Override
    protected void setName(Element traceEventElement) {
        String name = "";

        Element element = traceEventElement.element("AlertLabel");

        if (element != null) {
            name = element.getText();
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

    @Override
    protected void setDefaultBackground() {
        Color color = MyColor.LIGHT_PINK;
        fillColumnBackground(color);
    }

    @Override
    public Element getTraceEventPropertyElement() {

        Element traceEventPropertyElement = null;

        Element rootElement = getTraceEventRootElement();

        if (rootElement != null) {

            traceEventPropertyElement = getDefaultTraceEventPropertyElement(rootElement);

            // EventName
            Element element = rootElement.element("EventName");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Message ID", element));
            }

            // AlertLabel
            element = rootElement.element("AlertLabel");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Label", element));
            }

            // AlertType
            element = rootElement.element("AlertType");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Type", element));
            }

            // AlertTimestamp
            element = rootElement.element("AlertTimestamp");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Timestamp", element));
            }

            // AlertKPIThreshold
            element = rootElement.element("AlertKPIThreshold");

            if (element != null) {
                traceEventPropertyElement.add(createElement("KPI Threshold", element));
            }

            // AlertKPIValue
            element = rootElement.element("AlertKPIValue");

            if (element != null) {
                traceEventPropertyElement.add(createElement("KPI Value", element));
            }

            // AlertUniqueInt
            element = rootElement.element("AlertUniqueInt");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Unique Integer", element));
            }

            // InteractionQueryParam
            element = rootElement.element("AlertLine");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Data", element));
            }
        }

        return traceEventPropertyElement;
    }
}
