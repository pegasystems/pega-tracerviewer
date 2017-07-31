/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.SwingWorker;

import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.diff.EditCommand;
import com.pega.gcs.fringecommon.utilities.diff.Matcher;
import com.pega.gcs.fringecommon.utilities.diff.MyersDifferenceAlgorithm;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventEmpty;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTableCompareTask extends SwingWorker<Void, Void> {

	private static final Log4j2Helper LOG = new Log4j2Helper(TraceTableCompareTask.class);

	private ModalProgressMonitor mProgressMonitor;

	private TraceTableModel traceTableModel;

	private TraceTableCompareModel traceTableCompareModelLeft;

	private TraceTableCompareModel traceTableCompareModelRight;

	public TraceTableCompareTask(ModalProgressMonitor mProgressMonitor, TraceTableModel traceTableModel,
			TraceTableCompareModel traceTableCompareModelLeft, TraceTableCompareModel traceTableCompareModelRight) {

		super();

		this.mProgressMonitor = mProgressMonitor;
		this.traceTableModel = traceTableModel;
		this.traceTableCompareModelLeft = traceTableCompareModelLeft;
		this.traceTableCompareModelRight = traceTableCompareModelRight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {

		LOG.info("Tracetable compare task - Started");

		TreeMap<TraceEventKey, List<TraceEventKey>> compareNavIndexMap;
		compareNavIndexMap = new TreeMap<TraceEventKey, List<TraceEventKey>>();

		try {

			List<TraceEventKey> thisMarkerTraceEventKeyList = new ArrayList<TraceEventKey>();
			List<TraceEventKey> otherMarkerTraceEventKeyList = new ArrayList<TraceEventKey>();

			Map<TraceEventKey, TraceEvent> thisTEM = traceTableModel.getTraceEventMap();
			Map<TraceEventKey, TraceEvent> otherTEM = traceTableCompareModelRight.getTraceEventMap();

			// THIS
			List<TraceEventKey> thisTraceEventKeyList = new ArrayList<TraceEventKey>();
			List<TraceEvent> thisTraceEventList = new ArrayList<TraceEvent>();

			// OTHER
			List<TraceEventKey> otherTraceEventKeyList = new ArrayList<TraceEventKey>();
			List<TraceEvent> otherTraceEventList = new ArrayList<TraceEvent>();

			getKeyAndTraceEventList(thisTEM, thisTraceEventKeyList, thisTraceEventList);
			getKeyAndTraceEventList(otherTEM, otherTraceEventKeyList, otherTraceEventList);

			Matcher<TraceEvent> matcher = new Matcher<TraceEvent>() {

				@Override
				public boolean match(TraceEvent o1, TraceEvent o2) {
					return o1.equals(o2);
				}
			};

			long before = System.currentTimeMillis();

			List<EditCommand> editScript = MyersDifferenceAlgorithm.diffGreedyLCS(mProgressMonitor, thisTraceEventList,
					otherTraceEventList, matcher);

			long diff = System.currentTimeMillis() - before;
			DecimalFormat df = new DecimalFormat("#0.000");

			String time = df.format((double) diff / 1E3);

			LOG.info("MyersDifferenceAlgorithm diffGreedyLCS took " + time + "s.");

			if (!mProgressMonitor.isCanceled()) {

				traceTableCompareModelLeft.resetModel();
				traceTableCompareModelRight.resetModel();

				int index = 0;
				int indexThis = 0;
				int indexOther = 0;

				TraceEventKey compareNavIndexKey = null;
				EditCommand prevEC = EditCommand.SNAKE;

				TraceEventKey teKeyThis;
				TraceEventKey teKeyOther;
				TraceEventKey teKey;

				TraceEvent teCompare;
				TraceEvent te;

				Color deleteColor = Color.LIGHT_GRAY;
				Color insertColor = MyColor.LIGHTEST_GREEN;

				for (EditCommand ec : editScript) {

					TraceEventKey teKeyCompare = null;

					switch (ec) {
					case DELETE:
						// Add compare type to OTHER List
						// OTHER
						teKeyCompare = new TraceEventKey(index, -1, false);
						teCompare = new TraceEventEmpty(teKeyCompare, deleteColor);
						// newOtherTEM.put(teKeyCompare, teCompare);
						traceTableCompareModelRight.addTraceEventToMap(teCompare);

						// THIS
						teKeyThis = thisTraceEventKeyList.get(indexThis);
						// te = thisTEM.get(teKeyThis);
						te = thisTraceEventList.get(indexThis);
						teKey = new TraceEventKey(index, teKeyThis.getTraceEventIndex(), teKeyThis.isCorrupt());
						te.setTraceEventKey(teKey);
						// newThisTEM.put(teKey, te);
						traceTableCompareModelLeft.addTraceEventToMap(te);
						indexThis++;

						otherMarkerTraceEventKeyList.add(teKeyCompare);

						break;
					case INSERT:
						// Add compare type to THIS List
						// OTHER
						teKeyOther = otherTraceEventKeyList.get(indexOther);
						// te = otherTEM.get(teKeyOther);
						te = otherTraceEventList.get(indexOther);
						teKey = new TraceEventKey(index, teKeyOther.getTraceEventIndex(), teKeyOther.isCorrupt());
						te.setTraceEventKey(teKey);
						// newOtherTEM.put(teKey, te);
						traceTableCompareModelRight.addTraceEventToMap(te);
						indexOther++;

						// THIS
						teKeyCompare = new TraceEventKey(index, -1, false);
						teCompare = new TraceEventEmpty(teKeyCompare, insertColor);
						// newThisTEM.put(teKeyCompare, teCompare);
						traceTableCompareModelLeft.addTraceEventToMap(teCompare);

						thisMarkerTraceEventKeyList.add(teKeyCompare);

						break;
					case SNAKE:
						// OTHER
						teKeyOther = otherTraceEventKeyList.get(indexOther);
						// te = otherTEM.get(teKeyOther);
						te = otherTraceEventList.get(indexOther);
						teKey = new TraceEventKey(index, teKeyOther.getTraceEventIndex(), teKeyOther.isCorrupt());
						te.setTraceEventKey(teKey);
						// newOtherTEM.put(teKey, te);
						traceTableCompareModelRight.addTraceEventToMap(te);
						indexOther++;

						// THIS
						teKeyThis = thisTraceEventKeyList.get(indexThis);
						// te = thisTEM.get(teKeyThis);
						te = thisTraceEventList.get(indexThis);
						teKey = new TraceEventKey(index, teKeyThis.getTraceEventIndex(), teKeyThis.isCorrupt());
						te.setTraceEventKey(teKey);
						// newThisTEM.put(teKey, te);
						traceTableCompareModelLeft.addTraceEventToMap(te);
						indexThis++;

						break;
					default:
						break;

					}

					if ((!prevEC.equals(ec)) && (teKeyCompare != null)
					/* && (!ec.equals(EditCommand.SNAKE)) */) {

						compareNavIndexKey = teKeyCompare;

						List<TraceEventKey> compareIndexList = new ArrayList<TraceEventKey>();
						compareIndexList.add(teKeyCompare);

						compareNavIndexMap.put(compareNavIndexKey, compareIndexList);

					} else if ((compareNavIndexKey != null) && (teKeyCompare != null)) {

						List<TraceEventKey> compareIndexList;
						compareIndexList = compareNavIndexMap.get(compareNavIndexKey);

						compareIndexList.add(teKeyCompare);
					}

					prevEC = ec;
					index++;
				}

			}

			if (!mProgressMonitor.isCanceled()) {

				traceTableCompareModelLeft.setCompareMarkerList(thisMarkerTraceEventKeyList);
				traceTableCompareModelRight.setCompareMarkerList(otherMarkerTraceEventKeyList);

				traceTableCompareModelLeft.setCompareNavIndexMap(compareNavIndexMap);
			}
			// }

		} catch (Exception e) {
			LOG.error("Exception in Tracetable compare task", e);
		} finally {
			// cleanup
			System.gc();
		}

		LOG.info("TraceTableCompareTask compareNavIndexList: " + compareNavIndexMap.keySet());

		return null;
	}

	private void getKeyAndTraceEventList(Map<TraceEventKey, TraceEvent> traceEventMap,
			List<TraceEventKey> traceEventKeyList, List<TraceEvent> traceEventList) {

		traceEventKeyList.clear();
		traceEventList.clear();

		for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventMap.entrySet()) {

			TraceEventKey traceEventKey = entry.getKey();

			boolean corrupt = traceEventKey.isCorrupt();

			if ((corrupt) || (traceEventKey.getTraceEventIndex() != -1)) {

				traceEventKeyList.add(traceEventKey);

				TraceEvent traceEvent = entry.getValue();
				traceEventList.add(traceEvent);

			}
		}
	}

	private void printEventMap(Map<TraceEventKey, TraceEvent> traceEventMap) {

		LOG.info("traceEventMap: " + traceEventMap.size());
		for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventMap.entrySet()) {

			TraceEventKey traceEventKey = entry.getKey();
			TraceEvent traceEvent = entry.getValue();

			LOG.info("TraceEventKey[" + traceEventKey.getTraceEventIndex() + "] " + traceEvent);

		}

	}
}
