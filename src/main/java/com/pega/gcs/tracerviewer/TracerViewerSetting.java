/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.io.Serializable;

public class TracerViewerSetting implements Serializable {

    private static final long serialVersionUID = 211297964995715046L;

    // force reset, in case any default value changes
    private static final int SETTING_VERSION = 1;

    // for kryo obj persistence
    private final int objVersion;

    private int recentItemsCount;

    private String charset;

    private boolean reloadPreviousFiles;

    public TracerViewerSetting() {

        super();

        this.objVersion = SETTING_VERSION;

        setDefault();
    }

    public static int getSettingVersion() {
        return SETTING_VERSION;
    }

    public int getObjVersion() {
        return objVersion;
    }

    public void setDefault() {
        recentItemsCount = 20;
        charset = "UTF-8";
        reloadPreviousFiles = false;
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
