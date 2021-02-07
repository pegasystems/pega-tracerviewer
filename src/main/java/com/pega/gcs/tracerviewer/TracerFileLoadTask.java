/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.dom4j.io.SAXReader;

import com.pega.gcs.fringecommon.guiutilities.EventReadTaskInfo;
import com.pega.gcs.fringecommon.guiutilities.FileReadByteArray;
import com.pega.gcs.fringecommon.guiutilities.FileReadTaskInfo;
import com.pega.gcs.fringecommon.guiutilities.FileReaderThread;
import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.Message.MessageType;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.ReadCounterTaskInfo;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkContainer;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.KnuthMorrisPrattAlgorithm;
import com.pega.gcs.tracerviewer.model.TraceEvent;
import com.pega.gcs.tracerviewer.model.TraceEventEmpty;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class TracerFileLoadTask extends SwingWorker<Void, ReadCounterTaskInfo> {

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerFileLoadTask.class);

    private static final String TRACEEVENT_START = "<TraceEvent";

    private static final String TRACEEVENT_END = "</TraceEvent>";

    private boolean wait;

    private Component parent;

    private ModalProgressMonitor modalProgressMonitor;

    private TracerType tracerType;

    private TraceTableModel traceTableModel;

    private int processedCount;

    private int errorCount;

    public TracerFileLoadTask(ModalProgressMonitor modalProgressMonitor, TraceTableModel traceTableModel, boolean wait,
            Component parent) {

        this.modalProgressMonitor = modalProgressMonitor;
        this.traceTableModel = traceTableModel;
        this.wait = wait;
        this.parent = parent;

        tracerType = null;
        processedCount = 0;
        errorCount = 0;

    }

    public int getProcessedCount() {
        return processedCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Void doInBackground() throws Exception {

        long before = System.currentTimeMillis();
        int readCounter = 0;
        int traceEventIndex = 0;
        Charset charset = traceTableModel.getCharset();

        String filePath = traceTableModel.getFilePath();

        File tracerFile = new File(filePath);

        LOG.info("TracerFileLoadTask - Using Charset: " + charset);
        LOG.info("TracerFileLoadTask - Loading file: " + tracerFile);

        FileReadTaskInfo fileReadTaskInfo = new FileReadTaskInfo(0, 0);
        EventReadTaskInfo eventReadTaskInfo = new EventReadTaskInfo(0, 0);

        ReadCounterTaskInfo readCounterTaskInfo = new ReadCounterTaskInfo(fileReadTaskInfo);
        readCounterTaskInfo.setEventReadTaskInfo(eventReadTaskInfo);

        publish(readCounterTaskInfo);

        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding(charset.name());

        AtomicBoolean cancel = new AtomicBoolean(false);
        FileReaderThread frt;

        int fileReadQueueCapacity = 5;

        LinkedBlockingQueue<FileReadByteArray> fileReadQueue;
        fileReadQueue = new LinkedBlockingQueue<FileReadByteArray>(fileReadQueueCapacity);

        AtomicLong fileSize = new AtomicLong(-1);

        try {

            byte[] startTraceEventStr = TRACEEVENT_START.getBytes(charset);
            byte[] endTraceEventStr = TRACEEVENT_END.getBytes(charset);

            long totalread = 0;
            byte[] balanceByteArray = new byte[0];

            boolean first = true;

            // default is no tail
            frt = new FileReaderThread(tracerFile, fileSize, fileReadQueue, cancel);
            Thread frtThread = new Thread(frt);
            frtThread.start();

            while (!isCancelled()) {

                if (modalProgressMonitor.isCanceled()) {
                    cancel.set(true);
                    cancel(true);
                    break;
                }

                FileReadByteArray frba = null;

                try {
                    frba = fileReadQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException ie) {
                    // ignore InterruptedException
                }

                if (frba != null) {

                    byte[] byteBuffer = frba.getBytes();
                    int readLen = byteBuffer.length;

                    totalread = totalread + readLen;

                    readCounter++;

                    int startidx = 0;
                    int index = -1;

                    // copy the balance byte array
                    int balanceByteArrayLength = balanceByteArray.length;

                    if (balanceByteArrayLength > 0) {

                        int newSize = readLen + balanceByteArrayLength;

                        byte[] byteBufferNew = new byte[newSize];

                        // copy the balance to the beginning of new array.
                        System.arraycopy(balanceByteArray, 0, byteBufferNew, 0, balanceByteArrayLength);

                        // copy the newly read byteBuffer to the remaining space
                        System.arraycopy(byteBuffer, 0, byteBufferNew, balanceByteArrayLength, readLen);

                        byteBuffer = byteBufferNew;

                        // once copied, reset the balance array
                        balanceByteArray = new byte[0];

                    }

                    startidx = KnuthMorrisPrattAlgorithm.indexOf(byteBuffer, startTraceEventStr);

                    if (startidx == -1) {

                        if (first) {
                            first = false;
                            LOG.info("startSeek is -1. exiting..");
                            break;
                        }

                        startidx = 0;
                    }

                    index = KnuthMorrisPrattAlgorithm.indexOfWithPatternLength(byteBuffer, endTraceEventStr, startidx);

                    if (index != -1) {

                        while ((!isCancelled()) && (index != -1)) {

                            int teseRead = index - startidx;

                            byte[] teseByteBuffer = new byte[teseRead];

                            System.arraycopy(byteBuffer, startidx, teseByteBuffer, 0, teseRead);

                            TraceEvent traceEvent = TraceEventFactory.getTraceEvent(traceEventIndex, teseByteBuffer,
                                    saxReader);

                            if ((tracerType == null) && (traceEvent != null)) {

                                String dxApiInteractionId = traceEvent.getDxApiInteractionId();

                                if ((dxApiInteractionId != null) && (!"".equals(dxApiInteractionId))) {
                                    tracerType = TracerType.DX_API;
                                } else {
                                    tracerType = TracerType.NORMAL;
                                }

                                traceTableModel.setTracerType(tracerType);
                            }

                            if (traceEvent == null) {

                                errorCount++;

                                LOG.info("Error reading trace event id: " + traceEventIndex);

                                TraceEventKey traceEventKey = new TraceEventKey(traceEventIndex, -1, true);
                                traceEvent = new TraceEventEmpty(traceEventKey, Color.WHITE);
                            }

                            traceTableModel.addTraceEventToMap(traceEvent);

                            traceEventIndex++;

                            // to handle eventxxxx.bin files, every serialised trace event entry is preceded
                            // with some bytes of trace event length data. hence need to recalculate the
                            // begin position.
                            // startidx = index;
                            startidx = KnuthMorrisPrattAlgorithm.indexOf(byteBuffer, startTraceEventStr, index);

                            // cant find the start event hence set the startidx as last read position
                            if (startidx == -1) {
                                startidx = index;
                            }

                            index = KnuthMorrisPrattAlgorithm.indexOfWithPatternLength(byteBuffer, endTraceEventStr,
                                    startidx);
                        }
                    }

                    int byteBufferlen = byteBuffer.length;
                    int balance = byteBufferlen - startidx;

                    balanceByteArray = new byte[balance];

                    System.arraycopy(byteBuffer, startidx, balanceByteArray, 0, balance);

                    fileReadTaskInfo = new FileReadTaskInfo(fileSize.get(), totalread);
                    eventReadTaskInfo = new EventReadTaskInfo(traceEventIndex, -1);

                    readCounterTaskInfo = new ReadCounterTaskInfo(fileReadTaskInfo);
                    readCounterTaskInfo.setEventReadTaskInfo(eventReadTaskInfo);

                    publish(readCounterTaskInfo);

                } else {

                    if (!frtThread.isAlive()) {

                        LOG.info("File reader Thread finished. Breaking TracerFileLoadTask");

                        // file reader finished. exit this.
                        break;
                    }
                }
            }

            if (isCancelled() || (!frtThread.isAlive())) {
                // handle cancel operation
                cancel.set(true);
            }

        } finally {

            processedCount = traceEventIndex;

            Message.MessageType messageType = MessageType.INFO;

            StringBuilder messageB = new StringBuilder();
            messageB.append(tracerFile.getAbsolutePath());
            messageB.append(". ");

            String text = "Processed " + processedCount + " trace events.";
            messageB.append(text);

            if (errorCount > 0) {
                text = errorCount + " Error" + (errorCount > 1 ? "s" : "") + " while loading log file";

                messageType = MessageType.ERROR;
                messageB.append(text);

            }

            Message message = new Message(messageType, messageB.toString());
            traceTableModel.setMessage(message);

            // traceTableModel.fireTableDataChanged();

            long diff = System.currentTimeMillis() - before;

            int secs = (int) Math.ceil((double) diff / 1E3);

            double avg = (diff / readCounter);

            LOG.info("Processed " + processedCount + " trace events in " + secs + " secs Average: " + avg
                    + " FileReadByteArray blocks: " + readCounter);

        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process(List<ReadCounterTaskInfo> chunks) {

        if ((isDone()) || (isCancelled()) || (chunks == null) || (chunks.size() == 0)) {
            return;
        }

        Collections.sort(chunks);

        ReadCounterTaskInfo readCounterTaskInfo = chunks.get(chunks.size() - 1);

        FileReadTaskInfo fileReadTaskInfo = readCounterTaskInfo.getFileReadTaskInfo();
        EventReadTaskInfo eventReadTaskInfo = readCounterTaskInfo.getEventReadTaskInfo();

        long fileSize = fileReadTaskInfo.getFileSize();
        long fileRead = fileReadTaskInfo.getFileRead();

        long eventCount = eventReadTaskInfo.getEventCount();

        int progress = 0;

        if (fileSize > 0) {
            progress = (int) ((fileRead * 100) / fileSize);
        }

        modalProgressMonitor.setProgress(progress);

        String message = String.format("Loaded %d trace events (%d%%)", eventCount, progress);

        modalProgressMonitor.setNote(message);
        // disabled
        // statusProgressBar.setText(message);

    }

    @Override
    protected void done() {
        if (!wait) {
            completeLoad();
        }
    }

    public void completeTask() {

        if (wait) {
            completeLoad();
        }
    }

    @SuppressWarnings("unchecked")
    private void completeLoad() {

        String filePath = traceTableModel.getFilePath();

        try {

            get();

            System.gc();

            int processedCount = getProcessedCount();

            RecentFile recentFile = traceTableModel.getRecentFile();

            BookmarkContainer<TraceEventKey> bookmarkContainer;
            bookmarkContainer = (BookmarkContainer<TraceEventKey>) recentFile.getAttribute(RecentFile.KEY_BOOKMARK);

            if (bookmarkContainer == null) {

                bookmarkContainer = new BookmarkContainer<TraceEventKey>();

                recentFile.setAttribute(RecentFile.KEY_BOOKMARK, bookmarkContainer);
            }

            BookmarkModel<TraceEventKey> bookmarkModel = new BookmarkModel<TraceEventKey>(bookmarkContainer,
                    traceTableModel);

            traceTableModel.setBookmarkModel(bookmarkModel);

            traceTableModel.fireTableDataChanged();

            LOG.info("TracerFileLoadTask - Done: " + filePath + " processedCount:" + processedCount);

        } catch (CancellationException ce) {

            LOG.error("TracerFileLoadTask - Cancelled " + filePath);

            MessageType messageType = MessageType.ERROR;
            Message modelmessage = new Message(messageType, filePath + " - file loading cancelled.");
            traceTableModel.setMessage(modelmessage);

        } catch (ExecutionException ee) {

            LOG.error("Execution Error during TracerFileLoadTask", ee);

            String message = null;

            if (ee.getCause() instanceof OutOfMemoryError) {

                message = "Out Of Memory Error has occured while loading " + filePath
                        + ".\nPlease increase the JVM's max heap size (-Xmx) and try again.";

                JOptionPane.showMessageDialog(parent, message, "Out Of Memory Error", JOptionPane.ERROR_MESSAGE);
            } else {
                message = ee.getCause().getMessage() + " has occured while loading " + filePath + ".";

                JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
            }

            MessageType messageType = MessageType.ERROR;
            Message modelmessage = new Message(messageType, message);
            traceTableModel.setMessage(modelmessage);

        } catch (Exception e) {
            LOG.error("Error loading file: " + filePath, e);
            MessageType messageType = MessageType.ERROR;

            StringBuilder messageB = new StringBuilder();
            messageB.append("Error loading file: ");
            messageB.append(filePath);

            Message message = new Message(messageType, messageB.toString());
            traceTableModel.setMessage(message);

        } finally {

            if (!modalProgressMonitor.isCanceled()) {
                modalProgressMonitor.close();
            }

            System.gc();
        }
    }
}
