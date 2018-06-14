/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
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

    public static TraceEvent getTraceEvent(int id, byte[] bytes, String charset, SAXReader saxReader) {

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
                        // ACCESS_DENY_RULES
                        if (TraceEventType.ACCESS_DENY_RULES.matchName(eventType)) {
                            traceEvent = new TraceEventAccessDenyRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ACTIVITY.matchName(eventType)) {
                            traceEvent = new TraceEventActivity(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ACTIVITY_STEP.matchName(eventType)) {
                            traceEvent = new TraceEventActivitySteps(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_TRANSFORMS.matchName(eventType)) {
                            traceEvent = new TraceEventDataTransforms(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_TRANSFORM_ACTION.matchName(eventType)) {
                            traceEvent = new TraceEventDataTransformAction(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.EXCEPTION.matchName(eventType)) {
                            traceEvent = new TraceEventException(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.WHEN_RULES.matchName(eventType)) {
                            traceEvent = new TraceEventWhenRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ASYNC_DP_LOAD.matchName(eventType)) {
                            traceEvent = new TraceEventAsyncDPLoad(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ADAPTIVE_MODEL.matchName(eventType)) {
                            traceEvent = new TraceEventAdaptiveModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ADP_LOAD.matchName(eventType)) {
                            traceEvent = new TraceEventADPLoad(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ALERT.matchName(eventType)) {
                            traceEvent = new TraceEventAlert(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.ASYNCHRONOUS_ACTIVITY.matchName(eventType)) {
                            traceEvent = new TraceEventAsynchronousActivity(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.AUTOPOPULATE_PROPERTIES.matchName(eventType)) {
                            traceEvent = new TraceEventAutoPopulateProperties(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.CASE_TYPE.matchName(eventType)) {
                            traceEvent = new TraceEventCaseType(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DB_CACHE.matchName(eventType)) {
                            traceEvent = new TraceEventDBCache(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DB_QUERY.matchName(eventType)) {
                            traceEvent = new TraceEventDBQuery(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_FLOW.matchName(eventType)) {
                            traceEvent = new TraceEventDataFlow(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DATA_PAGES.matchName(eventType)) {
                            traceEvent = new TraceEventDataPages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DEBUG.matchName(eventType)) {
                            traceEvent = new TraceEventDebug(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECISION_DATA.matchName(eventType)) {
                            traceEvent = new TraceEventDecisionData(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECISION_PARAMETERS.matchName(eventType)) {
                            traceEvent = new TraceEventDecisionParameters(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_COLLECTION.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareCollection(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_CONSTRAINT.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareConstraint(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONMAP.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareDecisionMap(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONTABLE.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareDecisionTable(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_DECISIONTREE.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareDecisionTree(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_EXPRESSION.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareExpression(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_INDEX.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareIndex(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_ONCHANGE.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareOnChange(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_PAGES.matchName(eventType)) {
                            traceEvent = new TraceEventDeclarePages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.DECLARE_TRIGGER.matchName(eventType)) {
                            traceEvent = new TraceEventDeclareTrigger(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.FLOW.matchName(eventType)) {
                            traceEvent = new TraceEventFlow(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.FREE_TEXT_MODEL.matchName(eventType)) {
                            traceEvent = new TraceEventFreeTextModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.INTERACTION.matchName(eventType)) {
                            traceEvent = new TraceEventInteraction(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LINKED_PAGE_HIT.matchName(eventType)) {
                            traceEvent = new TraceEventLinkedPageHit(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LINKED_PAGE_MISS.matchName(eventType)) {
                            traceEvent = new TraceEventLinkedPageMiss(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LOCKING.matchName(eventType)) {
                            traceEvent = new TraceEventLocking(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.LOG_MESSAGES.matchName(eventType)) {
                            traceEvent = new TraceEventLogMessages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.NAMED_TRANSACTIONS.matchName(eventType)) {
                            traceEvent = new TraceEventNamedTransactions(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PARSE_RULES.matchName(eventType)) {
                            traceEvent = new TraceEventParseRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PREDICTIVE_MODEL.matchName(eventType)) {
                            traceEvent = new TraceEventPredictiveModel(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PROPOSITION_FILTER.matchName(eventType)) {
                            traceEvent = new TraceEventPropositionFilter(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.PUSH_NOTIFICATIONS.matchName(eventType)) {
                            traceEvent = new TraceEventPushNotifications(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.QUEUE_PROCESSING.matchName(eventType)) {
                            traceEvent = new TraceEventQueueProcessing(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.REFERENCE_PROPERTIES.matchName(eventType)) {
                            traceEvent = new TraceEventReferenceProperties(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SOAP_MESSAGES.matchName(eventType)) {
                            traceEvent = new TraceEventSoapMessages(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SCORECARD.matchName(eventType)) {
                            traceEvent = new TraceEventScorecard(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SERVICE_MAPPING.matchName(eventType)) {
                            traceEvent = new TraceEventServiceMapping(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.SERVICES.matchName(eventType)) {
                            traceEvent = new TraceEventServices(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.STRATEGY.matchName(eventType)) {
                            traceEvent = new TraceEventStrategy(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.STREAM_RULES.matchName(eventType)) {
                            traceEvent = new TraceEventStreamRules(traceEventKey, bytes, traceEventElement);
                        } else if (TraceEventType.UNIT_TEST_CASE.matchName(eventType)) {
                            traceEvent = new TraceEventUnitTestCase(traceEventKey, bytes, traceEventElement);
                        } else {
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

                    traceEvent.setCharset(charset);
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
