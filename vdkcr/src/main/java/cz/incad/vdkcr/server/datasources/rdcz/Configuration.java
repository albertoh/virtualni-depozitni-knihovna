/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.rdcz;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Incad
 */
public class Configuration {

    Logger logger = Logger.getLogger(this.getClass().getName());
    public HashMap<String, String> properties = new HashMap<String, String>();
    public ArrayList<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
    PreparedStatement psList;

    public ArrayList<String> queries = new ArrayList<String>();

    public Configuration(String filename) {
        try {
            logger.info("Loading configuration");
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!

            DocumentBuilder builder = domFactory.newDocumentBuilder();

            //InputSource source = new InputSource(new FileInputStream(new File(filename)));
            InputStream source = this.getClass().getResourceAsStream("res/"+filename+".xml");
            Document contentDom = builder.parse(source);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //Loading properties
            XPathExpression expr = xpath.compile("//setup/properties/property");
            NodeList propertiesNodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < propertiesNodes.getLength(); i++) {
                Node node = propertiesNodes.item(i);
                properties.put(node.getAttributes().getNamedItem("name").getNodeValue(),
                        node.getAttributes().getNamedItem("value").getNodeValue());
            }



            //Loading queries
            expr = xpath.compile("//setup/queries/query");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (childnode.getNodeName().equals("query")) {
                    queries.add(childnode.getFirstChild().getNodeValue());
                }
            }

            //Loading field mappings
            expr = xpath.compile("//setup/field_mappings/field_map");
            nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                fieldMappings.add(FieldMapping.CreateFieldMapping(nodes.item(i)));
            }

        } catch (Exception ex) {
            logger.severe("Can't load configuration");
            throw new RuntimeException(ex.toString());
        }
    }

    public String getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        } else {
            logger.log(Level.WARNING, "Can''t get property {0}", key);
            return null;
        }
    }

    public String getProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        } else {
            return defaultValue;
        }
    }
}
