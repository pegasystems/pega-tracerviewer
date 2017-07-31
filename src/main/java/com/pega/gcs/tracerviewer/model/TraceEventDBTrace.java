/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import java.awt.Color;

import org.dom4j.Element;

public abstract class TraceEventDBTrace extends TraceEvent {

	protected TraceEventDBTrace(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {
		
		super(traceEventKey, bytes, traceEventElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fringe.tracerviewer.TraceEvent#getName()
	 */
	@Override
	protected void setName(Element traceEventElement) {
		String name = "";

		Element element = traceEventElement.element("DBTNote");

		if (element != null) {
			name = element.getText();
		}

		if ((name == null) || "".equals(name)) {

			element = traceEventElement.element("DBTHighLevelOp");

			if (element != null) {
				name = element.getText();

				if ((name != null) && "root".equals(name)) {
					name = "";
				}
			}
		}

		setName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fringe.tracerviewer.TraceEvent#getRuleSet()
	 */
	@Override
	protected void setRuleSet(Element traceEventElement) {
		setRuleSet("");

	}

	@Override
	protected void setDefaultBackground() {
		Color color = Color.CYAN;
		fillColumnBackground(color);
	}

	@Override
	public Element getTraceEventPropertyElement() {

		Element traceEventPropertyElement = null;

		Element rootElement = getTraceEventRootElement();

		if (rootElement != null) {

			traceEventPropertyElement = getDefaultTraceEventPropertyElement(rootElement);

			// DBTDatabaseName
			Element element = rootElement.element("DBTDatabaseName");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Database Name", element));
			}

			// DBTSize
			element = rootElement.element("DBTSize");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Size", element));
			}

			// DBTTableName
			element = rootElement.element("DBTTableName");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Table Name", element));
			}

			// DBTSQLOperation
			element = rootElement.element("DBTSQLOperation");

			if (element != null) {
				traceEventPropertyElement.add(createElement("SQL Operation", element));
			}

			// DBTSQL
			element = rootElement.element("DBTSQL");

			if (element != null) {
				traceEventPropertyElement.add(createElement("SQL", element));
			}

			// DBTSQLInserts
			element = rootElement.element("DBTSQLInserts");

			if (element != null) {
				traceEventPropertyElement.add(createElement("SQLInserts", element));
			}

			// DBTCacheType
			element = rootElement.element("DBTCacheType");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Cache Type", element));
			}

			// DBTConnectionID
			element = rootElement.element("DBTConnectionID");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Connection ID", element));
			}

			// DBTHighLevelOpID
			element = rootElement.element("DBTHighLevelOpID");

			if (element != null) {
				traceEventPropertyElement.add(createElement("High Level Op ID", element));
			}

			// DBTHighLevelOp
			element = rootElement.element("DBTHighLevelOp");

			if (element != null) {
				traceEventPropertyElement.add(createElement("High Level Op", element));
			}

			// DBTLabel
			element = rootElement.element("DBTLabel");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Label", element));
			}

			// DBTNote
			element = rootElement.element("DBTNote");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Note", element));
			}

			// DBTObjectClass
			element = rootElement.element("DBTObjectClass");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Object Class", element));
			}
		}

		return traceEventPropertyElement;

	}
}
