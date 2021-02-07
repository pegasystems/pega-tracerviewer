/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TraceEventTreeNodeXMLExportTask extends SwingWorker<Boolean, Integer> {

    private TraceTreeTable traceTreeTable;

    private List<Integer> selectedRowList;

    private File exportFile;

    private ModalProgressMonitor modalProgressMonitor;

    public TraceEventTreeNodeXMLExportTask(TraceTreeTable traceTreeTable, List<Integer> selectedRowList,
            File exportFile, ModalProgressMonitor modalProgressMonitor) {
        super();
        this.traceTreeTable = traceTreeTable;
        this.selectedRowList = selectedRowList;
        this.exportFile = exportFile;
        this.modalProgressMonitor = modalProgressMonitor;
    }

    @Override
    protected Boolean doInBackground() throws Exception {

        TraceTreeTableModelAdapter traceTreeTableModelAdapter = (TraceTreeTableModelAdapter) traceTreeTable.getModel();

        Charset charset = traceTreeTableModelAdapter.getCharset();

        Map<TraceEventKey, Element> elementMap = new TreeMap<>();

        for (int selectedRow : selectedRowList) {

            if (modalProgressMonitor.isCanceled()) {
                break;
            }

            AbstractTraceEventTreeNode abstractTraceEventTreeNode = (AbstractTraceEventTreeNode) traceTreeTableModelAdapter
                    .getValueAt(selectedRow, 0);

            getElementsFromNodeHierarchy(abstractTraceEventTreeNode, elementMap, charset);

            if (modalProgressMonitor.isCanceled()) {
                break;
            }

            // In case of simple tree mode, check if the selected node is a begin event,
            // then include the end event node to complete the tree.
            if (abstractTraceEventTreeNode instanceof TraceEventTreeNode) {

                TraceEventTreeNode traceEventTreeNode = (TraceEventTreeNode) abstractTraceEventTreeNode;

                TraceEvent traceEvent = (TraceEvent) traceEventTreeNode.getUserObject();

                if (traceEvent != null) {

                    List<TraceEvent> traceEventList = new ArrayList<>();

                    Boolean isEndEvent = traceEvent.isEndEvent();

                    if ((isEndEvent != null) && isEndEvent) {
                        // end event - include the corresponding begin event
                        TraceEventTreeNode beginTraceEventTreeNode = TracerViewerUtil
                                .getBeginTraceEventTreeNode(traceEventTreeNode);

                        if (beginTraceEventTreeNode != null) {
                            traceEventList = beginTraceEventTreeNode.getTraceEvents();
                        }

                    } else if (isEndEvent != null) {
                        // begin event - include the corresponding end event

                        TraceEventTreeNode endTraceEventTreeNode = TracerViewerUtil
                                .getEndTraceEventTreeNode(traceEventTreeNode);

                        if (endTraceEventTreeNode != null) {
                            traceEventList = endTraceEventTreeNode.getTraceEvents();
                        }
                    }

                    for (TraceEvent te : traceEventList) {

                        TraceEventKey traceEventKey = te.getKey();

                        if (!elementMap.containsKey(traceEventKey)) {
                            Element element = te.getTraceEventRootElement(charset);
                            elementMap.put(traceEventKey, element);
                        }
                    }

                }
            }
        }

        if (!modalProgressMonitor.isCanceled()) {

            int counter = 0;
            int totalSize = elementMap.size();
            double increment = totalSize / 100d;

            int batchSize = 4194304; // 4096KB
            StringBuilder outputStrBatch = new StringBuilder();
            boolean append = false;

            modalProgressMonitor.setIndeterminate(false);

            for (Entry<TraceEventKey, Element> entry : elementMap.entrySet()) {

                if (modalProgressMonitor.isCanceled()) {
                    break;
                }

                Element element = entry.getValue();

                String xmlStr = GeneralUtilities.getElementAsXML(element);

                outputStrBatch.append(xmlStr);

                int accumulatedSize = outputStrBatch.length();

                if (accumulatedSize > batchSize) {

                    FileUtils.writeStringToFile(exportFile, outputStrBatch.toString(), charset, append);

                    outputStrBatch = new StringBuilder();

                    if (!append) {
                        append = true;
                    }

                    int progress = (int) Math.round(counter / increment);

                    publish((int) progress);
                }
            }

            if (!modalProgressMonitor.isCanceled()) {

                int accumulatedSize = outputStrBatch.length();

                if (accumulatedSize > 0) {
                    FileUtils.writeStringToFile(exportFile, outputStrBatch.toString(), charset, append);
                }

                int progress = (int) Math.round(counter / increment);

                publish((int) progress);

            }
        }

        Boolean returnValue = Boolean.TRUE;

        if (modalProgressMonitor.isCanceled()) {
            returnValue = Boolean.FALSE;
        }

        return returnValue;
    }

    private void getElementsFromNodeHierarchy(AbstractTraceEventTreeNode abstractTraceEventTreeNode,
            Map<TraceEventKey, Element> elementMap, Charset charset) {

        if (!modalProgressMonitor.isCanceled()) {

            List<TraceEvent> traceEventList = abstractTraceEventTreeNode.getTraceEvents();

            for (TraceEvent traceEvent : traceEventList) {

                TraceEventKey traceEventKey = traceEvent.getKey();
                Element element = traceEvent.getTraceEventRootElement(charset);
                elementMap.put(traceEventKey, element);
            }

            if (abstractTraceEventTreeNode.getChildCount() > 0) {

                for (Enumeration<?> e = abstractTraceEventTreeNode.children(); e.hasMoreElements();) {
                    AbstractTraceEventTreeNode childNode = (AbstractTraceEventTreeNode) e.nextElement();

                    getElementsFromNodeHierarchy(childNode, elementMap, charset);
                }
            }
        }
    }

    @Override
    protected void process(List<Integer> chunks) {

        if ((isDone()) || (isCancelled()) || (chunks == null) || (chunks.size() == 0)) {
            return;
        }

        boolean indeterminate = modalProgressMonitor.isIndeterminate();

        if (!indeterminate) {

            Collections.sort(chunks);

            Integer progress = chunks.get(chunks.size() - 1);

            String message = String.format("Exporting (%d%%)", progress);

            modalProgressMonitor.setProgress(progress);
            modalProgressMonitor.setNote(message);
        }
    }

}
