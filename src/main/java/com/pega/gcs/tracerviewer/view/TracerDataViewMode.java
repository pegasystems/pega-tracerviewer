/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer.view;

public enum TracerDataViewMode {

    // @formatter:off
    SINGLE_TABLE("Table"),
    SINGLE_TREE("Tree"),
    SINGLE_TREE_MERGED("Tree(Start-End Merged)"),
    COMPARE_TABLE("Compare");
    // @formatter:on

    private String displaytext;

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
}
