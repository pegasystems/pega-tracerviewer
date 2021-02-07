/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

import java.util.ArrayList;
import java.util.List;

public enum TracerDataViewMode {

    // @formatter:off
    // CHECKSTYLE:OFF
    SINGLE_TABLE        ( "Table"                   ),
    SINGLE_TREE         ( "Tree"                    ),
    SINGLE_TREE_MERGED  ( "Tree(Start-End Merged)"  ),
    COMPARE_TABLE       ( "Compare"                 );
    // CHECKSTYLE:ON
    // @formatter:on

    private final String displaytext;

    private TracerDataViewMode(String displaytext) {
        this.displaytext = displaytext;
    }

    public String getDisplaytext() {
        return displaytext;
    }

    @Override
    public String toString() {
        return displaytext;
    }

    public static TracerDataViewMode[] getTracerDataViewModeList(boolean isMultipleDxApi) {

        List<TracerDataViewMode> tracerDataViewModeList = new ArrayList<>();

        if (isMultipleDxApi) {
            tracerDataViewModeList.add(SINGLE_TABLE);
            tracerDataViewModeList.add(SINGLE_TREE);
            tracerDataViewModeList.add(SINGLE_TREE_MERGED);
        } else {
            tracerDataViewModeList.add(SINGLE_TABLE);
            tracerDataViewModeList.add(SINGLE_TREE);
            tracerDataViewModeList.add(SINGLE_TREE_MERGED);
            tracerDataViewModeList.add(COMPARE_TABLE);
        }

        return tracerDataViewModeList.toArray(new TracerDataViewMode[tracerDataViewModeList.size()]);
    }
}
