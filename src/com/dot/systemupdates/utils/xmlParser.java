package com.dot.systemupdates.utils;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class xmlParser extends AsyncTask<String, Integer, String[]> {
    NodeList nodeList, changelogList;
    String[] xmlValues = new String[9];
    @Override
    protected String[] doInBackground(String... strings) {
        try {
            URL url = new URL(strings[0]);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(url.openStream()));
            document.getDocumentElement().normalize();
            nodeList = document.getElementsByTagName("ROM");
            Node node = nodeList.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList versionNL = element.getElementsByTagName("VersionName").item(0).getChildNodes();
                Node versionNode = versionNL.item(0);
                NodeList changelogNL = element.getElementsByTagName("Changelog").item(0).getChildNodes();
                Node changelogNode = changelogNL.item(0);
                NodeList urlNL = element.getElementsByTagName("DirectUrl").item(0).getChildNodes();
                Node urlNode = urlNL.item(0);
                NodeList sizeINL = element.getElementsByTagName("FileSize").item(0).getChildNodes();
                Node sizeNode = sizeINL.item(0);
                xmlValues[0] = versionNode.getNodeValue();
                xmlValues[1] = changelogNode.getNodeValue();
                xmlValues[2] = urlNode.getNodeValue();
                xmlValues[3] = sizeNode.getNodeValue();
                changelogList = document.getElementsByTagName("Changelog");
                Node node2 = nodeList.item(0);
                if (node2.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node2;
                    Node system = element2.getElementsByTagName("System").item(0);
                    Node systemNode;
                    Node settings = element2.getElementsByTagName("Settings").item(0);
                    Node settingsNode;
                    Node device = element2.getElementsByTagName("Device").item(0);
                    Node deviceNode;
                    Node sec_patch = element2.getElementsByTagName("SecurityPatch").item(0);
                    Node sec_patchNode;
                    Node misc = element2.getElementsByTagName("Misc").item(0);
                    Node miscNode ;
                    if (system != null) {
                        systemNode = system.getChildNodes().item(0);
                        xmlValues[4] = systemNode.getNodeValue();
                    } else {
                        xmlValues[4] = null;
                    }
                    if (settings != null) {
                        settingsNode = settings.getChildNodes().item(0);
                        xmlValues[5] = settingsNode.getNodeValue();
                    } else {
                        xmlValues[5] = null;
                    }
                    if (device != null) {
                        deviceNode = device.getChildNodes().item(0);
                        xmlValues[6] = deviceNode.getNodeValue();
                    } else {
                        xmlValues[6] = null;
                    }
                    if (sec_patch != null) {
                        sec_patchNode = sec_patch.getChildNodes().item(0);
                        xmlValues[7] = sec_patchNode.getNodeValue();
                    } else {
                        xmlValues[7] = null;
                    }
                    if (misc != null) {
                        miscNode = misc.getChildNodes().item(0);
                        xmlValues[8] = miscNode.getNodeValue();
                    } else {
                        xmlValues[8] = null;
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return xmlValues;
    }
}