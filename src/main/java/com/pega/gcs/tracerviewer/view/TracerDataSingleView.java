/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.search.SearchPanel;
import com.pega.gcs.tracerviewer.TraceNavigationTableController;
import com.pega.gcs.tracerviewer.TraceTableModel;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public abstract class TracerDataSingleView extends TracerDataView {

	private static final long serialVersionUID = -1109125493476264152L;

	private SearchPanel<TraceEventKey> searchPanel;

	private JTextField statusBar;

	// not using TraceTable to accommodate tree view as well.
	protected abstract CustomJTable getTracerDataTable();

	protected abstract JPanel getAdditionalUtilityPanel();

	public TracerDataSingleView(TraceTableModel traceTableModel, JPanel supplementUtilityJPanel,
			TraceNavigationTableController traceNavigationTableController) {

		super(traceTableModel, traceNavigationTableController, supplementUtilityJPanel);

		CustomJTable traceTable = getTracerDataTable();
		traceNavigationTableController.addCustomJTable(traceTable);

		searchPanel = new SearchPanel<TraceEventKey>(traceNavigationTableController, traceTableModel.getSearchModel());
		//
		// SearchModel<TraceEventKey> searchModel =
		// traceTableModel.getSearchModel();
		// searchModel.addSearchModelListener(searchPanel);

		setLayout(new BorderLayout());

		JPanel utilityJPanel = getUtilityJPanel();
		JPanel tracerDataJPanel = getTracerDataJPanel();
		JPanel statusBarJPanel = getStatusBarJPanel();

		add(utilityJPanel, BorderLayout.NORTH);
		add(tracerDataJPanel, BorderLayout.CENTER);
		add(statusBarJPanel, BorderLayout.SOUTH);

		traceTableModel.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {

				String propertyName = evt.getPropertyName();

				if ("message".equals(propertyName)) {

					JTextField statusBar = getStatusBar();
					Message message = (Message) evt.getNewValue();
					setMessage(statusBar, message);
				}

			}
		});

	}

	@Override
	protected void updateSupplementUtilityJPanel() {

		JPanel supplementUtilityJPanel = getSupplementUtilityJPanel();

		supplementUtilityJPanel.removeAll();
		LayoutManager layout = new BoxLayout(supplementUtilityJPanel, BoxLayout.LINE_AXIS);
		supplementUtilityJPanel.setLayout(layout);
		supplementUtilityJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		supplementUtilityJPanel.revalidate();
		supplementUtilityJPanel.repaint();
	}

	private JPanel getUtilityJPanel() {

		JPanel utilityJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(utilityJPanel, BoxLayout.LINE_AXIS);
		utilityJPanel.setLayout(layout);

		// SearchPanel<TraceEventKey> searchPanel = getSearchPanel();
		JPanel tracerUtilsJPanel = getTracerUtilsJPanel();

		utilityJPanel.add(searchPanel);
		utilityJPanel.add(tracerUtilsJPanel);

		return utilityJPanel;
	}

	private JPanel getTracerDataJPanel() {

		// table can be tree or table
		CustomJTable tracerDataTable = getTracerDataTable();

		JPanel traceTableJPanel = new JPanel();
		traceTableJPanel.setLayout(new BorderLayout());

		JScrollPane traceTableScrollpane = new JScrollPane(tracerDataTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tracerDataTable.setPreferredScrollableViewportSize(traceTableScrollpane.getPreferredSize());

		// use the stored traceTableModel, as table can be tree or table
		TraceTableModel traceTableModel = getTraceTableModel();

		JPanel markerBarPanel = getMarkerBarPanel(traceTableModel);

		traceTableJPanel.add(traceTableScrollpane, BorderLayout.CENTER);
		traceTableJPanel.add(markerBarPanel, BorderLayout.EAST);

		return traceTableJPanel;
	}

	private JPanel getStatusBarJPanel() {

		JPanel statusBarJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(statusBarJPanel, BoxLayout.LINE_AXIS);
		statusBarJPanel.setLayout(layout);

		Dimension spacer = new Dimension(5, 16);

		JTextField statusBar = getStatusBar();

		statusBarJPanel.add(Box.createRigidArea(spacer));
		statusBarJPanel.add(statusBar);
		statusBarJPanel.add(Box.createRigidArea(spacer));

		statusBarJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		return statusBarJPanel;

	}

	private JPanel getTracerUtilsJPanel() {

		JPanel tracerReportJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(tracerReportJPanel, BoxLayout.LINE_AXIS);
		tracerReportJPanel.setLayout(layout);

		JPanel additionalUtilityPanel = getAdditionalUtilityPanel();

		Dimension dim = new Dimension(5, 30);

		tracerReportJPanel.add(Box.createHorizontalGlue());
		tracerReportJPanel.add(Box.createRigidArea(dim));

		if (additionalUtilityPanel != null) {
			tracerReportJPanel.add(additionalUtilityPanel);
			tracerReportJPanel.add(Box.createRigidArea(dim));
		}

		tracerReportJPanel.add(Box.createHorizontalGlue());

		tracerReportJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		return tracerReportJPanel;
	}

	protected JTextField getStatusBar() {

		if (statusBar == null) {
			statusBar = new JTextField();
			statusBar.setEditable(false);
			statusBar.setBackground(null);
			statusBar.setBorder(null);
		}

		return statusBar;
	}
}
