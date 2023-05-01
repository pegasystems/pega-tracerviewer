/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXParseException;

import com.pega.gcs.fringecommon.guiutilities.GUIUtilities;
import com.pega.gcs.fringecommon.guiutilities.TextLineNumber;
import com.pega.gcs.fringecommon.log4j2.Log4j2Helper;

public class TraceXMLLoadSnippetJDialog extends JDialog {

    private static final long serialVersionUID = 2126978098491174246L;

    private static final Log4j2Helper LOG = new Log4j2Helper(TraceXMLLoadSnippetJDialog.class);

    private TracerViewerSetting tracerViewerSetting;

    private Element xmlElement;

    private JTextArea xmlSnippetJTextArea;

    private JTextArea errorMessageJTextArea;

    private JButton okJButton;

    private JButton cancelJButton;

    private Highlighter.HighlightPainter saxParseExceptionHighlightPainter;

    private Highlighter.HighlightPainter saxParseExceptionLineHighlightPainter;

    private File prevLoadedFile;

    public TraceXMLLoadSnippetJDialog(String title, TracerViewerSetting tracerViewerSetting, ImageIcon appIcon,
            Component parent) {

        super();

        this.tracerViewerSetting = tracerViewerSetting;
        this.xmlElement = null;

        saxParseExceptionLineHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.ORANGE);
        saxParseExceptionHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

        prevLoadedFile = null;

        setTitle(title);

        setIconImage(appIcon.getImage());

        setPreferredSize(new Dimension(1000, 800));

        setModalityType(ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(getMainJPanel());

        pack();

        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent windowEvent) {
                super.windowClosed(windowEvent);
            }
        });

        // visible should be the last step
        setVisible(true);
    }

    public Element getXmlElement() {
        return xmlElement;
    }

    public String getFilename() {
        return (prevLoadedFile != null ? prevLoadedFile.getName() : null);
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

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 0.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;

        JPanel xmlSnippetJPanel = getXmlSnippetJPanel();
        JPanel errorMessageJPanel = getErrorMessageJPanel();
        JPanel buttonsJPanel = getButtonsJPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, xmlSnippetJPanel, errorMessageJPanel);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(550);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        mainJPanel.add(splitPane, gbc1);
        mainJPanel.add(buttonsJPanel, gbc2);

        mainJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        return mainJPanel;
    }

    private JTextArea getXmlSnippetJTextArea() {

        if (xmlSnippetJTextArea == null) {
            xmlSnippetJTextArea = new JTextArea();

            xmlSnippetJTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
            xmlSnippetJTextArea.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void removeUpdate(DocumentEvent documentEvent) {
                    clearHighlights();
                }

                @Override
                public void insertUpdate(DocumentEvent documentEvent) {
                    clearHighlights();
                }

                @Override
                public void changedUpdate(DocumentEvent documentEvent) {
                    clearHighlights();
                }

                private void clearHighlights() {
                    Highlighter xmlSnippetTextAreaHighlighter = xmlSnippetJTextArea.getHighlighter();
                    xmlSnippetTextAreaHighlighter.removeAllHighlights();

                }
            });
        }

        return xmlSnippetJTextArea;
    }

    private JTextArea getErrorMessageJTextArea() {

        if (errorMessageJTextArea == null) {

            errorMessageJTextArea = new JTextArea();
            errorMessageJTextArea.setEditable(false);
            errorMessageJTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));

            DefaultCaret caret = (DefaultCaret) errorMessageJTextArea.getCaret();
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }

        return errorMessageJTextArea;
    }

    private JButton getOkJButton() {

        if (okJButton == null) {

            okJButton = new JButton("OK");

            Dimension size = new Dimension(90, 20);
            okJButton.setPreferredSize(size);
            okJButton.setMaximumSize(size);

            okJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    xmlElement = buildXMLElement();

                    if (xmlElement != null) {
                        dispose();
                    }
                }
            });
        }

        return okJButton;
    }

    private JButton getCancelJButton() {

        if (cancelJButton == null) {

            cancelJButton = new JButton("Cancel");

            Dimension size = new Dimension(90, 20);
            cancelJButton.setPreferredSize(size);
            cancelJButton.setMaximumSize(size);

            cancelJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    dispose();
                }
            });
        }

        return cancelJButton;
    }

    private JComponent getXmlSnippetTextAreaJComponent() {

        JTextArea xmlSnippetTextArea = getXmlSnippetJTextArea();

        JScrollPane scrollPane = new JScrollPane(xmlSnippetTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setPreferredSize(new Dimension(600, 500));

        Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

        scrollPane.setBorder(BorderFactory.createTitledBorder(loweredEtched, "XML Snippet Text Area"));

        TextLineNumber textLineNumber = new TextLineNumber(xmlSnippetTextArea);
        scrollPane.setRowHeaderView(textLineNumber);

        return scrollPane;
    }

    private JComponent getErrorMessageTextAreaJComponent() {

        JTextArea errorMessageJTextArea = getErrorMessageJTextArea();

        JScrollPane scrollPane = new JScrollPane(errorMessageJTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setPreferredSize(new Dimension(600, 100));

        Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

        scrollPane.setBorder(BorderFactory.createTitledBorder(loweredEtched, "Error message"));

        return scrollPane;
    }

    private JPanel getXmlSnippetJPanel() {

        JPanel xmlSnippetJPanel = new JPanel();

        xmlSnippetJPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 0.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(5, 10, 5, 5);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0D;
        gbc2.weighty = 1.0D;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.insets = new Insets(5, 5, 5, 5);

        JPanel pasteOrLoadJPanel = getPasteOrLoadJPanel();
        JComponent xmlSnippetTextAreaJComponent = getXmlSnippetTextAreaJComponent();

        xmlSnippetJPanel.add(pasteOrLoadJPanel, gbc1);
        xmlSnippetJPanel.add(xmlSnippetTextAreaJComponent, gbc2);

        return xmlSnippetJPanel;
    }

    private JPanel getPasteOrLoadJPanel() {

        JPanel pasteOrLoadJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(pasteOrLoadJPanel, BoxLayout.X_AXIS);
        pasteOrLoadJPanel.setLayout(layout);

        JLabel descriptionLabel = new JLabel("Paste the xml snippet into the area below or load from file");
        JButton loadFromFileButton = new JButton("Load from File");

        Dimension size = new Dimension(200, 20);
        loadFromFileButton.setPreferredSize(size);
        loadFromFileButton.setMaximumSize(size);

        loadFromFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                File xmlFile = TracerViewer.openFileChooser(loadFromFileButton, TracerViewer.class, "Load XML File",
                        null, prevLoadedFile);

                if (xmlFile != null) {

                    prevLoadedFile = xmlFile;

                    JTextArea errorMessageJTextArea = getErrorMessageJTextArea();

                    try {
                        FileReader fileReader = new FileReader(xmlFile);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);

                        JTextArea xmlSnippetTextArea = getXmlSnippetJTextArea();

                        xmlSnippetTextArea.read(bufferedReader, null);

                        errorMessageJTextArea.setText(null);

                    } catch (Exception e) {
                        LOG.error("Error loading xml file: " + xmlFile, e);

                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));

                        errorMessageJTextArea.setText(sw.toString());
                    }

                }
            }
        });

        Dimension dim = new Dimension(30, 30);

        pasteOrLoadJPanel.add(descriptionLabel);
        pasteOrLoadJPanel.add(Box.createRigidArea(dim));
        pasteOrLoadJPanel.add(loadFromFileButton);
        pasteOrLoadJPanel.add(Box.createHorizontalGlue());

        return pasteOrLoadJPanel;
    }

    private JPanel getErrorMessageJPanel() {

        JPanel errorMessageJPanel = new JPanel();

        errorMessageJPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.weightx = 1.0D;
        gbc1.weighty = 1.0D;
        gbc1.fill = GridBagConstraints.BOTH;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.insets = new Insets(5, 5, 5, 5);

        JComponent errorMessageTextAreaJComponent = getErrorMessageTextAreaJComponent();

        errorMessageJPanel.add(errorMessageTextAreaJComponent, gbc1);

        return errorMessageJPanel;
    }

    private JPanel getButtonsJPanel() {

        JPanel buttonsJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(buttonsJPanel, BoxLayout.X_AXIS);
        buttonsJPanel.setLayout(layout);

        JButton okJButton = getOkJButton();
        JButton cancelJButton = getCancelJButton();

        Dimension dim = new Dimension(30, 30);
        buttonsJPanel.add(Box.createHorizontalGlue());
        buttonsJPanel.add(Box.createRigidArea(dim));
        buttonsJPanel.add(okJButton);
        buttonsJPanel.add(Box.createRigidArea(dim));
        buttonsJPanel.add(cancelJButton);
        buttonsJPanel.add(Box.createRigidArea(dim));
        buttonsJPanel.add(Box.createHorizontalGlue());

        buttonsJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return buttonsJPanel;
    }

    private Element buildXMLElement() {

        Element xmlElement = null;

        JTextArea xmlSnippetTextArea = getXmlSnippetJTextArea();

        String xmlSnippet = xmlSnippetTextArea.getText();
        // not doing trim as data in UI should match the error, in any
        // xmlSnippet = xmlSnippet.trim();

        String charsetName = tracerViewerSetting.getCharset();
        Charset charset = Charset.forName(charsetName);

        byte[] xmlSnippetBytes = xmlSnippet.getBytes(charset);

        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding(charsetName);

        ByteArrayInputStream bais = new ByteArrayInputStream(xmlSnippetBytes);
        BufferedInputStream bis = new BufferedInputStream(bais);

        JTextArea errorMessageJTextArea = getErrorMessageJTextArea();
        errorMessageJTextArea.setText(null);

        Highlighter xmlSnippetTextAreaHighlighter = xmlSnippetTextArea.getHighlighter();
        xmlSnippetTextAreaHighlighter.removeAllHighlights();

        try {

            Document doc = saxReader.read(bis);
            xmlElement = doc.getRootElement();

        } catch (Exception e) {
            LOG.error("Error parsing xml snippet:", e);

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            errorMessageJTextArea.setText(sw.toString());

            if (e instanceof DocumentException) {
                DocumentException de = (DocumentException) e;

                Throwable throwable = de.getNestedException();

                if (throwable instanceof SAXParseException) {

                    SAXParseException saxParseException = (SAXParseException) throwable;

                    int lineNumber = saxParseException.getLineNumber();
                    int columnNumber = saxParseException.getColumnNumber();

                    javax.swing.text.Document document = xmlSnippetTextArea.getDocument();

                    if ((document.getLength() > 0) && (lineNumber >= 0) && (columnNumber >= 0)) {

                        javax.swing.text.Element element = document.getDefaultRootElement();

                        int lineStartPos = element.getElement(lineNumber - 1).getStartOffset();
                        int lineEndPos = element.getElement(lineNumber - 1).getEndOffset();

                        int charEndPos = lineStartPos + columnNumber;
                        int charStartPos = (charEndPos > 1) ? (charEndPos - 2) : charEndPos;

                        try {

                            Rectangle viewRect;
                            viewRect = xmlSnippetTextArea.modelToView(lineStartPos);

                            if (errorMessageJTextArea.getParent() instanceof JViewport) {

                                JViewport viewport = (JViewport) xmlSnippetTextArea.getParent();
                                GUIUtilities.scrollRectangleToVisible(viewport, viewRect);
                            } else {
                                xmlSnippetTextArea.scrollRectToVisible(viewRect);
                            }

                            xmlSnippetTextAreaHighlighter.addHighlight(charStartPos, charEndPos,
                                    saxParseExceptionHighlightPainter);

                            xmlSnippetTextAreaHighlighter.addHighlight(lineStartPos, lineEndPos,
                                    saxParseExceptionLineHighlightPainter);

                        } catch (BadLocationException ble) {
                            LOG.error("Error highlighting", ble);
                        }

                        xmlSnippetTextArea.setCaretPosition(charStartPos);
                        xmlSnippetTextArea.moveCaretPosition(charStartPos);
                    }
                }
            }
        }

        return xmlElement;
    }
}
