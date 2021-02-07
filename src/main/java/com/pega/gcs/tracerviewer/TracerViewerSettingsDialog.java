/*******************************************************************************
 * Copyright (c) 2017, 2018 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;
import com.pega.gcs.fringecommon.guiutilities.MyColor;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;

public class TracerViewerSettingsDialog extends JDialog {

    private static final long serialVersionUID = 350671851883068863L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TracerViewerSettingsDialog.class);

    private TracerViewerSetting tracerViewerSetting;

    private JTextField recentItemsTextField;

    private JComboBox<String> charsetComboBox;

    private JCheckBox reloadPreviousFilesCheckBox;

    private boolean settingUpdated;

    public TracerViewerSettingsDialog(TracerViewerSetting tracerViewerSetting, ImageIcon appIcon, Component parent) {

        super();

        this.tracerViewerSetting = tracerViewerSetting;

        this.settingUpdated = false;

        setIconImage(appIcon.getImage());

        setTitle("Tracer Viewer Default Settings");

        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(getMainPanel());

        pack();

        setLocationRelativeTo(parent);

        populateSettings();

        // setVisible called by caller.
        // setVisible(true);

    }

    protected TracerViewerSetting getTracerViewerSetting() {
        return tracerViewerSetting;
    }

    public boolean isSettingUpdated() {
        return settingUpdated;
    }

    protected void setSettingUpdated(boolean settingUpdated) {
        this.settingUpdated = settingUpdated;
    }

    private JPanel getMainPanel() {

        JPanel mainPanel = new JPanel();

        LayoutManager layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
        mainPanel.setLayout(layout);

        JPanel settingsPanel = getSettingsPanel();
        JPanel buttonsPanel = getButtonsPanel();

        mainPanel.add(settingsPanel);
        mainPanel.add(buttonsPanel);

        return mainPanel;
    }

    private JPanel getSettingsPanel() {

        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(10, 10, 3, 2);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 0.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(10, 2, 3, 10);

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.gridx = 0;
        gbc3.gridy = 1;
        gbc3.weightx = 1.0D;
        gbc3.weighty = 0.0D;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.anchor = GridBagConstraints.NORTHWEST;
        gbc3.insets = new Insets(3, 10, 3, 2);

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.gridx = 1;
        gbc4.gridy = 1;
        gbc4.weightx = 1.0D;
        gbc4.weighty = 0.0D;
        gbc4.fill = GridBagConstraints.BOTH;
        gbc4.anchor = GridBagConstraints.NORTHWEST;
        gbc4.insets = new Insets(3, 2, 3, 10);

        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.gridx = 0;
        gbc5.gridy = 2;
        gbc5.weightx = 1.0D;
        gbc5.weighty = 0.0D;
        gbc5.fill = GridBagConstraints.BOTH;
        gbc5.anchor = GridBagConstraints.NORTHWEST;
        gbc5.insets = new Insets(0, 10, 3, 10);
        gbc5.gridwidth = GridBagConstraints.REMAINDER;

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.gridx = 0;
        gbc6.gridy = 3;
        gbc6.weightx = 1.0D;
        gbc6.weighty = 0.0D;
        gbc6.fill = GridBagConstraints.BOTH;
        gbc6.anchor = GridBagConstraints.NORTHWEST;
        gbc6.insets = new Insets(3, 10, 10, 2);

        GridBagConstraints gbc7 = new GridBagConstraints();
        gbc7.gridx = 1;
        gbc7.gridy = 3;
        gbc7.weightx = 1.0D;
        gbc7.weighty = 0.0D;
        gbc7.fill = GridBagConstraints.BOTH;
        gbc7.anchor = GridBagConstraints.NORTHWEST;
        gbc7.insets = new Insets(3, 2, 10, 10);

        JLabel recentItemsLabel = new JLabel("Recent Counts");
        JLabel charsetLabel = new JLabel("File Encoding");

        JTextArea charsetInfoTextArea;
        charsetInfoTextArea = new JTextArea("This charset is applied only for new files. Files from recent "
                + "menu will still use previously applied setting.");
        charsetInfoTextArea.setEditable(false);
        charsetInfoTextArea.setWrapStyleWord(true);
        charsetInfoTextArea.setLineWrap(true);
        charsetInfoTextArea.setRows(2);
        charsetInfoTextArea.setFont(this.getFont());
        charsetInfoTextArea.setBackground(null);
        charsetInfoTextArea.setForeground(MyColor.LIGHT_BLUE);

        JLabel reloadPreviousFilesLabel = new JLabel("Reload previous open files");

        JTextField recentItemsTextField = getRecentItemsTextField();
        JComboBox<String> charsetComboBox = getCharsetComboBox();
        JCheckBox reloadPreviousFilesCheckBox = getReloadPreviousFilesCheckBox();

        settingsPanel.add(recentItemsLabel, gbc1);
        settingsPanel.add(recentItemsTextField, gbc2);
        settingsPanel.add(charsetLabel, gbc3);
        settingsPanel.add(charsetComboBox, gbc4);
        settingsPanel.add(charsetInfoTextArea, gbc5);
        settingsPanel.add(reloadPreviousFilesLabel, gbc6);
        settingsPanel.add(reloadPreviousFilesCheckBox, gbc7);

        Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

        settingsPanel.setBorder(BorderFactory.createTitledBorder(loweredEtched, "Settings"));

        return settingsPanel;
    }

    private JPanel getButtonsPanel() {

        JPanel buttonsPanel = new JPanel();

        LayoutManager layout = new BoxLayout(buttonsPanel, BoxLayout.X_AXIS);
        buttonsPanel.setLayout(layout);

        // OK Button
        JButton okButton = new JButton("OK");
        okButton.setToolTipText("OK");

        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                setSettingUpdated(true);
                dispose();
            }
        });

        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setToolTipText("Cancel");

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });

        // Reset button
        JButton resetJButton = new JButton("Reset");
        resetJButton.setToolTipText("Reset");

        resetJButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                getTracerViewerSetting().setDefault();
                populateSettings();
            }
        });

        Dimension dim = new Dimension(20, 40);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(okButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(resetJButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(Box.createHorizontalGlue());

        buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        return buttonsPanel;
    }

    private JTextField getRecentItemsTextField() {

        if (recentItemsTextField == null) {
            recentItemsTextField = new JTextField();
        }

        return recentItemsTextField;
    }

    private JComboBox<String> getCharsetComboBox() {

        if (charsetComboBox == null) {
            charsetComboBox = GUIUtilities.getCharsetJComboBox();
        }

        return charsetComboBox;
    }

    private JCheckBox getReloadPreviousFilesCheckBox() {

        if (reloadPreviousFilesCheckBox == null) {
            reloadPreviousFilesCheckBox = new JCheckBox();
        }

        return reloadPreviousFilesCheckBox;
    }

    protected void populateSettings() {

        JTextField recentItemsTextField = getRecentItemsTextField();
        JComboBox<String> charsetComboBox = getCharsetComboBox();
        JCheckBox reloadPreviousFilesCheckBox = getReloadPreviousFilesCheckBox();

        String text = String.valueOf(tracerViewerSetting.getRecentItemsCount());
        recentItemsTextField.setText(text);

        String charset = tracerViewerSetting.getCharset();
        charsetComboBox.setSelectedItem(charset);

        boolean reloadPreviousFiles = tracerViewerSetting.isReloadPreviousFiles();
        reloadPreviousFilesCheckBox.setSelected(reloadPreviousFiles);
    }

    public int getRecentItemsCount() {

        int recentItemsCount = tracerViewerSetting.getRecentItemsCount();

        String text = getRecentItemsTextField().getText();

        try {
            recentItemsCount = Integer.parseInt(text);
        } catch (NumberFormatException nfe) {
            LOG.error("Error parsing recent items count", nfe);
        }

        return recentItemsCount;
    }

    public String getSelectedCharset() {

        String charset = (String) getCharsetComboBox().getSelectedItem();

        if ((charset == null) || ("".equals(charset))) {
            charset = tracerViewerSetting.getCharset();
        }

        return charset;
    }

    public boolean isReloadPreviousFiles() {

        JCheckBox reloadPreviousFilesCheckBox = getReloadPreviousFilesCheckBox();

        boolean reloadPreviousFiles = reloadPreviousFilesCheckBox.isSelected();

        return reloadPreviousFiles;
    }
}
