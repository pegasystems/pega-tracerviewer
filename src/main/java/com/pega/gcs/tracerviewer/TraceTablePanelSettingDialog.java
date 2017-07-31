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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.pega.gcs.fringecommon.guiutilities.AutoCompleteJComboBox;
import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;

public class TraceTablePanelSettingDialog extends JDialog {

	private static final long serialVersionUID = -4890020854049502839L;

	private String charset;

	private boolean settingUpdated;

	private AutoCompleteJComboBox<String> charsetJComboBox;

	public TraceTablePanelSettingDialog(String charset, ImageIcon appIcon, Component parent) {

		super();

		this.charset = charset;

		this.settingUpdated = false;

		setIconImage(appIcon.getImage());

		// setPreferredSize(new Dimension(350, 175));
		setTitle("Tracer file Settings");
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

	protected String getCharset() {
		return charset;
	}

	/**
	 * @return the settingUpdated
	 */
	public boolean isSettingUpdated() {
		return settingUpdated;
	}

	protected void setSettingUpdated(boolean aSettingUpdated) {
		settingUpdated = aSettingUpdated;
	}

	/**
	 * @return the charsetJComboBox
	 */
	public AutoCompleteJComboBox<String> getCharsetJComboBox() {

		if (charsetJComboBox == null) {
			charsetJComboBox = GUIUtilities.getCharsetJComboBox();
		}

		return charsetJComboBox;
	}

	private JPanel getMainJPanel() {

		JPanel mainJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(mainJPanel, BoxLayout.Y_AXIS);
		mainJPanel.setLayout(layout);

		JPanel settingsJPanel = getSettingsJPanel();
		JPanel buttonsJPanel = getButtonsJPanel();

		mainJPanel.add(settingsJPanel);
		mainJPanel.add(Box.createRigidArea(new Dimension(4, 2)));
		mainJPanel.add(buttonsJPanel);
		mainJPanel.add(Box.createRigidArea(new Dimension(4, 4)));
		// mainJPanel.add(Box.createHorizontalGlue());

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

		JLabel charsetJLabel = new JLabel("File Encoding");

		AutoCompleteJComboBox<String> charsetJComboBox = getCharsetJComboBox();

		settingsJPanel.add(charsetJLabel, gbc1);
		settingsJPanel.add(charsetJComboBox, gbc2);

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
				AutoCompleteJComboBox<String> charsetJComboBox = getCharsetJComboBox();

				charsetJComboBox.setSelectedItem(getCharset());
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

	private void populateSettingsJPanel() {

		AutoCompleteJComboBox<String> charsetJComboBox = getCharsetJComboBox();

		charsetJComboBox.setSelectedItem(charset);

	}

	public String getSelectedCharset() {
		AutoCompleteJComboBox<String> charsetJComboBox = getCharsetJComboBox();
		String charset = (String) charsetJComboBox.getSelectedItem();

		if ((charset == null) || ("".equals(charset))) {
			charset = this.charset;
		}

		return charset;
	}

}
