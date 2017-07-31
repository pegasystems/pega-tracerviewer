/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
/**
 * 
 */
package com.pega.gcs.tracerviewer.report;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.FilterTable;
import com.pega.gcs.tracerviewer.report.TracerReportRulesTableModel.RulesetRule;

public class TracerReportRulesTable extends FilterTable<Integer> {

	private static final long serialVersionUID = -5210682944554711757L;

	public TracerReportRulesTable(TracerReportRulesTableModel tracerReportRulesTableModel) {

		super(tracerReportRulesTableModel);

		setAutoCreateColumnsFromModel(false);

		setRowHeight(20);

		setRowSelectionAllowed(true);

		setFillsViewportHeight(true);

		TableColumnModel columnModel = tracerReportRulesTableModel.getTableColumnModel();

		setColumnModel(columnModel);

		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		JTableHeader tableHeader = getTableHeader();

		tableHeader.setReorderingAllowed(false);

		// bold the header
		Font existingFont = tableHeader.getFont();
		String existingFontName = existingFont.getName();
		int existFontSize = existingFont.getSize();
		Font newFont = new Font(existingFontName, Font.BOLD, existFontSize);
		tableHeader.setFont(newFont);

		final TableCellRenderer origTableCellRenderer = tableHeader.getDefaultRenderer();

		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {

			private static final long serialVersionUID = 2523481693501568166L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

				JLabel origComponent = (JLabel) origTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				origComponent.setHorizontalAlignment(CENTER);

				// set header height
				Dimension dim = origComponent.getPreferredSize();
				dim.setSize(dim.getWidth(), 30);
				origComponent.setPreferredSize(dim);

				return origComponent;
			}

		};

		tableHeader.setDefaultRenderer(dtcr);

		setTransferHandler(new TransferHandler() {

			private static final long serialVersionUID = 3940727586956347107L;

			@Override
			protected Transferable createTransferable(JComponent c) {

				int[] selectedRows = getSelectedRows();

				StringBuffer dataSB = new StringBuffer();

				if (selectedRows != null) {

					for (int selectedRow : selectedRows) {

						RulesetRule rulesetRule = (RulesetRule) getValueAt(selectedRow, 0);

						int columnCount = tracerReportRulesTableModel.getColumnCount();

						for (int column = 0; column < columnCount; column++) {
							String columnValue = tracerReportRulesTableModel.getColumnValue(rulesetRule, column);

							dataSB.append(columnValue);
							dataSB.append("\t");
						}
						dataSB.append("\n");
					}

				}
				return new StringSelection(dataSB.toString());
			}

			@Override
			public int getSourceActions(JComponent c) {
				return TransferHandler.COPY;
			}

		});

	}
}
