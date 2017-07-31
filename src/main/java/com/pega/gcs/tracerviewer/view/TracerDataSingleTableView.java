/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.view;

import javax.swing.JPanel;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTable;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.TraceTableMouseListener;

public class TracerDataSingleTableView extends TracerDataSingleView {

	private static final long serialVersionUID = -3080637746044405825L;

	private TraceTable traceTable;

	public TracerDataSingleTableView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
			TraceNavigationTableController traceNavigationTableController) {
		super(traceTableModel, supplementUtilityJPanel, traceNavigationTableController);
	}

	@Override
	protected CustomJTable getTracerDataTable() {
		if (traceTable == null) {

			TraceTableModel traceTableModel = getTraceTableModel();

			traceTable = new TraceTable(traceTableModel);
			traceTable.setFillsViewportHeight(true);

			TraceTableMouseListener traceTableMouseListener = new TraceTableMouseListener(this);

			traceTableMouseListener.addTraceTable(traceTable);

			traceTable.addMouseListener(traceTableMouseListener);
		}

		return traceTable;
	}

	@Override
	protected JPanel getAdditionalUtilityPanel() {
		return null;
	}

}
