/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import javax.swing.SwingConstants;

import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;

public enum TraceTableModelColumn {

	// @formatter:off
//                    displayName   prefColumnWidth	horizontalAlignment    columnClass                     filterable
//	TREE             (""		         , 70  , SwingConstants.CENTER , TreeTableColumn.TREE_COLUMN_CLASS), 
	LINE             ("LINE"		     , 80  , SwingConstants.CENTER , TreeTableColumn.TREE_COLUMN_CLASS , false ), 
	TIMESTAMP        ("TIMESTAMP"	     , 140 , SwingConstants.CENTER , String.class                      , false ),
	THREAD           ("THREAD"		     , 80 , SwingConstants.CENTER , String.class                      , true  ), 
	INT              ("INT"			     , 50  , SwingConstants.CENTER , String.class                      , true  ), 
	RULE             ("RULE#"		     , 50 , SwingConstants.CENTER , String.class                      , false ), 
	STEP_METHOD      ("STEP METHOD"	     , 200 , SwingConstants.CENTER , String.class                      , false ),
	STEP_PAGE        ("STEP PAGE"	     , 200 , SwingConstants.CENTER , String.class                      , false ),
	STEP             ("STEP"		     , 50  , SwingConstants.CENTER , String.class                      , false ),
	STATUS           ("STATUS"		     , 100 , SwingConstants.CENTER , String.class                      , true  ),
	EVENT_TYPE       ("EVENT TYPE"	     , 100 , SwingConstants.CENTER , String.class                      , true  ),
	EVENT_NAME       ("EVENT NAME"	     , 100 , SwingConstants.CENTER , String.class                      , false ),
	TOTAL_ELAPSED    ("TOTAL ELAPSED"    , 70 , SwingConstants.CENTER , String.class                      , false ),
	OWN_ELAPSED      ("OWN ELAPSED"	     , 70 , SwingConstants.CENTER , String.class                      , false ),
	CHILDREN_ELAPSED ("CHILDREN ELAPSED" , 70 , SwingConstants.CENTER , String.class                      , false ),
	NAME             ("NAME"		     , 350 , SwingConstants.LEFT   , String.class                      , true  ),
	RULESET          ("RULESET"		     , 170 , SwingConstants.CENTER , String.class                      , true  );
	//@formatter:on

	private String name;

	private int prefColumnWidth;

	private int horizontalAlignment;

	private Class<?> columnClass;

	private boolean filterable;

	/**
	 * @param name
	 * @param prefColumnWidth
	 * @param horizontalAlignment
	 * @param columnClass
	 */
	private TraceTableModelColumn(String name, int prefColumnWidth, int horizontalAlignment, Class<?> columnClass, boolean filterable) {
		this.name = name;
		this.prefColumnWidth = prefColumnWidth;
		this.horizontalAlignment = horizontalAlignment;
		this.columnClass = columnClass;
		this.filterable = filterable;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the prefColumnWidth
	 */
	public int getPrefColumnWidth() {
		return prefColumnWidth;
	}

	/**
	 * @return the horizontalAlignment
	 */
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	/**
	 * @return the columnClass
	 */
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

	public static int getColumnNameIndex(TraceTableModelColumn traceTableModelColumn) {

		int index = -1;
		int counter = 0;

		for (TraceTableModelColumn column : values()) {

			if (column.equals(traceTableModelColumn)) {
				index = counter;
				break;
			}

			counter++;
		}

		return index;
	}

	//	public static TreeTableColumn[] getTreeTableColumnArray() {
	//
	//		TreeTableColumn[] columns = null;
	//		int columnIndex = 0;
	//		String columnName;
	//		int prefColumnWidth;
	//		int alignment;
	//		Class<?> columnClass;
	//
	//		TraceTableModelColumn[] traceTableModelColumnArray = values();
	//
	//		int size = traceTableModelColumnArray.length;
	//		columns = new TreeTableColumn[size];
	//
	//		for (TraceTableModelColumn traceTableModelColumn : traceTableModelColumnArray) {
	//
	//			columnName = traceTableModelColumn.getName();
	//			prefColumnWidth = traceTableModelColumn.getPrefColumnWidth();
	//			alignment = traceTableModelColumn.getHorizontalAlignment();
	//			columnClass = traceTableModelColumn.getColumnClass();
	//
	//			TreeTableColumn column = new TreeTableColumn(columnName, columnClass);
	//			column.setAlignment(alignment);
	//			column.setPreferredWidth(prefColumnWidth);
	//			columns[columnIndex] = column;
	//			columnIndex++;
	//		}
	//
	//		return columns;
	//	}

	public static TraceTableModelColumn[] getTraceTableModelColumnArray() {

		TraceTableModelColumn[] traceTableModelColumnArray = values();

		return traceTableModelColumnArray;
	}

	public static TraceTableModelColumn[] getTraceTreeTableModelColumnArray() {

		TraceTableModelColumn[] traceTreeTableModelColumnArray = values();

		return traceTreeTableModelColumnArray;
	}

	public static TraceTableModelColumn[] getTraceTreeCombinedTableModelColumnArray() {

		TraceTableModelColumn[] traceTreeCombinedTableModelColumnArray = new TraceTableModelColumn[15];

		traceTreeCombinedTableModelColumnArray[0] = LINE;
		traceTreeCombinedTableModelColumnArray[1] = TIMESTAMP;
		traceTreeCombinedTableModelColumnArray[2] = THREAD;
		traceTreeCombinedTableModelColumnArray[3] = INT;
		traceTreeCombinedTableModelColumnArray[4] = RULE;
		traceTreeCombinedTableModelColumnArray[5] = STEP_METHOD;
		traceTreeCombinedTableModelColumnArray[6] = STEP_PAGE;
		traceTreeCombinedTableModelColumnArray[7] = STEP;
		traceTreeCombinedTableModelColumnArray[8] = STATUS;
		traceTreeCombinedTableModelColumnArray[9] = EVENT_TYPE;
		traceTreeCombinedTableModelColumnArray[10] = TOTAL_ELAPSED;
		traceTreeCombinedTableModelColumnArray[11] = OWN_ELAPSED;
		traceTreeCombinedTableModelColumnArray[12] = CHILDREN_ELAPSED;
		traceTreeCombinedTableModelColumnArray[13] = NAME;
		traceTreeCombinedTableModelColumnArray[14] = RULESET;

		return traceTreeCombinedTableModelColumnArray;
	}

	public static TraceTableModelColumn[] getReportTraceTableModelColumnArray() {

		TraceTableModelColumn[] reportTraceTableModelColumnArray = new TraceTableModelColumn[12];

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
