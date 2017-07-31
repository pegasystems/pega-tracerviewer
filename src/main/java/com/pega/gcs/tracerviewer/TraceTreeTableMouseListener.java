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
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.RightClickMenuItem;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkAddDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkDeleteDialog;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkOpenDialog;
import com.pega.gcs.fringecommon.guiutilities.markerbar.Marker;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableModelAdapter;
import com.pega.gcs.fringecommon.xmltreetable.XMLNode;
import com.pega.gcs.fringecommon.xmltreetable.XMLTreeTableDetailJDialog;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTreeTableMouseListener extends MouseAdapter {

	private Component mainWindow;

	private List<TraceTreeTable> traceTreeTableList;

	private Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap;

	/**
	 * @param traceTableList
	 */
	public TraceTreeTableMouseListener(Component mainWindow) {

		this.mainWindow = mainWindow;

		traceTreeTableList = new ArrayList<TraceTreeTable>();
		xmlTreeTableDetailJDialogMap = new HashMap<String, XMLTreeTableDetailJDialog>();
	}

	protected List<TraceTreeTable> getTraceTreeTableList() {
		return traceTreeTableList;
	}

	protected Component getMainWindow() {
		return mainWindow;
	}

	protected Map<String, XMLTreeTableDetailJDialog> getXmlTreeTableDetailJDialogMap() {
		return xmlTreeTableDetailJDialogMap;
	}

	public void addTraceTreeTable(TraceTreeTable traceTreeTable) {
		traceTreeTableList.add(traceTreeTable);
	}

	private boolean isIntendedSource(TraceTreeTable traceTable) {

		boolean intendedSource = traceTreeTableList.contains(traceTable);

		return intendedSource;

	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if (SwingUtilities.isRightMouseButton(e)) {

			final List<Integer> selectedRowList = new ArrayList<Integer>();

			final TraceTreeTable source = (TraceTreeTable) e.getSource();

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

				final int size = selectedRowList.size();

				if (size > 0) {

					TraceTreeTableModelAdapter traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) source
							.getModel();

					final JPopupMenu popupMenu = new JPopupMenu();

					RightClickMenuItem copyAsXML = getCopyAsXMLRightClickMenuItem(popupMenu, selectedRowList);

					popupMenu.add(copyAsXML);

					// setup compare between 2 rows
					if (size == 2) {

						RightClickMenuItem compareMenuItem = getCompareRightClickMenuItem(selectedRowList,
								traceTreeTableModelAdapter);

						popupMenu.add(compareMenuItem);
					}

					RightClickMenuItem addBookmarkMenuItem = getAddBookmarkRightClickMenuItem(popupMenu,
							selectedRowList, traceTreeTableModelAdapter);

					popupMenu.add(addBookmarkMenuItem);

					if (size == 1) {

						getOpenDeleteBookmarkRightClickMenuItem(popupMenu, selectedRowList, traceTreeTableModelAdapter);

						final JTree tree = source.getTree();

						final TreePath treePath = tree.getPathForRow(selectedRows[0]);

						AbstractTreeTableNode treeNode = (AbstractTreeTableNode) treePath.getLastPathComponent();

						if (treeNode instanceof TraceEventTreeNode) {

							final TraceEventTreeNode traceEventTreeNode = (TraceEventTreeNode) treeNode;

							TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

							if (traceEvent != null) {

								Boolean isEndEvent = traceEvent.isEndEvent();

								// end event is true
								if ((isEndEvent != null) && isEndEvent) {

									RightClickMenuItem beginEvent = new RightClickMenuItem("Select Begin Event");

									beginEvent.addActionListener(new ActionListener() {

										@Override
										public void actionPerformed(ActionEvent e) {

											TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) traceEventTreeNode
													.getParent();

											int currentNodeIndex = parentTraceEventTreeNode
													.getIndex(traceEventTreeNode);

											if (currentNodeIndex > 0) {

												TraceEventTreeNode beginNode = (TraceEventTreeNode) parentTraceEventTreeNode
														.getChildAt(currentNodeIndex - 1);

												source.scrollNodeToVisible(beginNode);
											}

											popupMenu.setVisible(false);

										}
									});

									popupMenu.add(beginEvent);

								} else if (isEndEvent != null) {

									RightClickMenuItem endEvent = new RightClickMenuItem("Select End Event");

									endEvent.addActionListener(new ActionListener() {

										@Override
										public void actionPerformed(ActionEvent e) {

											TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) traceEventTreeNode
													.getParent();

											int currentNodeIndex = parentTraceEventTreeNode
													.getIndex(traceEventTreeNode);

											int childCount = parentTraceEventTreeNode.getChildCount();

											if ((currentNodeIndex != -1) && (currentNodeIndex < (childCount - 1))) {

												TraceEventTreeNode endNode = (TraceEventTreeNode) parentTraceEventTreeNode
														.getChildAt(currentNodeIndex + 1);

												source.scrollNodeToVisible(endNode);
											}

											popupMenu.setVisible(false);

										}
									});

									popupMenu.add(endEvent);
								}
							}
						}

						// Select Parent node
						AbstractTreeTableNode parentTreeNode = (AbstractTreeTableNode) treeNode.getParent();

						Object parentUserobject = parentTreeNode.getUserObject();

						if (parentUserobject != null) {

							RightClickMenuItem parentEvent = new RightClickMenuItem("Select Parent Event");

							parentEvent.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {

									source.scrollNodeToVisible(parentTreeNode);

									popupMenu.setVisible(false);
								}
							});

							popupMenu.add(parentEvent);

						}

						boolean expanded = tree.isExpanded(treePath);

						if (!treeNode.isLeaf() && !expanded) {

							RightClickMenuItem expand = new RightClickMenuItem("Expand");

							expand.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {

									tree.expandPath(treePath);

									popupMenu.setVisible(false);

									tree.setSelectionRow(selectedRowList.get(0));

								}
							});

							RightClickMenuItem expandAllChildren = new RightClickMenuItem("Expand All Children");

							expandAllChildren.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {

									source.expandAll(treePath, true, 1);
									popupMenu.setVisible(false);

								}
							});

							popupMenu.add(expand);
							popupMenu.add(expandAllChildren);

						} else if (!treeNode.isLeaf() && expanded) {
							RightClickMenuItem collapse = new RightClickMenuItem("Collapse");

							collapse.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {

									tree.collapsePath(treePath);

									popupMenu.setVisible(false);

								}
							});

							RightClickMenuItem collapseAllChildren = new RightClickMenuItem("Collapse All Children");

							collapseAllChildren.addActionListener(new ActionListener() {

								@Override
								public void actionPerformed(ActionEvent e) {

									// collapse only children
									source.expandAll(treePath, false, 0);
									popupMenu.setVisible(false);

								}
							});

							popupMenu.add(collapse);
							popupMenu.add(collapseAllChildren);
						}
					}

					popupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		} else if (e.getClickCount() == 2) {

			TraceTreeTable source = (TraceTreeTable) e.getSource();

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

				List<TraceTreeTable> traceTreeTableList = getTraceTreeTableList();

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

				String data = "";
				boolean compareMode = traceTreeTableList.size() > 1;
				int counter = 1;

				for (TraceTreeTable traceTreeTable : traceTreeTableList) {

					TreeTableModelAdapter treeTableModelAdapter = (TreeTableModelAdapter) traceTreeTable.getModel();

					List<Element> elementList = new ArrayList<Element>();

					for (int selectedRow : selectedRowList) {

						AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) treeTableModelAdapter
								.getValueAt(selectedRow, 0);

						List<TraceEvent> traceEventList = traceEventTreeNode.getTraceEvents();

						for (TraceEvent traceEvent : traceEventList) {
							elementList.add(traceEvent.getTraceEventRootElement());
						}
					}

					String elementXML = XMLNode.getElementsAsXML(elementList);

					if (compareMode) {
						data = data + "<!--- Table " + counter + "--->";
					}

					data = data + elementXML;

					if (compareMode) {
						data = data + "<!--- Table " + counter + "--->";
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
			TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

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

					List<Element> traceEventPropertyElementList = new ArrayList<>();

					List<String> searchStrList = new ArrayList<>();

					List<TreeTableColumn> treeTableColumnList = new ArrayList<>();
					treeTableColumnList.add(TreeTableColumn.NAME_COLUMN);

					for (int i = 0; i < size; i++) {

						int row = selectedRowList.get(i);
						rowStrSB.append(row);
						rowStrSB.append("-");

						AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
								.getValueAt(row, 0);

						List<TraceEvent> traceEventList = traceEventTreeNode.getTraceEvents();

						for (TraceEvent traceEvent : traceEventList) {

							Element traceEventPropertyElement = null;
							String searchStr = null;

							if (traceEvent != null) {

								String traceLine = traceEvent.getLine();

								seqSB.append(traceLine);

								TreeTableColumn treeTableColumn = new TreeTableColumn(traceLine, String.class);
								treeTableColumnList.add(treeTableColumn);

								if (seqSB.length() > 0) {
									seqSB.append(", ");
								}

								Object searchStrObj = null;

								boolean searchFound = traceEvent.isSearchFound();

								if (searchFound) {

									SearchModel<TraceEventKey> searchModel = traceTreeTableModelAdapter
											.getSearchModel();
									searchStrObj = searchModel.getSearchStrObj();

									if (!(searchStrObj instanceof SearchEventType)) {
										searchStr = searchStrObj.toString();
									}
								}

								traceEventPropertyElement = traceEvent.getTraceEventRootElement();

							}

							traceEventPropertyElementList.add(traceEventPropertyElement);
							searchStrList.add(searchStr);

						}
					}

					String modelName = traceTreeTableModelAdapter.getModelName();

					StringBuffer titleSB = new StringBuffer();

					titleSB.append(modelName);
					titleSB.append(" - Properties on Page - Trace Event [");
					titleSB.append(seqSB);
					titleSB.append("]");

					String title = titleSB.toString();

					Element[] traceEventPropertyElementArray = traceEventPropertyElementList
							.toArray(new Element[traceEventPropertyElementList.size()]);
					String[] searchStrArray = searchStrList.toArray(new String[searchStrList.size()]);
					TreeTableColumn[] treeTableColumnArray = treeTableColumnList
							.toArray(new TreeTableColumn[treeTableColumnList.size()]);

					Dimension dimension = new Dimension(1000, 800);

					ImageIcon appIcon = BaseFrame.getAppIcon();
					Component mainWindow = getMainWindow();

					xmlTreeTableDetailJDialog = new XMLTreeTableDetailJDialog(title, traceEventPropertyElementArray,
							searchStrArray, treeTableColumnArray, dimension, TraceEventFactory.xmlElementTableTypeMap,
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
			TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

		RightClickMenuItem addBookmarkMenuItem = new RightClickMenuItem("Add Bookmark");

		addBookmarkMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				BookmarkModel<TraceEventKey> bookmarkModel = traceTreeTableModelAdapter.getBookmarkModel();

				// add bookmark
				Map<TraceEventKey, TraceEvent> teKeyMap = new HashMap<>();

				for (int selectedRow : selectedRowList) {

					AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
							.getValueAt(selectedRow, 0);

					List<TraceEvent> traceEvents = traceEventTreeNode.getTraceEvents();

					if (traceEvents != null) {

						for (TraceEvent traceEvent : traceEvents) {
							teKeyMap.put(traceEvent.getKey(), traceEvent);
						}
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
			TraceTreeTableModelAdapter traceTreeTableModelAdapter) {

		Map<TraceEventKey, List<Marker<TraceEventKey>>> bookmarkListMap = new HashMap<>();

		BookmarkModel<TraceEventKey> bookmarkModel = traceTreeTableModelAdapter.getBookmarkModel();

		AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
				.getValueAt(selectedRowList.get(0), 0);

		List<TraceEvent> traceEvents = traceEventTreeNode.getTraceEvents();

		if (traceEvents != null) {

			for (TraceEvent traceEvent : traceEvents) {

				TraceEventKey traceEventKey = traceEvent.getKey();
				List<Marker<TraceEventKey>> bookmarkList = bookmarkModel.getMarkers(traceEventKey);

				if ((bookmarkList != null) && (bookmarkList.size() > 0)) {
					bookmarkListMap.put(traceEventKey, bookmarkList);
				}
			}
		}

		if (bookmarkListMap.size() > 0) {

			RightClickMenuItem openBookmarkMenuItem = new RightClickMenuItem("Open Bookmark");

			openBookmarkMenuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					ImageIcon appIcon = BaseFrame.getAppIcon();
					Component mainWindow = getMainWindow();

					BookmarkOpenDialog<TraceEventKey> bookmarkOpenDialog;
					bookmarkOpenDialog = new BookmarkOpenDialog<TraceEventKey>(appIcon, mainWindow, bookmarkListMap);

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

					List<TraceEventKey> keyList = new ArrayList<>(bookmarkListMap.keySet());

					BookmarkDeleteDialog<TraceEventKey> bookmarkDeleteDialog;

					bookmarkDeleteDialog = new BookmarkDeleteDialog<TraceEventKey>(appIcon, mainWindow, bookmarkModel,
							keyList);

					bookmarkDeleteDialog.setVisible(true);

					popupMenu.setVisible(false);

				}
			});

			popupMenu.add(openBookmarkMenuItem);
			popupMenu.add(deleteBookmarkMenuItem);

		}

	}

	protected void performDoubleClick(TraceTreeTable source) {

		if (isIntendedSource(source)) {

			int row = source.getSelectedRow();
			final String rowStr = String.valueOf(row);

			Map<String, XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogMap = getXmlTreeTableDetailJDialogMap();

			XMLTreeTableDetailJDialog xmlTreeTableDetailJDialog = xmlTreeTableDetailJDialogMap.get(rowStr);

			if (xmlTreeTableDetailJDialog == null) {

				List<TraceEvent> traceEventList = new ArrayList<TraceEvent>();
				List<String> searchStrList = new ArrayList<String>();
				StringBuffer seqSB = new StringBuffer();
				StringBuffer modelNameSB = new StringBuffer();

				List<TraceTreeTable> traceTreeTableList = getTraceTreeTableList();

				for (TraceTreeTable traceTreeTable : traceTreeTableList) {

					TraceTreeTableModelAdapter traceTreeTableModelAdapter;

					traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable.getModel();

					String modelName = traceTreeTableModelAdapter.getModelName();

					if (modelNameSB.length() > 0) {
						modelNameSB.append(", ");
					}

					modelNameSB.append(modelName);

					SearchModel<TraceEventKey> searchModel = traceTreeTableModelAdapter.getSearchModel();
					Object searchStrObj = searchModel.getSearchStrObj();

					String searchStr = null;

					if (searchStrObj != null) {
						if (!(searchStrObj instanceof SearchEventType)) {
							searchStr = searchStrObj.toString();
						}
					}

					AbstractTraceEventTreeNode traceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
							.getValueAt(row, 0);

					List<TraceEvent> traceEvents = traceEventTreeNode.getTraceEvents();

					for (TraceEvent traceEvent : traceEvents) {
						traceEventList.add(traceEvent);
						searchStrList.add(searchStr);
					}
				}

				int size = traceEventList.size();
				Element[] traceEventPropertyElementArray = new Element[size];
				String[] searchStrArray = new String[size];

				TreeTableColumn[] treeTableColumns = new TreeTableColumn[size + 1];
				treeTableColumns[0] = TreeTableColumn.NAME_COLUMN;

				for (int i = 0; i < size; i++) {

					TraceEvent traceEvent = traceEventList.get(i);

					String traceLine = traceEvent.getLine();

					String columnName = traceEvent.getEventName();
					columnName = columnName + " [" + traceLine + "]";

					if (seqSB.length() > 0) {
						seqSB.append(", ");
					}

					seqSB.append(traceLine);

					treeTableColumns[i + 1] = new TreeTableColumn(columnName, String.class);

					String searchStr = searchStrList.get(i);

					Element traceEventPropertyElement;
					traceEventPropertyElement = traceEvent.getTraceEventRootElement();

					traceEventPropertyElementArray[i] = traceEventPropertyElement;
					searchStrArray[i] = searchStr;
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

		Collection<XMLTreeTableDetailJDialog> xmlTreeTableDetailJDialogList = xmlTreeTableDetailJDialogMap.values();

		for (XMLTreeTableDetailJDialog xmlTreeTableDetailJDialog : xmlTreeTableDetailJDialogList) {
			xmlTreeTableDetailJDialog.dispose();
		}

		xmlTreeTableDetailJDialogList.clear();

	}

}
