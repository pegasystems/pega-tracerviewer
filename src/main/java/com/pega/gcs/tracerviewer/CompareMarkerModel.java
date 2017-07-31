/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.fringecommon.guiutilities.markerbar.MarkerModel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class CompareMarkerModel extends MarkerModel<TraceEventKey> {

	private static final Log4j2Helper LOG = new Log4j2Helper(CompareMarkerModel.class);

	private TraceTableCompareModel traceTableCompareModel;

	public CompareMarkerModel(Color markerColor, TraceTableCompareModel traceTableCompareModel) {

		super(markerColor, traceTableCompareModel);

		this.traceTableCompareModel = traceTableCompareModel;

		resetFilteredMarkerMap();
	}

	@Override
	protected void resetFilteredMarkerMap() {

		clearFilteredMarkerMap();

		List<TraceEventKey> compareMarkerList = traceTableCompareModel.getCompareMarkerList();

		if (compareMarkerList != null) {
			Iterator<TraceEventKey> iterator = compareMarkerList.iterator();

			while (iterator.hasNext()) {

				TraceEventKey key = iterator.next();

				addToFilteredMarkerMap(key);
			}
		}
	}

	@Override
	public List<Marker<TraceEventKey>> getMarkers(TraceEventKey key) {

		Marker<TraceEventKey> marker = new Marker<TraceEventKey>(key, key.toString());

		List<Marker<TraceEventKey>> markerList = new ArrayList<>();
		markerList.add(marker);

		return markerList;
	}

	@Override
	public void addMarker(Marker<TraceEventKey> marker) {
		LOG.info("Error: CompareMarkerModel doesnt explictly add marker.");
	}

	@Override
	public void removeMarker(TraceEventKey key, int index) {
		LOG.info("Error: CompareMarkerModel doesnt explictly remove marker.");
	}

	@Override
	public void clearMarkers() {
		LOG.info("Error: CompareMarkerModel doesnt explictly clear markers.");

	}

}
