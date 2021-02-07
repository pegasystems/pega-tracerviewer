/*******************************************************************************
 *  Copyright (c) 2021 Pegasystems Inc. All rights reserved.
 *
 *  Contributors:
 *      Manu Varghese
 *******************************************************************************/

package com.pega.gcs.tracerviewer;

import java.awt.Component;
import java.awt.Dimension;
import java.nio.charset.Charset;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.dom4j.Element;

import com.pega.gcs.fringecommon.guiutilities.BaseFrame;
import com.pega.gcs.fringecommon.guiutilities.treetable.TreeTableColumn;
import com.pega.gcs.fringecommon.xmltreetable.XMLElementType;
import com.pega.gcs.fringecommon.xmltreetable.XMLTreeTableDetailPanel;

public class TraceXMLTreeTableFrame extends JFrame {

    private static final long serialVersionUID = -7723542575088644558L;

    public TraceXMLTreeTableFrame(String title, Element[] traceEventPropertyElementArray, String[] searchStrArray,
            TreeTableColumn[] treeTableColumnArray, Map<String, XMLElementType> xmlElementTypeMap, Charset charset,
            Component parent) {

        super();

        XMLTreeTableDetailPanel xmlTreeTableDetailPanel;
        xmlTreeTableDetailPanel = new XMLTreeTableDetailPanel(traceEventPropertyElementArray, searchStrArray,
                treeTableColumnArray, xmlElementTypeMap, charset);

        Dimension dimension = new Dimension(1000, 800);

        ImageIcon appIcon = BaseFrame.getAppIcon();

        setIconImage(appIcon.getImage());
        setPreferredSize(dimension);
        setTitle(title);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setContentPane(xmlTreeTableDetailPanel);

        pack();

        setLocationRelativeTo(parent);

        // visible should be the last step
        setVisible(true);
    }

}
