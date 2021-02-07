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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.pega.gcs.fringecommon.guiutilities.AutoCompleteJComboBox;
import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;

public class TraceTablePanelSettingDialog extends JDialog {

    private static final long serialVersionUID = -4890020854049502839L;

    private String charsetName;

    private boolean settingUpdated;

    private AutoCompleteJComboBox<String> charsetComboBox;

    public TraceTablePanelSettingDialog(String charsetName, ImageIcon appIcon, Component parent) {

        super();

        this.charsetName = charsetName;

        this.settingUpdated = false;

        setIconImage(appIcon.getImage());

        setTitle("Tracer file Settings");

        setModalityType(ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(getMainPanel());

        pack();

        setLocationRelativeTo(parent);

        populateSettings();

        // visible should be the last step
        setVisible(true);

    }

    private String getCharsetName() {
        return charsetName;
    }

    public boolean isSettingUpdated() {
        return settingUpdated;
    }

    private void setSettingUpdated(boolean settingUpdated) {
        this.settingUpdated = settingUpdated;
    }

    public AutoCompleteJComboBox<String> getCharsetComboBox() {

        if (charsetComboBox == null) {
            charsetComboBox = GUIUtilities.getCharsetJComboBox();
        }

        return charsetComboBox;
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
        gbc1.insets = new Insets(10, 10, 10, 2);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 0.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(10, 2, 10, 10);

        JLabel charsetJLabel = new JLabel("File Encoding");

        AutoCompleteJComboBox<String> charsetComboBox = getCharsetComboBox();

        settingsPanel.add(charsetJLabel, gbc1);
        settingsPanel.add(charsetComboBox, gbc2);

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
        JButton resetButton = new JButton("Reset");
        resetButton.setToolTipText("Reset");

        resetButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                AutoCompleteJComboBox<String> charsetComboBox = getCharsetComboBox();

                charsetComboBox.setSelectedItem(getCharsetName());
            }
        });

        Dimension dim = new Dimension(20, 40);
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(okButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(resetButton);
        buttonsPanel.add(Box.createRigidArea(dim));
        buttonsPanel.add(Box.createHorizontalGlue());

        buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return buttonsPanel;
    }

    private void populateSettings() {

        AutoCompleteJComboBox<String> charsetJComboBox = getCharsetComboBox();

        charsetJComboBox.setSelectedItem(charsetName);
    }

    public String getSelectedCharsetName() {

        AutoCompleteJComboBox<String> charsetJComboBox = getCharsetComboBox();

        String selectedCharsetName = (String) charsetJComboBox.getSelectedItem();

        if ((selectedCharsetName == null) || ("".equals(selectedCharsetName))) {
            selectedCharsetName = this.charsetName;
        }

        return selectedCharsetName;
    }

}
