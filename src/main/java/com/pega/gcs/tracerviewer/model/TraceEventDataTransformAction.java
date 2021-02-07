/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventDataTransformAction extends TraceEvent {

    public TraceEventDataTransformAction(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);

        traceEventType = TraceEventType.DATA_TRANSFORM_ACTION;
    }

    @Override
    protected void setStepPageFromElement(Element traceEventElement) {

        // setting the steppage first because we may need it below.
        super.setStepPageFromElement(traceEventElement);

        StringBuilder stepPageSB = new StringBuilder();

        Element element = traceEventElement.element("ActionTargetPageName");

        if ((element != null) && (!"".equals(element.getText()))) {
            stepPageSB.append(element.getText());
        } else {

            // get the step page from super.
            stepPageSB.append(getStepPage());
        }

        element = traceEventElement.element("ActionSourcePageName");

        if ((element != null) && (!"".equals(element.getText()))) {

            stepPageSB.append("<--");
            stepPageSB.append(element.getText());
        }

        String stepPage = stepPageSB.toString();
        setStepPage(stepPage);
    }

    @Override
    protected boolean checkStart() {

        boolean start = false;

        String eventName = getEventName();

        if ("Action Begin".equals(eventName)) {
            start = true;
        }

        return start;
    }

    @Override
    protected boolean checkEnd() {

        boolean end = false;

        String eventName = getEventName();

        if ("Action End".equals(eventName)) {
            end = true;
        }

        return end;
    }
}
