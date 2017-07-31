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

public class TraceEventAsyncDPLoad extends TraceEvent {

	public TraceEventAsyncDPLoad(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);

		traceEventType = TraceEventType.ASYNC_DP_LOAD;
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
		String stepMethod = "";

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
	 * @see com.fringe.tracerviewer.TraceEvent#getStepPage()
	 */
	@Override
	protected void setStepPage(Element traceEventElement) {
		String stepPage = "";

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

		if (!isEndOfAsyncTraceEventSent()) {
			super.setStatus(traceEventElement);
		} else {
			setStatus(status);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fringe.tracerviewer.TraceEvent#getEventType()
	 */
	@Override
	protected void setEventName(Element traceEventElement) {
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
	protected void setDefaultBackground() {
		Color color = MyColor.LIGHTEST_LIME;
		fillColumnBackground(color);
	}
}
