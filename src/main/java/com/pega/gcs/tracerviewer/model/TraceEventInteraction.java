/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
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
     * @see com.fringe.tracerviewer.TraceEvent#getRule()
     */
    @Override
    protected void setRuleNo(Element traceEventElement) {
        setRuleNo((Integer) null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getStepMethod()
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
     * @see com.fringe.tracerviewer.TraceEvent#getEventType()
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
     * @see com.fringe.tracerviewer.TraceEvent#getName()
     */
    @Override
    protected void setName(Element traceEventElement) {
        String name = "";

        Element element = traceEventElement.element("InteractionQueryParam");

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
    public Element getTraceEventPropertyElement() {

        Element traceEventPropertyElement = null;

        Element rootElement = getTraceEventRootElement();

        if (rootElement != null) {

            traceEventPropertyElement = getDefaultTraceEventPropertyElement(rootElement);

            // Interaction
            Element element = rootElement.element("InteractionBytes");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Bytes", element));
            }

            // InteractionQueryParam
            element = rootElement.element("InteractionQueryParam");

            if (element != null) {
                traceEventPropertyElement.add(createElement("Query Param", element));
            }

            // InteractionQueryData
            element = rootElement.element("InteractionQueryData");

            if (element != null) {

                String queryData = element.getText();

                Element queryDataElem = createElement("Query Data", null, "QueryData");

                element = rootElement.element("JSONDataFlag");

                if (element != null) {
                    String jsonValue = element.getText();

                    // TODO: implement JSON parsing
                    if ("true".equals(jsonValue)) {
                        queryDataElem.setText(queryData);
                    } else {
                        queryDataElem = buildTokenTable(queryDataElem, queryData, "&");
                    }
                } else {
                    queryDataElem = buildTokenTable(queryDataElem, queryData, "&");
                }

                traceEventPropertyElement.add(queryDataElem);

            }

            // InteractionPAL
            element = rootElement.element("InteractionPAL");

            if (element != null) {

                String palData = element.getText();

                Element palDataElem = createElement("PAL", null, "PAL");

                palDataElem = buildTokenTable(palDataElem, palData, ";");

                traceEventPropertyElement.add(palDataElem);
            }
        }

        return traceEventPropertyElement;
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
