/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import com.pega.gcs.fringecommon.guiutilities.NavigationController;

public interface NavigationPanelController<T> extends NavigationController<T> {

	public void first();

	public void previous();

	public void next();

	public void last();

	public void updateState();

}
