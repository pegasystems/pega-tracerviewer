/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

public class TraceTableCompareEntry {

	private int startEntry;

	private int endEntry;

	public TraceTableCompareEntry(int startEntry, int endEntry) {

		super();

		this.startEntry = startEntry;
		this.endEntry = endEntry;
	}

	public int getStartEntry() {
		return startEntry;
	}

	public int getEndEntry() {
		return endEntry;
	}

}
