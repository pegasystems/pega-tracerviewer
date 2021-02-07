/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.DateTimeUtilities;
import com.pega.gcs.fringecommon.xmltreetable.XMLElementType;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventADPLoad;
import com.pega.gcs.tracerviewer.model.TraceEventAccessDenyRules;
import com.pega.gcs.tracerviewer.model.TraceEventActivity;
import com.pega.gcs.tracerviewer.model.TraceEventActivitySteps;
import com.pega.gcs.tracerviewer.model.TraceEventAdaptiveModel;
import com.pega.gcs.tracerviewer.model.TraceEventAlert;
import com.pega.gcs.tracerviewer.model.TraceEventAsyncDPLoad;
import com.pega.gcs.tracerviewer.model.TraceEventAsynchronousActivity;
import com.pega.gcs.tracerviewer.model.TraceEventAutoPopulateProperties;
import com.pega.gcs.tracerviewer.model.TraceEventCaseType;
import com.pega.gcs.tracerviewer.model.TraceEventDBCache;
import com.pega.gcs.tracerviewer.model.TraceEventDBQuery;
import com.pega.gcs.tracerviewer.model.TraceEventDataFlow;
import com.pega.gcs.tracerviewer.model.TraceEventDataPages;
import com.pega.gcs.tracerviewer.model.TraceEventDataTransformAction;
import com.pega.gcs.tracerviewer.model.TraceEventDataTransforms;
import com.pega.gcs.tracerviewer.model.TraceEventDebug;
import com.pega.gcs.tracerviewer.model.TraceEventDecisionData;
import com.pega.gcs.tracerviewer.model.TraceEventDecisionParameters;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareCollection;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareConstraint;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareDecisionMap;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareDecisionTable;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareDecisionTree;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareExpression;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareIndex;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareOnChange;
import com.pega.gcs.tracerviewer.model.TraceEventDeclarePages;
import com.pega.gcs.tracerviewer.model.TraceEventDeclareTrigger;
import com.pega.gcs.tracerviewer.model.TraceEventException;
import com.pega.gcs.tracerviewer.model.TraceEventFlow;
import com.pega.gcs.tracerviewer.model.TraceEventFreeTextModel;
import com.pega.gcs.tracerviewer.model.TraceEventInteraction;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.model.TraceEventLinkedPageHit;
import com.pega.gcs.tracerviewer.model.TraceEventLinkedPageMiss;
import com.pega.gcs.tracerviewer.model.TraceEventLocking;
import com.pega.gcs.tracerviewer.model.TraceEventLogMessages;
import com.pega.gcs.tracerviewer.model.TraceEventNamedTransactions;
import com.pega.gcs.tracerviewer.model.TraceEventParseRules;
import com.pega.gcs.tracerviewer.model.TraceEventPredictiveModel;
import com.pega.gcs.tracerviewer.model.TraceEventPropositionFilter;
import com.pega.gcs.tracerviewer.model.TraceEventPushNotifications;
import com.pega.gcs.tracerviewer.model.TraceEventQueueProcessing;
import com.pega.gcs.tracerviewer.model.TraceEventReferenceProperties;
import com.pega.gcs.tracerviewer.model.TraceEventSoapMessages;
import com.pega.gcs.tracerviewer.model.TraceEventScorecard;
import com.pega.gcs.tracerviewer.model.TraceEventServiceMapping;
import com.pega.gcs.tracerviewer.model.TraceEventServices;
import com.pega.gcs.tracerviewer.model.TraceEventStrategy;
import com.pega.gcs.tracerviewer.model.TraceEventStreamRules;
import com.pega.gcs.tracerviewer.model.TraceEventType;
import com.pega.gcs.tracerviewer.model.TraceEventUnitTestCase;
import com.pega.gcs.tracerviewer.model.TraceEventUnknown;
import com.pega.gcs.tracerviewer.model.TraceEventWhenRules;

public class TraceEventFactory {

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceEventFactory.class);

    private static DateFormat dateFormat;

    private static DateFormat displayDateFormat;

    public static Map<String, XMLElementType> xmlElementTableTypeMap;

    private static DecimalFormat elapsedDecimalFormat;

    static {
        dateFormat = new SimpleDateFormat(DateTimeUtilities.DATEFORMAT_PEGA_INTERNAL);

        displayDateFormat = new SimpleDateFormat(DateTimeUtilities.DATEFORMAT_ISO8601);

        displayDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        elapsedDecimalFormat = new DecimalFormat("#0.0000");

        // setup xmlElementTableTypeMap
        xmlElementTableTypeMap = new HashMap<String, XMLElementType>();

        XMLElementType xmlElementType;

        // InteractionPAL
        xmlElementType = new XMLElementType("InteractionPAL", ";", false);
        xmlElementTableTypeMap.put(xmlElementType.getElementName(), xmlElementType);

        // InteractionQueryData
        xmlElementType = new XMLElementType("InteractionQueryData", "&", true);
        xmlElementTableTypeMap.put(xmlElementType.getElementName(), xmlElementType);
    }

    public static DateFormat getDateFormat() {
        return dateFormat;
    }

    public static DateFormat getDisplayDateFormat() {
        return displayDateFormat;
    }

    private static Element getDOMElement(byte[] bytes, SAXReader saxReader) throws DocumentException {

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedInputStream bis = new BufferedInputStream(bais);

        Document doc = saxReader.read(bis);

        Element traceEventElement = doc.getRootElement();

        return traceEventElement;
    }

    public static TraceEvent getTraceEvent(int id, byte[] bytes, SAXReader saxReader) {

        TraceEvent traceEvent = null;
        TraceEventKey traceEventKey = new TraceEventKey(id, id, false);

        try {

            Element traceEventElement = getDOMElement(bytes, saxReader);

            if (traceEventElement != null) {

                Element eventTypeElement = traceEventElement.element("EventType");

                if (eventTypeElement != null) {

                    String eventType = eventTypeElement.getText();

                    if ((eventType != null) && (!"".equals(eventType))) {

                        // ---- Events ---- //
                        if (TraceEventType.ACCESS_DENY_RULES.matchName(eventType)) {
                            // ACCESS_DENY_RULES
                            traceEvent = new TraceEventAccessDenyRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ACTIVITY.matchName(eventType)) {
                            // ACTIVITY
                            traceEvent = new TraceEventActivity(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ACTIVITY_STEP.matchName(eventType)) {
                            // ACTIVITY_STEPS
                            traceEvent = new TraceEventActivitySteps(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_TRANSFORMS.matchName(eventType)) {
                            // DATA_TRANSFORMS
                            traceEvent = new TraceEventDataTransforms(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_TRANSFORM_ACTION.matchName(eventType)) {
                            // DATA_TRANSFORM_ACTION
                            traceEvent = new TraceEventDataTransformAction(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.EXCEPTION.matchName(eventType)) {
                            // EXCEPTION
                            traceEvent = new TraceEventException(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.WHEN_RULES.matchName(eventType)) {
                            // WHEN_RULES
                            traceEvent = new TraceEventWhenRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ASYNC_DP_LOAD.matchName(eventType)) { // ---- Event Types ---- //
                            // ASYNC_DP_LOAD
                            traceEvent = new TraceEventAsyncDPLoad(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ADAPTIVE_MODEL.matchName(eventType)) {
                            // ADAPTIVE_MODEL
                            traceEvent = new TraceEventAdaptiveModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ADP_LOAD.matchName(eventType)) {
                            // ADP_LOAD
                            traceEvent = new TraceEventADPLoad(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ALERT.matchName(eventType)) {
                            // ALERT
                            traceEvent = new TraceEventAlert(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ASYNCHRONOUS_ACTIVITY.matchName(eventType)) {
                            // ASYNCHRONOUS_ACTIVITY
                            traceEvent = new TraceEventAsynchronousActivity(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.AUTOPOPULATE_PROPERTIES.matchName(eventType)) {
                            // AUTOPOPULATE_PROPERTIES
                            traceEvent = new TraceEventAutoPopulateProperties(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.CASE_TYPE.matchName(eventType)) {
                            // CASE_TYPE
                            traceEvent = new TraceEventCaseType(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DB_CACHE.matchName(eventType)) {
                            // DB_CACHE
                            traceEvent = new TraceEventDBCache(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DB_QUERY.matchName(eventType)) {
                            // DB_QUERY
                            traceEvent = new TraceEventDBQuery(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_FLOW.matchName(eventType)) {
                            // DATA_FLOW
                            traceEvent = new TraceEventDataFlow(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_PAGES.matchName(eventType)) {
                            // DATA_PAGES
                            traceEvent = new TraceEventDataPages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DEBUG.matchName(eventType)) {
                            // DEBUG
                            traceEvent = new TraceEventDebug(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECISION_DATA.matchName(eventType)) {
                            // DECISION_DATA
                            traceEvent = new TraceEventDecisionData(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECISION_PARAMETERS.matchName(eventType)) {
                            // DECISION_PARAMETERS
                            traceEvent = new TraceEventDecisionParameters(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_COLLECTION.matchName(eventType)) {
                            // DECLARE_COLLECTION
                            traceEvent = new TraceEventDeclareCollection(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_CONSTRAINT.matchName(eventType)) {
                            // DECLARE_CONSTRAINT
                            traceEvent = new TraceEventDeclareConstraint(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONMAP.matchName(eventType)) {
                            // DECLARE_DECISIONMAP
                            traceEvent = new TraceEventDeclareDecisionMap(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONTABLE.matchName(eventType)) {
                            // DECLARE_DECISIONTABLE
                            traceEvent = new TraceEventDeclareDecisionTable(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONTREE.matchName(eventType)) {
                            // DECLARE_DECISIONTREE
                            traceEvent = new TraceEventDeclareDecisionTree(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_EXPRESSION.matchName(eventType)) {
                            // DECLARE_EXPRESSION
                            traceEvent = new TraceEventDeclareExpression(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_INDEX.matchName(eventType)) {
                            // DECLARE_INDEX
                            traceEvent = new TraceEventDeclareIndex(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_ONCHANGE.matchName(eventType)) {
                            // DECLARE_ONCHANGE
                            traceEvent = new TraceEventDeclareOnChange(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_PAGES.matchName(eventType)) {
                            // DECLARE_PAGES
                            traceEvent = new TraceEventDeclarePages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_TRIGGER.matchName(eventType)) {
                            // DECLARE_TRIGGER
                            traceEvent = new TraceEventDeclareTrigger(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.FLOW.matchName(eventType)) {
                            // FLOW
                            traceEvent = new TraceEventFlow(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.FREE_TEXT_MODEL.matchName(eventType)) {
                            // FREE_TEXT_MODEL
                            traceEvent = new TraceEventFreeTextModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.INTERACTION.matchName(eventType)) {
                            // INTERACTION
                            traceEvent = new TraceEventInteraction(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LINKED_PAGE_HIT.matchName(eventType)) {
                            // LINKED_PAGE_HIT
                            traceEvent = new TraceEventLinkedPageHit(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LINKED_PAGE_MISS.matchName(eventType)) {
                            // LINKED_PAGE_MISS
                            traceEvent = new TraceEventLinkedPageMiss(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LOCKING.matchName(eventType)) {
                            // LOCKING
                            traceEvent = new TraceEventLocking(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LOG_MESSAGES.matchName(eventType)) {
                            // LOG_MESSAGES
                            traceEvent = new TraceEventLogMessages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.NAMED_TRANSACTIONS.matchName(eventType)) {
                            // NAMED_TRANSACTIONS
                            traceEvent = new TraceEventNamedTransactions(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PARSE_RULES.matchName(eventType)) {
                            // PARSE_RULES
                            traceEvent = new TraceEventParseRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PREDICTIVE_MODEL.matchName(eventType)) {
                            // PREDICTIVE_MODEL
                            traceEvent = new TraceEventPredictiveModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PROPOSITION_FILTER.matchName(eventType)) {
                            // PROPOSITION_FILTER
                            traceEvent = new TraceEventPropositionFilter(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PUSH_NOTIFICATIONS.matchName(eventType)) {
                            // PUSH_NOTIFICATIONS
                            traceEvent = new TraceEventPushNotifications(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.QUEUE_PROCESSING.matchName(eventType)) {
                            // QUEUE_PROCESSING
                            traceEvent = new TraceEventQueueProcessing(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.REFERENCE_PROPERTIES.matchName(eventType)) {
                            // REFERENCE_PROPERTIES
                            traceEvent = new TraceEventReferenceProperties(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SOAP_MESSAGES.matchName(eventType)) {
                            // SOAP_MESSAGES
                            traceEvent = new TraceEventSoapMessages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SCORECARD.matchName(eventType)) {
                            // SCORECARD
                            traceEvent = new TraceEventScorecard(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SERVICE_MAPPING.matchName(eventType)) {
                            // SERVICE_MAPPING
                            traceEvent = new TraceEventServiceMapping(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SERVICES.matchName(eventType)) {
                            // SERVICES
                            traceEvent = new TraceEventServices(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.STRATEGY.matchName(eventType)) {
                            // STRATEGY
                            traceEvent = new TraceEventStrategy(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.STREAM_RULES.matchName(eventType)) {
                            // STREAM_RULES
                            traceEvent = new TraceEventStreamRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.UNIT_TEST_CASE.matchName(eventType)) {
                            // UNIT_TEST_CASE
                            traceEvent = new TraceEventUnitTestCase(traceEventKey, bytes, traceEventElement);
                        } else {
                            // UNKNOWN
                            traceEvent = new TraceEventUnknown(traceEventKey, bytes, traceEventElement);
                        }
                    } else {

                        Element eventNameElement = traceEventElement.element("EventName");

                        if (eventNameElement != null) {

                            String eventName = eventNameElement.getText();

                            // currently data flow doesn't set the event type.
                            // DATA_FLOW
                            if (TraceEventType.DATA_FLOW.matchName(eventName)) {
                                traceEvent = new TraceEventDataFlow(traceEventKey, bytes, traceEventElement);
                            }
                        }
                    }

                    if (traceEvent == null) {
                        traceEvent = new TraceEventUnknown(traceEventKey, bytes, traceEventElement);
                    }
                }
            }

        } catch (Exception e) {
            LOG.info("Unable to parse the trace Event: " + id, e);
        }

        return traceEvent;
    }

    public static String getElapsedString(double elapsed) {

        String elapsedString = null;

        if (elapsed >= 0) {
            elapsedString = elapsedDecimalFormat.format(elapsed);
        }

        return elapsedString;
    }
}
