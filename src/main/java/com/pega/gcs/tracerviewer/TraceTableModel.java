/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;

import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CheckBoxMenuItemPopupEntry;
import com.pega.gcs.fringecommon.guiutilities.FilterColumn;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModel;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModelNavigation;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.SearchTableModelEvent;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventAlert;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.model.TraceEventType;

public class TraceTableModel extends FilterTableModel<TraceEventKey> {

	private static final long serialVersionUID = -2061492402283117131L;

	private static final Log4j2Helper LOG = new Log4j2Helper(TraceTableModel.class);

	private TraceTableModelColumn[] traceTableModelColumnArray;

	// Main list. for reference purpose only, not working on this map.
	private Map<TraceEventKey, TraceEvent> traceEventMap;

	private List<TraceEventKey> traceEventKeyList;

	// search
	private SearchData<TraceEventKey> searchData;
	private SearchModel<TraceEventKey> searchModel;

	// sets for event filters
	private Map<TraceEventType, CheckBoxMenuItemPopupEntry<TraceEventKey>> traceEventTypeCheckBoxMenuItemMap;

	// tree related variables - trace event tree node map
	private TraceEventTreeNode rootTraceEventTreeNode;
	private TraceEventCombinedTreeNode rootTraceEventCombinedTreeNode;

	private LinkedList<TraceEvent> treeBuildTraceEventList;

	private Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap;
	private Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap;

	// reporting
	private List<TraceEventKey> failedEventKeyList;
	private List<TraceEventKey> exceptionEventKeyList;
	private List<TraceEventKey> alertEventKeyList;
	private List<TraceEventKey> noStartEventKeyList;
	private List<TraceEventKey> noEndEventKeyList;
	private TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap;
	private TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap;

	public TraceTableModel(RecentFile recentFile, SearchData<TraceEventKey> searchData) {

		super(recentFile);
		this.searchData = searchData;

		resetModel();

	}

	public SearchData<TraceEventKey> getSearchData() {
		return searchData;
	}

	public TraceTableModelColumn[] getTraceTableModelColumnArray() {

		if (traceTableModelColumnArray == null) {
			traceTableModelColumnArray = TraceTableModelColumn.getTraceTableModelColumnArray();
		}

		return traceTableModelColumnArray;
	}

	protected Map<TraceEventKey, TraceEvent> getTraceEventMap() {

		if (traceEventMap == null) {
			traceEventMap = new TreeMap<TraceEventKey, TraceEvent>();
		}

		return traceEventMap;
	}

	private Map<TraceEventKey, TraceEventTreeNode> getTraceEventTreeNodeMap() {

		if (traceEventTreeNodeMap == null) {
			traceEventTreeNodeMap = new HashMap<TraceEventKey, TraceEventTreeNode>();
		}

		return traceEventTreeNodeMap;
	}

	private Map<TraceEventKey, TraceEventCombinedTreeNode> getTraceEventCombinedTreeNodeMap() {

		if (traceEventCombinedTreeNodeMap == null) {
			traceEventCombinedTreeNodeMap = new HashMap<TraceEventKey, TraceEventCombinedTreeNode>();
		}

		return traceEventCombinedTreeNodeMap;
	}

	@Override
	public void resetModel() {

		List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();
		traceEventKeyList.clear();

		Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
		traceEventMap.clear();

		Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();
		traceEventTreeNodeMap.clear();

		Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();
		traceEventCombinedTreeNodeMap.clear();

		// tree
		TraceEventTreeNode rootTraceEventTreeNode = getRootTraceEventTreeNode();
		rootTraceEventTreeNode.removeAllChildren();

		TraceEventCombinedTreeNode rootTraceEventCombinedTreeNode = getRootTraceEventCombinedTreeNode();
		rootTraceEventCombinedTreeNode.removeAllChildren();

		LinkedList<TraceEvent> treeBuildTraceEventList = getTreeBuildTraceEventList();
		treeBuildTraceEventList.clear();

		Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap;
		columnFilterMap = getColumnFilterMap();

		columnFilterMap.clear();

		TraceTableModelColumn[] traceTableModelColumnArray = getTraceTableModelColumnArray();

		for (int columnIndex = 1; columnIndex < traceTableModelColumnArray.length; columnIndex++) {

			TraceTableModelColumn traceTableModelColumn = traceTableModelColumnArray[columnIndex];

			// preventing unnecessary buildup of filter map
			if (traceTableModelColumn.isFilterable()) {

				FilterColumn filterColumn = new FilterColumn(columnIndex);

				filterColumn.setColumnFilterEnabled(false);

				columnFilterMap.put(filterColumn, null);
			}
		}

		traceEventTypeCheckBoxMenuItemMap = new TreeMap<TraceEventType, CheckBoxMenuItemPopupEntry<TraceEventKey>>();

		TraceEventType[] values = TraceEventType.values();

		Arrays.sort(values, new Comparator<TraceEventType>() {

			@Override
			public int compare(TraceEventType o1, TraceEventType o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

		for (TraceEventType traceEventType : values) {

			CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = new CheckBoxMenuItemPopupEntry<TraceEventKey>(
					traceEventType);

			traceEventTypeCheckBoxMenuItemMap.put(traceEventType, cbmipe);
		}

		// reporting
		List<TraceEventKey> failedEventKeyList = getFailedEventKeyList();
		List<TraceEventKey> exceptionEventKeyList = getExceptionEventKeyList();
		List<TraceEventKey> alertEventKeyList = getAlertEventKeyList();
		List<TraceEventKey> noStartEventKeyList = getNoStartEventKeyList();
		List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();

		Map<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();
		Map<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap = getRulesInvokedMap();

		failedEventKeyList.clear();
		exceptionEventKeyList.clear();
		alertEventKeyList.clear();
		noStartEventKeyList.clear();
		noEndEventKeyList.clear();

		ownElapsedEventKeyMap.clear();
		rulesInvokedMap.clear();

		clearSearchResults(true);

		fireTableDataChanged();
	}

	@Override
	public List<TraceEventKey> getFtmEntryKeyList() {

		if (traceEventKeyList == null) {
			traceEventKeyList = new ArrayList<TraceEventKey>();
		}

		return traceEventKeyList;
	}

	// this is called from load task. hence the order is expected to be
	// sequential
	public void addTraceEventToMap(TraceEvent traceEvent) {

		TraceEventKey traceEventKey = traceEvent.getKey();

		Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
		traceEventMap.put(traceEventKey, traceEvent);

		List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();

		traceEventKeyList.add(traceEventKey);

		// performing updateColumnFilterMap to avoid re-parsing the full map if
		// we used applyFilterEventSet(null).
		Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap = getColumnFilterMap();
		updateColumnFilterMap(traceEvent, columnFilterMap);

		TraceEventType tet = traceEvent.getTraceEventType();

		if (tet != null) {
			CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe;
			cbmipe = traceEventTypeCheckBoxMenuItemMap.get(tet);
			cbmipe.addRowIndex(traceEventKey);
		}

		buildTree(traceEvent);

	}

	// fix Issue #1 - Compare functionality not working
	// to be overridden in TraceTableCompareModel to avoid building tree for compare
	// view
	protected void buildTree(TraceEvent currentTraceEvent) {

		LinkedList<TraceEvent> treeBuildTraceEventList = getTreeBuildTraceEventList();
		Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();
		Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();

		TraceEventTreeNode previousParentTraceEventTreeNode = null;
		TraceEventCombinedTreeNode previousParentTraceEventCombinedTreeNode = null;

		TraceEvent previousParentTraceEvent = getMatchingParentTraceEvent(currentTraceEvent);

		// if no parent found. ex if this is first entry. assign to root.
		if (previousParentTraceEvent == null) {
			previousParentTraceEventTreeNode = getRootTraceEventTreeNode();
			previousParentTraceEventCombinedTreeNode = getRootTraceEventCombinedTreeNode();
		} else {
			// get the tree nodes corresponding to key
			TraceEventKey traceEventKey = previousParentTraceEvent.getKey();

			previousParentTraceEventTreeNode = traceEventTreeNodeMap.get(traceEventKey);
			previousParentTraceEventCombinedTreeNode = traceEventCombinedTreeNodeMap.get(traceEventKey);
		}

		TraceEventKey currentTraceEventKey = currentTraceEvent.getKey();

		TraceEventTreeNode currentTraceEventTreeNode = new TraceEventTreeNode(currentTraceEvent);
		TraceEventCombinedTreeNode currentTraceEventCombinedTreeNode = new TraceEventCombinedTreeNode(
				currentTraceEvent);

		traceEventTreeNodeMap.put(currentTraceEventKey, currentTraceEventTreeNode);

		Boolean endEvent = currentTraceEvent.isEndEvent();

		if (endEvent != null) {
			// current event is a block event

			if (endEvent) {
				// find the correct 'begin' node. correct = same INT, RULE#,
				// EVENT_TYPE

				TraceEvent startTraceEvent = getMatchingStartTraceEvent(currentTraceEvent);

				if (startTraceEvent != null) {

					TraceEventKey startTraceEventKey = startTraceEvent.getKey();

					// single tree node
					TraceEventTreeNode startTraceEventTreeNode = traceEventTreeNodeMap.get(startTraceEventKey);

					TraceEventTreeNode parentTraceEventTreeNode = (TraceEventTreeNode) startTraceEventTreeNode
							.getParent();
					parentTraceEventTreeNode.add(currentTraceEventTreeNode);

					// combined tree node
					TraceEventCombinedTreeNode startTraceEventCombinedTreeNode = traceEventCombinedTreeNodeMap
							.get(startTraceEventKey);

					startTraceEventCombinedTreeNode.setEndEvent(currentTraceEvent);

					traceEventCombinedTreeNodeMap.put(currentTraceEventKey, startTraceEventCombinedTreeNode);

					processTraceEventElapsed(startTraceEventTreeNode, currentTraceEventTreeNode);

				} else {
					LOG.info("Could'nt find a matching start for trace event: " + currentTraceEvent.toDebugString());

					List<TraceEventKey> noStartEventKeyList = getNoStartEventKeyList();
					noStartEventKeyList.add(currentTraceEventKey);

					previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
					previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

					traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);
				}
			} else {
				// if starting event, add to the stack
				treeBuildTraceEventList.add(currentTraceEvent);

				previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
				previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

				traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);
			}
		} else {
			// current event is a singular event.
			previousParentTraceEventTreeNode.add(currentTraceEventTreeNode);
			previousParentTraceEventCombinedTreeNode.add(currentTraceEventCombinedTreeNode);

			traceEventCombinedTreeNodeMap.put(currentTraceEventKey, currentTraceEventCombinedTreeNode);

		}

		processEvent(currentTraceEvent);

	}

	private TraceEvent getMatchingParentTraceEvent(TraceEvent childTraceEvent) {

		TraceEvent parentTraceEvent = null;

		LinkedList<TraceEvent> treeBuildTraceEventList = getTreeBuildTraceEventList();

		Iterator<TraceEvent> descendingIterator = treeBuildTraceEventList.descendingIterator();

		while (descendingIterator.hasNext()) {

			TraceEvent traceEvent = descendingIterator.next();

			if (traceEvent.isMatchingParentTraceEvent(childTraceEvent)) {
				parentTraceEvent = traceEvent;
				break;
			}
		}

		// if no possible parent found, assign it to last start block
		if ((parentTraceEvent == null)) {
			parentTraceEvent = treeBuildTraceEventList.peekLast();
		}

		return parentTraceEvent;
	}

	private TraceEvent getMatchingStartTraceEvent(TraceEvent endTraceEvent) {

		TraceEvent startTraceEvent = null;

		LinkedList<TraceEvent> treeBuildTraceEventList = getTreeBuildTraceEventList();

		int loopCounter = 0;
		int treeBuildTraceEventListSize = treeBuildTraceEventList.size();

		ArrayList<TraceEvent> noEndTraceEventList = new ArrayList<>();

		Iterator<TraceEvent> descendingIterator = treeBuildTraceEventList.descendingIterator();

		while (descendingIterator.hasNext()) {

			TraceEvent traceEvent = descendingIterator.next();
			loopCounter++;

			if (traceEvent.isMatchingStartTraceEvent(endTraceEvent)) {
				startTraceEvent = traceEvent;
				break;
			} else {
				noEndTraceEventList.add(traceEvent);
			}
		}

		// remove the entry from stack
		if (startTraceEvent != null) {
			int removeIndex = treeBuildTraceEventListSize - loopCounter;
			treeBuildTraceEventList.remove(removeIndex);
		}

		if (noEndTraceEventList.size() > 0) {

			List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();

			for (TraceEvent traceEvent : noEndTraceEventList) {

				LOG.info("Could'nt find a matching end for trace event: " + traceEvent.toDebugString());

				noEndEventKeyList.add(traceEvent.getKey());

				treeBuildTraceEventList.remove(traceEvent);

			}
		}

		return startTraceEvent;
	}

	private TraceEventTreeNode addToTree(TraceEventTreeNode parentNode, TraceEvent currentTraceEvent) {

		Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();

		TraceEventTreeNode startNode = parentNode;

		TraceEventTreeNode currentNode = new TraceEventTreeNode(currentTraceEvent);

		traceEventTreeNodeMap.put(currentTraceEvent.getKey(), currentNode);

		Boolean endEvent = currentTraceEvent.isEndEvent();

		if (endEvent != null) {

			// if end event then move 1 step higher parent
			if (endEvent) {

				// startNode is the begin node at this moment
				processTraceEventElapsed(startNode, currentNode);

				TraceEventTreeNode curParent = (TraceEventTreeNode) startNode.getParent();

				if (curParent != null) {
					startNode = curParent;
					startNode.add(currentNode);
				} else {
					startNode.add(currentNode);
				}

			}
			// if begin event then this node become parent.
			else {
				startNode.add(currentNode);
				startNode = currentNode;
			}
		} else {

			startNode.add(currentNode);
			processTraceEventElapsed(currentNode, currentNode);
		}

		processEvent(currentTraceEvent);

		return startNode;
	}

	private TraceEventCombinedTreeNode addToTreeMerged(TraceEventCombinedTreeNode parentNode,
			TraceEvent currentTraceEvent) {

		Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();

		TraceEventKey traceEventKey = currentTraceEvent.getKey();

		TraceEventCombinedTreeNode startNode = parentNode;

		TraceEventCombinedTreeNode currentNode = new TraceEventCombinedTreeNode(currentTraceEvent);

		Boolean endEvent = currentTraceEvent.isEndEvent();

		if (endEvent != null) {

			// if end event then move 1 step higher parent
			if (endEvent) {

				startNode.setEndEvent(currentTraceEvent);

				traceEventCombinedTreeNodeMap.put(traceEventKey, startNode);

				TraceEventCombinedTreeNode curParent = (TraceEventCombinedTreeNode) startNode.getParent();

				if (curParent != null) {
					startNode = curParent;
				}
			}
			// if begin event then this node become parent.
			else {
				traceEventCombinedTreeNodeMap.put(traceEventKey, currentNode);

				startNode.add(currentNode);
				startNode = currentNode;
			}
		} else {
			traceEventCombinedTreeNodeMap.put(traceEventKey, currentNode);

			startNode.add(currentNode);
		}

		return startNode;
	}

	@Override
	public int getColumnCount() {
		TraceTableModelColumn[] traceTableModelColumnArray = getTraceTableModelColumnArray();
		return traceTableModelColumnArray.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		List<TraceEventKey> traceEventIndexList = getFtmEntryKeyList();

		// supply records in reverse order??.
		int reverseRowIndex = traceEventIndexList.size() - rowIndex - 1;
		TraceEventKey traceEventKey = traceEventIndexList.get(reverseRowIndex);

		// supply in right order
		// TraceEventKey traceEventKey = traceEventIndexList.get(rowIndex);

		TraceEvent traceEvent = getEventForKey(traceEventKey);

		return traceEvent;
	}

	@Override
	public String getColumnName(int column) {

		TraceTableModelColumn[] traceTableModelColumnArray = getTraceTableModelColumnArray();

		return traceTableModelColumnArray[column].toString();
	}

	@Override
	protected int getModelColumnIndex(int column) {
		return column;
	}

	public TraceTableModelColumn getColumn(int column) {
		TraceTableModelColumn[] traceTableModelColumnArray = getTraceTableModelColumnArray();
		return traceTableModelColumnArray[column];
	}

	public Set<TraceEventType> getTraceEventTypeList() {
		return traceEventTypeCheckBoxMenuItemMap.keySet();
	}

	public CheckBoxMenuItemPopupEntry<TraceEventKey> getCheckBoxMenuItem(TraceEventType traceEventType) {
		return traceEventTypeCheckBoxMenuItemMap.get(traceEventType);
	}

	// clearing the columnFilterMap will skip the below loop
	private void updateColumnFilterMap(TraceEvent traceEvent,
			Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<TraceEventKey>>> columnFilterMap) {

		if (traceEvent != null) {

			Iterator<FilterColumn> fcIterator = columnFilterMap.keySet().iterator();

			while (fcIterator.hasNext()) {

				FilterColumn filterColumn = fcIterator.next();

				List<CheckBoxMenuItemPopupEntry<TraceEventKey>> columnFilterEntryList;
				columnFilterEntryList = columnFilterMap.get(filterColumn);

				if (columnFilterEntryList == null) {
					columnFilterEntryList = new ArrayList<CheckBoxMenuItemPopupEntry<TraceEventKey>>();
					columnFilterMap.put(filterColumn, columnFilterEntryList);
				}

				int columnIndex = filterColumn.getIndex();

				TraceTableModelColumn traceTableModelColumn = getColumn(columnIndex);

				String traceEventKeyStr = traceEvent.getColumnValueForTraceTableModelColumn(traceTableModelColumn);

				if (traceEventKeyStr == null) {
					traceEventKeyStr = FilterTableModel.NULL_STR;
				} else if ("".equals(traceEventKeyStr)) {
					traceEventKeyStr = FilterTableModel.EMPTY_STR;
				}

				CheckBoxMenuItemPopupEntry<TraceEventKey> columnFilterEntry;

				CheckBoxMenuItemPopupEntry<TraceEventKey> searchKey;
				searchKey = new CheckBoxMenuItemPopupEntry<TraceEventKey>(traceEventKeyStr);

				int index = columnFilterEntryList.indexOf(searchKey);

				if (index == -1) {
					columnFilterEntry = new CheckBoxMenuItemPopupEntry<TraceEventKey>(traceEventKeyStr);
					columnFilterEntryList.add(columnFilterEntry);
				} else {
					columnFilterEntry = columnFilterEntryList.get(index);
				}

				TraceEventKey traceEventKey = traceEvent.getKey();

				columnFilterEntry.addRowIndex(traceEventKey);

				boolean filterable = traceTableModelColumn.isFilterable();
				
				if ((filterable) && (columnFilterEntryList.size() > 1)) {
					filterColumn.setColumnFilterEnabled(true);
				}

				// boolean columnFilterEnabled =
				// filterColumn.isColumnFilterEnabled();
				//
				// if ((!columnFilterEnabled) && (columnFilterEntryList.size() >
				// 1)) {
				// filterColumn.setColumnFilterEnabled(true);
				// }
			}
		}
	}

	// performing one by one search because of showing progress in the monitor
	// also when cancelling the task we should keep the old search results
	// hence not search result is stored unless the task is completed
	@Override
	public boolean search(TraceEventKey key, Object searchStrObj) {

		TraceEvent traceEvent = getEventForKey(key);

		boolean found = traceEvent.search(searchStrObj);

		return found;
	}

	@Override
	protected FilterTableModelNavigation<TraceEventKey> getNavigationRowIndex(List<TraceEventKey> resultList,
			int selectedRowIndex, boolean forward, boolean first, boolean last, boolean wrap) {

		int currSelectedRowIndex = selectedRowIndex;
		// tracer viewer search results are NOT reversed
		TraceEventKey navigationKey = null;
		int navigationIndex = 0;
		int navigationRowIndex = 0;

		if ((resultList != null) && (resultList.size() > 0)) {

			int resultListSize = resultList.size();

			List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();

			int traceEventKeyListSize = traceEventKeyList.size();

			if (first) {

				int lastIndex = resultListSize - 1;
				navigationKey = resultList.get(lastIndex);
				navigationIndex = 1;

			} else if (last) {

				navigationKey = resultList.get(0);
				navigationIndex = resultListSize;

			} else if (forward) {
				// NEXT
				if (currSelectedRowIndex >= 0) {

					if (currSelectedRowIndex < (traceEventKeyListSize - 1)) {
						currSelectedRowIndex++;
					} else {
						if (wrap) {
							currSelectedRowIndex = 0;
						}
					}
				} else {
					currSelectedRowIndex = 0;
				}

				TraceEventKey currSelectedTraceEventKey = traceEventKeyList
						.get((traceEventKeyListSize - 1) - currSelectedRowIndex);

				int searchIndex = Collections.binarySearch(resultList, currSelectedTraceEventKey);

				if (searchIndex >= 0) {
					// exact search found
					navigationKey = resultList.get(searchIndex);
				} else {

					searchIndex = (searchIndex * -1) - 1;

					if ((searchIndex == resultListSize) || (searchIndex == 0)) {

						searchIndex = resultListSize - 1;
					} else {
						searchIndex--;
					}

					navigationKey = resultList.get(searchIndex);
				}

				navigationIndex = resultList.indexOf(navigationKey);
				navigationIndex = (resultListSize - 1) - navigationIndex + 1;

			} else {
				// PREVIOUS
				if (currSelectedRowIndex >= 0) {

					if (currSelectedRowIndex > 0) {
						currSelectedRowIndex--;
					} else {
						if (wrap) {
							currSelectedRowIndex = traceEventKeyListSize - 1;
						}
					}
				} else {
					currSelectedRowIndex = 0;
				}

				TraceEventKey currSelectedTraceEventKey = traceEventKeyList
						.get((traceEventKeyListSize - 1) - currSelectedRowIndex);

				int searchIndex = Collections.binarySearch(resultList, currSelectedTraceEventKey);

				if (searchIndex >= 0) {
					// exact search found
					navigationKey = resultList.get(searchIndex);
				} else {

					searchIndex = (searchIndex * -1) - 1;

					if (searchIndex == resultListSize) {
						searchIndex = 0;
					}

					navigationKey = resultList.get(searchIndex);
				}

				navigationIndex = resultList.indexOf(navigationKey);
				navigationIndex = (resultListSize - 1) - navigationIndex + 1;
			}

			if (navigationKey != null) {

				navigationRowIndex = getIndexOfKey(navigationKey);
				// navigationRowIndex =
				// traceEventKeyList.indexOf(navigationKey);
				//
				// navigationRowIndex = (traceEventKeyListSize - 1)
				// - navigationRowIndex;

			} else {
				navigationRowIndex = currSelectedRowIndex;
			}

		}

		FilterTableModelNavigation<TraceEventKey> ttmn = new FilterTableModelNavigation<TraceEventKey>();
		ttmn.setNavigationIndex(navigationIndex);
		ttmn.setNavigationRowIndex(navigationRowIndex);
		ttmn.setNavigationKey(navigationKey);

		return ttmn;
	}

	@Override
	/**
	 * this uses treepmap's comparator which is based on traceeventkey's id
	 */
	public TraceEvent getEventForKey(TraceEventKey traceEventKey) {

		TraceEvent traceEvent = null;

		if (traceEventKey != null) {
			Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
			traceEvent = traceEventMap.get(traceEventKey);
		}

		return traceEvent;
	}

	/**
	 * alternate implementation to getEventForKey to use traceeventkey's
	 * traceeventindex. used for reports where entries can change because of compare
	 * view.
	 */
	public TraceEvent getTraceEventForKey(TraceEventKey traceEventKey) {

		TraceEvent traceEvent = null;

		if (traceEventKey != null) {

			int traceEventIndex = traceEventKey.getTraceEventIndex();

			if (traceEventIndex != -1) {

				Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();
				Set<Map.Entry<TraceEventKey, TraceEvent>> traceEventEntrySet = traceEventMap.entrySet();

				for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventEntrySet) {

					TraceEventKey key = entry.getKey();
					TraceEvent te = entry.getValue();

					if (traceEventIndex == key.getTraceEventIndex()) {
						traceEvent = te;
						break;
					}
				}
			} else {
				traceEvent = getEventForKey(traceEventKey);
			}

		}

		return traceEvent;
	}

	public void updateRecentFile(String charset) {

		RecentFile recentFile = getRecentFile();

		if (charset != null) {
			recentFile.setAttribute(RecentFile.KEY_CHARSET, charset);
			// change in character set will trigger reloading of file.
		}

		fireTableDataChanged();

	}

	@Override
	public void clearSearchResults(boolean clearResults) {

		getSearchModel().resetResults(clearResults);

		clearTraceEventSearchResults();
	}

	protected void clearTraceEventSearchResults() {
		List<TraceEventKey> filteredList = getFtmEntryKeyList();

		Iterator<TraceEventKey> fListIterator = filteredList.iterator();

		while (fListIterator.hasNext()) {

			TraceEventKey traceEventKey = fListIterator.next();

			TraceEvent traceEvent = getEventForKey(traceEventKey);
			traceEvent.setSearchFound(false);
		}

		clearAbstractTraceEventTreeNode(rootTraceEventTreeNode);

		clearAbstractTraceEventTreeNode(rootTraceEventCombinedTreeNode);
	}

	private void clearAbstractTraceEventTreeNode(AbstractTraceEventTreeNode abstractTraceEventTreeNode) {

		for (Enumeration<?> e = abstractTraceEventTreeNode.children(); e.hasMoreElements();) {

			AbstractTraceEventTreeNode childNode = (AbstractTraceEventTreeNode) e.nextElement();

			clearAbstractTraceEventTreeNode(childNode);
		}

		abstractTraceEventTreeNode.setSearchFound(false);
	}

	@Override
	public int getIndexOfKey(TraceEventKey traceEventKey) {

		List<TraceEventKey> traceEventIndexList = getFtmEntryKeyList();

		int size = traceEventIndexList.size();

		int reverseIndex = -1;

		if (traceEventIndexList != null) {

			int index = traceEventIndexList.indexOf(traceEventKey);

			if (index != -1) {
				reverseIndex = size - index - 1;
			}
		}

		return reverseIndex;
	}

	@Override
	public TraceEventTreeNode getTreeNodeForKey(TraceEventKey key) {

		TraceEventTreeNode traceEventTreeNode = null;

		Map<TraceEventKey, TraceEventTreeNode> traceEventTreeNodeMap = getTraceEventTreeNodeMap();

		traceEventTreeNode = traceEventTreeNodeMap.get(key);

		return traceEventTreeNode;
	}

	public TraceEventCombinedTreeNode getTraceEventCombinedTreeNodeForKey(TraceEventKey key) {

		TraceEventCombinedTreeNode traceEventCombinedTreeNode = null;

		Map<TraceEventKey, TraceEventCombinedTreeNode> traceEventCombinedTreeNodeMap = getTraceEventCombinedTreeNodeMap();

		traceEventCombinedTreeNode = traceEventCombinedTreeNodeMap.get(key);

		return traceEventCombinedTreeNode;
	}

	private List<TraceEventKey> getFailedEventKeyList() {

		if (failedEventKeyList == null) {
			failedEventKeyList = new ArrayList<TraceEventKey>();
		}

		return failedEventKeyList;
	}

	private List<TraceEventKey> getExceptionEventKeyList() {

		if (exceptionEventKeyList == null) {
			exceptionEventKeyList = new ArrayList<TraceEventKey>();
		}

		return exceptionEventKeyList;
	}

	private List<TraceEventKey> getAlertEventKeyList() {

		if (alertEventKeyList == null) {
			alertEventKeyList = new ArrayList<TraceEventKey>();
		}

		return alertEventKeyList;
	}

	private List<TraceEventKey> getNoStartEventKeyList() {

		if (noStartEventKeyList == null) {
			noStartEventKeyList = new ArrayList<>();
		}

		return noStartEventKeyList;
	}

	private List<TraceEventKey> getNoEndEventKeyList() {

		if (noEndEventKeyList == null) {
			noEndEventKeyList = new ArrayList<>();
		}

		return noEndEventKeyList;
	}

	private TreeMap<Double, List<TraceEventKey>> getOwnElapsedEventKeyMap() {

		if (ownElapsedEventKeyMap == null) {
			ownElapsedEventKeyMap = new TreeMap<Double, List<TraceEventKey>>();
		}

		return ownElapsedEventKeyMap;
	}

	private TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>> getRulesInvokedMap() {

		if (rulesInvokedMap == null) {
			rulesInvokedMap = new TreeMap<TraceEventRuleset, TreeSet<TraceEventRule>>();
		}

		return rulesInvokedMap;
	}

	private void processTraceEventElapsed(TraceEventTreeNode beginNode, TraceEventTreeNode endNode) {

		TraceEvent endTE = (TraceEvent) endNode.getUserObject();

		if (endTE != null) {

			double mainElapsed = endTE.getElapsed();

			if (mainElapsed > 0) {

				double childrenElapsed = 0;

				for (Enumeration<?> e = beginNode.children(); e.hasMoreElements();) {

					TraceEventTreeNode childNode = (TraceEventTreeNode) e.nextElement();

					TraceEvent childTE = (TraceEvent) childNode.getUserObject();

					double childElapsed = childTE.getElapsed();

					if (childElapsed >= 0) {
						childrenElapsed += childElapsed;
					}
				}

				double ownElapsed = mainElapsed - childrenElapsed;

				endTE.setChildrenElapsed((beginNode.getChildCount() > 0) ? childrenElapsed : -1);
				endTE.setOwnElapsed(ownElapsed);

				// add ownElapsed to map for reporting
				TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();

				List<TraceEventKey> traceEventKeyList = ownElapsedEventKeyMap.get(ownElapsed);

				if (traceEventKeyList == null) {
					traceEventKeyList = new ArrayList<TraceEventKey>();
					ownElapsedEventKeyMap.put(ownElapsed, traceEventKeyList);
				}

				traceEventKeyList.add(endTE.getKey());

			}
		}
	}

	private void processEvent(TraceEvent traceEvent) {

		TraceEventKey traceEventKey = traceEvent.getKey();

		if (traceEventKey.getTraceEventIndex() != -1) {

			List<TraceEventKey> failedEventKeyList = getFailedEventKeyList();
			List<TraceEventKey> exceptionEventKeyList = getExceptionEventKeyList();
			List<TraceEventKey> alertEventKeyList = getAlertEventKeyList();
			Map<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap = getRulesInvokedMap();

			// add failed events
			boolean stepStatusFail = traceEvent.isStepStatusFail();

			if (stepStatusFail) {
				failedEventKeyList.add(traceEventKey);
			}

			// add Exception events
			boolean stepStatusException = traceEvent.isStepStatusException();

			if (stepStatusException) {
				exceptionEventKeyList.add(traceEventKey);
			}

			boolean isAlertEvent = (traceEvent instanceof TraceEventAlert);

			if (isAlertEvent) {
				alertEventKeyList.add(traceEventKey);
			}

			String rulesetVersion = traceEvent.getRuleSet();

			if (rulesetVersion == null) {
				rulesetVersion = "<NULL>";
			}

			TraceEventRuleset traceEventRuleset = new TraceEventRuleset(rulesetVersion);

			TreeSet<TraceEventRule> traceEventRules = rulesInvokedMap.get(traceEventRuleset);

			if (traceEventRules == null) {
				traceEventRules = new TreeSet<TraceEventRule>();
				rulesInvokedMap.put(traceEventRuleset, traceEventRules);
			}

			String insKey = traceEvent.getInsKey();

			if ((insKey != null) && (!"".equals(insKey))) {

				TraceEventType traceEventType = traceEvent.getTraceEventType();

				Color background = traceEvent.getColumnBackground(0);

				TraceEventRule traceEventRule = new TraceEventRule(insKey, traceEventType, background);

				boolean success = traceEventRules.add(traceEventRule);

				Boolean isEndEvent = traceEvent.isEndEvent();

				// this is called after processTraceEventElapsed hence timing
				// info should be available
				if ((!success) && (isEndEvent != null) && (isEndEvent)) {

					for (TraceEventRule ter : traceEventRules) {

						if (ter.equals(traceEventRule)) {

							// only increment if the entry is of same type.
							// in case of activities, the child 'steps' and
							// 'whens' have the activity's insKey set.
							if (ter.getTraceEventType().equals(traceEventRule.getTraceEventType())) {
								ter.incrementExecutionCount();
							}

							ter.processElapsed(traceEvent.getOwnElapsed());
						}
					}
				} else if (isEndEvent == null) {
					traceEventRule.incrementExecutionCount();
					traceEventRule.processElapsed(traceEvent.getOwnElapsed());
				}
			}
		}
	}

	public TraceEventTreeNode getRootTraceEventTreeNode() {

		if (rootTraceEventTreeNode == null) {
			rootTraceEventTreeNode = new TraceEventTreeNode(null);
		}

		return rootTraceEventTreeNode;
	}

	public TraceEventCombinedTreeNode getRootTraceEventCombinedTreeNode() {

		if (rootTraceEventCombinedTreeNode == null) {
			rootTraceEventCombinedTreeNode = new TraceEventCombinedTreeNode(null);
		}

		return rootTraceEventCombinedTreeNode;
	}

	private LinkedList<TraceEvent> getTreeBuildTraceEventList() {

		if (treeBuildTraceEventList == null) {
			treeBuildTraceEventList = new LinkedList<>();
		}

		return treeBuildTraceEventList;
	}

	@Override
	public SearchModel<TraceEventKey> getSearchModel() {

		if (searchModel == null) {

			searchModel = new SearchModel<TraceEventKey>(searchData) {

				@Override
				public void searchInEvents(final Object searchStrObj, final ModalProgressMonitor mProgressMonitor) {

					if ((searchStrObj != null) && (!((searchStrObj instanceof SearchEventType)
							&& searchStrObj.equals(SearchEventType.SEPERATOR))
							|| !("".equals(searchStrObj.toString())))) {

						TraceTableSearchTask ttst = new TraceTableSearchTask(mProgressMonitor, TraceTableModel.this,
								searchStrObj) {

							/*
							 * (non-Javadoc)
							 * 
							 * @see javax.swing.SwingWorker#done()
							 */
							@Override
							protected void done() {

								try {
									List<TraceEventKey> searchResultList = get();

									if (searchResultList != null) {
										// LOG.info("TraceTableSearchTask
										// done "
										// + searchResultList.size()
										// + " entries found");
										setSearchResultList(searchStrObj, searchResultList);

									}
								} catch (CancellationException ce) {
									LOG.info("TraceTableSearchTask cancelled: ");

								} catch (Exception e) {
									LOG.error("Exception in TraceTableSearchTask", e);

								} finally {

									// general fire will reload the tree,
									// collapsing the whole tree.
									// hence generating a special to identify
									// search action. used in
									// TraceTreeTableModelAdapter
									// fireTableDataChanged();
									fireTableChanged(new SearchTableModelEvent(TraceTableModel.this));

									mProgressMonitor.close();
								}
							}
						};

						ttst.execute();

					}
				}

				@Override
				public void resetResults(boolean clearResults) {

					// clears search result on search model and reset the search
					// panel
					resetSearchResults(clearResults);

					// clear search results from within trace events and tree
					// nodes
					clearTraceEventSearchResults();

					// general fire will reload the tree,
					// collapsing the whole tree.
					// hence generating a special to identify
					// search action. used in
					// TraceTreeTableModelAdapter
					// fireTableDataChanged();
					fireTableChanged(new SearchTableModelEvent(TraceTableModel.this));
				}
			};
		}

		return searchModel;
	}

	public boolean isIncompletedTracerXML() {

		List<TraceEventKey> reportNoEndEventKeyList = getReportNoEndEventKeyList();

		int reportNoEndEventKeyListsize = reportNoEndEventKeyList.size();

		boolean incompleteTracerXML = reportNoEndEventKeyListsize > 0;

		if (incompleteTracerXML) {

			StringBuffer sb = new StringBuffer();

			for (TraceEventKey traceEventKey : reportNoEndEventKeyList) {

				if (sb.length() > 0) {
					sb.append(", ");
				}

				sb.append(traceEventKey.toString());
			}

			String modelName = getModelName();

			LOG.info("Incomplete tracer xml: " + modelName + " - " + reportNoEndEventKeyListsize + " events - "
					+ sb.toString());
		}

		return incompleteTracerXML;
	}

	@Override
	protected TableColumnModel getTableColumnModel() {

		TableColumnModel tableColumnModel = new DefaultTableColumnModel();

		for (int i = 0; i < getColumnCount(); i++) {

			TableColumn tableColumn = new TableColumn(i);

			String text = getColumnName(i);

			tableColumn.setHeaderValue(text);

			TraceTableModelColumn ttmc = getColumn(i);

			TraceTableCellRenderer ttcr = new TraceTableCellRenderer();
			ttcr.setBorder(new EmptyBorder(1, 3, 1, 1));
			ttcr.setHorizontalAlignment(ttmc.getHorizontalAlignment());

			tableColumn.setCellRenderer(ttcr);

			int colWidth = ttmc.getPrefColumnWidth();
			tableColumn.setPreferredWidth(colWidth);
			// tableColumn.setMinWidth(colWidth);
			tableColumn.setWidth(colWidth);
			tableColumn.setResizable(true);

			tableColumnModel.addColumn(tableColumn);
		}

		return tableColumnModel;
	}

	public List<TraceEventKey> getReportFailedEventKeyList() {
		return Collections.unmodifiableList(getFailedEventKeyList());
	}

	public List<TraceEventKey> getReportExceptionEventKeyList() {
		return Collections.unmodifiableList(getExceptionEventKeyList());
	}

	public List<TraceEventKey> getReportAlertEventKeyList() {
		return Collections.unmodifiableList(getAlertEventKeyList());
	}

	public List<TraceEventKey> getReportNoStartEventKeyList() {
		return Collections.unmodifiableList(getNoStartEventKeyList());
	}

	public List<TraceEventKey> getReportNoEndEventKeyList() {

		List<TraceEventKey> reportNoEndEventKeyList = new ArrayList<TraceEventKey>();

		List<TraceEventKey> noEndEventKeyList = getNoEndEventKeyList();
		List<TraceEvent> treeBuildTraceEventList = getTreeBuildTraceEventList();

		for (TraceEventKey traceEventKey : noEndEventKeyList) {
			reportNoEndEventKeyList.add(traceEventKey);
		}

		for (TraceEvent traceEvent : treeBuildTraceEventList) {
			reportNoEndEventKeyList.add(traceEvent.getKey());
		}

		Collections.sort(reportNoEndEventKeyList);

		return Collections.unmodifiableList(reportNoEndEventKeyList);
	}

	public List<TraceEventKey> getReportOwnElapsedEventKeyList() {

		List<TraceEventKey> reportElapsedTimeEventList = new ArrayList<TraceEventKey>();

		TreeMap<Double, List<TraceEventKey>> ownElapsedEventKeyMap = getOwnElapsedEventKeyMap();

		// select last 50 items
		int mapSize = ownElapsedEventKeyMap.size();
		int size = (mapSize > 50) ? 50 : mapSize;

		Iterator<Double> it = ownElapsedEventKeyMap.navigableKeySet().descendingIterator();

		while (it.hasNext() && size > 0) {

			Double key = it.next();
			List<TraceEventKey> traceEventKeyList = ownElapsedEventKeyMap.get(key);

			for (TraceEventKey traceEventKey : traceEventKeyList) {
				reportElapsedTimeEventList.add(traceEventKey);
			}

			size--;
		}

		return Collections.unmodifiableList(reportElapsedTimeEventList);

	}

	public Map<TraceEventRuleset, TreeSet<TraceEventRule>> getReportRulesInvokedMap() {
		return Collections.unmodifiableMap(getRulesInvokedMap());
	}
}
