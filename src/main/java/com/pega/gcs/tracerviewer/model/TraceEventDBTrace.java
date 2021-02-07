/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

public abstract class TraceEventDBTrace extends TraceEvent {

    protected TraceEventDBTrace(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        super(traceEventKey, bytes, traceEventElement);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.fringe.tracerviewer.TraceEvent#getNameFromElement()
     */
    @Override
    protected void setNameFromElement(Element traceEventElement) {

        String name = null;

        Element element = traceEventElement.element("DBTNote");

        if (element != null) {
            name = element.getText();
        }

        if ((name == null) || "".equals(name)) {

            element = traceEventElement.element("DBTHighLevelOp");

            if (element != null) {
                name = element.getText();

                if ((name != null) && "root".equals(name)) {
                    name = "";
                }
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

    @Override
    public Color getBaseColumnBackground() {
        return Color.CYAN;
    }

}
