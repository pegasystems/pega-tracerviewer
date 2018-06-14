/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.pega.gcs.fringecommon.guiutilities.FilterTableModelNavigation;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.Searchable.SelectedRowPosition;
import com.pega.gcs.fringecommon.guiutilities.TableCompareEntry;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTableCompareModel extends TraceTableModel {

    private static final long serialVersionUID = -3594854147621727335L;

    private int compareNavIndex;

    private TraceEventKey compareNavTraceEventKey;

    private boolean compareResultsWrap;

    private Map<TraceEventKey, List<TraceEventKey>> compareNavIndexMap;

    private List<TraceEventKey> compareMarkerList;

    public TraceTableCompareModel(RecentFile recentFile, SearchData<TraceEventKey> searchData) {
        super(recentFile, searchData);
    }

    @Override
    public void resetModel() {

        super.resetModel();

        compareNavIndex = 0;
        compareNavTraceEventKey = null;
        compareResultsWrap = false;
        compareNavIndexMap = new HashMap<TraceEventKey, List<TraceEventKey>>();
        compareMarkerList = null;

    }

    @Override
    protected void buildTree(TraceEvent currentTraceEvent) {
        // fix Issue #1 - Compare functionality not working
        // do nothing - compare model doesn't implements tree
    }

    @Override
    public boolean isColumnFilterEnabled(int column) {
        return false;
    }

    public boolean isCompareResultsWrap() {
        return compareResultsWrap;
    }

    public void setCompareResultsWrap(boolean compareResultsWrap) {
        this.compareResultsWrap = compareResultsWrap;
    }

    public int getCompareNavIndex() {
        return compareNavIndex;
    }

    public Map<TraceEventKey, List<TraceEventKey>> getCompareNavIndexMap() {
        return compareNavIndexMap;
    }

    public void setCompareNavIndexMap(TreeMap<TraceEventKey, List<TraceEventKey>> compareNavIndexMap) {
        this.compareNavIndexMap = compareNavIndexMap;
    }

    public List<TraceEventKey> getCompareMarkerList() {
        return compareMarkerList;
    }

    public void setCompareMarkerList(List<TraceEventKey> compareMarkerList) {
        this.compareMarkerList = compareMarkerList;
    }

    public int getCompareCount() {
        int compareCount = 0;

        compareCount = (compareNavIndexMap != null) ? compareNavIndexMap.size() : 0;

        return compareCount;
    }

    private int getCompareRowIndex(int currSelectedRowIndex, boolean forward, boolean first, boolean last) {

        List<TraceEventKey> compareNavIndexList = new LinkedList<TraceEventKey>(compareNavIndexMap.keySet());

        FilterTableModelNavigation<TraceEventKey> ttmn = getNavigationRowIndex(compareNavIndexList,
                currSelectedRowIndex, forward, first, last, compareResultsWrap);

        compareNavIndex = ttmn.getNavigationIndex();
        int compareRowIndex = ttmn.getNavigationRowIndex();
        compareNavTraceEventKey = ttmn.getNavigationKey();

        return compareRowIndex;
    }

    public TableCompareEntry compareFirst() {

        TableCompareEntry tableCompareEntry;

        int startEntry = getCompareRowIndex(0, false, true, false);
        int endEntry = startEntry;

        if (compareNavTraceEventKey != null) {

            List<TraceEventKey> compareNavIndexList = compareNavIndexMap.get(compareNavTraceEventKey);

            int size = compareNavIndexList.size();

            TraceEventKey endKey = compareNavIndexList.get(size - 1);

            endEntry = getIndexOfKey(endKey);
        }

        tableCompareEntry = new TableCompareEntry(startEntry, endEntry);

        return tableCompareEntry;
    }

    public TableCompareEntry comparePrevious(int currSelectedRow) {

        TableCompareEntry tableCompareEntry;

        int startEntry = getCompareRowIndex(currSelectedRow, false, false, false);
        int endEntry = startEntry;

        if (compareNavTraceEventKey != null) {

            List<TraceEventKey> compareNavIndexList = compareNavIndexMap.get(compareNavTraceEventKey);

            int size = compareNavIndexList.size();

            TraceEventKey endKey = compareNavIndexList.get(size - 1);

            endEntry = getIndexOfKey(endKey);
        }

        tableCompareEntry = new TableCompareEntry(startEntry, endEntry);

        return tableCompareEntry;
    }

    public TableCompareEntry compareNext(int currSelectedRow) {

        TableCompareEntry tableCompareEntry;

        int startEntry = getCompareRowIndex(currSelectedRow, true, false, false);
        int endEntry = startEntry;

        if (compareNavTraceEventKey != null) {

            List<TraceEventKey> compareNavIndexList = compareNavIndexMap.get(compareNavTraceEventKey);

            int size = compareNavIndexList.size();

            TraceEventKey endKey = compareNavIndexList.get(size - 1);

            endEntry = getIndexOfKey(endKey);
        }

        tableCompareEntry = new TableCompareEntry(startEntry, endEntry);

        return tableCompareEntry;
    }

    public TableCompareEntry compareLast() {

        TableCompareEntry tableCompareEntry;

        int startEntry = getCompareRowIndex(0, false, false, true);
        int endEntry = startEntry;

        if (compareNavTraceEventKey != null) {

            List<TraceEventKey> compareNavIndexList = compareNavIndexMap.get(compareNavTraceEventKey);

            int size = compareNavIndexList.size();

            TraceEventKey endKey = compareNavIndexList.get(size - 1);

            endEntry = getIndexOfKey(endKey);
        }

        tableCompareEntry = new TableCompareEntry(startEntry, endEntry);

        return tableCompareEntry;
    }

    public SelectedRowPosition getCompareSelectedRowPosition(int selectedRow) {

        SelectedRowPosition selectedRowPosition = SelectedRowPosition.NONE;

        List<TraceEventKey> compareNavIndexList = new LinkedList<TraceEventKey>(compareNavIndexMap.keySet());

        if ((compareNavIndexList != null) && (compareNavIndexList.size() > 0)) {

            List<TraceEventKey> traceEventKeyList = getFtmEntryKeyList();

            int traceEventKeyListSize = traceEventKeyList.size();

            TraceEventKey traceEventKey = traceEventKeyList.get((traceEventKeyListSize - 1) - selectedRow);

            int selectedRowIndex = traceEventKey.getId();

            int size = compareNavIndexList.size();

            int firstIndex = compareNavIndexList.get(size - 1).getId();
            int lastIndex = compareNavIndexList.get(0).getId();

            if ((selectedRowIndex < firstIndex) && (selectedRowIndex > lastIndex)) {
                selectedRowPosition = SelectedRowPosition.BETWEEN;
            } else if (selectedRowIndex >= firstIndex) {
                selectedRowPosition = SelectedRowPosition.FIRST;
            } else if (selectedRowIndex <= lastIndex) {
                selectedRowPosition = SelectedRowPosition.LAST;
            } else {
                selectedRowPosition = SelectedRowPosition.NONE;
            }
        }

        return selectedRowPosition;
    }

    // public Set<CheckBoxMenuItemPopupEntry<TraceEventKey>>
    // getFilterTraceEventTypeEntrySet() {
    // return filterTraceEventTypeEntrySet;
    // }
    //
    // public Set<CheckBoxMenuItemPopupEntry<TraceEventKey>>
    // getFilterTraceEventEntrySet() {
    // return filterTraceEventEntrySet;
    // }

    private Map<TraceEventKey, TraceEvent> getCompareClearedTraceEventMap() {

        Map<TraceEventKey, TraceEvent> newTraceEventMap = new TreeMap<TraceEventKey, TraceEvent>();

        int traceEventIndex = 0;

        Map<TraceEventKey, TraceEvent> traceEventMap = getTraceEventMap();

        for (Map.Entry<TraceEventKey, TraceEvent> entry : traceEventMap.entrySet()) {

            TraceEventKey traceEventKey = entry.getKey();
            TraceEvent traceEvent = entry.getValue();

            boolean corrupt = traceEventKey.isCorrupt();

            if ((corrupt) || (traceEventKey.getTraceEventIndex() != -1)) {

                TraceEventKey newtraceEventKey = new TraceEventKey(traceEventIndex, traceEventIndex, corrupt);

                newTraceEventMap.put(newtraceEventKey, traceEvent);
                traceEventIndex++;
            }
        }

        return newTraceEventMap;
    }
}
