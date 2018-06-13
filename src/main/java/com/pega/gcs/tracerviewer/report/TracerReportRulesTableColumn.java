/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
/**
 * 
 */
package com.pega.gcs.tracerviewer.report;

import javax.swing.SwingConstants;

public enum TracerReportRulesTableColumn {

    // @formatter:off
    SNO                 ("S No"                    , 30  , SwingConstants.CENTER , false ),
    RULESET             ("Ruleset"                , 100 , SwingConstants.LEFT   , true  ),
    RULESET_VERSION     ("Ruleset Version"      , 60  , SwingConstants.CENTER , true  ),
    RULE_TYPE           ("Rule Type"            , 100 , SwingConstants.LEFT   , true  ),
    INSKEY              ("Instance Key"         , 500 , SwingConstants.LEFT   , true  ),
    EXECUTION_COUNT     ("No of Executions"     , 60  , SwingConstants.CENTER , false ),
    MAX_OWN_ELAPSED     ("Max Own Elapsed"      , 60  , SwingConstants.CENTER , false ),
    MIN_OWN_ELAPSED     ("Min Own Elapsed"      , 60  , SwingConstants.CENTER , false ),
    TOTAL_OWN_ELAPSED   ("Total Own Elapsed"    , 60  , SwingConstants.CENTER , false );
    // @formatter:on

    private String displayName;

    private int prefColumnWidth;

    private int horizontalAlignment;

    private boolean filterable;

    private TracerReportRulesTableColumn(String displayName, int prefColumnWidth, int horizontalAlignment, boolean filterable) {
        this.displayName = displayName;
        this.prefColumnWidth = prefColumnWidth;
        this.horizontalAlignment = horizontalAlignment;
        this.filterable = filterable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPrefColumnWidth() {
        return prefColumnWidth;
    }

    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public boolean isFilterable() {
        return filterable;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
