/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.SearchTableModelEvent;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableModelAdapter;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTreeTableModelAdapter extends TreeTableModelAdapter {

	private static final long serialVersionUID = -4334926703683969380L;

	private TraceTableModel traceTableModel;

	public TraceTreeTableModelAdapter(DefaultTreeTableTree tree) {

		super(tree);

		this.traceTableModel = null;
	}

	/**
	 * @param traceTableModel
	 *            the traceTableModel to set
	 */
	public void setTraceTableModel(TraceTableModel traceTableModel) {

		this.traceTableModel = traceTableModel;
		this.traceTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				DefaultTreeTableTree defaultTreeTableTree = getTree();

				AbstractTreeTableTreeModel abstractTreeTableTreeModel;
				abstractTreeTableTreeModel = (AbstractTreeTableTreeModel) defaultTreeTableTree.getModel();

				// in case of search action, just refresh. reload causes
				// collapsing of tree.
				if (e instanceof SearchTableModelEvent) {
					abstractTreeTableTreeModel.nodeChanged(getRoot());
				} else {
					if (e.getType() == TableModelEvent.UPDATE) {
						abstractTreeTableTreeModel.reload();
					}
				}
			}
		});
	}

	public String getModelName() {
		return traceTableModel.getModelName();
	}

	public SearchModel<TraceEventKey> getSearchModel() {
		return traceTableModel.getSearchModel();
	}

	public BookmarkModel<TraceEventKey> getBookmarkModel() {

		return traceTableModel.getBookmarkModel();
	}

}
