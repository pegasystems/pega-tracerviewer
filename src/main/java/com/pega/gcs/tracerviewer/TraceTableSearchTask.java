/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingWorker;

import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.ProgressTaskInfo;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceTableSearchTask extends SwingWorker<List<TraceEventKey>, ProgressTaskInfo> {

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceTableSearchTask.class);

    private TraceTableModel traceTableModel;

    private ModalProgressMonitor mProgressMonitor;

    private Object searchStrObj;

    public TraceTableSearchTask(ModalProgressMonitor mProgressMonitor, TraceTableModel traceTableModel,
            Object searchStrObj) {
        super();
        this.mProgressMonitor = mProgressMonitor;
        this.traceTableModel = traceTableModel;
        this.searchStrObj = searchStrObj;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected List<TraceEventKey> doInBackground() throws Exception {

        List<TraceEventKey> searchResultList = null;

        if (traceTableModel != null) {

            searchResultList = traceTableModel.getSearchModel().getSearchResultList(searchStrObj);

            if (searchResultList == null) {
                searchResultList = search();
            } else {
                update();
            }

            TraceEventTreeNode rootTraceEventTreeNode = traceTableModel.getRootTraceEventTreeNode();
            TraceEventCombinedTreeNode rootMergedTraceEventTreeNode = traceTableModel
                    .getRootTraceEventCombinedTreeNode();

            updateTraceEventTreeNode(rootTraceEventTreeNode, searchResultList);
            updateMergedTraceEventTreeNode(rootMergedTraceEventTreeNode, searchResultList);
        }

        // Collections.reverse(searchResultList);
        return searchResultList;
    }

    private List<TraceEventKey> search() {

        List<TraceEventKey> searchResultList = new ArrayList<TraceEventKey>();

        long before = System.currentTimeMillis();

        try {

            int traceEventCount = 0;

            List<TraceEventKey> filteredList = traceTableModel.getFtmEntryKeyList();

            int totalSize = filteredList.size();

            Iterator<TraceEventKey> fListIterator = filteredList.iterator();

            while ((!isCancelled()) && (fListIterator.hasNext())) {

                if (mProgressMonitor.isCanceled()) {
                    cancel(true);
                }

                TraceEventKey key = fListIterator.next();

                boolean found = traceTableModel.search(key, searchStrObj);

                if (found) {
                    searchResultList.add(key);
                }

                traceEventCount++;

                ProgressTaskInfo progressTaskInfo = new ProgressTaskInfo(totalSize, traceEventCount);
                publish(progressTaskInfo);

            }

        } finally {
            long diff = System.currentTimeMillis() - before;

            int secs = (int) Math.ceil((double) diff / 1E3);

            LOG.info("Search '" + searchStrObj + "' completed in " + secs + " secs. " + searchResultList.size()
                    + " results found.");
        }

        return searchResultList;
    }

    private void update() {

        long before = System.currentTimeMillis();

        List<TraceEventKey> searchResultList = traceTableModel.getSearchModel().getSearchResultList(searchStrObj);

        try {

            int traceEventCount = 0;

            List<TraceEventKey> filteredList = traceTableModel.getFtmEntryKeyList();

            int totalSize = filteredList.size();

            Iterator<TraceEventKey> fListIterator = filteredList.iterator();

            while ((!isCancelled()) && (fListIterator.hasNext())) {

                if (mProgressMonitor.isCanceled()) {
                    cancel(true);
                }

                TraceEventKey key = fListIterator.next();

                int index = Collections.binarySearch(searchResultList, key);

                boolean searchFound = false;

                if (index >= 0) {
                    searchFound = true;
                }

                TraceEvent traceEvent = traceTableModel.getEventForKey(key);
                traceEvent.setSearchFound(searchFound);

                traceEventCount++;

                ProgressTaskInfo progressTaskInfo = new ProgressTaskInfo(totalSize, traceEventCount);
                publish(progressTaskInfo);

            }

        } finally {
            long diff = System.currentTimeMillis() - before;

            int secs = (int) Math.ceil((double) diff / 1E3);

            LOG.info("Search updated '" + searchStrObj + "' completed in " + secs + " secs. " + searchResultList.size()
                    + " results found.");
        }
    }

    private boolean updateTraceEventTreeNode(AbstractTraceEventTreeNode treeNode,
            List<TraceEventKey> searchResultList) {

        boolean searchFound = false;

        if (!isCancelled()) {

            Object userObject = treeNode.getUserObject();

            if ((userObject != null) && (userObject instanceof TraceEvent)) {

                TraceEvent traceEvent = (TraceEvent) userObject;

                searchFound = traceEvent.isSearchFound();

            }

            for (Enumeration<?> e = treeNode.children(); e.hasMoreElements();) {

                AbstractTraceEventTreeNode childNode = (AbstractTraceEventTreeNode) e.nextElement();

                boolean childSearchFound = false;

                childSearchFound = updateTraceEventTreeNode(childNode, searchResultList);

                if ((!searchFound) && childSearchFound) {
                    searchFound = true;
                }
            }

            treeNode.setSearchFound(searchFound);
        }

        return searchFound;
    }

    private boolean updateMergedTraceEventTreeNode(TraceEventCombinedTreeNode treeNode,
            List<TraceEventKey> searchResultList) {

        boolean searchFound = false;

        if (!isCancelled()) {

            TraceEvent startEvent = treeNode.getStartEvent();
            TraceEvent endEvent = treeNode.getEndEvent();

            if (startEvent != null) {
                searchFound = startEvent.isSearchFound();
            }

            if (endEvent != null) {
                searchFound = searchFound || endEvent.isSearchFound();
            }

            for (Enumeration<?> e = treeNode.children(); e.hasMoreElements();) {

                TraceEventCombinedTreeNode childNode = (TraceEventCombinedTreeNode) e.nextElement();

                boolean childSearchFound = false;

                childSearchFound = updateMergedTraceEventTreeNode(childNode, searchResultList);

                if ((!searchFound) && childSearchFound) {
                    searchFound = true;
                }
            }

            treeNode.setSearchFound(searchFound);
        }

        return searchFound;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process(List<ProgressTaskInfo> chunks) {
        if ((isDone()) || (isCancelled()) || (chunks == null) || (chunks.size() == 0)) {
            return;
        }

        Collections.sort(chunks);

        ProgressTaskInfo progressTaskInfo = chunks.get(chunks.size() - 1);

        long total = progressTaskInfo.getTotal();
        long count = progressTaskInfo.getCount();

        int progress = (int) ((count * 100) / total);

        mProgressMonitor.setProgress(progress);

        String message = String.format("Searching %d trace events (%d%%)", count, progress);

        mProgressMonitor.setNote(message);
    }
}
