/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.model;

import java.util.Comparator;

public enum TraceEventType {

    // pzLoadTracerEventTypesForRequestor
    // @formatter:off
    // CHECKSTYLE:OFF
    // DEFAULT
    UNKNOWN("_UNKNOWN", true),

    // Events
    ACCESS_DENY_RULES        ("Access Denied"            ,          "Access Deny rules"    , false ),
    ACTIVITY                 ("Activity"                 , false  , "Activity Begin"       , "Activity End"),
    ACTIVITY_STEP            ("Step"                     ,          "Activity Steps"       , false                , "Step Begin"    , "Step End"),
    DATA_TRANSFORMS          ("Data Transform"           , false  , "Data Transform Begin" , "Data Transform End"),
    DATA_TRANSFORM_ACTION    ("Action"                   , false  , "Action Begin"         , "Action End"),
    EXCEPTION                ("Exception"                , false ),
    WHEN_RULES               ("When"                     ,          "When rules"           , false                , "When Begin"    , "When End"),

    // Event Types
    //Data-TRACERSettings • TracerOptionsAvailable
    ASYNC_DP_LOAD            ("Async DP Load"            , true ),
    ADAPTIVE_MODEL           ("Adaptive Model"           , true ),
    ADP_LOAD                 ("ADP Load"                 , true ),
    ALERT                    ("Alert"                    , true ),
    ASYNCHRONOUS_ACTIVITY    ("Asynchronous Activity"    , true ),
    AUTOPOPULATE_PROPERTIES  ("AutoPopulate Properties"  , true ),
    CASE_TYPE                ("CaseType"                 , true ),
    DATA_FLOW                ("Data Flow"                , true  , "Data Flow Start"       , "Data Flow Begin", "Data Flow End" , "Data Flow Fail"),
    DATA_PAGES               ("Data Pages"               , true ),
    DATA_SYNC                ("DataSync"                 ,         "Data Sync"             , true ),
    DB_CACHE                 ("DB Cache"                 , true ),
    DB_QUERY                 ("DB Query"                 , true ),
    DEBUG                    ("Debug"                    , true ),
    DECISION_DATA            ("Decision Data"            , true ),
    DECISION_PARAMETERS      ("Decision Parameters"      , true ),
    DECLARE_COLLECTION       ("Declare Collection"       , true ),
    DECLARE_CONSTRAINT       ("Declare Constraint"       , true ),
    DECLARE_DECISIONMAP      ("Declare DecisionMap"      , true ),
    DECLARE_DECISIONTABLE    ("Declare DecisionTable"    , true ),
    DECLARE_DECISIONTREE     ("Declare DecisionTree"     , true ),
    DECLARE_EXPRESSION       ("Declare Expression"       , true ),
    DECLARE_INDEX            ("Declare Index"            , true ),
    DECLARE_ONCHANGE         ("Declare OnChange"         , true ),
    DECLARE_PAGES            ("Declare Pages"            , true ),
    DECLARE_TRIGGER          ("Declare Trigger"          , true ),
    FLOW                     ("Flow"                     , true ),
    FREE_TEXT_MODEL          ("Free Text Model"          , true ),
    INTERACTION              ("Interaction"              , true ),
    LINKED_PAGE_HIT          ("Linked Page Hit"          , true ),
    LINKED_PAGE_MISS         ("Linked Page Miss"         , true ),
    LOCKING                  ("Locking"                  , true ),
    LOG_MESSAGES             ("Log Messages"             , true ),
    NAMED_TRANSACTIONS       ("Named Transactions"       , true ),
    PARSE_RULES              ("Parse Rules"              , true ),
    PREDICTIVE_MODEL         ("Predictive Model"         , true ),
    PROPOSITION_FILTER       ("Proposition Filter"       , true ),
    PUSH_NOTIFICATIONS       ("Push Notifications"       , true ),
    QUERY_RESOLUTION         ("Query Resolution"         , true ),
    QUEUE_PROCESSING         ("Queue Processing"         , true ),
    REFERENCE_PROPERTIES     ("Named Transactions"       , true ),
    RULE_COVERAGE            ("Rule Coverage"            , true ),
    SCORECARD                ("Scorecard"                , true ),
    SERVICE_MAPPING          ("Service Mapping"          , true ),
    SERVICES                 ("Services"                 , true ),
    SOAP_MESSAGES            ("SOAP Messages"            , true ),
    STRATEGY                 ("Strategy"                 , true ),
    STREAM_RULES             ("Stream Rules"             , true ),
    UNIT_TEST_CASE           ("Unit Test Case"           , true );
    // CHECKSTYLE:ON
    // @formatter:on

    private final String name;

    private final boolean eventType;

    private final String displayName;

    // for certain events like Activity, the type has 'Begin' or 'End' appended to it.
    private final String[] altNameList;

    private TraceEventType(String name, boolean type) {
        this(name, name, type);
    }

    private TraceEventType(String name, boolean type, String... altNames) {
        this(name, name, type, altNames);
    }

    private TraceEventType(String name, String displayName, boolean type, String... altNames) {
        this.name = name;
        this.eventType = type;
        this.displayName = displayName;
        this.altNameList = altNames;
    }

    public String getName() {
        return name;
    }

    public boolean isEventType() {
        return eventType;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return displayName;
    }

    // public static EnumSet<TraceEventType> getValidValues() {
    // EnumSet<TraceEventType> enumSet = EnumSet.range(ACCESS_DENY_RULES,
    // STREAM_RULES);
    //
    // return enumSet;
    // }

    public Comparator<TraceEventType> getComparator() {

        Comparator<TraceEventType> comparator = new Comparator<TraceEventType>() {

            @Override
            public int compare(TraceEventType o1, TraceEventType o2) {
                int compareTo = Boolean.valueOf(o1.isEventType()).compareTo(Boolean.valueOf(o2.isEventType()));

                if (compareTo != 0) {
                    return compareTo;
                }

                return o1.getName().compareTo(o2.getName());
            }
        };

        return comparator;
    }

    public boolean matchName(String eventType) {

        boolean matchFound = false;

        if (getName().equalsIgnoreCase(eventType)) {
            matchFound = true;
        } else {

            for (String altName : altNameList) {

                if (altName.equalsIgnoreCase(eventType)) {
                    matchFound = true;
                    break;
                }
            }
        }

        return matchFound;
    }
}
