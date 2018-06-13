/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

public enum SearchEventType {

    //@formatter:off
    STATUS_EXCEPTION("Status - Exception"),
    STATUS_FAIL("Status - Fail"),
    STATUS_WARN("Status - Warn"),
    PAGE_MESSAGES("Page Messages"),
    SEPERATOR("--------------------------------");
    //@formatter:on

    private String name;

    /**
     * @param name
     */
    private SearchEventType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
