/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.pega.gcs.fringecommon.utilities.FileUtilities;
import com.pega.gcs.tracerviewer.model.TraceEventKey;

public class NavigationPanel extends JPanel {

	private static final long serialVersionUID = -2726689022594491214L;

	NavigationPanelController<TraceEventKey> navigationPanelController;

	private ImageIcon firstImageIcon;

	private ImageIcon prevImageIcon;

	private ImageIcon nextImageIcon;

	private ImageIcon lastImageIcon;

	private JLabel dataJLabel;

	private JButton firstJButton;

	private JButton prevJButton;

	private JButton nextJButton;

	private JButton lastJButton;

	public NavigationPanel(JLabel label, NavigationPanelController<TraceEventKey> navigationPanelController) {

		this.navigationPanelController = navigationPanelController;

		firstImageIcon = FileUtilities.getImageIcon(this.getClass(), "first.png");

		lastImageIcon = FileUtilities.getImageIcon(this.getClass(), "last.png");

		prevImageIcon = FileUtilities.getImageIcon(this.getClass(), "prev.png");

		nextImageIcon = FileUtilities.getImageIcon(this.getClass(), "next.png");

		LayoutManager layout = new BoxLayout(this, BoxLayout.LINE_AXIS);
		setLayout(layout);

		JLabel dataJLabel = getDataJLabel();
		JButton firstJButton = getFirstJButton();
		JButton prevJButton = getPrevJButton();
		JButton nextJButton = getNextJButton();
		JButton lastJButton = getLastJButton();

		int height = 30;

		add(Box.createHorizontalGlue());
		add(Box.createRigidArea(new Dimension(4, height)));
		add(firstJButton);
		add(Box.createRigidArea(new Dimension(2, height)));
		add(prevJButton);
		add(Box.createRigidArea(new Dimension(4, height)));
		add(label);
		add(Box.createRigidArea(new Dimension(2, height)));
		add(dataJLabel);
		add(Box.createRigidArea(new Dimension(4, height)));
		add(nextJButton);
		add(Box.createRigidArea(new Dimension(2, height)));
		add(lastJButton);
		add(Box.createRigidArea(new Dimension(4, height)));
		add(Box.createHorizontalGlue());

		// setBorder(BorderFactory.createLineBorder(MyColor.LIGHT_GRAY, 1));
	}

	/**
	 * @return the dataJLabel
	 */
	public JLabel getDataJLabel() {

		if (dataJLabel == null) {
			dataJLabel = new JLabel();

			Dimension size = new Dimension(80, 20);
			dataJLabel.setPreferredSize(size);
			dataJLabel.setMaximumSize(size);
			dataJLabel.setForeground(Color.BLUE);
		}

		return dataJLabel;
	}

	/**
	 * @return the firstJButton
	 */
	public JButton getFirstJButton() {

		if (firstJButton == null) {

			firstJButton = new JButton(firstImageIcon);

			Dimension size = new Dimension(40, 20);
			firstJButton.setPreferredSize(size);
			firstJButton.setMaximumSize(size);
			firstJButton.setBorder(BorderFactory.createEmptyBorder());
			firstJButton.setEnabled(false);
			firstJButton.setToolTipText("First");
			firstJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					navigationPanelController.first();
				}
			});
		}

		return firstJButton;
	}

	/**
	 * @return the prevJButton
	 */
	public JButton getPrevJButton() {

		if (prevJButton == null) {

			prevJButton = new JButton(prevImageIcon);

			Dimension size = new Dimension(40, 20);
			prevJButton.setPreferredSize(size);
			prevJButton.setMaximumSize(size);
			prevJButton.setBorder(BorderFactory.createEmptyBorder());
			prevJButton.setEnabled(false);
			prevJButton.setToolTipText("Previous");
			prevJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					navigationPanelController.previous();
				}
			});
		}

		return prevJButton;
	}

	/**
	 * @return the nextJButton
	 */
	public JButton getNextJButton() {

		if (nextJButton == null) {

			nextJButton = new JButton(nextImageIcon);

			Dimension size = new Dimension(40, 20);
			nextJButton.setPreferredSize(size);
			nextJButton.setMaximumSize(size);
			nextJButton.setBorder(BorderFactory.createEmptyBorder());
			nextJButton.setEnabled(false);
			nextJButton.setToolTipText("Next");
			nextJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					navigationPanelController.next();
				}
			});
		}

		return nextJButton;
	}

	/**
	 * @return the lastJButton
	 */
	public JButton getLastJButton() {

		if (lastJButton == null) {

			lastJButton = new JButton(lastImageIcon);

			Dimension size = new Dimension(40, 20);
			lastJButton.setPreferredSize(size);
			lastJButton.setMaximumSize(size);
			lastJButton.setBorder(BorderFactory.createEmptyBorder());
			lastJButton.setEnabled(false);
			lastJButton.setToolTipText("Last");
			lastJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					navigationPanelController.last();
				}
			});
		}

		return lastJButton;
	}

	public void resetNavigationPanel() {

		JLabel dataJLabel = getDataJLabel();
		dataJLabel.setText("");
		setEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {

		JLabel dataJLabel = getDataJLabel();
		JButton firstJButton = getFirstJButton();
		JButton prevJButton = getPrevJButton();
		JButton nextJButton = getNextJButton();
		JButton lastJButton = getLastJButton();

		dataJLabel.setEnabled(enabled);
		firstJButton.setEnabled(enabled);
		prevJButton.setEnabled(enabled);
		nextJButton.setEnabled(enabled);
		lastJButton.setEnabled(enabled);

		super.setEnabled(enabled);

	}

}
