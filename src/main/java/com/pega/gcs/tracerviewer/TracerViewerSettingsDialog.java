/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
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

	private JTextField recentItemsJTextField;

	private JComboBox<String> charsetJComboBox;

	private JCheckBox reloadPreviousFilesJComboBox;

	private boolean settingUpdated;

	public TracerViewerSettingsDialog(TracerViewerSetting tracerViewerSetting, ImageIcon appIcon, Component parent) {

		super();

		this.tracerViewerSetting = tracerViewerSetting;

		this.settingUpdated = false;

		setIconImage(appIcon.getImage());

		setPreferredSize(new Dimension(300, 220));
		setTitle("Tracer Viewer Default Settings");
		// setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		// setAlwaysOnTop(true);

		setContentPane(getMainJPanel());

		pack();

		setLocationRelativeTo(parent);

		populateSettingsJPanel();

		// visible should be the last step
		setVisible(true);

	}

	protected TracerViewerSetting getTracerViewerSetting() {
		return tracerViewerSetting;
	}

	public boolean isSettingUpdated() {
		return settingUpdated;
	}

	protected void setSettingUpdated(boolean aSettingUpdated) {
		settingUpdated = aSettingUpdated;
	}

	private JPanel getMainJPanel() {

		JPanel mainJPanel = new JPanel();

		mainJPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 1.0D;
		gbc1.weighty = 1.0D;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.insets = new Insets(2, 2, 0, 2);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = 1;
		gbc2.weightx = 1.0D;
		gbc2.weighty = 1.0D;
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		gbc2.insets = new Insets(0, 4, 2, 4);

		JPanel settingsJPanel = getSettingsJPanel();
		JPanel buttonsJPanel = getButtonsJPanel();

		mainJPanel.add(settingsJPanel, gbc1);
		mainJPanel.add(buttonsJPanel, gbc2);

		return mainJPanel;
	}

	private JPanel getSettingsJPanel() {
		JPanel settingsJPanel = new JPanel();
		settingsJPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 1.0D;
		gbc1.weighty = 0.0D;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.insets = new Insets(2, 2, 2, 2);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 1;
		gbc2.gridy = 0;
		gbc2.weightx = 1.0D;
		gbc2.weighty = 0.0D;
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		gbc2.insets = new Insets(2, 2, 2, 2);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 0;
		gbc3.gridy = 1;
		gbc3.weightx = 1.0D;
		gbc3.weighty = 0.0D;
		gbc3.fill = GridBagConstraints.BOTH;
		gbc3.anchor = GridBagConstraints.NORTHWEST;
		gbc3.insets = new Insets(2, 2, 2, 2);

		GridBagConstraints gbc4 = new GridBagConstraints();
		gbc4.gridx = 1;
		gbc4.gridy = 1;
		gbc4.weightx = 1.0D;
		gbc4.weighty = 0.0D;
		gbc4.fill = GridBagConstraints.BOTH;
		gbc4.anchor = GridBagConstraints.NORTHWEST;
		gbc4.insets = new Insets(2, 2, 2, 2);

		GridBagConstraints gbc5 = new GridBagConstraints();
		gbc5.gridx = 0;
		gbc5.gridy = 2;
		gbc5.weightx = 1.0D;
		gbc5.weighty = 0.0D;
		gbc5.fill = GridBagConstraints.BOTH;
		gbc5.anchor = GridBagConstraints.NORTHWEST;
		gbc5.insets = new Insets(2, 2, 2, 2);
		gbc5.gridwidth = GridBagConstraints.REMAINDER;

		GridBagConstraints gbc6 = new GridBagConstraints();
		gbc6.gridx = 0;
		gbc6.gridy = 3;
		gbc6.weightx = 1.0D;
		gbc6.weighty = 0.0D;
		gbc6.fill = GridBagConstraints.BOTH;
		gbc6.anchor = GridBagConstraints.NORTHWEST;
		gbc6.insets = new Insets(2, 2, 2, 2);

		GridBagConstraints gbc7 = new GridBagConstraints();
		gbc7.gridx = 1;
		gbc7.gridy = 3;
		gbc7.weightx = 1.0D;
		gbc7.weighty = 0.0D;
		gbc7.fill = GridBagConstraints.BOTH;
		gbc7.anchor = GridBagConstraints.NORTHWEST;
		gbc7.insets = new Insets(2, 2, 2, 2);

		JLabel recentItemsJLabel = new JLabel("Recent Counts");
		JLabel charsetJLabel = new JLabel("File Encoding");

		JTextArea charsetInfoJTextArea = new JTextArea(
				"This charset is applied only for new files.Files from recent menu will use previously applied setting.");
		charsetInfoJTextArea.setEditable(false);
		charsetInfoJTextArea.setWrapStyleWord(true);
		charsetInfoJTextArea.setLineWrap(true);
		charsetInfoJTextArea.setRows(2);
		charsetInfoJTextArea.setFont(this.getFont());
		charsetInfoJTextArea.setBackground(null);
		charsetInfoJTextArea.setForeground(MyColor.LIGHT_BLUE);

		JLabel reloadPreviousFilesJLabel = new JLabel("Reload previous open files");

		JTextField recentItemsJTextField = getRecentItemsJTextField();
		JComboBox<String> charsetJComboBox = getCharsetJComboBox();
		JCheckBox reloadPreviousFilesJComboBox = getReloadPreviousFilesJComboBox();

		settingsJPanel.add(recentItemsJLabel, gbc1);
		settingsJPanel.add(recentItemsJTextField, gbc2);
		settingsJPanel.add(charsetJLabel, gbc3);
		settingsJPanel.add(charsetJComboBox, gbc4);
		settingsJPanel.add(charsetInfoJTextArea, gbc5);
		settingsJPanel.add(reloadPreviousFilesJLabel, gbc6);
		settingsJPanel.add(reloadPreviousFilesJComboBox, gbc7);
		
		Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

		settingsJPanel.setBorder(BorderFactory.createTitledBorder(loweredEtched, "Settings"));

		return settingsJPanel;
	}

	private JPanel getButtonsJPanel() {

		JPanel buttonsJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(buttonsJPanel, BoxLayout.X_AXIS);
		buttonsJPanel.setLayout(layout);

		// OK Button
		JButton okJButton = new JButton("OK");
		okJButton.setToolTipText("OK");

		okJButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				setSettingUpdated(true);
				dispose();
			}
		});

		// Cancel button
		JButton cancelJButton = new JButton("Cancel");
		cancelJButton.setToolTipText("Cancel");

		cancelJButton.addActionListener(new ActionListener() {
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
				populateSettingsJPanel();
			}
		});

		Dimension dim = new Dimension(20, 30);
		buttonsJPanel.add(Box.createHorizontalGlue());
		buttonsJPanel.add(Box.createRigidArea(dim));
		buttonsJPanel.add(okJButton);
		buttonsJPanel.add(Box.createRigidArea(dim));
		buttonsJPanel.add(cancelJButton);
		buttonsJPanel.add(Box.createRigidArea(dim));
		buttonsJPanel.add(resetJButton);
		buttonsJPanel.add(Box.createRigidArea(dim));
		buttonsJPanel.add(Box.createHorizontalGlue());

		buttonsJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
		return buttonsJPanel;
	}

	private JTextField getRecentItemsJTextField() {

		if (recentItemsJTextField == null) {
			recentItemsJTextField = new JTextField();
		}

		return recentItemsJTextField;
	}

	private JComboBox<String> getCharsetJComboBox() {

		if (charsetJComboBox == null) {
			charsetJComboBox = GUIUtilities.getCharsetJComboBox();
		}

		return charsetJComboBox;
	}

	private JCheckBox getReloadPreviousFilesJComboBox() {

		if (reloadPreviousFilesJComboBox == null) {
			reloadPreviousFilesJComboBox = new JCheckBox();
		}

		return reloadPreviousFilesJComboBox;
	}

	protected void populateSettingsJPanel() {

		JTextField recentItemsJTextField = getRecentItemsJTextField();
		JComboBox<String> charsetJComboBox = getCharsetJComboBox();
		JCheckBox reloadPreviousFilesJComboBox = getReloadPreviousFilesJComboBox();

		String text = String.valueOf(tracerViewerSetting.getRecentItemsCount());
		recentItemsJTextField.setText(text);

		String charset = tracerViewerSetting.getCharset();
		charsetJComboBox.setSelectedItem(charset);

		boolean reloadPreviousFiles = tracerViewerSetting.isReloadPreviousFiles();
		reloadPreviousFilesJComboBox.setSelected(reloadPreviousFiles);
	}

	public int getRecentItemsCount() {

		int recentItemsCount = tracerViewerSetting.getRecentItemsCount();

		String text = getRecentItemsJTextField().getText();

		try {
			recentItemsCount = Integer.parseInt(text);
		} catch (NumberFormatException nfe) {
			LOG.error("Error parsing recent items count", nfe);
		}

		return recentItemsCount;
	}

	public String getSelectedCharset() {

		String charset = (String) getCharsetJComboBox().getSelectedItem();

		if ((charset == null) || ("".equals(charset))) {
			charset = tracerViewerSetting.getCharset();
		}

		return charset;
	}

	public boolean isReloadPreviousFiles() {

		JCheckBox reloadPreviousFilesJComboBox = getReloadPreviousFilesJComboBox();

		boolean reloadPreviousFiles = reloadPreviousFilesJComboBox.isSelected();

		return reloadPreviousFiles;
	}
}
