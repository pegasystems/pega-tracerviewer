/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.pega.gcs.fringecommon.guiutilities.CheckBoxLabelMenuItem;
import com.pega.gcs.fringecommon.guiutilities.CheckBoxMenuItemPopupEntry;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.model.TraceEventType;

public abstract class TraceEventTypePopupMenu extends JPopupMenu {

    private static final long serialVersionUID = 3225142996534047809L;

    public abstract void applyJButtonAction();

    private TraceTableModel traceTableModel;

    private List<CheckBoxLabelMenuItem<TraceEventKey>> checkBoxLabelMenuItemList;

    private JPanel checkBoxLabelMenuItemListJPanel;

    public TraceEventTypePopupMenu(TraceTableModel traceTableModel) {
        super();

        this.traceTableModel = traceTableModel;

        this.checkBoxLabelMenuItemList = new ArrayList<CheckBoxLabelMenuItem<TraceEventKey>>();
        this.checkBoxLabelMenuItemListJPanel = null;

        setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(2, 0, 2, 0);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(2, 0, 2, 0);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 2;
        gbc3.weightx = 1.0D;
        gbc3.weighty = 0.0D;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.anchor = GridBagConstraints.NORTHWEST;
        gbc3.insets = new Insets(2, 0, 2, 0);

        JPanel clearAllButtonJPanel = getClearAllButtonJPanel();
        JComponent checkBoxLabelMenuItemListJComponent = getCheckBoxLabelMenuItemListJComponent();
        JPanel applyCancelButtonJPanel = getApplyCancelButtonJPanel();

        add(clearAllButtonJPanel, gbc1);
        add(checkBoxLabelMenuItemListJComponent, gbc2);
        add(applyCancelButtonJPanel, gbc3);

    }

    private JPanel getClearAllButtonJPanel() {

        JPanel clearAllButtonJPanel = new JPanel();

        clearAllButtonJPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        // gbc1.weightx = 0.0D;
        // gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.CENTER;
        gbc1.insets = new Insets(0, 0, 0, 3);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        // gbc2.weightx = 0.0D;
        // gbc2.weighty = 0.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.CENTER;
        gbc2.insets = new Insets(0, 3, 0, 0);

        JButton selectAllJButton = getSelectAllJButton();
        JButton clearAllJButton = getClearAllJButton();

        clearAllButtonJPanel.add(selectAllJButton, gbc1);
        clearAllButtonJPanel.add(clearAllJButton, gbc2);

        return clearAllButtonJPanel;
    }

    private JButton getSelectAllJButton() {
        JButton selectAllJButton = new JButton("Select All");

        Dimension size = new Dimension(80, 20);
        selectAllJButton.setPreferredSize(size);
        selectAllJButton.setMinimumSize(size);
        selectAllJButton.setMaximumSize(size);

        selectAllJButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                applySelectAll();
            }
        });

        return selectAllJButton;
    }

    private JButton getClearAllJButton() {
        JButton clearAllJButton = new JButton("Clear All");

        Dimension size = new Dimension(80, 20);
        clearAllJButton.setPreferredSize(size);
        clearAllJButton.setMinimumSize(size);
        clearAllJButton.setMaximumSize(size);

        clearAllJButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                // applyColumnHeaderFilter(columnIndex, true);
                // clearAllJButtonAction();
                //
                // setVisible(false);
                applyClearAll();
            }
        });

        return clearAllJButton;
    }

    public JComponent getCheckBoxLabelMenuItemListJComponent() {

        JPanel checkBoxLabelMenuItemListJPanel = getCheckBoxLabelMenuItemListJPanel();

        JScrollPane jscrollPane = new JScrollPane(checkBoxLabelMenuItemListJPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        int vbarWidth = jscrollPane.getVerticalScrollBar().getPreferredSize().width;
        int hbarHeight = jscrollPane.getHorizontalScrollBar().getPreferredSize().height + 3/* border size */;

        int compWidth = checkBoxLabelMenuItemListJPanel.getPreferredSize().width;
        int compHeight = checkBoxLabelMenuItemListJPanel.getPreferredSize().height;

        int newCompWidth = compWidth + vbarWidth;
        int newCompHeight = compHeight + hbarHeight;

        Dimension newDim = new Dimension(newCompWidth, newCompHeight);

        jscrollPane.setPreferredSize(newDim);

        jscrollPane.getVerticalScrollBar().setUnitIncrement(14);

        return jscrollPane;
    }

    private JPanel getCheckBoxLabelMenuItemListJPanel() {

        if (checkBoxLabelMenuItemListJPanel == null) {

            checkBoxLabelMenuItemListJPanel = new JPanel();
            checkBoxLabelMenuItemListJPanel.setLayout(new GridBagLayout());

            populateCheckBoxLabelMenuItemListJPanel();

        }

        return checkBoxLabelMenuItemListJPanel;
    }

    private void populateCheckBoxLabelMenuItemListJPanel() {

        JPanel checkBoxLabelMenuItemListJPanel = getCheckBoxLabelMenuItemListJPanel();

        checkBoxLabelMenuItemList.clear();
        checkBoxLabelMenuItemListJPanel.removeAll();

        JPanel traceEventTypesJPanel = new JPanel();
        traceEventTypesJPanel.setLayout(new GridBagLayout());

        JPanel traceEventJPanel = new JPanel();
        traceEventJPanel.setLayout(new GridBagLayout());

        int eventIndex = 0;
        int eventTypeIndex = 0;

        for (TraceEventType traceEventType : traceTableModel.getTraceEventTypeList()) {

            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = traceTableModel.getCheckBoxMenuItem(traceEventType);

            if (cbmipe.isVisible()) {

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.0D;
                gbc.fill = GridBagConstraints.BOTH;
                gbc.anchor = GridBagConstraints.NORTHWEST;
                gbc.insets = new Insets(0, 0, 0, 0);

                CheckBoxLabelMenuItem<TraceEventKey> cblmi;
                cblmi = new CheckBoxLabelMenuItem<TraceEventKey>(cbmipe, true);

                checkBoxLabelMenuItemList.add(cblmi);

                if (traceEventType.isEventType()) {
                    gbc.gridy = eventTypeIndex;
                    traceEventTypesJPanel.add(cblmi, gbc);
                    eventTypeIndex++;
                } else {
                    gbc.gridy = eventIndex;
                    traceEventJPanel.add(cblmi, gbc);
                    eventIndex++;
                }
            }
        }

        Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

        traceEventJPanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Events"));

        traceEventTypesJPanel.setBorder(BorderFactory.createTitledBorder(loweredetched, "Event Types"));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 1.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(0, 0, 0, 0);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(0, 0, 0, 0);

        checkBoxLabelMenuItemListJPanel.add(traceEventJPanel, gbc1);
        checkBoxLabelMenuItemListJPanel.add(traceEventTypesJPanel, gbc2);

        revalidate();
        repaint();
    }

    private JPanel getApplyCancelButtonJPanel() {

        JPanel applyCancelButtonJPanel = new JPanel();

        applyCancelButtonJPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 0.0D;
        gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.CENTER;
        gbc1.insets = new Insets(0, 10, 0, 10);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 0.0D;
        gbc2.weighty = 0.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.CENTER;
        gbc2.insets = new Insets(0, 10, 0, 10);

        JButton applyJButton = getApplyJButton();
        JButton cancelJButton = getCancelJButton();

        applyCancelButtonJPanel.add(applyJButton, gbc1);

        applyCancelButtonJPanel.add(cancelJButton, gbc2);

        return applyCancelButtonJPanel;
    }

    private JButton getApplyJButton() {

        JButton applyJButton = new JButton("Apply");

        Dimension size = new Dimension(70, 20);
        applyJButton.setPreferredSize(size);
        applyJButton.setMinimumSize(size);
        applyJButton.setMaximumSize(size);

        applyJButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                // applyColumnHeaderFilter(columnIndex, false);
                // applyJButtonAction();
                apply();
                setVisible(false);
            }
        });

        return applyJButton;
    }

    private JButton getCancelJButton() {

        JButton cancelJButton = new JButton("Cancel");

        Dimension size = new Dimension(70, 20);
        cancelJButton.setPreferredSize(size);
        cancelJButton.setMinimumSize(size);
        cancelJButton.setMaximumSize(size);

        cancelJButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });

        return cancelJButton;
    }

    protected void applySelectAll() {

        for (TraceEventType traceEventType : traceTableModel.getTraceEventTypeList()) {

            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = traceTableModel.getCheckBoxMenuItem(traceEventType);

            cbmipe.setSelected(true);
        }

        populateCheckBoxLabelMenuItemListJPanel();
    }

    protected void applyClearAll() {

        for (TraceEventType traceEventType : traceTableModel.getTraceEventTypeList()) {

            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = traceTableModel.getCheckBoxMenuItem(traceEventType);

            cbmipe.setSelected(false);
        }

        populateCheckBoxLabelMenuItemListJPanel();
    }

    protected void apply() {

        // Dissociating the checkbox action with the underlying object. the
        // action with be completed when user confirms
        for (CheckBoxLabelMenuItem<TraceEventKey> cblmi : checkBoxLabelMenuItemList) {
            CheckBoxMenuItemPopupEntry<TraceEventKey> fthcEntry;

            fthcEntry = (CheckBoxMenuItemPopupEntry<TraceEventKey>) cblmi.getFilterTableHeaderPopupEntry();

            boolean selected = cblmi.isSelected();
            fthcEntry.setSelected(selected);
            // fthcEntry.setVisible(selected);
        }

        applyJButtonAction();

    }

    public Set<TraceEventType> createFilterEventSet() {

        Set<TraceEventType> filterEventSet = new HashSet<TraceEventType>();

        for (TraceEventType traceEventType : traceTableModel.getTraceEventTypeList()) {

            CheckBoxMenuItemPopupEntry<TraceEventKey> cbmipe = traceTableModel.getCheckBoxMenuItem(traceEventType);

            if (cbmipe.isSelected()) {

                filterEventSet.add(traceEventType);
            }
        }

        return filterEventSet;

    }
}
