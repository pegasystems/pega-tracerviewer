/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

/**
 * for unknown event types. for corrupt, it will of type empty.
 */
public class TraceEventUnknown extends TraceEvent {

	public TraceEventUnknown(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);

		traceEventType = TraceEventType.UNKNOWN;
	}

	@Override
	protected void setDefaultBackground() {
		Color color = Color.WHITE;
		fillColumnBackground(color);
	}
}
