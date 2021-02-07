/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.nio.charset.Charset;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.SearchTableModelEvent;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.search.SearchModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.AbstractTreeTableTreeModel;
import com.pega.gcs.fringecommon.guiutilities.treetable.DefaultTreeTableTree;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableModelAdapter;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public abstract class TraceTreeTableModelAdapter extends TreeTableModelAdapter {

    private static final long serialVersionUID = -4334926703683969380L;

    private TraceTableModel traceTableModel;

    public TraceTreeTableModelAdapter(DefaultTreeTableTree tree) {

        super(tree);

        this.traceTableModel = null;
    }

    public void setTraceTableModel(TraceTableModel traceTableModel) {

        this.traceTableModel = traceTableModel;
        this.traceTableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent tableModelEvent) {

                DefaultTreeTableTree defaultTreeTableTree = getTree();

                AbstractTreeTableTreeModel abstractTreeTableTreeModel;
                abstractTreeTableTreeModel = (AbstractTreeTableTreeModel) defaultTreeTableTree.getModel();

                // in case of search action, just refresh. reload causes
                // collapsing of tree.
                if (tableModelEvent instanceof SearchTableModelEvent) {
                    abstractTreeTableTreeModel.nodeChanged(getRoot());
                } else {
                    if (tableModelEvent.getType() == TableModelEvent.UPDATE) {
                        abstractTreeTableTreeModel.reload();
                    }
                }
            }
        });
    }

    public String getModelName() {
        return traceTableModel.getModelName();
    }

    public String getFilePath() {
        return traceTableModel.getFilePath();
    }

    public Charset getCharset() {
        return traceTableModel.getCharset();
    }

    public SearchModel<TraceEventKey> getSearchModel() {
        return traceTableModel.getSearchModel();
    }

    public BookmarkModel<TraceEventKey> getBookmarkModel() {

        return traceTableModel.getBookmarkModel();
    }

    public TraceEventColumn getTraceTableModelColumn(int column) {
        return traceTableModel.getColumn(column);
    }

}
