/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.awt.Color;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.text.StringEscapeUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.Identifiable;
import com.pega.gcs.fringecommon.utilities.KnuthMorrisPrattAlgorithm;
import com.pega.gcs.fringecommon.utilities.kyro.KryoSerializer;
import com.pega.gcs.tracerviewer.SearchEventType;
import com.pega.gcs.tracerviewer.TraceEventColumn;
import com.pega.gcs.tracerviewer.TraceEventFactory;

//@formatter:off
/*
 * TraceEvents are made hierarchical  based on the the colour they display.
 * 
 * TraceEvent
 *         Activity
 *         Activity Steps
 *         AsynchronousActivity
 *         CaseType
 *         DataPages
 *         Data Transforms
 *         Exception
 *         ReferenceProperties
 *         When rules
 * 
 *         Alert
 * 
 *         DBTrace
 *             DBQuery
 *             DBCache
 *             Named Transactions
 * 
 *         NonActivity
 *             AdaptiveModel
 *             AutoPopulateProperties
 *             DeclareCollection
 *             DeclareConstraint
 *             DeclareDecisionMap
 *             DeclareDecisionTable
 *             DeclareDecisionTree
 *             DeclareExpression
 *             DeclareIndex
 *             DeclareOnChange
 *             DeclarePages
 *             DeclareTrigger
 *             Flow
 *             Interaction
 *             LinkedPageHit
 *             LinkedPageMiss
 *             Locking
 *             LogMessages
 *             ParseRules
 *             PredictiveModel
 *             Scorecard
 *             Services
 *             ServiceMapping
 *             Strategy
 *             StreamRules
 */
//@formatter:on

public abstract class TraceEvent implements Identifiable<TraceEventKey> {

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceEvent.class);

    private static final String KEY_PAGE_MESSAGES = "PAGE_MESSAGES";

    private static final String KEY_STATUS_WARN = "STATUS_WARN";

    private static final String KEY_STATUS_FAIL = "STATUS_FAIL";

    private static final String KEY_PZ__ERROR = "PZ__ERROR";

    protected TraceEventType traceEventType;

    private byte[] compressedTraceEventBytes;

    private TraceEventKey traceEventKey;

    private String line;

    private Date timestamp;

    private String dxApiInteractionId;

    private String dxApiPathInfo;

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

    private boolean endOfAsyncTraceEventSent;

    private boolean hasMessages;

    private boolean stepStatusWarn;

    private boolean stepStatusFail;

    private boolean stepStatusException;

    private boolean searchFound;

    private double childrenElapsed;

    private double ownElapsed;

    private String insKey;

    private int bytesLength;

    public TraceEvent(TraceEventKey traceEventKey, byte[] bytes, Element traceEventElement) {

        this.traceEventKey = traceEventKey;
        this.traceEventType = null;

        this.elapsed = -1;
        this.childrenElapsed = -1;
        this.ownElapsed = -1;

        if (bytes != null) {

            bytesLength = bytes.length;

            try {
                setTraceEventBytes(bytes);
            } catch (Exception e) {
                LOG.error("Exception on compressing trace event bytes: " + traceEventKey, e);
            }
        } else {
            bytesLength = 0;
        }

        if (traceEventElement != null) {

            try {

                initializeGlobals(traceEventElement);

                setLineFromElement(traceEventElement);
                setTimestampFromElement(traceEventElement);
                setDxApiInteractionIdFromElement(traceEventElement);
                setDxApiPathInfoFromElement(traceEventElement);
                setThreadFromElement(traceEventElement);
                setInteractionFromElement(traceEventElement);
                setRuleNoFromElement(traceEventElement);
                setStepMethodFromElement(traceEventElement);
                setStepPageFromElement(traceEventElement);
                setStepFromElement(traceEventElement);
                setStatusFromElement(traceEventElement);
                setEventTypeFromElement(traceEventElement);
                setEventNameFromElement(traceEventElement);
                setElapsedFromElement(traceEventElement);
                setNameFromElement(traceEventElement);
                setRuleSetFromElement(traceEventElement);
                // for reporting purpose
                setInsKeyFromElement(traceEventElement);

            } catch (Exception e) {
                LOG.error("Exception on traceEvent: " + traceEventKey, e);
            }
        }
    }

    public void setTraceEventKey(TraceEventKey traceEventKey) {
        this.traceEventKey = traceEventKey;
    }

    public TraceEventType getTraceEventType() {
        return traceEventType;
    }

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

    public String getDxApiInteractionId() {
        return dxApiInteractionId;
    }

    public String getDxApiPathInfo() {
        return dxApiPathInfo;
    }

    public String getThread() {
        return thread;
    }

    public Integer getInteraction() {
        return interaction;
    }

    public Integer getRuleNo() {
        return ruleNo;
    }

    public String getStepMethod() {
        return stepMethod;
    }

    public String getStepPage() {
        return stepPage;
    }

    public String getStep() {
        return step;
    }

    public String getStatus() {
        return status;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public double getElapsed() {
        return elapsed;
    }

    public String getName() {
        return name;
    }

    public String getRuleSet() {
        return ruleSet;
    }

    public String getInsKey() {
        return insKey;
    }

    protected void setLine(String line) {
        this.line = line;
    }

    protected void setLineFromElement(Element traceEventElement) {

        String line = null;

        Element element = traceEventElement.element("Sequence");

        if (element != null) {

            int seq = Integer.parseInt(element.getText());

            // display starts with 1
            line = String.valueOf((seq + 1));
        }

        setLine(line);

    }

    protected void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    protected void setTimestampFromElement(Element traceEventElement) {

        Date timestamp = null;

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

        setTimestamp(timestamp);
    }

    protected void setDxApiInteractionId(String dxApiInteractionId) {
        this.dxApiInteractionId = dxApiInteractionId;
    }

    protected void setDxApiInteractionIdFromElement(Element traceEventElement) {

        String dxApiInteractionId = null;

        Element element = traceEventElement.element("DxAPIInteractionId");

        if (element != null) {
            dxApiInteractionId = element.getText();
        }

        setDxApiInteractionId(dxApiInteractionId);
    }

    protected void setDxApiPathInfo(String dxApiPathInfo) {
        this.dxApiPathInfo = dxApiPathInfo;
    }

    protected void setDxApiPathInfoFromElement(Element traceEventElement) {

        String dxApiPathInfo = null;

        Element element = traceEventElement.element("DxAPIPathInfo");

        if (element != null) {
            dxApiPathInfo = element.getText();
        }

        setDxApiPathInfo(dxApiPathInfo);
    }

    protected void setThread(String thread) {
        this.thread = thread;
    }

    protected void setThreadFromElement(Element traceEventElement) {

        String thread = null;

        Element element = traceEventElement.element("ThreadName");

        if (element != null) {
            thread = element.getText();
        }

        setThread(thread);
    }

    protected void setInteraction(Integer interaction) {
        this.interaction = interaction;
    }

    protected void setInteractionFromElement(Element traceEventElement) {

        Integer interaction = null;

        Element element = traceEventElement.element("Interaction");

        if (element != null) {

            String intStr = element.getText();

            try {
                interaction = Integer.valueOf(intStr);
            } catch (NumberFormatException nfe) {
                LOG.error("Unable to parse Interaction number: " + intStr, nfe);
            }
        }

        setInteraction(interaction);
    }

    protected void setRuleNo(Integer ruleNo) {
        this.ruleNo = ruleNo;
    }

    protected void setRuleNoFromElement(Element traceEventElement) {

        Integer ruleNo = null;

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
                ruleNo = Integer.valueOf(ruleNoStr);
            } catch (NumberFormatException nfe) {
                LOG.error("Unable to parse Rule number: " + ruleNoStr, nfe);
            }
        }

        setRuleNo(ruleNo);
    }

    protected void setStepMethod(String stepMethod) {
        this.stepMethod = stepMethod;
    }

    protected void setStepMethodFromElement(Element traceEventElement) {

        String stepMethod = null;

        Element element = traceEventElement.element("StepMethod");

        if (element != null) {
            stepMethod = element.getText();
        } else {

            element = traceEventElement.element("DBTSQLOperation");

            if (element != null) {
                stepMethod = element.getText();
            }
        }

        setStepMethod(stepMethod);
    }

    protected void setStepPage(String stepPage) {
        this.stepPage = stepPage;
    }

    protected void setStepPageFromElement(Element traceEventElement) {

        String stepPage = null;

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

        setStepPage(stepPage);
    }

    protected void setStep(String step) {
        this.step = step;
    }

    protected void setStepFromElement(Element traceEventElement) {

        String step = null;

        Element element = traceEventElement.element("StepNumber");

        if (element != null) {
            step = element.getText();
        }

        setStep(step);
    }

    protected void setStatus(String status) {
        this.status = status;
    }

    protected void setStatusFromElement(Element traceEventElement) {

        String status = null;

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

            String curStatus = status.toUpperCase();

            if (curStatus.indexOf("WARN") >= 0) {
                stepStatusWarn = true;
            } else if (curStatus.indexOf("FAIL") >= 0) {
                stepStatusFail = true;
            } else if (curStatus.indexOf("EXCEPTION") >= 0) {
                stepStatusException = true;
            }
        }

        setStatus(status);
    }

    protected void setEventType(String eventType) {
        this.eventType = eventType;
    }

    protected void setEventTypeFromElement(Element traceEventElement) {

        String eventType = null;

        Element element = traceEventElement.element("EventType");

        if (element != null) {
            eventType = element.getText();
        }

        setEventType(eventType);
    }

    protected void setEventName(String eventName) {
        this.eventName = eventName;
    }

    protected void setEventNameFromElement(Element traceEventElement) {

        String eventName = null;

        Element element = traceEventElement.element("EventName");

        if (element != null) {
            eventName = element.getText();
        }

        setEventName(eventName);
    }

    protected void setElapsed(double elapsed) {
        this.elapsed = elapsed;
    }

    protected void setElapsedFromElement(Element traceEventElement) {

        double elapsed = -1;

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

        setElapsed(elapsed);
    }

    private void setInsKey(String insKey) {
        this.insKey = insKey;
    }

    private void setInsKeyFromElement(Element traceEventElement) {

        String insKey = null;

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

        setInsKey(insKey);
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected void setNameFromElement(Element traceEventElement) {

        String name = null;

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

        setName(name);
    }

    protected void setRuleSet(String ruleSet) {
        this.ruleSet = ruleSet;
    }

    protected void setRuleSetFromElement(Element traceEventElement) {

        String ruleSet = null;

        Attribute attribute = traceEventElement.attribute("rsname");

        if (attribute != null) {
            ruleSet = attribute.getText();
        }

        attribute = traceEventElement.attribute("rsvers");

        if (attribute != null) {
            ruleSet = ruleSet + " " + attribute.getText();
        }

        setRuleSet(ruleSet);
    }

    public Color getBaseColumnBackground() {
        return MyColor.LIGHTEST_LIGHT_GRAY;
    }

    public Color getColumnBackground(TraceEventColumn traceEventColumn) {

        Color columnBackground = null;

        if (traceEventColumn.equals(TraceEventColumn.STATUS)) {

            if (stepStatusWarn) {
                columnBackground = Color.ORANGE;
            } else if (stepStatusFail) {
                columnBackground = MyColor.TOMATO;
            } else if (stepStatusException) {
                columnBackground = Color.RED;
            } else {
                columnBackground = getBaseColumnBackground();
            }
        } else if ((traceEventColumn.equals(TraceEventColumn.STEP_PAGE)) && (hasMessages)) {
            columnBackground = Color.ORANGE;
        } else {
            columnBackground = getBaseColumnBackground();
        }

        return columnBackground;
    }

    public boolean isEndOfAsyncTraceEventSent() {
        return endOfAsyncTraceEventSent;
    }

    public boolean isHasMessages() {
        return hasMessages;
    }

    public boolean isStepStatusWarn() {
        return stepStatusWarn;
    }

    public boolean isStepStatusFail() {
        return stepStatusFail;
    }

    public boolean isStepStatusException() {
        return stepStatusException;
    }

    private void setTraceEventBytes(byte[] bytes) throws Exception {
        compressedTraceEventBytes = KryoSerializer.compress(bytes);
    }

    public String getTraceEventStr(Charset charset) {
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TraceEvent [" + getLine() + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(eventName, name, stepMethod, stepPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TraceEvent)) {
            return false;
        }

        TraceEvent other = (TraceEvent) obj;

        return Objects.equals(eventName, other.eventName) && Objects.equals(name, other.name)
                && Objects.equals(stepMethod, other.stepMethod) && Objects.equals(stepPage, other.stepPage);
    }

    public String toDebugString() {

        StringBuilder traceEventDebugSB = new StringBuilder();

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
    // StringBuilder traceEventColumnSB = new StringBuilder();
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

    public String getColumnValueForTraceTableModelColumn(TraceEventColumn traceEventColumn) {

        String value = null;

        switch (traceEventColumn) {

        case LINE:// Line
            value = getLine();
            break;
        case TIMESTAMP:// TimeStamp
            value = getTimestamp();
            break;
        case DXAPI_INT_ID:
            value = getDxApiInteractionId();
            break;
        case DXAPI_PATHINFO:
            value = getDxApiPathInfo();
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
        case EVENT_SIZE:
            Integer bytesLength = getBytesLength();
            value = (bytesLength != null) ? bytesLength.toString() : null;
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

    private boolean getEndOfAsyncTraceEventSent(Element traceEventElement) {

        boolean endOfAsyncTraceEventSent = false;

        Element element = traceEventElement.element("EventName");

        if (element != null) {

            String eventName = element.getText();

            if (eventName != null && "AsyncTracerEnd".equals(eventName)) {
                endOfAsyncTraceEventSent = true;
            }
        }

        return endOfAsyncTraceEventSent;
    }

    private boolean getHasMessages(Element traceEventElement) {

        boolean hasMessages = false;

        Element primaryPageContentElement = traceEventElement.element("PrimaryPageContent");

        if (primaryPageContentElement != null) {
            Element pagedataElement = primaryPageContentElement.element("pagedata");

            if (pagedataElement != null) {

                // recent tracers are not showing warning on pzstatus field
                // Element pzStatusElement = pagedataElement.element("pzStatus");
                //
                // if (pzStatusElement != null) {
                //
                // String statusText = pzStatusElement.getText();
                //
                // if ((statusText != null) && (!"".equals(statusText)) && ("false".equals(statusText))) {
                // hasMessages = true;
                // }
                // }

                if (!hasMessages) {

                    Element pzErrorElement = pagedataElement.element("PZ__ERROR");

                    if (pzErrorElement != null) {
                        hasMessages = true;
                    }
                }

                if (!hasMessages) {

                    Element pzElement = pagedataElement.element("PZ__");

                    if (pzElement != null) {
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

        int pos = eventKey.indexOf(" ");

        if (pos >= 0) {
            activityName = eventKey.substring(pos + 1, eventKey.length());
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

    private boolean isInstanceWithKeys(String eventKey) {

        boolean isInstanceWithKeys = false;

        if ((eventKey != null) && (!"".equals(eventKey)) && (eventKey.length() >= 5)) {

            String ruleName = eventKey.substring(0, 5);
            ruleName = ruleName.toLowerCase();

            if (ruleName.indexOf("rule-") >= 0) {
                if (eventKey.indexOf(".") != -1) {
                    isInstanceWithKeys = true;
                }
            }
        }

        return isInstanceWithKeys;
    }

    public final Element getTraceEventRootElement(Charset charset) {

        Element rootElement = null;

        try {

            String traceEventStr = getTraceEventStr(charset);

            if (traceEventStr != null) {
                // LOG.info(traceEventStr);
                SAXReader reader = new SAXReader();
                reader.setEncoding(charset.name());
                Document doc = reader.read(new StringReader(traceEventStr));
                rootElement = doc.getRootElement();
            }

        } catch (Exception e) {
            LOG.error("Error creating xml doc", e);
        }

        return rootElement;

    }

    public boolean search(Object searchStrObj, Charset charset) {

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

            String traceEventStr = getTraceEventStr(charset);
            String searchStr = (String) searchStrObj;

            // traceEventStr will null in case of empty or corrupt TE's
            if ((traceEventStr != null) && (searchStr != null)) {

                traceEventStr = traceEventStr.toLowerCase();
                String traceSearchStr = searchStr.toLowerCase();

                byte[] pattern = traceSearchStr.getBytes(charset);
                byte[] data = traceEventStr.getBytes(charset);

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

                        data = unescTraceEventStr.getBytes(charset);

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

    public int getBytesLength() {
        return bytesLength;
    }

    protected boolean isDataPageEventKey() {
        return false;
    }

    protected boolean matchObject(Object startObject, Object endObject) {

        boolean match = false;

        if ((startObject == null) && (endObject == null)) {
            match = true;
        } else if ((startObject == null) || (endObject == null)) {
            match = false;
        } else {
            match = startObject.equals(endObject);
        }

        return match;
    }

    public boolean isMatchingParentTraceEvent(TraceEvent childTraceEvent) {

        boolean matchingParentTraceEvent = false;

        Integer childInteraction = childTraceEvent.getInteraction();
        // Integer childRuleNo = childTraceEvent.getRuleNo();
        TraceEventType childTraceEventType = childTraceEvent.getTraceEventType();

        if ((childInteraction != null) && /* (childRuleNo != null) && */ (childTraceEventType != null)) {

            // String endTraceEventTypeName = childTraceEventType.getName();

            Integer interaction = getInteraction();
            TraceEventType traceEventType = getTraceEventType();

            if ((interaction != null) /* && (ruleNo != null) */ && (traceEventType != null)) {

                matchingParentTraceEvent = matchObject(interaction, childInteraction);

                // Excluding ruleno evaluation completely
                // // found that db query has only interaction set. hence this logic to
                // // fall back if case some detail are not set.
                // if (matchingParentTraceEvent) {
                //
                // String traceEventTypeName = traceEventType.getName();
                //
                // // if parent and child type is same and RuleNo is present, then check RuleNo
                // if (matchObject(traceEventTypeName, endTraceEventTypeName)) {
                //
                // Integer ruleNo = getRuleNo();
                // // if (childRuleNo != null) {
                // matchingParentTraceEvent = matchObject(ruleNo, childRuleNo);
                // // }
                // }
                // }
            }
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

        if ((matchObject(interaction, endInteraction)) /* && (matchObject(ruleNo, endRuleNo)) */
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
