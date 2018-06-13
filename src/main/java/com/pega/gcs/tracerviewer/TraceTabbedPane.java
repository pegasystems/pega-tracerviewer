/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import com.pega.gcs.fringecommon.guiutilities.ButtonTabComponent;
import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;

public class TraceTabbedPane extends JTabbedPane implements DropTargetListener {

    private static final long serialVersionUID = 8534656255850550268L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceTabbedPane.class);

    private TracerViewerSetting tracerViewerSetting;

    private RecentFileContainer recentFileContainer;

    private Map<String, Integer> fileTabIndexMap;

    private Border normalBorder;

    public TraceTabbedPane(TracerViewerSetting tracerViewerSetting, RecentFileContainer recentFileContainer) {
        super();

        this.tracerViewerSetting = tracerViewerSetting;
        this.recentFileContainer = recentFileContainer;

        fileTabIndexMap = new LinkedHashMap<String, Integer>();

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        normalBorder = getBorder();

        try {
            DropTarget dt = new DropTarget();
            dt.addDropTargetListener(this);
            setDropTarget(dt);
        } catch (TooManyListenersException e) {
            LOG.error("Error adding drag drop listener", e);
        }
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (isDragOk(dtde)) {

            setBorder(BorderFactory.createLineBorder(Color.RED));

            dtde.acceptDrag(DnDConstants.ACTION_NONE);
        } else {
            dtde.rejectDrag();
        }

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        if (isDragOk(dtde)) {
            dtde.acceptDrag(DnDConstants.ACTION_NONE);
        } else {
            dtde.rejectDrag();
        }

    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        setBorder(normalBorder);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

        try {
            Transferable tr = dtde.getTransferable();

            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

                dtde.acceptDrop(DnDConstants.ACTION_COPY);

                // Get a useful list
                @SuppressWarnings("unchecked")
                List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);

                for (File file : fileList) {
                    loadFile(file);
                }

                // Mark that drop is completed.
                dtde.getDropTargetContext().dropComplete(true);

            }

        } catch (Exception e) {
            LOG.error("Error in drop operation", e);
        } finally {
            // reset border
            setBorder(normalBorder);
        }

    }

    private boolean isDragOk(final DropTargetDragEvent evt) {

        boolean retValue = false;

        // Get data flavors being dragged
        DataFlavor[] dataFlavorArray = evt.getCurrentDataFlavors();

        for (int i = 0; i < dataFlavorArray.length; i++) {

            final DataFlavor dataFlavor = dataFlavorArray[i];

            if (dataFlavor.equals(DataFlavor.javaFileListFlavor) || dataFlavor.isRepresentationClassReader()) {
                retValue = true;
                break;
            }
        }

        return retValue;
    }

    private void addTab(File selectedFile, JPanel tabPanel) {

        String tabTitle = selectedFile.getName();

        addTab(tabTitle, null, tabPanel, selectedFile.getPath());

        int index = getTabCount() - 1;

        final ButtonTabComponent btc = new ButtonTabComponent(tabTitle, this);

        fileTabIndexMap.put(selectedFile.getPath(), index);

        setTabComponentAt(index, btc);
        setSelectedIndex(index);
    }

    @Override
    public void remove(int index) {
        super.remove(index);

        fileTabIndexMap.values().remove(index);

        int tabIndex = 0;

        for (String key : fileTabIndexMap.keySet()) {
            fileTabIndexMap.put(key, tabIndex);
            tabIndex++;
        }

        System.gc();

    }

    public void loadFile(final File selectedFile) throws Exception {

        Integer index = fileTabIndexMap.get(selectedFile.getPath());

        if (index != null) {

            setSelectedIndex(index);

        } else {

            TracerDataMainPanel tracerDataMainPanel = new TracerDataMainPanel(selectedFile, recentFileContainer,
                    tracerViewerSetting);
            
            addTab(selectedFile, tracerDataMainPanel);
        }

    }

    public ArrayList<String> getOpenFileList() {

        ArrayList<String> openFileList = new ArrayList<>(fileTabIndexMap.keySet());

        return openFileList;
    }
}
