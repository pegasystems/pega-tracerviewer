/**
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 * <p>
 * Contributors:
 * Manu Varghese
 */

package com.pega.gcs.tracerviewer.report;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.pega.gcs.fringecommon.guiutilities.CheckBoxMenuItemPopupEntry;
import com.pega.gcs.fringecommon.guiutilities.FilterColumn;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModel;
import com.pega.gcs.fringecommon.guiutilities.FilterTableModelNavigation;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableNode;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.TraceEventFactory;
import com.pega.gcs.tracerviewer.TraceEventRule;
import com.pega.gcs.tracerviewer.TraceEventRuleset;

public class TracerReportRulesTableModel extends FilterTableModel<Integer> {

    private static final long serialVersionUID = 2010790662059130714L;

    Map<Integer, TracerReportRulesetRule> tracerReportRulesetRuleMap;

    private List<Integer> tracerReportRulesetRuleIndexList;

    private HashMap<Integer, Integer> keyIndexMap;

    private TableColumnModel tableColumnModel;

    public TracerReportRulesTableModel(Map<TraceEventRuleset, TreeSet<TraceEventRule>> rulesInvokedMap) {

        super(null);

        resetModel();

        initialise(rulesInvokedMap);
    }

    protected Map<Integer, TracerReportRulesetRule> getTracerReportRulesetRule() {

        if (tracerReportRulesetRuleMap == null) {
            tracerReportRulesetRuleMap = new TreeMap<Integer, TracerReportRulesetRule>();
        }

        return tracerReportRulesetRuleMap;
    }

    @Override
    public int getColumnCount() {
        return TracerReportRulesTableColumn.values().length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        List<Integer> tracerReportRulesetRuleIndexList = getFtmEntryKeyList();

        Integer tracerReportRulesetRuleIndex = tracerReportRulesetRuleIndexList.get(rowIndex);

        TracerReportRulesetRule tracerReportRulesetRule = tracerReportRulesetRuleMap.get(tracerReportRulesetRuleIndex);

        return tracerReportRulesetRule;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getColumnValue(java. lang.Object, int)
     */
    @Override
    public String getColumnValue(Object valueAtObject, int columnIndex) {

        TracerReportRulesetRule tracerReportRulesetRule = (TracerReportRulesetRule) valueAtObject;

        String columnValue = null;

        if (tracerReportRulesetRule != null) {

            TracerReportRulesTableColumn tracerReportRulesTableColumn;
            tracerReportRulesTableColumn = TracerReportRulesTableColumn.values()[columnIndex];

            TraceEventRuleset traceEventRuleset = tracerReportRulesetRule.getTraceEventRuleset();
            TraceEventRule traceEventRule = tracerReportRulesetRule.getTraceEventRule();

            switch (tracerReportRulesTableColumn) {

            case SNO:
                columnValue = String.valueOf(tracerReportRulesetRule.getRulesetRuleIndex());
                break;

            case EXECUTION_COUNT:
                columnValue = String.valueOf(traceEventRule.getExecutionCount());
                break;

            case INSKEY:
                columnValue = traceEventRule.getInsKey();
                break;

            case RULE_TYPE:
                columnValue = traceEventRule.getTraceEventType().getName();
                break;

            case MAX_OWN_ELAPSED:
                columnValue = TraceEventFactory.getElapsedString(traceEventRule.getMaxOwnElapsed());
                break;

            case MIN_OWN_ELAPSED:
                columnValue = TraceEventFactory.getElapsedString(traceEventRule.getMinOwnElapsed());
                break;

            case RULESET:
                columnValue = traceEventRuleset.getRuleset();
                break;

            case RULESET_VERSION:
                columnValue = traceEventRuleset.getVersion();
                break;

            case TOTAL_OWN_ELAPSED:
                columnValue = TraceEventFactory.getElapsedString(traceEventRule.getTotalOwnElapsed());
                break;

            default:
                break;

            }
        }

        return columnValue;

    }

    @Override
    protected int getModelColumnIndex(int column) {
        return column;
    }

    @Override
    protected boolean search(Integer key, Object searchStrObj) {
        return false;
    }

    @Override
    protected FilterTableModelNavigation<Integer> getNavigationRowIndex(List<Integer> resultList,
            int currSelectedRowIndex, boolean forward, boolean first, boolean last, boolean wrap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Integer> getFtmEntryKeyList() {

        if (tracerReportRulesetRuleIndexList == null) {
            tracerReportRulesetRuleIndexList = new ArrayList<Integer>();
        }

        return tracerReportRulesetRuleIndexList;
    }

    @Override
    protected HashMap<Integer, Integer> getKeyIndexMap() {

        if (keyIndexMap == null) {
            keyIndexMap = new HashMap<>();
        }

        return keyIndexMap;
    }

    @Override
    public void resetModel() {

        Map<Integer, TracerReportRulesetRule> tracerReportRulesetRuleMap = getTracerReportRulesetRule();
        tracerReportRulesetRuleMap.clear();

        Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<Integer>>> columnFilterMap;
        columnFilterMap = getColumnFilterMap();
        columnFilterMap.clear();

        TracerReportRulesTableColumn[] tracerReportRulesTableColumnArray = TracerReportRulesTableColumn.values();

        for (int columnIndex = 0; columnIndex < tracerReportRulesTableColumnArray.length; columnIndex++) {

            TracerReportRulesTableColumn tracerReportRulesTableColumn = tracerReportRulesTableColumnArray[columnIndex];

            // preventing unnecessary buildup of filter map
            if (tracerReportRulesTableColumn.isFilterable()) {

                FilterColumn filterColumn = new FilterColumn(columnIndex);

                filterColumn.setColumnFilterEnabled(true);

                columnFilterMap.put(filterColumn, null);
            }
        }
    }

    @Override
    public Object getEventForKey(Integer key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AbstractTreeTableNode getTreeNodeForKey(Integer key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearSearchResults(boolean clearResults) {
        // TODO Auto-generated method stub

    }

    @Override
    public SearchModel<Integer> getSearchModel() {
        // TODO Auto-generated method stub
        return null;
    }

    private void initialise(Map<TraceEventRuleset, ? extends Collection<TraceEventRule>> rulesInvokedMap) {

        List<Integer> tracerReportRulesetRuleIndexList = getFtmEntryKeyList();
        Map<Integer, TracerReportRulesetRule> tracerReportRulesetRuleMap = getTracerReportRulesetRule();

        Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<Integer>>> columnFilterMap = getColumnFilterMap();

        Comparator<Map.Entry<TraceEventRuleset, TraceEventRule>> comparator;
        comparator = new Comparator<Map.Entry<TraceEventRuleset, TraceEventRule>>() {

            @Override
            public int compare(Map.Entry<TraceEventRuleset, TraceEventRule> o1,
                    Map.Entry<TraceEventRuleset, TraceEventRule> o2) {

                TraceEventRule o1TraceEventRule = o1.getValue();
                TraceEventRule o2TraceEventRule = o2.getValue();

                Double o1totalOwnElapsed = o1TraceEventRule.getTotalOwnElapsed();
                Double o2totalOwnElapsed = o2TraceEventRule.getTotalOwnElapsed();

                o1totalOwnElapsed = (o1totalOwnElapsed == null) ? 0d : o1totalOwnElapsed;
                o2totalOwnElapsed = (o2totalOwnElapsed == null) ? 0d : o2totalOwnElapsed;

                return o2totalOwnElapsed.compareTo(o1totalOwnElapsed);
            }
        };

        List<Map.Entry<TraceEventRuleset, TraceEventRule>> rulesetInsKeyEntryList = GeneralUtilities
                .denormalizeNestedListMap(rulesInvokedMap);

        Collections.sort(rulesetInsKeyEntryList, comparator);

        Integer tracerReportRulesetRuleIndex = 1;

        for (Map.Entry<TraceEventRuleset, TraceEventRule> rulesetInsKeyEntry : rulesetInsKeyEntryList) {

            TraceEventRuleset traceEventRuleset = rulesetInsKeyEntry.getKey();
            TraceEventRule traceEventRule = rulesetInsKeyEntry.getValue();

            TracerReportRulesetRule tracerReportRulesetRule = new TracerReportRulesetRule(tracerReportRulesetRuleIndex,
                    traceEventRuleset, traceEventRule);

            tracerReportRulesetRuleIndexList.add(tracerReportRulesetRuleIndex);
            tracerReportRulesetRuleMap.put(tracerReportRulesetRuleIndex, tracerReportRulesetRule);

            tracerReportRulesetRuleIndex++;

            updateColumnFilterMap(tracerReportRulesetRule, columnFilterMap);
        }

        updateKeyIndexMap();
    }

    // clearing the columnFilterMap will skip the below loop
    private void updateColumnFilterMap(TracerReportRulesetRule tracerReportRulesetRule,
            Map<FilterColumn, List<CheckBoxMenuItemPopupEntry<Integer>>> columnFilterMap) {

        if (tracerReportRulesetRule != null) {

            Iterator<FilterColumn> fcIterator = columnFilterMap.keySet().iterator();

            while (fcIterator.hasNext()) {

                FilterColumn filterColumn = fcIterator.next();

                List<CheckBoxMenuItemPopupEntry<Integer>> columnFilterEntryList;
                columnFilterEntryList = columnFilterMap.get(filterColumn);

                if (columnFilterEntryList == null) {
                    columnFilterEntryList = new ArrayList<CheckBoxMenuItemPopupEntry<Integer>>();
                    columnFilterMap.put(filterColumn, columnFilterEntryList);
                }

                int columnIndex = filterColumn.getIndex();

                Integer tracerReportRulesetRuleIndex = tracerReportRulesetRule.getRulesetRuleIndex();
                String rulesetInsKeyStr = getColumnValue(tracerReportRulesetRule, columnIndex);

                if (rulesetInsKeyStr == null) {
                    rulesetInsKeyStr = FilterTableModel.NULL_STR;
                } else if ("".equals(rulesetInsKeyStr)) {
                    rulesetInsKeyStr = FilterTableModel.EMPTY_STR;
                }

                CheckBoxMenuItemPopupEntry<Integer> columnFilterEntry;

                CheckBoxMenuItemPopupEntry<Integer> searchKey;
                searchKey = new CheckBoxMenuItemPopupEntry<Integer>(rulesetInsKeyStr);

                int index = columnFilterEntryList.indexOf(searchKey);

                if (index == -1) {
                    columnFilterEntry = new CheckBoxMenuItemPopupEntry<Integer>(rulesetInsKeyStr);
                    columnFilterEntryList.add(columnFilterEntry);
                } else {
                    columnFilterEntry = columnFilterEntryList.get(index);
                }

                columnFilterEntry.addRowIndex(tracerReportRulesetRuleIndex);
            }
        }
    }

    private DefaultTableCellRenderer getDefaultTableCellRenderer() {

        DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1504347306097747771L;

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent( javax.swing.JTable, java.lang.Object, boolean,
             * boolean, int, int)
             */
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                TracerReportRulesetRule tracerReportRulesetRule = (TracerReportRulesetRule) value;

                if (tracerReportRulesetRule != null) {

                    String text = getColumnValue(tracerReportRulesetRule, column);

                    super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);

                    if (!table.isRowSelected(row)) {

                        TraceEventRule traceEventRule = tracerReportRulesetRule.getTraceEventRule();

                        Color backgorund = traceEventRule.getBackground();

                        setBackground(backgorund);

                    }

                    setBorder(new EmptyBorder(1, 8, 1, 1));

                    setToolTipText(text);

                } else {
                    setBackground(Color.WHITE);
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }

                return this;
            }
        };

        return dtcr;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.pega.gcs.fringecommon.guiutilities.CustomJTableModel#getTableColumnModel( )
     */
    @Override
    public TableColumnModel getTableColumnModel() {

        if (tableColumnModel == null) {

            tableColumnModel = new DefaultTableColumnModel();

            TableColumn tableColumn = null;
            int columnIndex = 0;

            for (TracerReportRulesTableColumn tracerReportRulesTableColumn : TracerReportRulesTableColumn.values()) {

                DefaultTableCellRenderer dtcr = getDefaultTableCellRenderer();
                dtcr.setHorizontalAlignment(tracerReportRulesTableColumn.getHorizontalAlignment());

                int prefColumnWidth = tracerReportRulesTableColumn.getPrefColumnWidth();

                tableColumn = new TableColumn(columnIndex++);
                tableColumn.setHeaderValue(tracerReportRulesTableColumn.getDisplayName());
                tableColumn.setCellRenderer(dtcr);
                tableColumn.setPreferredWidth(prefColumnWidth);
                tableColumn.setWidth(prefColumnWidth);

                tableColumnModel.addColumn(tableColumn);
            }
        }

        return tableColumnModel;
    }
}
