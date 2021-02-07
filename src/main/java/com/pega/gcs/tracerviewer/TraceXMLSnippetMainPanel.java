/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.xmltreetable.XMLTreeTableDetailPanel;

public class TraceXMLSnippetMainPanel extends JPanel {

    private static final long serialVersionUID = 606459104147458867L;

    private Element xmlElement;

    private TracerViewerSetting tracerViewerSetting;

    private JButton compareJButton;

    private JPanel xmlTreeTablePanel;

    public TraceXMLSnippetMainPanel(Element xmlElement, TracerViewerSetting tracerViewerSetting) {

        super();

        this.xmlElement = xmlElement;
        this.tracerViewerSetting = tracerViewerSetting;

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

        JPanel utilityJPanel = getUtilityJPanel();
        JPanel xmlTreeTablePanel = getXmlTreeTablePanel();

        add(utilityJPanel, gbc1);
        add(xmlTreeTablePanel, gbc2);

        Element[] traceEventPropertyElementArray = new Element[1];
        traceEventPropertyElementArray[0] = xmlElement;

        populateXMLTreeTableJPanel(traceEventPropertyElementArray);
    }

    private JButton getCompareJButton() {

        if (compareJButton == null) {

            compareJButton = new JButton("Compare XML Snippet");

            Dimension size = new Dimension(200, 20);
            compareJButton.setPreferredSize(size);
            compareJButton.setMaximumSize(size);

            compareJButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {

                    String title = "Load XML Snippet";

                    TraceXMLLoadSnippetJDialog traceXMLLoadSnippetJDialog;
                    traceXMLLoadSnippetJDialog = new TraceXMLLoadSnippetJDialog(title, tracerViewerSetting,
                            BaseFrame.getAppIcon(), TraceXMLSnippetMainPanel.this);

                    Element compareXMLElement = traceXMLLoadSnippetJDialog.getXmlElement();

                    if (compareXMLElement != null) {

                        Element[] traceEventPropertyElementArray = new Element[2];
                        traceEventPropertyElementArray[0] = xmlElement;
                        traceEventPropertyElementArray[1] = compareXMLElement;

                        populateXMLTreeTableJPanel(traceEventPropertyElementArray);

                    }

                }
            });

        }

        return compareJButton;
    }

    private JPanel getXmlTreeTablePanel() {

        if (xmlTreeTablePanel == null) {

            xmlTreeTablePanel = new JPanel();
            xmlTreeTablePanel.setLayout(new BorderLayout());

        }

        return xmlTreeTablePanel;
    }

    private JPanel getUtilityJPanel() {

        JPanel utilityJPanel = new JPanel();

        LayoutManager layout = new BoxLayout(utilityJPanel, BoxLayout.LINE_AXIS);
        utilityJPanel.setLayout(layout);

        Dimension spacer = new Dimension(15, 30);

        JButton compareJButton = getCompareJButton();

        utilityJPanel.add(Box.createHorizontalGlue());
        utilityJPanel.add(Box.createRigidArea(spacer));
        utilityJPanel.add(compareJButton);
        utilityJPanel.add(Box.createRigidArea(spacer));
        utilityJPanel.add(Box.createHorizontalGlue());

        utilityJPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));

        return utilityJPanel;
    }

    private void populateXMLTreeTableJPanel(Element[] traceEventPropertyElementArray) {

        int arraySize = traceEventPropertyElementArray.length;

        String[] searchStrArray = new String[arraySize];
        TreeTableColumn[] treeTableColumnArray = new TreeTableColumn[arraySize + 1];
        treeTableColumnArray[0] = TreeTableColumn.NAME_COLUMN;
        treeTableColumnArray[1] = TreeTableColumn.VALUE_COLUMN;

        if (arraySize > 1) {
            treeTableColumnArray[2] = new TreeTableColumn("Compare", String.class);
        }

        String charsetName = tracerViewerSetting.getCharset();
        Charset charset = Charset.forName(charsetName);

        XMLTreeTableDetailPanel xmlTreeTableDetailPanel;
        xmlTreeTableDetailPanel = new XMLTreeTableDetailPanel(traceEventPropertyElementArray, searchStrArray,
                treeTableColumnArray, TraceEventFactory.xmlElementTableTypeMap, charset);

        JPanel xmlTreeTablePanel = getXmlTreeTablePanel();

        xmlTreeTablePanel.removeAll();

        xmlTreeTablePanel.add(xmlTreeTableDetailPanel, BorderLayout.CENTER);

        xmlTreeTablePanel.revalidate();

    }
}
