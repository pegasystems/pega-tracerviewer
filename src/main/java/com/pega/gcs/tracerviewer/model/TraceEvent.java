/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer.model;

import java.awt.Color;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.Identifiable;
import com.pega.gcs.fringecommon.utilities.KnuthMorrisPrattAlgorithm;
import com.pega.gcs.fringecommon.utilities.kyro.KryoSerializer;
import com.pega.gcs.tracerviewer.SearchEventType;
import com.pega.gcs.tracerviewer.TraceEventFactory;
import com.pega.gcs.tracerviewer.TraceTableModelColumn;

//@formatter:off
/*
 * TraceEvents are made hierarchical  based on the the colour they display.
 * 
 * TraceEvent
 * 		Activity
 * 		Activity Steps
 * 		AsynchronousActivity
 * 		CaseType
 * 		DataPages
 * 		Data Transforms
 * 		Exception
 * 		ReferenceProperties
 * 		When rules
 * 
 * 		Alert
 * 
 * 		DBTrace
 * 			DBQuery
 * 			DBCache
 * 			Named Transactions
 * 
 * 		NonActivity
 * 			AdaptiveModel
 * 			AutoPopulateProperties
 * 			DeclareCollection
 * 			DeclareConstraint
 * 			DeclareDecisionMap
 * 			DeclareDecisionTable
 * 			DeclareDecisionTree
 * 			DeclareExpression
 * 			DeclareIndex
 * 			DeclareOnChange
 * 			DeclarePages
 * 			DeclareTrigger
 * 			Flow
 * 			Interaction
 * 			LinkedPageHit
 * 			LinkedPageMiss
 * 			Locking
 * 			LogMessages
 * 			ParseRules
 * 			PredictiveModel
 * 			Scorecard
 * 			Services
 * 			ServiceMapping
 * 			Strategy
 * 			StreamRules
 */
//@formatter:on

public abstract class TraceEvent implements Identifiable<TraceEventKey> {

	private static final Log4j2Helper LOG = new Log4j2Helper(TraceEvent.class);

	protected TraceEventType traceEventType;

	private String charset;

	private byte[] compressedTraceEventBytes;

	private TraceEventKey traceEventKey;

	private String line;

	private Date timestamp;

	private String thread;

	private Integer interaction;

	private Integer ruleNo;

	private String stepMethod;

	private String stepPage;

	private String step; // can be in x.x format

	private String status;

	private String eventType;

	private String eventName;

	private double elapsed;

	private String name;

	private String ruleSet;

	private Color[] columnBackground;

	private boolean endOfAsyncTraceEventSent;

	private boolean hasMessages;

	private boolean stepStatusWarn;

	private boolean stepStatusFail;

	private boolean stepStatusException;

	private boolean searchFound;

	private double childrenElapsed;

	private double ownElapsed;

	private String insKey;

	public TraceEvent(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

		this.traceEventKey = traceEventKey;
		this.traceEventType = null;
		this.charset = Charset.defaultCharset().name();

		this.elapsed = -1;
		this.childrenElapsed = -1;
		this.ownElapsed = -1;

		setDefaultBackground();

		if (bytes != null) {
			try {
				setTraceEventBytes(bytes);
			} catch (Exception e) {
				LOG.error("Exception on compressing trace event bytes: " + traceEventKey, e);
			}
		}

		if (traceEventElement != null) {

			try {

				initializeGlobals(traceEventElement);

				setLine(traceEventElement);
				setTimestamp(traceEventElement);
				setThread(traceEventElement);
				setInt(traceEventElement);
				setRuleNo(traceEventElement);
				setStepMethod(traceEventElement);
				setStepPage(traceEventElement);
				setPageMessages();
				setStep(traceEventElement);
				setStatus(traceEventElement);
				setEventType(traceEventElement);
				setEventName(traceEventElement);
				setElapsed(traceEventElement);
				setName(traceEventElement);
				setRuleSet(traceEventElement);
				// for reporting purpose
				setInsKey(traceEventElement);
			} catch (Exception e) {
				LOG.error("Exception on traceEvent: " + traceEventKey, e);
			}
		}
	}

	public void setTraceEventKey(TraceEventKey traceEventKey) {
		this.traceEventKey = traceEventKey;
	}

	/**
	 * @return the traceEventType
	 */
	public TraceEventType getTraceEventType() {
		return traceEventType;
	}

	/**
	 * @return the line
	 */
	public String getLine() {
		return line;
	}

	public String getTimestamp() {

		String timestampStr = "";

		if (timestamp != null) {
			try {
				DateFormat df = TraceEventFactory.getDisplayDateFormat();
				timestampStr = df.format(timestamp);
			} catch (Exception e) {
				LOG.error("Error formatting timestamp: " + timestamp, e);
			}
		}

		return timestampStr;
	}

	/**
	 * @return the thread
	 */
	public String getThread() {
		return thread;
	}

	/**
	 * @return the interaction
	 */
	public Integer getInteraction() {
		return interaction;
	}

	/**
	 * @return the ruleNo
	 */
	public Integer getRuleNo() {
		return ruleNo;
	}

	/**
	 * @return the stepMethod
	 */
	public String getStepMethod() {
		return stepMethod;
	}

	/**
	 * @return the stepPage
	 */
	public String getStepPage() {
		return stepPage;
	}

	/**
	 * @return the step
	 */
	public String getStep() {
		return step;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	public String getEventType() {
		return eventType;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @return the elapsed
	 */
	public double getElapsed() {
		return elapsed;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the ruleSet
	 */
	public String getRuleSet() {
		return ruleSet;
	}

	/**
	 * @return the insKey
	 */
	public String getInsKey() {
		return insKey;
	}

	protected void setInteraction(Integer interaction) {
		this.interaction = interaction;
	}

	protected void setRuleNo(Integer ruleNo) {
		this.ruleNo = ruleNo;
	}

	/**
	 * @param stepMethod
	 *            the stepMethod to set
	 */
	protected void setStepMethod(String stepMethod) {
		this.stepMethod = stepMethod;
	}

	/**
	 * @param stepPage
	 *            the stepPage to set
	 */
	protected void setStepPage(String stepPage) {
		this.stepPage = stepPage;
	}

	/**
	 * @param step
	 *            the step to set
	 */
	protected void setStep(String step) {
		this.step = step;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	protected void setStatus(String status) {
		this.status = status;
	}

	protected void setEventType(String eventType) {
		this.eventType = eventType;
	}

	/**
	 * @param eventName
	 *            the eventName to set
	 */
	protected void setEventName(String eventName) {

		this.eventName = eventName;
	}

	/**
	 * @param elapsed
	 *            the elapsed to set
	 */
	protected void setElapsed(double elapsed) {
		this.elapsed = elapsed;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	protected void setName(String name) {
		this.name = name;
	}

	/**
	 * @param ruleSet
	 *            the ruleSet to set
	 */
	protected void setRuleSet(String ruleSet) {
		this.ruleSet = ruleSet;
	}

	protected void setLine(Element traceEventElement) {

		Element element = traceEventElement.element("Sequence");

		if (element != null) {

			int seq = Integer.parseInt(element.getText());

			// display starts with 1
			line = String.valueOf((seq + 1));
		}

	}

	protected void setTimestamp(Element traceEventElement) {

		Element element = traceEventElement.element("DateTime");

		if (element != null) {

			timestamp = null;
			try {

				DateFormat df = TraceEventFactory.getDateFormat();

				synchronized (df) {
					// parallel loading causes parsing errors. not thread safe
					timestamp = df.parse(element.getText());
				}
			} catch (Exception e) {
				LOG.error("Error parsing DateTime element: :" + element.getText(), e);
			}
		}

	}

	/**
	 * @param thread
	 *            the thread to set
	 */
	protected void setThread(Element traceEventElement) {
		Element element = traceEventElement.element("ThreadName");

		if (element != null) {
			thread = element.getText();
		}
	}

	protected void setInt(Element traceEventElement) {

		Element element = traceEventElement.element("Interaction");

		if (element != null) {

			String intStr = element.getText();

			try {
				int interaction = Integer.parseInt(intStr);

				setInteraction(interaction);

			} catch (NumberFormatException nfe) {
				LOG.error("Unable to parse Interaction number: " + intStr, nfe);
			}
		}

	}

	protected void setRuleNo(Element traceEventElement) {

		Element element = traceEventElement.element("ActivityNumber");

		String ruleNoStr = null;

		if (element != null) {
			ruleNoStr = element.getText();
		} else {
			@SuppressWarnings("unchecked")
			List<Element> elements = traceEventElement.elements("OptionalProperties");

			for (Element elem : elements) {
				element = elem.element("ActivityNumber");
				if (element != null) {
					ruleNoStr = element.getText();
				}
			}
		}

		if ((ruleNoStr != null) && (!"".equals(ruleNoStr))) {
			try {
				int ruleNo = Integer.parseInt(ruleNoStr);

				setRuleNo(ruleNo);
			} catch (NumberFormatException nfe) {
				LOG.error("Unable to parse Rule number: " + ruleNoStr, nfe);
			}
		}
	}

	protected void setStepMethod(Element traceEventElement) {

		Element element = traceEventElement.element("StepMethod");

		if (element != null) {
			stepMethod = element.getText();
		} else {

			element = traceEventElement.element("DBTSQLOperation");

			if (element != null) {
				stepMethod = element.getText();
			}
		}

	}

	protected void setStepPage(Element traceEventElement) {

		Element element = traceEventElement.element("PrimaryPageName");

		if (element != null) {
			stepPage = element.getText();
		} else {

			element = traceEventElement.element("StepPageName");

			if (element != null) {
				stepPage = element.getText();
			} else {

				element = traceEventElement.element("DBTTableName");

				if (element != null) {
					stepPage = element.getText();
				}
			}
		}
	}

	private void setPageMessages() {

		if (hasMessages) {

			int columnIndex = TraceTableModelColumn.getColumnNameIndex(TraceTableModelColumn.STEP_PAGE);

			setColumnBackground(Color.ORANGE, columnIndex);
		}
	}

	protected void setStep(Element traceEventElement) {

		Element element = traceEventElement.element("StepNumber");

		if (element != null) {
			step = element.getText();
		}
	}

	protected void setStatus(Element traceEventElement) {

		Element element = traceEventElement.element("mStepStatus");

		if (element != null) {
			status = element.getText();
		} else {

			element = traceEventElement.element("WhenStatus");

			if (element != null) {
				status = element.getText();
			} else {

				element = traceEventElement.element("DBTSize");

				if (element != null) {
					status = element.getText();
				}
			}

		}

		if ((status != null) && (!"".equals(status))) {

			int columnIndex = TraceTableModelColumn.getColumnNameIndex(TraceTableModelColumn.STATUS);

			String curStatus = status.toUpperCase();

			if (curStatus.indexOf("WARN") >= 0) {
				stepStatusWarn = true;
				setColumnBackground(Color.ORANGE, columnIndex);
			} else if (curStatus.indexOf("FAIL") >= 0) {
				stepStatusFail = true;
				setColumnBackground(MyColor.TOMATO, columnIndex);
			} else if (curStatus.indexOf("EXCEPTION") >= 0) {
				stepStatusException = true;
				setColumnBackground(Color.RED, columnIndex);
			}
		}
	}

	protected void setEventType(Element traceEventElement) {

		String eventType = null;

		Element element = traceEventElement.element("EventType");

		if (element != null) {
			eventType = element.getText();
		}

		setEventType(eventType);
	}

	protected void setEventName(Element traceEventElement) {

		String eventName = null;

		Element element = traceEventElement.element("EventName");

		if (element != null) {
			eventName = element.getText();
		}

		setEventName(eventName);
	}

	protected void setElapsed(Element traceEventElement) {

		elapsed = -1;

		Element element = traceEventElement.element("Elapsed");

		if (element != null) {
			String elapsedStr = element.getText();

			if ((elapsedStr != null) && (!"".equals(elapsedStr))) {

				try {
					elapsed = Double.parseDouble(elapsedStr);
					// convert to seconds
					elapsed = elapsed / 1000;
				} catch (NumberFormatException nfe) {
					LOG.error("Error parsing elapsed string: " + elapsedStr, nfe);
				}
			}
		}
	}

	private void setInsKey(Element traceEventElement) {

		Element element = traceEventElement.element("EventKey");

		if (element != null) {
			insKey = element.getText();
		} else {
			element = traceEventElement.element("InstanceName");

			if (element != null) {
				insKey = element.getText();
			}
		}

		// flow type has different eventkey, try to get inskey attribute, if
		// available
		if ((insKey != null) && (!"".equals(insKey))) {

			if (!isInstanceHandle(insKey)) {

				Attribute attribute = traceEventElement.attribute("inskey");

				if (attribute != null) {
					insKey = attribute.getText();
				}
			}
		}

	}

	protected void setName(Element traceEventElement) {

		Element element = traceEventElement.element("EventKey");

		if (element != null) {
			name = element.getText();
		} else {
			element = traceEventElement.element("InstanceName");

			if (element != null) {
				name = element.getText();
			}
		}

		if ((name != null) && (!"".equals(name))) {

			if (isInstanceHandle(name)) {

				Attribute attribute = traceEventElement.attribute("inskey");

				if (attribute != null) {

					attribute = traceEventElement.attribute("keyname");

					if (attribute != null) {

						name = attribute.getText();

						if (isDataPageEventKey()) {
							name = buildActivityName(name);
							name = buildDataPageDisplayName(name);
						}
					}

				} else {
					name = buildActivityName(name);
				}
			} else if (!isInstanceWithKeys(name)) {
				name = buildActivityName(name);
			}
		}
	}

	protected void setRuleSet(Element traceEventElement) {

		Attribute attribute = traceEventElement.attribute("rsname");

		if (attribute != null) {
			ruleSet = attribute.getText();
		}

		attribute = traceEventElement.attribute("rsvers");

		if (attribute != null) {
			ruleSet = ruleSet + " " + attribute.getText();
		}

	}

	public Color getColumnBackground(int column) {
		return columnBackground[column];
	}

	protected void setColumnBackground(Color color, int column) {
		columnBackground[column] = color;
	}

	/**
	 * @return the endOfAsyncTraceEventSent
	 */
	public boolean isEndOfAsyncTraceEventSent() {
		return endOfAsyncTraceEventSent;
	}

	/**
	 * @return the hasMessages
	 */
	public boolean isHasMessages() {
		return hasMessages;
	}

	/**
	 * @return the stepStatusWarn
	 */
	public boolean isStepStatusWarn() {
		return stepStatusWarn;
	}

	/**
	 * @return the stepStatusFail
	 */
	public boolean isStepStatusFail() {
		return stepStatusFail;
	}

	/**
	 * @return the stepStatusException
	 */
	public boolean isStepStatusException() {
		return stepStatusException;
	}

	private void setTraceEventBytes(byte[] bytes) throws Exception {
		compressedTraceEventBytes = KryoSerializer.compress(bytes);
	}

	public String getTraceEventStr() {
		String traceEventStr = null;

		if (compressedTraceEventBytes != null) {
			try {

				byte[] byteArray = KryoSerializer.decompress(compressedTraceEventBytes,
						compressedTraceEventBytes.getClass());

				traceEventStr = new String(byteArray, charset);

			} catch (Exception e) {
				LOG.error("Error decompressing trace event bytes", e);
			}
		}

		return traceEventStr;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((stepMethod == null) ? 0 : stepMethod.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraceEvent other = (TraceEvent) obj;
		if (eventName == null) {
			if (other.eventName != null)
				return false;
		} else if (!eventName.equals(other.eventName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (stepMethod == null) {
			if (other.stepMethod != null)
				return false;
		} else if (!stepMethod.equals(other.stepMethod))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TraceEvent [" + getLine() + "]";
	}

	public String toDebugString() {

		StringBuffer traceEventDebugSB = new StringBuffer();

		traceEventDebugSB.append("TraceEvent [");
		traceEventDebugSB.append(getLine());
		traceEventDebugSB.append(", ");
		traceEventDebugSB.append(getTraceEventType().getName());
		traceEventDebugSB.append(", ");
		traceEventDebugSB.append(getEventType());
		traceEventDebugSB.append(",");
		traceEventDebugSB.append(getName());
		traceEventDebugSB.append("]");

		return traceEventDebugSB.toString();
	}

	// public String getTraceEventColumnString() {
	//
	// StringBuffer traceEventColumnSB = new StringBuffer();
	//
	// for (TraceTableModelColumn ttmc : TraceTableModelColumn.values()) {
	//
	// String value = getColumnValueForTraceTableModelColumn(ttmc, false);
	//
	// traceEventColumnSB.append(value);
	// traceEventColumnSB.append("\t");
	// }
	//
	// return traceEventColumnSB.toString();
	// }
	//
	// public String getColumnValueForTraceEvent(int columnIndex, boolean
	// treeMode) {
	//
	// String value = null;
	//
	// TraceTableModelColumn traceTableModelColumn =
	// TraceTableModelColumn.values()[columnIndex];
	// value = getColumnValueForTraceTableModelColumn(traceTableModelColumn,
	// treeMode);
	//
	// return value;
	//
	// }

	public String getColumnValueForTraceTableModelColumn(TraceTableModelColumn traceTableModelColumn) {

		String value = null;

		switch (traceTableModelColumn) {

		case LINE:// Line
			value = getLine();
			break;
		case TIMESTAMP:// TimeStamp
			value = getTimestamp();
			break;
		case THREAD:// Thread
			value = getThread();
			break;
		case INT:// Int
			Integer interaction = getInteraction();
			value = (interaction != null) ? interaction.toString() : null;
			break;
		case RULE:// Rule#
			Integer ruleNo = getRuleNo();
			value = (ruleNo != null) ? ruleNo.toString() : null;
			break;
		case STEP_METHOD:// Step Method
			value = getStepMethod();
			break;
		case STEP_PAGE:// Step Page
			value = getStepPage();
			break;
		case STEP:// Step
			value = getStep();
			break;
		case STATUS:// Status
			value = getStatus();
			break;
		case EVENT_TYPE:// Event Type

			TraceEventType traceEventType = getTraceEventType();

			if (traceEventType != null) {
				value = traceEventType.getName();
			}
			break;
		case EVENT_NAME:// Event Name
			value = getEventName();
			break;
		case TOTAL_ELAPSED:// Elapsed
			value = TraceEventFactory.getElapsedString(getElapsed());
			break;
		case OWN_ELAPSED:
			value = TraceEventFactory.getElapsedString(getOwnElapsed());
			break;
		case CHILDREN_ELAPSED:
			value = TraceEventFactory.getElapsedString(getChildrenElapsed());
			break;
		case NAME:// Name
			value = getName();
			break;
		case RULESET:// RuleSet
			value = getRuleSet();
			break;
		default:
			value = null;
			break;
		}

		return value;
	}

	private void initializeGlobals(Element traceEventElement) {

		endOfAsyncTraceEventSent = getEndOfAsyncTraceEventSent(traceEventElement);
		hasMessages = getHasMessages(traceEventElement);

	}

	protected void fillColumnBackground(Color color) {

		int columnCount = TraceTableModelColumn.values().length;
		columnBackground = new Color[columnCount];

		for (int i = 0; i < columnCount; i++) {
			setColumnBackground(color, i);
		}
	}

	protected void setDefaultBackground() {

		Color color = MyColor.LIGHTEST_LIGHT_GRAY;
		fillColumnBackground(color);
	}

	private boolean getEndOfAsyncTraceEventSent(Element traceEventElement) {

		boolean endOfAsyncTraceEventSent = false;

		Element element = traceEventElement.element("EventName");

		if (element != null) {

			String eventName = element.getText();

			if (eventName != null && eventName == "AsyncTracerEnd") {
				endOfAsyncTraceEventSent = true;
			}
		}

		return endOfAsyncTraceEventSent;
	}

	private boolean getHasMessages(Element traceEventElement) {

		boolean hasMessages = false;

		Element element = traceEventElement.element("PrimaryPageContent");

		if (element != null) {
			element = element.element("pagedata");

			if (element != null) {
				element = element.element("pzStatus");

				if (element != null) {
					String statusText = element.getText();
					if ((statusText != null) && (!"".equals(statusText)) && ("false".equals(statusText))) {
						hasMessages = true;
					}
				}
			}
		}

		return hasMessages;
	}

	private boolean isInstanceHandle(String eventKey) {

		boolean isInstanceHandle = false;

		if (eventKey.indexOf(" GMT") != -1) {
			isInstanceHandle = true;
		}

		return isInstanceHandle;
	}

	protected String buildActivityName(String eventKey) {
		String activityName = "";
		int nPos = -1;

		nPos = eventKey.indexOf(" ");
		if (nPos >= 0) {
			activityName = eventKey.substring(nPos + 1, eventKey.length());
		}

		return activityName;
	}

	protected String buildDataPageDisplayName(String lineValue) {
		String dataPageDisplayName = "";

		if ((lineValue != null) && (lineValue.startsWith("D_") || (lineValue.startsWith("DECLARE_")))) {
			if (lineValue.indexOf("[") >= 0) {
				dataPageDisplayName = lineValue.substring(0, lineValue.indexOf("#"))
						+ lineValue.substring(lineValue.indexOf("["), lineValue.length());
			} else {
				dataPageDisplayName = lineValue.substring(0, lineValue.indexOf("#"));
			}
		} else {

			dataPageDisplayName = lineValue;
		}

		return dataPageDisplayName;
	}

	// not using this function because the declare page name has results counter
	// as well.
	private String getDPNameFromParameterizedDPName(String primaryPageName) {
		String dpNameFromParameterizedDPName = "";

		if (primaryPageName != null && (primaryPageName.startsWith("D_") || primaryPageName.startsWith("Declare_"))
				&& (primaryPageName.indexOf("_pa") > 0)) {
			return primaryPageName.substring(0, primaryPageName.indexOf("_pa"));
		} else {

			dpNameFromParameterizedDPName = primaryPageName;
		}

		return dpNameFromParameterizedDPName;
	}

	private boolean isInstanceWithKeys(String eventKey) {
		boolean isInstanceWithKeys = false;
		if ((eventKey != null) && (!"".equals(eventKey)) && (eventKey.length() >= 5)) {
			String aRule = eventKey.substring(0, 5);
			aRule = aRule.toLowerCase();
			if (aRule.indexOf("rule-") >= 0) {
				if (eventKey.indexOf(".") != -1) {
					isInstanceWithKeys = true;
				}
			}
		}
		return isInstanceWithKeys;
	}

	public final Element getTraceEventRootElement() {

		Element rootElement = null;

		try {

			String traceEventStr = getTraceEventStr();

			if (traceEventStr != null) {
				// LOG.info(traceEventStr);
				SAXReader reader = new SAXReader();
				reader.setEncoding(getCharset());
				Document doc = reader.read(new StringReader(traceEventStr));
				rootElement = doc.getRootElement();
			}

		} catch (Exception e) {
			LOG.error("Error creating xml doc", e);
		}

		return rootElement;

	}

	protected final Element createElement(String newName, Element element) {
		return createElement(newName, element, element.getName());
	}

	protected Element createElement(String newName, Element element, String elementName) {
		Element newElement = null;
		String targetName = newName;

		if (element != null) {

			if ((newName == null) || ("".equals(newName))) {
				targetName = element.getName();
			}

			newElement = element.createCopy();
			newElement.addAttribute("name", targetName);
		} else {
			if ((elementName != null) && (!"".equals(elementName))) {
				DocumentFactory factory = DocumentFactory.getInstance();
				newElement = factory.createElement(elementName);
				newElement.addAttribute("name", targetName);
			}
		}

		return newElement;
	}

	protected Element getDefaultTraceEventPropertyElement(Element traceEventRootElement) {

		DocumentFactory factory = DocumentFactory.getInstance();
		Element rootElement = factory.createElement("TraceEventProperty");

		// Sequence
		Element element = traceEventRootElement.element("Sequence");

		if (element != null) {
			rootElement.add(createElement(null, element));
		}

		// Interaction
		element = traceEventRootElement.element("Interaction");

		if (element != null) {
			rootElement.add(createElement(null, element));
		}

		// DateTime
		element = traceEventRootElement.element("DateTime");

		if (element != null) {
			rootElement.add(createElement("Timestamp", element));
		}

		// Elapsed
		element = traceEventRootElement.element("Elapsed");

		if (element != null) {
			rootElement.add(createElement("Elapsed Time", element));
		}

		// EventType
		element = traceEventRootElement.element("EventType");

		if (element != null) {
			rootElement.add(createElement("Event Type", element));
		}

		// EventName
		element = traceEventRootElement.element("EventName");

		if (element != null) {
			rootElement.add(createElement("Event Name", element));
		}

		// EventKey
		element = traceEventRootElement.element("EventKey");

		if (element != null) {
			rootElement.add(createElement("Event Key", element));
		}

		// ThreadName
		element = traceEventRootElement.element("ThreadName");

		if (element != null) {
			rootElement.add(createElement("Thread Name", element));
		}

		// WorkPool
		element = traceEventRootElement.element("WorkPool");

		if (element != null) {
			rootElement.add(createElement("Work Pool", element));
		}

		// ActivePALStat
		element = traceEventRootElement.element("ActivePALStat");

		if (element != null) {
			rootElement.add(createElement("Active PAL Stat", element));
		}

		// LastStep
		element = traceEventRootElement.element("LastStep");

		if (element != null) {
			rootElement.add(createElement("Last Step", element));
		}

		// FirstInput
		element = traceEventRootElement.element("FirstInput");

		if (element != null) {
			rootElement.add(createElement("Input", element));
		}

		// Ruleset Name
		Attribute attribute = traceEventRootElement.attribute("rsname");

		if (attribute != null) {
			String value = attribute.getText();
			Element newElement = createElement("Ruleset Name", null, "rsname");
			newElement.setText(value);
			rootElement.add(newElement);
		}

		// Ruleset Version
		attribute = traceEventRootElement.attribute("rsvers");

		if (attribute != null) {
			String value = attribute.getText();
			Element newElement = createElement("Ruleset Version", null, "rsvers");
			newElement.setText(value);
			rootElement.add(newElement);
		}

		return rootElement;

	}

	public Element getTraceEventPropertyElement() {

		Element traceEventPropertyElement = null;

		Element rootElement = getTraceEventRootElement();

		if (rootElement != null) {

			traceEventPropertyElement = getDefaultTraceEventPropertyElement(rootElement);

			// ActivityName
			Element element = rootElement.element("ActivityName");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Activity Name", element));
			}

			// ActivityNumber
			element = rootElement.element("ActivityNumber");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Size", element));
			}

			// ParameterPageName
			element = rootElement.element("ParameterPageName");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Parameter Page Name", element));
			}

			// ParameterPageContent
			element = rootElement.element("ParameterPageContent");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Parameter Page Content", element));
			}

			// PrimaryPageClass
			element = rootElement.element("PrimaryPageClass");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Primary Page Class", element));
			}

			// PrimaryPageName
			element = rootElement.element("PrimaryPageName");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Primary Page Name", element));
			}

			// PrimaryPageContent
			element = rootElement.element("PrimaryPageContent");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Primary Page Content", element));
			}

			// LocalVariables
			element = rootElement.element("LocalVariables");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Local Variables", element));
			}

			// NamedPages
			element = rootElement.element("NamedPages");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Additional Named Pages", element));
			}

			// AccessDenialReason
			element = rootElement.element("AccessDenialReason");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Access Denial Reason", element));
			}

			// AccessSnapshotPageName
			element = rootElement.element("AccessSnapshotPageName");

			if (element != null) {

				// TODO: might need another attibute to display unformatted xml
				// traceEventPropertyElement.add(createElement("High Level Op",
				// element));
			}

			// StepMethod
			element = rootElement.element("StepMethod");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Step Method", element));
			}

			// StepNumber
			element = rootElement.element("StepNumber");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Step Number", element));
			}

			// StepStatus
			element = rootElement.element("StepStatus");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Step Status", element));
			}

			// WhenStatus
			element = rootElement.element("WhenStatus");

			if (element != null) {
				traceEventPropertyElement.add(createElement("When Status", element));
			}

			// mStepStatus
			element = rootElement.element("mStepStatus");
			// TODO set background colour
			if (element != null) {
				traceEventPropertyElement.add(createElement("mStepStatus", element));
			}

			// mStepStatusInfo
			element = rootElement.element("mStepStatusInfo");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Step Status Info", element));
			}

			// OptionalProperties
			element = rootElement.element("OptionalProperties");

			if (element != null) {

				traceEventPropertyElement.add(createElement("Optional Properties", element));

				// element = traceEventPropertyElement
				// .element("OptionalPropertiesDescription");
				// if (element != null) {
				// traceEventPropertyElement.add(createElement(
				// "Optional Properties", element));
				// } else {
				// traceEventPropertyElement.add(createElement(
				// "Optional Properties", null,
				// "OptionalPropertiesDescription"));
				// }
			}

			// ExceptionTrace
			element = rootElement.element("ExceptionTrace");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Exception Trace", element));
			}

			// JavaStackTrace
			element = rootElement.element("JavaStackTrace");

			if (element != null) {
				traceEventPropertyElement.add(createElement("Java Stack Trace", element));
			}
		}
		return traceEventPropertyElement;

	}

	public boolean search(Object searchStrObj) {

		boolean found = false;

		if (searchStrObj instanceof SearchEventType) {

			SearchEventType searchEventType = (SearchEventType) searchStrObj;

			switch (searchEventType) {

			case PAGE_MESSAGES:
				found = isHasMessages();
				break;

			case STATUS_WARN:
				found = isStepStatusWarn();
				break;

			case STATUS_FAIL:
				found = isStepStatusFail();
				break;

			case STATUS_EXCEPTION:
				found = isStepStatusException();
				break;

			case SEPERATOR:
				break;
			default:
				break;
			}
		} else {

			String traceEventStr = getTraceEventStr();
			String searchStr = (String) searchStrObj;

			// traceEventStr will null in case of empty or corrupt TE's
			if ((traceEventStr != null) && (searchStr != null)) {

				traceEventStr = traceEventStr.toLowerCase();
				String traceSearchStr = searchStr.toLowerCase();

				byte[] pattern = traceSearchStr.getBytes();
				byte[] data = traceEventStr.getBytes();

				int index = KnuthMorrisPrattAlgorithm.indexOf(data, pattern);

				if (index != -1) {
					found = true;
				}

				// double searching - because some data is escaped. but user can use escaped or
				// unescaped text for search.
				if (!found) {

					// if the unescaped string is same as orig, then dont do second search
					String unescTraceEventStr = StringEscapeUtils.unescapeHtml4(traceEventStr);

					if (!unescTraceEventStr.equals(traceEventStr)) {
						
						data = unescTraceEventStr.getBytes();

						index = KnuthMorrisPrattAlgorithm.indexOf(data, pattern);

						if (index != -1) {
							found = true;
						}
					}
				}
			}
		}

		searchFound = found;
		
		return found;
	}

	private void buildTokenTableForJSONData(Element parent, String elemText, String token1) {
		// TODO
	}

	protected Element buildTokenTable(Element parent, String elemText, String token1) {

		String[] valueArray1 = elemText.split(token1);

		if (valueArray1 != null) {

			for (String val1 : valueArray1) {

				if ((val1 != null) && (!"".equals(val1))) {
					String[] valArray2 = val1.split("=");

					if ((valArray2 != null) && (valArray2.length > 0)) {

						String name = valArray2[0];

						if ((name != null) && (!"".equals(name))) {
							String value = "";

							if (valArray2.length > 1) {
								value = valArray2[1];

								try {
									value = java.net.URLDecoder.decode(value, "UTF-8");
								} catch (UnsupportedEncodingException uee) {
									LOG.error("Error decoding: " + value, uee);
								}
							}

							Element valElem = createElement(name, null, name);
							valElem.setText(value);

							parent.add(valElem);
						}
					}
				}

			}
		}

		return parent;

	}

	protected boolean checkStart() {
		return false;
	}

	protected boolean checkEnd() {
		return false;
	}

	// using object in order to get 3 states, true false and null.
	public Boolean isEndEvent() {

		Boolean endEvent = null;

		if (checkStart()) {
			endEvent = false;
		} else if (checkEnd()) {
			endEvent = true;
		}

		return endEvent;
	}

	public boolean isSearchFound() {
		return searchFound;
	}

	public void setSearchFound(boolean searchFound) {
		this.searchFound = searchFound;
	}

	@Override
	public TraceEventKey getKey() {
		return traceEventKey;
	}

	public double getChildrenElapsed() {
		return childrenElapsed;
	}

	public void setChildrenElapsed(double childrenElapsed) {
		this.childrenElapsed = childrenElapsed;
	}

	public double getOwnElapsed() {
		return ownElapsed;
	}

	public void setOwnElapsed(double ownElapsed) {
		this.ownElapsed = ownElapsed;
	}

	protected boolean isDataPageEventKey() {
		return false;
	}

	protected boolean matchObject(Object startObject, Object endObject) {

		boolean match = false;

		if ((startObject == null) && (endObject == null)) {
			match = true;
		} else if ((startObject == null) || (startObject == null)) {
			match = false;
		} else {
			match = startObject.equals(endObject);
		}

		return match;
	}

	public boolean isMatchingParentTraceEvent(TraceEvent childTraceEvent) {

		boolean matchingParentTraceEvent = false;

		Integer childInteraction = childTraceEvent.getInteraction();
		Integer childRuleNo = childTraceEvent.getRuleNo();
		String endTraceEventType = childTraceEvent.getTraceEventType().getName();

		Integer interaction = getInteraction();
		Integer ruleNo = getRuleNo();
		String traceEventType = getTraceEventType().getName();

		matchingParentTraceEvent = matchObject(interaction, childInteraction);

		// found that db query has only interaction set. hence this logic to
		// fall back if case some detail are not set.
		if (matchingParentTraceEvent) {

			// if parent and child type is same and RuleNo is present, then
			// check RuleNo
			if (matchObject(traceEventType, endTraceEventType)) {

				if (childRuleNo != null) {
					matchingParentTraceEvent = matchObject(ruleNo, childRuleNo);
				}
			}
			// removing the else condition because RuleNo increment as new child
			// activities spawn
			// else {
			// // else check the rule no only if present in both
			// if ((ruleNo != null) && (childRuleNo != null)) {
			// matchingParentTraceEvent = childRuleNo >= ruleNo ? true : false;
			// }
			// }

		}

		return matchingParentTraceEvent;

	}

	public boolean isMatchingStartTraceEvent(TraceEvent endTraceEvent) {

		boolean matchingStartTraceEvent = false;

		Integer endInteraction = endTraceEvent.getInteraction();
		Integer endRuleNo = endTraceEvent.getRuleNo();
		String endStep = endTraceEvent.getStep();
		String endTraceEventType = endTraceEvent.getTraceEventType().getName();

		Integer interaction = getInteraction();
		Integer ruleNo = getRuleNo();
		String step = getStep();
		String traceEventType = getTraceEventType().getName();

		if ((matchObject(interaction, endInteraction)) && (matchObject(ruleNo, endRuleNo))
				&& (matchObject(step, endStep)) && (matchObject(traceEventType, endTraceEventType))) {
			matchingStartTraceEvent = true;
		}

		// Boolean endEvent = isEndEvent();
		//
		// if (matchingStartTraceEvent && (endEvent != null) && (endEvent == false)) {
		// matchingStartTraceEvent = checkStart();
		// }

		return matchingStartTraceEvent;
	}
}
