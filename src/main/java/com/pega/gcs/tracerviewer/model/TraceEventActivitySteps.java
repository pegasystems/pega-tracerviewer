/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventActivitySteps extends TraceEvent {

	public TraceEventActivitySteps(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);

		traceEventType = TraceEventType.ACTIVITY_STEP;
	}

	@Override
	protected boolean checkStart() {

		boolean start = false;

		String eventName = getEventName();

		if ("Step Begin".equals(eventName)) {
			start = true;
		}

		return start;
	}

	@Override
	protected boolean checkEnd() {

		boolean end = false;

		String eventName = getEventName();

		if ("Step End".equals(eventName)) {
			end = true;
		}

		return end;
	}
}
