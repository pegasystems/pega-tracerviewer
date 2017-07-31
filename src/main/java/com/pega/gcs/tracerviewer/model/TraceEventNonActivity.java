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

public abstract class TraceEventNonActivity extends TraceEvent {

	public TraceEventNonActivity(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);
	}

	@Override
	protected void setDefaultBackground() {
		Color color = MyColor.PAPAYAWHIP;
		fillColumnBackground(color);
	}
}
