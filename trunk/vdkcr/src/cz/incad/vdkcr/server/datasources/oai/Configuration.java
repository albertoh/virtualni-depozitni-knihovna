/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.oai;

import java.io.File;
import java.io.FileInputStream;
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
import org.aplikator.server.util.Configurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Alberto Hernandez
 */
public class Configuration {

    static final Logger logger = Logger.getLogger(Configuration.class.getName());
    public HashMap<String, String> properties = new HashMap<String, String>();
    public ArrayList<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
    PreparedStatement psList;
    public ArrayList<String> queries = new ArrayList<String>();

    public Configuration(String filename) {
        try {
            logger.info("Loading configuration");
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false);

            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document contentDom;
            //check home directory
            String home = Configurator.get().getConfig().getString(Configurator.HOME)
                + File.separator + "OAI" + File.separator + filename + ".xml";
            File file = new File(home);
                logger.log(Level.INFO, home);
            if (file.exists()) {
                logger.log(Level.INFO, "Loading from home: " + home);
                InputSource source = new InputSource(new FileInputStream(file));
                contentDom = builder.parse(source);
            } else {
                InputStream source = this.getClass().getResourceAsStream("res/" + filename + ".xml");
                contentDom = builder.parse(source);
            }



            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            //Loading properties
            XPathExpression expr = xpath.compile("//setup/properties/property");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                properties.put(node.getAttributes().getNamedItem("name").getNodeValue(),
                        node.getAttributes().getNamedItem("value").getNodeValue());
            }

            //Loading field mappings
            expr = xpath.compile("//setup/field_mappings/field_map");
            nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                fieldMappings.add(FieldMapping.CreateFieldMapping(nodes.item(i)));
            }

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Can't load configuration from " + filename, ex);
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
