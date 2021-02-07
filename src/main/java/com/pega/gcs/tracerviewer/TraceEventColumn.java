/*******************************************************************************
 * Copyright (c) 2017, 2021 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;

import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;

public enum TraceEventColumn {

    // @formatter:off
    // CHECKSTYLE:OFF
    //               displayName   prefColumnWidth  horizontalAlignment    columnClass                     filterable
    // TREE             (""                 , 70  , SwingConstants.CENTER , TreeTableColumn.TREE_COLUMN_CLASS),
    LINE             ("LINE"                , 80  , SwingConstants.CENTER , TreeTableColumn.TREE_COLUMN_CLASS , false ),
    DXAPI_INT_ID     ("DxAPI INTERACTION ID", 100 , SwingConstants.CENTER , String.class                      , true  ),
    DXAPI_PATHINFO   ("DxAPI PATH INFO"     , 100 , SwingConstants.CENTER , String.class                      , true  ),
    TIMESTAMP        ("TIMESTAMP"           , 160 , SwingConstants.CENTER , String.class                      , false ),
    THREAD           ("THREAD"              , 80  , SwingConstants.CENTER , String.class                      , true  ),
    INT              ("INT"                 , 50  , SwingConstants.CENTER , String.class                      , true  ),
    RULE             ("RULE#"               , 50  , SwingConstants.CENTER , String.class                      , true  ),
    STEP_METHOD      ("STEP METHOD"         , 200 , SwingConstants.CENTER , String.class                      , true  ),
    STEP_PAGE        ("STEP PAGE"           , 200 , SwingConstants.CENTER , String.class                      , true  ),
    STEP             ("STEP"                , 50  , SwingConstants.CENTER , String.class                      , true  ),
    STATUS           ("STATUS"              , 100 , SwingConstants.CENTER , String.class                      , true  ),
    EVENT_TYPE       ("EVENT TYPE"          , 100 , SwingConstants.CENTER , String.class                      , true  ),
    EVENT_NAME       ("EVENT NAME"          , 100 , SwingConstants.CENTER , String.class                      , false ),
    TOTAL_ELAPSED    ("TOTAL ELAPSED"       , 70  , SwingConstants.CENTER , String.class                      , true  ),
    OWN_ELAPSED      ("OWN ELAPSED"         , 70  , SwingConstants.CENTER , String.class                      , false ),
    CHILDREN_ELAPSED ("CHILDREN ELAPSED"    , 70  , SwingConstants.CENTER , String.class                      , false ),
    NAME             ("NAME"                , 350 , SwingConstants.LEFT   , String.class                      , true  ),
    RULESET          ("RULESET"             , 170 , SwingConstants.CENTER , String.class                      , true  );
    // CHECKSTYLE:ON
    // @formatter:on

    private final String name;

    private final int prefColumnWidth;

    private final int horizontalAlignment;

    private final Class<?> columnClass;

    private final boolean filterable;

    private TraceEventColumn(String name, int prefColumnWidth, int horizontalAlignment, Class<?> columnClass,
            boolean filterable) {
        this.name = name;
        this.prefColumnWidth = prefColumnWidth;
        this.horizontalAlignment = horizontalAlignment;
        this.columnClass = columnClass;
        this.filterable = filterable;
    }

    public String getName() {
        return name;
    }

    public int getPrefColumnWidth() {
        return prefColumnWidth;
    }

    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

    public boolean isFilterable() {
        return filterable;
    }

    @Override
    public String toString() {
        return name;
    }

    public static int getColumnNameIndex(TraceEventColumn traceTableModelColumn) {

        int index = -1;
        int counter = 0;

        for (TraceEventColumn column : values()) {

            if (column.equals(traceTableModelColumn)) {
                index = counter;
                break;
            }

            counter++;
        }

        return index;
    }

    public static List<TraceEventColumn> getTraceEventColumnList(TracerType tracerType, boolean combined) {

        List<TraceEventColumn> traceEventColumnList = null;

        if (tracerType != null) {

            switch (tracerType) {

            case DX_API:
                traceEventColumnList = getDxApiTraceEventColumnList(combined);
                break;

            case NORMAL:
                traceEventColumnList = getNormalTraceEventColumnList(combined);
                break;

            default:
                traceEventColumnList = new ArrayList<>();
                break;
            }
        } else {
            traceEventColumnList = new ArrayList<>();
        }

        return traceEventColumnList;
    }

    private static List<TraceEventColumn> getNormalTraceEventColumnList(boolean combined) {

        List<TraceEventColumn> normalTraceEventColumnList = new ArrayList<>();

        normalTraceEventColumnList.add(LINE);
        normalTraceEventColumnList.add(TIMESTAMP);
        normalTraceEventColumnList.add(THREAD);
        normalTraceEventColumnList.add(INT);
        normalTraceEventColumnList.add(RULE);
        normalTraceEventColumnList.add(STEP_METHOD);
        normalTraceEventColumnList.add(STEP_PAGE);
        normalTraceEventColumnList.add(STEP);
        normalTraceEventColumnList.add(STATUS);
        normalTraceEventColumnList.add(EVENT_TYPE);

        if (!combined) {
            normalTraceEventColumnList.add(EVENT_NAME);
        }

        normalTraceEventColumnList.add(TOTAL_ELAPSED);
        normalTraceEventColumnList.add(OWN_ELAPSED);
        normalTraceEventColumnList.add(CHILDREN_ELAPSED);
        normalTraceEventColumnList.add(NAME);
        normalTraceEventColumnList.add(RULESET);

        return normalTraceEventColumnList;
    }

    private static List<TraceEventColumn> getDxApiTraceEventColumnList(boolean combined) {

        List<TraceEventColumn> dxApiTraceEventColumnList = new ArrayList<>();

        dxApiTraceEventColumnList.add(LINE);
        dxApiTraceEventColumnList.add(TIMESTAMP);
        dxApiTraceEventColumnList.add(DXAPI_INT_ID);
        dxApiTraceEventColumnList.add(DXAPI_PATHINFO);
        dxApiTraceEventColumnList.add(THREAD);
        dxApiTraceEventColumnList.add(INT);
        dxApiTraceEventColumnList.add(RULE);
        dxApiTraceEventColumnList.add(STEP_METHOD);
        dxApiTraceEventColumnList.add(STEP_PAGE);
        dxApiTraceEventColumnList.add(STEP);
        dxApiTraceEventColumnList.add(STATUS);
        dxApiTraceEventColumnList.add(EVENT_TYPE);

        if (!combined) {
            dxApiTraceEventColumnList.add(EVENT_NAME);
        }

        dxApiTraceEventColumnList.add(TOTAL_ELAPSED);
        dxApiTraceEventColumnList.add(OWN_ELAPSED);
        dxApiTraceEventColumnList.add(CHILDREN_ELAPSED);
        dxApiTraceEventColumnList.add(NAME);
        dxApiTraceEventColumnList.add(RULESET);

        return dxApiTraceEventColumnList;
    }

    public static TraceEventColumn[] getReportTraceTableModelColumnArray() {

        TraceEventColumn[] reportTraceTableModelColumnArray = new TraceEventColumn[12];

        reportTraceTableModelColumnArray[0] = LINE;
        reportTraceTableModelColumnArray[1] = TIMESTAMP;
        reportTraceTableModelColumnArray[2] = INT;
        reportTraceTableModelColumnArray[3] = STEP;
        reportTraceTableModelColumnArray[4] = STATUS;
        reportTraceTableModelColumnArray[5] = EVENT_TYPE;
        reportTraceTableModelColumnArray[6] = EVENT_NAME;
        reportTraceTableModelColumnArray[7] = TOTAL_ELAPSED;
        reportTraceTableModelColumnArray[8] = OWN_ELAPSED;
        reportTraceTableModelColumnArray[9] = CHILDREN_ELAPSED;
        reportTraceTableModelColumnArray[10] = NAME;
        reportTraceTableModelColumnArray[11] = RULESET;

        return reportTraceTableModelColumnArray;
    }
}
