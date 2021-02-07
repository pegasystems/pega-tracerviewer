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

public class TraceEventAsyncDPLoad extends TraceEvent {

    public TraceEventAsyncDPLoad(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.ASYNC_DP_LOAD;
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

        String stepMethod = null;

        if (!isEndOfAsyncTraceEventSent()) {

            Element element = traceEventElement.element("EventName");

            if (element != null) {
                stepMethod = element.getText();
            }
        }

        setStepMethod(stepMethod);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#setStepPageFromElement()
     */
    @Override
    protected void setStepPageFromElement(Element traceEventElement) {

        String stepPage = null;

        if (!isEndOfAsyncTraceEventSent()) {

            Element element = traceEventElement.element("OptionalProperties");

            if (element != null) {

                Element element1 = element.element("pagedata");

                if (element1 != null) {

                    Element element2 = element1.element("ADPLoadPageName");

                    if (element != null) {
                        stepPage = element2.getText();
                    }
                }
            }

        }

        setStepPage(stepPage);
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

        if (!isEndOfAsyncTraceEventSent()) {
            super.setStatusFromElement(traceEventElement);
        } else {
            setStatus(status);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getEventTypeFromElement()
     */
    @Override
    protected void setEventNameFromElement(Element traceEventElement) {

        String eventName = "End of trace";

        if (!isEndOfAsyncTraceEventSent()) {

            Element element = traceEventElement.element("EventType");

            if (element != null) {
                eventName = element.getText();
            }
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

        if (!isEndOfAsyncTraceEventSent()) {

            Element element = traceEventElement.element("OptionalProperties");

            if (element != null) {

                Element element1 = element.element("pagedata");

                if (element1 != null) {

                    Element element2 = element1.element("BGTracerKey");

                    if (element != null) {
                        name = element2.getText();
                        name = buildActivityName(name);
                        name = buildDataPageDisplayName(name);
                    }
                }
            }
        }

        setName(name);
    }

    @Override
    public Color getBaseColumnBackground() {
        return MyColor.LIGHTEST_LIME;
    }
}
