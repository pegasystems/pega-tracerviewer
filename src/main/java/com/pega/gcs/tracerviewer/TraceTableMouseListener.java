/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.CustomJTable;
import com.pega.gcs.fringecommon.guiutilities.RightClickMenuItem;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkAddDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkDeleteDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkOpenDialog;
import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.xmltreetable.XMLNode;
import com.pega.gcs.fringecommon.xmltreetable.XMLTreeTableDetailJDialog;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTableMouseListener extends MouseAdapter {

	private Component mainWindow;

	private List<TraceTable> traceTableList;

	private Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap;

	/**
	 * @param traceTableList
	 */
	public TraceTableMouseListener(Component mainWindow) {

		this.mainWindow = mainWindow;

		traceTableList = new ArrayList<TraceTable>();
		xmlTreeTableDetailJDialogMap = new HashMap<String, XMLTreeTableDetailJDialog>();
	}

	protected List<TraceTable> getTraceTableList() {
		return traceTableList;
	}

	protected Component getMainWindow() {
		return mainWindow;
	}

	protected Map<String, XMLTreeTableDetailJDialog> getXmlTreeTableDetailJDialogMap() {
		return xmlTreeTableDetailJDialogMap;
	}

	public void addTraceTable(TraceTable traceTable) {
		traceTableList.add(traceTable);
	}

	protected boolean isIntendedSource(TraceTable traceTable) {

		boolean intendedSource = traceTableList.contains(traceTable);

		return intendedSource;

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (SwingUtilities.isRightMouseButton(e)) {

			final List<Integer> selectedRowList = new ArrayList<Integer>();

			final TraceTable source = (TraceTable) e.getSource();

			if (isIntendedSource(source)) {

				int[] selectedRows = source.getSelectedRows();

				// in case the row was not selected when right clicking then
				// based on the point, select the row.
				Point point = e.getPoint();

				if ((selectedRows != null) && (selectedRows.length <= 1)) {

					int selectedRow = source.rowAtPoint(point);

					if (selectedRow != -1) {
						// select the row first
						source.setRowSelectionInterval(selectedRow, selectedRow);
						selectedRows = new int[] { selectedRow };
					}
				}

				for (int selectedRow : selectedRows) {
					selectedRowList.add(selectedRow);
				}

				int size = selectedRowList.size();

				if (size > 0) {

					TraceTableModel traceTableModel = (TraceTableModel) source.getModel();

					JPopupMenu popupMenu = new JPopupMenu();

					RightClickMenuItem copyAsXML = getCopyAsXMLRightClickMenuItem(popupMenu, selectedRowList);

					popupMenu.add(copyAsXML);

					// setup compare between 2 rows
					if (size == 2) {

						RightClickMenuItem compareMenuItem = getCompareRightClickMenuItem(selectedRowList,
								traceTableModel);

						popupMenu.add(compareMenuItem);
					}

					RightClickMenuItem addBookmarkMenuItem = getAddBookmarkRightClickMenuItem(popupMenu,
							selectedRowList, traceTableModel);

					popupMenu.add(addBookmarkMenuItem);

					// show open and delete
					if (size == 1) {
						getOpenDeleteBookmarkRightClickMenuItem(popupMenu, selectedRowList, traceTableModel);
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		} else if (e.getClickCount() == 2) {

			TraceTable source = (TraceTable) e.getSource();

			performDoubleClick(source);

		} else {
			super.mouseClicked(e);
		}
	}

	protected RightClickMenuItem getCopyAsXMLRightClickMenuItem(JPopupMenu popupMenu, List<Integer> selectedRowList) {

		RightClickMenuItem copyAsXML = new RightClickMenuItem("Copy as XML");

		copyAsXML.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				List<TraceTable> traceTableList = getTraceTableList();

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

				String data = "";
				boolean compareMode = traceTableList.size() > 1;
				int counter = 1;

				for (CustomJTable traceTable : traceTableList) {

					TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

					List<Element> elementList = new ArrayList<Element>();

					for (int selectedRow : selectedRowList) {

						TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRow, 0);

						if ((traceEvent != null) && (traceEvent.getTraceEventRootElement() != null)) {
							elementList.add(traceEvent.getTraceEventRootElement());
						}
					}

					String elementXML = XMLNode.getElementsAsXML(elementList);

					if (compareMode) {
						data = data + "<!--- Table " + counter + "--->";
					}

					data = data + elementXML;

					if (compareMode) {
						data = data + "<!--- Table " + counter + "--->\n";
					}

					counter++;
				}

				clipboard.setContents(new StringSelection(data), copyAsXML);

				popupMenu.setVisible(false);

			}
		});

		return copyAsXML;
	}

	private RightClickMenuItem getCompareRightClickMenuItem(List<Integer> selectedRowList,
			TraceTableModel traceTableModel) {

		RightClickMenuItem compareMenuItem = new RightClickMenuItem("Compare");

		compareMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				StringBuffer rowStrSB = new StringBuffer();
				int size = 2; // comparing 2 rows only

				Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap;
				xmlTreeTableDetailJDialogMap = getXmlTreeTableDetailJDialogMap();

				for (int i = 0; i < size; i++) {

					int row = selectedRowList.get(i);
					rowStrSB.append(row);
					rowStrSB.append("-");
				}

				final String rowStr = rowStrSB.toString();

				XMLTreeTableDetailJDialog xmlTreeTableDetailJDialog = xmlTreeTableDetailJDialogMap.get(rowStr);

				if (xmlTreeTableDetailJDialog == null) {

					StringBuffer seqSB = new StringBuffer();

					Element[] traceEventPropertyElementArray = new Element[size];
					String[] searchStrArray = new String[size];

					TreeTableColumn[] treeTableColumns = new TreeTableColumn[size + 1];
					treeTableColumns[0] = TreeTableColumn.NAME_COLUMN;

					for (int i = 0; i < size; i++) {

						int row = selectedRowList.get(i);
						rowStrSB.append(row);
						rowStrSB.append("-");

						TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(row, 0);

						Element traceEventPropertyElement = null;
						String searchStr = null;

						if (traceEvent != null) {

							String traceLine = traceEvent.getLine();

							seqSB.append(traceLine);

							treeTableColumns[i + 1] = new TreeTableColumn(traceLine, String.class);

							if (seqSB.length() > 0) {
								seqSB.append(", ");
							}

							Object searchStrObj = null;

							boolean searchFound = traceEvent.isSearchFound();

							if (searchFound) {
								searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

								if (!(searchStrObj instanceof SearchEventType)) {
									searchStr = searchStrObj.toString();
								}
							}

							traceEventPropertyElement = traceEvent.getTraceEventRootElement();

						}

						traceEventPropertyElementArray[i] = traceEventPropertyElement;
						searchStrArray[i] = searchStr;

					}

					String modelName = traceTableModel.getModelName();

					StringBuffer titleSB = new StringBuffer();

					titleSB.append(modelName);
					titleSB.append(" - Properties on Page - Trace Event [");
					titleSB.append(seqSB);
					titleSB.append("]");

					String title = titleSB.toString();

					Dimension dimension = new Dimension(1000, 800);

					ImageIcon appIcon = BaseFrame.getAppIcon();
					Component mainWindow = getMainWindow();

					xmlTreeTableDetailJDialog = new XMLTreeTableDetailJDialog(title, traceEventPropertyElementArray,
							searchStrArray, treeTableColumns, dimension, TraceEventFactory.xmlElementTableTypeMap,
							appIcon, mainWindow);

					xmlTreeTableDetailJDialog.addWindowListener(new WindowAdapter() {

						@Override
						public void windowClosing(WindowEvent e) {
							super.windowClosing(e);

							xmlTreeTableDetailJDialogMap.remove(rowStr);
						}

					});

					xmlTreeTableDetailJDialogMap.put(rowStr, xmlTreeTableDetailJDialog);

				} else {
					xmlTreeTableDetailJDialog.toFront();
				}
			}
		});

		return compareMenuItem;
	}

	private RightClickMenuItem getAddBookmarkRightClickMenuItem(JPopupMenu popupMenu, List<Integer> selectedRowList,
			TraceTableModel traceTableModel) {

		RightClickMenuItem addBookmarkMenuItem = new RightClickMenuItem("Add Bookmark");

		addBookmarkMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

				// add bookmark
				Map<TraceEventKey, TraceEvent> teKeyMap = new HashMap<>();

				for (int selectedRow : selectedRowList) {

					TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRow, 0);

					if (traceEvent != null) {

						teKeyMap.put(traceEvent.getKey(), traceEvent);
					}
				}

				ImageIcon appIcon = BaseFrame.getAppIcon();
				Component mainWindow = getMainWindow();

				BookmarkAddDialog<TraceEventKey> bookmarkAddDialog;
				bookmarkAddDialog = new BookmarkAddDialog<TraceEventKey>(appIcon, mainWindow, bookmarkModel,
						new ArrayList<TraceEventKey>(teKeyMap.keySet())) {

					private static final long serialVersionUID = -2139967893143937083L;

					@Override
					public List<Marker<TraceEventKey>> getMarkerList(List<TraceEventKey> keyList, String text) {

						List<Marker<TraceEventKey>> markerList = new ArrayList<>();

						for (TraceEventKey key : keyList) {

							TraceEvent te = teKeyMap.get(key);

							String line = te.getLine();

							TraceEventMarker traceEventMarker;

							traceEventMarker = new TraceEventMarker(key, text, line);

							markerList.add(traceEventMarker);
						}

						return markerList;
					}
				};

				bookmarkAddDialog.setVisible(true);

				popupMenu.setVisible(false);

			}
		});

		return addBookmarkMenuItem;
	}

	private void getOpenDeleteBookmarkRightClickMenuItem(JPopupMenu popupMenu, List<Integer> selectedRowList,
			TraceTableModel traceTableModel) {

		TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(selectedRowList.get(0), 0);

		if (traceEvent != null) {

			BookmarkModel<TraceEventKey> bookmarkModel = traceTableModel.getBookmarkModel();

			TraceEventKey traceEventKey = traceEvent.getKey();

			List<Marker<TraceEventKey>> bookmarkList = bookmarkModel.getMarkers(traceEventKey);

			if ((bookmarkList != null) && (bookmarkList.size() > 0)) {

				RightClickMenuItem openBookmarkMenuItem = new RightClickMenuItem("Open Bookmark");

				openBookmarkMenuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						ImageIcon appIcon = BaseFrame.getAppIcon();
						Component mainWindow = getMainWindow();

						Map<TraceEventKey, List<Marker<TraceEventKey>>> bookmarkListMap = new HashMap<>();
						bookmarkListMap.put(traceEventKey, bookmarkList);

						BookmarkOpenDialog<TraceEventKey> bookmarkOpenDialog;
						bookmarkOpenDialog = new BookmarkOpenDialog<TraceEventKey>(appIcon, mainWindow,
								bookmarkListMap);

						bookmarkOpenDialog.setVisible(true);

						popupMenu.setVisible(false);

					}
				});

				RightClickMenuItem deleteBookmarkMenuItem = new RightClickMenuItem("Delete Bookmark");

				deleteBookmarkMenuItem.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						ImageIcon appIcon = BaseFrame.getAppIcon();
						Component mainWindow = getMainWindow();

						List<TraceEventKey> keyList = new ArrayList<>();
						keyList.add(traceEventKey);
						BookmarkDeleteDialog<TraceEventKey> bookmarkDeleteDialog;

						bookmarkDeleteDialog = new BookmarkDeleteDialog<TraceEventKey>(appIcon, mainWindow,
								bookmarkModel, keyList);
						bookmarkDeleteDialog.setVisible(true);

						popupMenu.setVisible(false);

					}
				});

				popupMenu.add(openBookmarkMenuItem);
				popupMenu.add(deleteBookmarkMenuItem);

			}

		}
	}

	protected void performDoubleClick(TraceTable source) {

		if (isIntendedSource(source)) {

			int row = source.getSelectedRow();
			final String rowStr = String.valueOf(row);

			StringBuffer seqSB = new StringBuffer();

			Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap = getXmlTreeTableDetailJDialogMap();

			XMLTreeTableDetailJDialog xmlTreeTableDetailJDialog = xmlTreeTableDetailJDialogMap.get(rowStr);

			if (xmlTreeTableDetailJDialog == null) {

				List<TraceTable> traceTableList = getTraceTableList();

				int size = traceTableList.size();
				Element[] traceEventPropertyElementArray = new Element[size];
				String[] searchStrArray = new String[size];

				int counter = 0;

				TreeTableColumn[] treeTableColumns = new TreeTableColumn[size + 1];
				treeTableColumns[0] = TreeTableColumn.NAME_COLUMN;

				StringBuffer modelNameSB = new StringBuffer();

				for (CustomJTable traceTable : traceTableList) {

					Element traceEventPropertyElement = null;
					String searchStr = null;

					TraceTableModel traceTableModel = (TraceTableModel) traceTable.getModel();

					String modelName = traceTableModel.getModelName();

					if (modelNameSB.length() > 0) {
						modelNameSB.append(", ");
					}

					modelNameSB.append(modelName);

					TraceEvent traceEvent = (TraceEvent) traceTableModel.getValueAt(row, 0);

					if (traceEvent != null) {

						String traceLine = traceEvent.getLine();

						String columnName = traceEvent.getEventName();
						columnName = columnName + " [" + traceLine + "]";

						if (seqSB.length() > 0) {
							seqSB.append(", ");
						}

						seqSB.append(traceLine);

						treeTableColumns[counter + 1] = new TreeTableColumn(columnName, String.class);

						Object searchStrObj = null;

						boolean searchFound = traceEvent.isSearchFound();

						if (searchFound) {
							searchStrObj = traceTableModel.getSearchModel().getSearchStrObj();

							if (!(searchStrObj instanceof SearchEventType)) {
								searchStr = searchStrObj.toString();
							}
						}

						// now show all node data for trace event
						// // show full trace event dialog on last column
						// // (ruleset)
						// if (column == traceTable.getColumnCount() - 1) {

						traceEventPropertyElement = traceEvent.getTraceEventRootElement();

						// } else {
						//
						// traceEventPropertyElement = traceEvent
						// .getTraceEventPropertyElement();
						//
						// }

					}

					traceEventPropertyElementArray[counter] = traceEventPropertyElement;
					searchStrArray[counter] = searchStr;
					counter++;
				}

				StringBuffer titleSB = new StringBuffer();

				titleSB.append(modelNameSB);
				titleSB.append(" - Properties on Page - Trace Event [");
				titleSB.append(seqSB);
				titleSB.append("]");

				String title = titleSB.toString();

				Dimension dimension = new Dimension(1000, 800);

				xmlTreeTableDetailJDialog = new XMLTreeTableDetailJDialog(title, traceEventPropertyElementArray,
						searchStrArray, treeTableColumns, dimension, TraceEventFactory.xmlElementTableTypeMap,
						BaseFrame.getAppIcon(), mainWindow);

				xmlTreeTableDetailJDialog.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosing(WindowEvent e) {
						super.windowClosing(e);

						Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap;
						xmlTreeTableDetailJDialogMap = getXmlTreeTableDetailJDialogMap();

						xmlTreeTableDetailJDialogMap.remove(rowStr);
					}

				});

				xmlTreeTableDetailJDialogMap.put(rowStr, xmlTreeTableDetailJDialog);
			} else {
				// bring back to focus
				xmlTreeTableDetailJDialog.toFront();
			}
		}
	}

	public void clearXMLTreeTableDetailJDialogList() {

		Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap = getXmlTreeTableDetailJDialogMap();

		Collection<XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogList = xmlTreeTableDetailJDialogMap.values();

		for (XMLTreeTableDetailJDialog xmlTreeTableDetailJDialog : xmlTreeTableDetailJDialogList) {
			xmlTreeTableDetailJDialog.dispose();
		}

		xmlTreeTableDetailJDialogList.clear();

	}

}
