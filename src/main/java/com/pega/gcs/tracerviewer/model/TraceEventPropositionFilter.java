/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import org.dom4j.Element;

public class TraceEventPropositionFilter extends TraceEventNonActivity {

	public TraceEventPropositionFilter(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);

		traceEventType = TraceEventType.PROPOSITION_FILTER;
	}

}
