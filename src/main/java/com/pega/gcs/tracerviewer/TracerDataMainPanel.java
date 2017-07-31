/*******************************************************************************
 * Copyright (c) 2017 Pegasystems Inc. All rights reserved.
 *
 * Contributors:
 *     Manu Varghese
 *******************************************************************************/
package com.pega.gcs.tracerviewer;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.GoToLineDialog;
import com.pega.gcs.fringecommon.guiutilities.Message;
import com.pega.gcs.fringecommon.guiutilities.Message.MessageType;
import com.pega.gcs.fringecommon.guiutilities.ModalProgressMonitor;
import com.pega.gcs.fringecommon.guiutilities.RecentFile;
import com.pega.gcs.fringecommon.guiutilities.RecentFileContainer;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkContainer;
import com.pega.gcs.fringecommon.guiutilities.bookmark.BookmarkModel;
import com.pega.gcs.fringecommon.guiutilities.search.SearchData;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;
import com.pega.gcs.fringecommon.utilities.GeneralUtilities;
import com.pega.gcs.tracerviewer.model.TraceEventKey;
import com.pega.gcs.tracerviewer.report.TracerSimpleReportFrame;
import com.pega.gcs.tracerviewer.view.TracerDataCompareTableView;
import com.pega.gcs.tracerviewer.view.TracerDataSingleTableView;
import com.pega.gcs.tracerviewer.view.TracerDataTreeMergedTableView;
import com.pega.gcs.tracerviewer.view.TracerDataTreeTableView;
import com.pega.gcs.tracerviewer.view.TracerDataView;
import com.pega.gcs.tracerviewer.view.TracerDataViewMode;

public class TracerDataMainPanel extends JPanel {

	private static final long serialVersionUID = 5238167301628481200L;

	private static final Log4j2Helper LOG = new Log4j2Helper(TracerDataMainPanel.class);

	private RecentFileContainer recentFileContainer;

	private TracerViewerSetting tracerViewerSetting;

	private TraceTableModel traceTableModel;

	private RecentFile recentFile;

	private HashMap<String, TracerDataView> tracerDataViewMap;

	private JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox;

	private JLabel incompleteTracerJLabel;

	private JLabel charsetJLabel;

	private JLabel sizeJLabel;

	private JPanel tracerDataViewCardJPanel;

	private JPanel supplementUtilityJPanel;

	private JButton gotoLineJButton;

	private JButton reloadJButton;

	private JButton overviewJButton;

	private TracerSimpleReportFrame tracerSimpleReportFrame;

	private TraceNavigationTableController traceNavigationTableController;

	@SuppressWarnings("unchecked")
	public TracerDataMainPanel(File selectedFile, RecentFileContainer recentFileContainer,
			TracerViewerSetting tracerViewerSetting) {

		super();

		this.recentFileContainer = recentFileContainer;
		this.tracerViewerSetting = tracerViewerSetting;

		this.recentFile = getRecentFile(selectedFile, recentFileContainer, tracerViewerSetting);

		SearchData<TraceEventKey> searchData = new SearchData<>(SearchEventType.values());

		this.traceTableModel = new TraceTableModel(recentFile, searchData);

		BookmarkContainer<TraceEventKey> bookmarkContainer;
		bookmarkContainer = (BookmarkContainer<TraceEventKey>) recentFile.getAttribute(RecentFile.KEY_BOOKMARK);

		BookmarkModel<TraceEventKey> bookmarkModel = new BookmarkModel<TraceEventKey>(bookmarkContainer,
				traceTableModel);

		traceTableModel.setBookmarkModel(bookmarkModel);

		traceNavigationTableController = new TraceNavigationTableController(traceTableModel);

		tracerDataViewMap = new HashMap<String, TracerDataView>();

		traceTableModel.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent aE) {
				updateDisplayJPanel();
			}
		});

		setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 1.0D;
		gbc1.weighty = 0.0D;
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

		JPanel utilityCompositeJPanel = getUtilityCompositeJPanel();
		JPanel tracerDataViewCardJPanel = getTracerDataViewCardJPanel();

		add(utilityCompositeJPanel, gbc1);
		add(tracerDataViewCardJPanel, gbc2);

		// set default view
		JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox = getTracerDataViewModeJComboBox();

		// http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4699927
		// tree need to be built once the root node has some child nodes. hence
		// setting the default to single table
		tracerDataViewModeJComboBox.setSelectedItem(TracerDataViewMode.SINGLE_TABLE);

		loadFile(traceTableModel, this, false);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#removeNotify()
	 */
	@Override
	public void removeNotify() {
		super.removeNotify();

		clearJDialogList();
	}

	protected TraceTableModel getTraceTableModel() {
		return traceTableModel;
	}

	protected TraceNavigationTableController getTraceNavigationTableController() {
		return traceNavigationTableController;
	}

	private JPanel getUtilityCompositeJPanel() {

		JPanel utilityCompositeJPanel = new JPanel();

		utilityCompositeJPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = 0;
		gbc1.weightx = 0.0D;
		gbc1.weighty = 1.0D;
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.insets = new Insets(0, 0, 0, 0);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 1;
		gbc2.gridy = 0;
		gbc2.weightx = 1.0D;
		gbc2.weighty = 1.0D;
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		gbc2.insets = new Insets(0, 0, 0, 0);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 2;
		gbc3.gridy = 0;
		gbc3.weightx = 0.0D;
		gbc3.weighty = 1.0D;
		gbc3.fill = GridBagConstraints.BOTH;
		gbc3.anchor = GridBagConstraints.NORTHWEST;
		gbc3.insets = new Insets(0, 0, 0, 0);

		JPanel utilityJPanel = getUtilityJPanel();
		JPanel supplementUtilityJPanel = getSupplementUtilityJPanel();
		JPanel infoJPanel = getTracerInfoJPanel();

		utilityCompositeJPanel.add(utilityJPanel, gbc1);
		utilityCompositeJPanel.add(supplementUtilityJPanel, gbc2);
		utilityCompositeJPanel.add(infoJPanel, gbc3);

		return utilityCompositeJPanel;
	}

	private JPanel getUtilityJPanel() {

		JPanel utilityJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(utilityJPanel, BoxLayout.LINE_AXIS);
		utilityJPanel.setLayout(layout);

		Dimension spacer = new Dimension(15, 30);
		Dimension endSpacer = new Dimension(10, 30);

		JLabel tracerDataViewModeJLabel = new JLabel("Select view: ");

		JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox = getTracerDataViewModeJComboBox();
		JButton gotoLineJButton = getGotoLineJButton();
		JButton overviewJButton = getOverviewJButton();
		JButton reloadJButton = getReloadJButton();

		utilityJPanel.add(Box.createRigidArea(endSpacer));
		utilityJPanel.add(tracerDataViewModeJLabel);
		utilityJPanel.add(Box.createRigidArea(spacer));
		utilityJPanel.add(tracerDataViewModeJComboBox);
		utilityJPanel.add(Box.createRigidArea(spacer));
		utilityJPanel.add(gotoLineJButton);
		utilityJPanel.add(Box.createRigidArea(spacer));
		utilityJPanel.add(overviewJButton);
		utilityJPanel.add(Box.createRigidArea(spacer));
		utilityJPanel.add(reloadJButton);
		utilityJPanel.add(Box.createRigidArea(spacer));

		utilityJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		return utilityJPanel;
	}

	private JPanel getTracerInfoJPanel() {

		JPanel infoJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(infoJPanel, BoxLayout.X_AXIS);
		infoJPanel.setLayout(layout);

		// Dimension preferredSize = new Dimension(300, 30);
		// infoJPanel.setPreferredSize(preferredSize);

		JPanel incompleteTracerJPanel = getIncompleteTracerJPanel();
		JPanel charsetJPanel = getCharsetJPanel();
		JPanel sizeJPanel = getSizeJPanel();

		infoJPanel.add(incompleteTracerJPanel);
		infoJPanel.add(charsetJPanel);
		infoJPanel.add(sizeJPanel);

		return infoJPanel;
	}

	private JPanel getIncompleteTracerJPanel() {

		JLabel incompleteTracerJLabel = getIncompleteTracerJLabel();

		JPanel incompleteTracerJPanel = getInfoJPanel(incompleteTracerJLabel);

		Dimension preferredSize = new Dimension(150, 30);
		incompleteTracerJPanel.setPreferredSize(preferredSize);

		return incompleteTracerJPanel;
	}

	private JPanel getCharsetJPanel() {

		JLabel charsetJLabel = getCharsetJLabel();

		JPanel charsetJPanel = getInfoJPanel(charsetJLabel);

		Dimension preferredSize = new Dimension(100, 30);
		charsetJPanel.setPreferredSize(preferredSize);

		return charsetJPanel;
	}

	private JPanel getSizeJPanel() {

		JLabel sizeJLabel = getSizeJLabel();

		JPanel sizeJPanel = getInfoJPanel(sizeJLabel);

		Dimension preferredSize = new Dimension(100, 30);
		sizeJPanel.setPreferredSize(preferredSize);

		return sizeJPanel;
	}

	private JPanel getInfoJPanel(JLabel jLabel) {

		JPanel infoJPanel = new JPanel();

		LayoutManager layout = new BoxLayout(infoJPanel, BoxLayout.X_AXIS);
		infoJPanel.setLayout(layout);

		Dimension dim = new Dimension(1, 30);

		infoJPanel.add(Box.createHorizontalGlue());
		infoJPanel.add(Box.createRigidArea(dim));
		infoJPanel.add(jLabel);
		infoJPanel.add(Box.createHorizontalGlue());

		infoJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

		return infoJPanel;
	}

	protected JPanel getSupplementUtilityJPanel() {

		if (supplementUtilityJPanel == null) {

			supplementUtilityJPanel = new JPanel();
			LayoutManager layout = new BoxLayout(supplementUtilityJPanel, BoxLayout.LINE_AXIS);
			supplementUtilityJPanel.setLayout(layout);
		}

		return supplementUtilityJPanel;
	}

	private JPanel getTracerDataViewCardJPanel() {

		if (tracerDataViewCardJPanel == null) {

			TraceTableModel traceTableModel = getTraceTableModel();

			TraceNavigationTableController traceNavigationTableController = getTraceNavigationTableController();

			JPanel supplementUtilityJPanel = getSupplementUtilityJPanel();

			tracerDataViewCardJPanel = new JPanel(new CardLayout());

			for (TracerDataViewMode tracerDataViewMode : TracerDataViewMode.values()) {

				TracerDataView tracerDataView;

				switch (tracerDataViewMode) {

				case SINGLE_TABLE:
					tracerDataView = new TracerDataSingleTableView(traceTableModel, supplementUtilityJPanel,
							traceNavigationTableController);
					break;

				case SINGLE_TREE:
					tracerDataView = new TracerDataTreeTableView(traceTableModel, supplementUtilityJPanel,
							traceNavigationTableController);
					break;

				case SINGLE_TREE_MERGED:
					tracerDataView = new TracerDataTreeMergedTableView(traceTableModel, supplementUtilityJPanel,
							traceNavigationTableController);
					break;

				case COMPARE_TABLE:
					tracerDataView = new TracerDataCompareTableView(traceTableModel, supplementUtilityJPanel,
							traceNavigationTableController, recentFileContainer, tracerViewerSetting);
					break;

				default:
					tracerDataView = new TracerDataSingleTableView(traceTableModel, supplementUtilityJPanel,
							traceNavigationTableController);
					break;
				}

				String tracerDataViewModeName = tracerDataViewMode.name();

				tracerDataViewMap.put(tracerDataViewModeName, tracerDataView);

				tracerDataViewCardJPanel.add(tracerDataView, tracerDataViewModeName);
			}
		}

		return tracerDataViewCardJPanel;
	}

	private JComboBox<TracerDataViewMode> getTracerDataViewModeJComboBox() {

		if (tracerDataViewModeJComboBox == null) {

			tracerDataViewModeJComboBox = new JComboBox<TracerDataViewMode>(TracerDataViewMode.values());

			Dimension size = new Dimension(150, 20);
			tracerDataViewModeJComboBox.setPreferredSize(size);
			// tracerDataViewModeJComboBox.setMinimumSize(size);
			tracerDataViewModeJComboBox.setMaximumSize(size);

			tracerDataViewModeJComboBox.addActionListener(new ActionListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void actionPerformed(ActionEvent e) {

					JComboBox<TracerDataViewMode> tracerDataViewModeJComboBox;
					tracerDataViewModeJComboBox = (JComboBox<TracerDataViewMode>) e.getSource();

					TracerDataViewMode tracerDataViewMode;
					tracerDataViewMode = (TracerDataViewMode) tracerDataViewModeJComboBox.getSelectedItem();

					switchTracerDataViewMode(tracerDataViewMode);
				}
			});
		}

		return tracerDataViewModeJComboBox;
	}

	private JLabel getIncompleteTracerJLabel() {

		if (incompleteTracerJLabel == null) {
			incompleteTracerJLabel = new JLabel();
			incompleteTracerJLabel.setForeground(Color.RED);
		}

		return incompleteTracerJLabel;
	}

	private JLabel getCharsetJLabel() {

		if (charsetJLabel == null) {
			charsetJLabel = new JLabel();
		}

		return charsetJLabel;
	}

	private JLabel getSizeJLabel() {

		if (sizeJLabel == null) {
			sizeJLabel = new JLabel();
		}

		return sizeJLabel;
	}

	protected void switchTracerDataViewMode(TracerDataViewMode tracerDataViewMode) {

		String tracerDataViewModeName = tracerDataViewMode.name();

		TracerDataView tracerDataView = tracerDataViewMap.get(tracerDataViewModeName);

		if (tracerDataView != null) {

			tracerDataView.switchToFront();

			JPanel tracerDataViewCardJPanel = getTracerDataViewCardJPanel();
			CardLayout cardLayout = (CardLayout) (tracerDataViewCardJPanel.getLayout());

			cardLayout.show(tracerDataViewCardJPanel, tracerDataViewModeName);

		}
	}

	protected void updateDisplayJPanel() {

		LOG.info("updateDisplayJPanel");
		populateDisplayJPanel();
	}

	protected void populateDisplayJPanel() {

		JLabel incompleteTracerJLabel = getIncompleteTracerJLabel();
		JLabel charsetJLabel = getCharsetJLabel();
		JLabel sizeJLabel = getSizeJLabel();

		boolean incompleteTracerXML = traceTableModel.isIncompletedTracerXML();
		String incompleteTracerStr = (incompleteTracerXML ? "Incomplete Tracer XML" : "");
		String charset = traceTableModel.getCharset();

		Long size = (Long) recentFile.getAttribute(RecentFile.KEY_SIZE);
		String sizeStr = GeneralUtilities.humanReadableSize(size.longValue(), false);

		incompleteTracerJLabel.setText(incompleteTracerStr);
		charsetJLabel.setText(charset);
		sizeJLabel.setText(sizeStr);
	}

	@SuppressWarnings("unchecked")
	public static RecentFile getRecentFile(File selectedFile, RecentFileContainer recentFileContainer,
			TracerViewerSetting tracerViewerSetting) {

		RecentFile recentFile = null;

		// identify the recent file
		for (RecentFile rf : recentFileContainer.getRecentFileList()) {

			String file = (String) rf.getAttribute(RecentFile.KEY_FILE);

			if ((file != null) && (file.toLowerCase().equals(selectedFile.getPath().toLowerCase()))) {
				// found in recent files
				recentFile = rf;
				break;
			}
		}

		if (recentFile == null) {
			String charset = tracerViewerSetting.getCharset();
			recentFile = new RecentFile(selectedFile.getPath(), charset);
		}

		BookmarkContainer<TraceEventKey> bookmarkContainer;
		bookmarkContainer = (BookmarkContainer<TraceEventKey>) recentFile.getAttribute(RecentFile.KEY_BOOKMARK);

		if (bookmarkContainer == null) {

			bookmarkContainer = new BookmarkContainer<TraceEventKey>();

			recentFile.setAttribute(RecentFile.KEY_BOOKMARK, bookmarkContainer);
		}

		try (FileInputStream fis = new FileInputStream(selectedFile)) {

			long totalSize = fis.getChannel().size();
			recentFile.setAttribute(RecentFile.KEY_SIZE, totalSize);

		} catch (IOException e) {
			LOG.error("Error reading file " + selectedFile.toString(), e);
		}

		// save and bring it to front
		recentFileContainer.addRecentFile(recentFile);

		return recentFile;
	}

	public static void loadFile(TraceTableModel traceTableModel, final JComponent parent, final boolean waitMode) {

		RecentFile recentFile = traceTableModel.getRecentFile();

		if (recentFile != null) {

			final String tracerFilePath = (String) recentFile.getAttribute(RecentFile.KEY_FILE);

			UIManager.put("ModalProgressMonitor.progressText", "Loading Tracer XML file");

			final ModalProgressMonitor mProgressMonitor = new ModalProgressMonitor(parent, "",
					"Loaded 0 trace events (0%)", 0, 100);
			mProgressMonitor.setMillisToDecideToPopup(0);
			mProgressMonitor.setMillisToPopup(0);

			TracerFileLoadTask tflt = new TracerFileLoadTask(mProgressMonitor, traceTableModel) {

				/*
				 * (non-Javadoc)
				 * 
				 * @see javax.swing.SwingWorker#done()
				 */
				@Override
				protected void done() {

					if (!waitMode) {
						completeLoad(this, mProgressMonitor, tracerFilePath, parent, traceTableModel);
					}
				}
			};

			tflt.execute();

			if (waitMode) {
				completeLoad(tflt, mProgressMonitor, tracerFilePath, parent, traceTableModel);
			}
		} else {
			traceTableModel.setMessage(new Message(MessageType.ERROR, "No file selected for model"));
		}
	}

	protected static void completeLoad(TracerFileLoadTask tflt, ModalProgressMonitor mProgressMonitor,
			String tracerFilePath, JComponent parent, TraceTableModel traceTableModel) {

		try {

			tflt.get();

			System.gc();

			int processedCount = tflt.getProcessedCount();

			traceTableModel.fireTableDataChanged();

			LOG.info("TracerFileLoadTask - Done: " + tracerFilePath + " processedCount:" + processedCount);

		} catch (CancellationException ce) {

			LOG.error("TracerFileLoadTask - Cancelled " + tracerFilePath);

			MessageType messageType = MessageType.ERROR;
			Message modelmessage = new Message(messageType, tracerFilePath + " - file loading cancelled.");
			traceTableModel.setMessage(modelmessage);

		} catch (ExecutionException ee) {

			LOG.error("Execution Error during TracerFileLoadTask", ee);

			String message = null;

			if (ee.getCause() instanceof OutOfMemoryError) {

				message = "Out Of Memory Error has occured while loading " + tracerFilePath
						+ ".\nPlease increase the JVM's max heap size (-Xmx) and try again.";

				JOptionPane.showMessageDialog(parent, message, "Out Of Memory Error", JOptionPane.ERROR_MESSAGE);
			} else {
				message = ee.getCause().getMessage() + " has occured while loading " + tracerFilePath + ".";

				JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
			}

			MessageType messageType = MessageType.ERROR;
			Message modelmessage = new Message(messageType, message);
			traceTableModel.setMessage(modelmessage);

		} catch (Exception e) {
			LOG.error("Error loading file: " + tracerFilePath, e);
			MessageType messageType = MessageType.ERROR;

			StringBuffer messageB = new StringBuffer();
			messageB.append("Error loading file: ");
			messageB.append(tracerFilePath);

			Message message = new Message(messageType, messageB.toString());
			traceTableModel.setMessage(message);

		} finally {

			mProgressMonitor.close();

			System.gc();
		}
	}

	private JButton getGotoLineJButton() {

		if (gotoLineJButton == null) {
			gotoLineJButton = new JButton("Go to line");
			Dimension size = new Dimension(90, 20);
			gotoLineJButton.setPreferredSize(size);
			gotoLineJButton.setMaximumSize(size);

			gotoLineJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					List<TraceEventKey> traceEventKeyList;
					traceEventKeyList = getTraceTableModel().getFtmEntryKeyList();

					// increment by 1 to get the display sequence no.
					int startIndex = traceEventKeyList.get(0).getId() + 1;
					int endIndex = traceEventKeyList.get(traceEventKeyList.size() - 1).getId() + 1;

					GoToLineDialog goToLineDialog = new GoToLineDialog(startIndex, endIndex, BaseFrame.getAppIcon(),
							TracerDataMainPanel.this);
					goToLineDialog.setVisible(true);

					Integer selectedIndex = goToLineDialog.getSelectedInteger();

					if (selectedIndex != null) {

						TraceEventKey selectedTraceEventKey = null;

						for (TraceEventKey traceEventKey : traceEventKeyList) {

							int traceEventKeyId = traceEventKey.getId();

							// decrement by 1 to get actual id.
							if ((selectedIndex.intValue() - 1) == traceEventKeyId) {
								selectedTraceEventKey = traceEventKey;
								break;
							}
						}

						if (selectedTraceEventKey != null) {
							getTraceNavigationTableController().scrollToKey(selectedTraceEventKey);
							// traverseToKey(selectedTraceEventKey);
						}
					}
				}
			});

		}

		return gotoLineJButton;
	}

	private JButton getReloadJButton() {

		if (reloadJButton == null) {
			reloadJButton = new JButton("Reload file");

			Dimension size = new Dimension(90, 20);
			reloadJButton.setPreferredSize(size);
			reloadJButton.setMaximumSize(size);
			reloadJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					TraceTableModel traceTableModel = getTraceTableModel();

					String origCharset = traceTableModel.getCharset();
					TraceTablePanelSettingDialog traceTablePanelSettingDialog;

					traceTablePanelSettingDialog = new TraceTablePanelSettingDialog(origCharset, BaseFrame.getAppIcon(),
							TracerDataMainPanel.this);

					boolean settingUpdated = traceTablePanelSettingDialog.isSettingUpdated();

					if (settingUpdated) {

						String charset = traceTablePanelSettingDialog.getSelectedCharset();
						traceTableModel.updateRecentFile(charset);

						if (origCharset.equals(charset)) {
							populateDisplayJPanel();

						} else {
							// charset changed, read/parse the file again

							// clear and reset the model.
							traceTableModel.resetModel();

							// charset changed, read/parse the file again
							loadFile(traceTableModel, TracerDataMainPanel.this, false);
						}
					}
				}
			});
		}

		return reloadJButton;
	}

	private JButton getOverviewJButton() {

		if (overviewJButton == null) {

			overviewJButton = new JButton("Overview");
			Dimension size = new Dimension(90, 20);
			overviewJButton.setPreferredSize(size);
			overviewJButton.setMaximumSize(size);

			overviewJButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					JFrame tracerSimpleReportFrame = getTracerOverviewFrame();

					tracerSimpleReportFrame.toFront();
				}
			});
		}

		return overviewJButton;

	}

	protected TracerSimpleReportFrame getTracerOverviewFrame() {

		if (tracerSimpleReportFrame == null) {

			TraceTableModel traceTableModel = getTraceTableModel();
			TraceNavigationTableController traceNavigationTableController = getTraceNavigationTableController();

			tracerSimpleReportFrame = new TracerSimpleReportFrame(traceTableModel, traceNavigationTableController,
					BaseFrame.getAppIcon(), this);

			tracerSimpleReportFrame.addWindowListener(new WindowAdapter() {

				@Override
				public void windowClosed(WindowEvent e) {
					TracerSimpleReportFrame tracerSimpleReportFrame;
					tracerSimpleReportFrame = getTracerOverviewFrame();

					tracerSimpleReportFrame.destroyFrame();
					setTracerSimpleReportFrame(null);
				}

			});
		}

		return tracerSimpleReportFrame;
	}

	/**
	 * @param tracerSimpleReportFrame
	 *            the tracerSimpleReportFrame to set
	 */
	protected void setTracerSimpleReportFrame(TracerSimpleReportFrame tracerSimpleReportFrame) {
		this.tracerSimpleReportFrame = tracerSimpleReportFrame;
	}

	private void clearJDialogList() {

		if (tracerSimpleReportFrame != null) {
			tracerSimpleReportFrame.dispose();
			tracerSimpleReportFrame = null;
		}
	}
}
