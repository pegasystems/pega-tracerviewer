/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventServices extends TraceEventNonActivity {

	public TraceEventServices(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

		super(traceEventKey, bytes, traceEventElement);

		traceEventType = TraceEventType.SERVICES;
	}

	@Override
	protected boolean checkStart() {

		boolean start = false;

		String eventName = getEventName();

		if ("Service Start".equals(eventName)) {
			start = true;
		}

		return start;
	}

	@Override
	protected boolean checkEnd() {

		boolean end = false;

		String eventName = getEventName();

		if ("Service End".equals(eventName)) {
			end = true;
		}

		return end;
	}
}
