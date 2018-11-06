/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.io.Serializable;
import java.nio.charset.Charset;

public class TracerViewerSetting implements Serializable {

    private static final long serialVersionUID = 211297964995715046L;

    private int recentItemsCount;

    private String charset;

    private boolean reloadPreviousFiles;

    public TracerViewerSetting() {
        super();
        setDefault();
    }

    public void setDefault() {
        recentItemsCount = 10;
        charset = Charset.defaultCharset().name();
        reloadPreviousFiles = true;
    }

    public int getRecentItemsCount() {
        return recentItemsCount;
    }

    public void setRecentItemsCount(int recentItemsCount) {
        this.recentItemsCount = recentItemsCount;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isReloadPreviousFiles() {
        return reloadPreviousFiles;
    }

    public void setReloadPreviousFiles(boolean reloadPreviousFiles) {
        this.reloadPreviousFiles = reloadPreviousFiles;
    }
}
