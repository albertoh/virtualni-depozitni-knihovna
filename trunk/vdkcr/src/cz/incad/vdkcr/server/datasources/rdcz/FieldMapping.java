/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.rdcz;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Node;

/**
 *
 * @author Alberto
 */
public class FieldMapping {

    public String name;
    //public ArrayList<String> sources = new ArrayList<String>();
    public String source;
    public FieldMappingType type;
    public boolean hasRange = false;
    public boolean isMultiple = false;
    public int startIndex;
    public int endIndex;
    public String separator = ";";
    public String booleanTrueIs;
    public String booleanFalseIs;
    public String list;
    public String dateFormat;
    public String transform = "";

    public FieldMapping(String _name, String _source, FieldMappingType _type, String _range) {

        name = _name;
        //sources.add(_source);
        source =_source;
        type = _type;
        if (!_range.equals("")) {
            startIndex = Integer.parseInt(_range.split("-")[0]);
            endIndex = Integer.parseInt(_range.split("-")[1]);
            hasRange = true;
        }
    }

    public FieldMapping(String _name, String _source, FieldMappingType _type) {
        name = _name;
        //sources.add(_source);
        source =_source;
        type = _type;

    }

    public String applyTransform(String value){
        if(transform.equals("")){
            return value;
        }else{
            String[] args = transform.split(separator);
            if(args[0].equalsIgnoreCase("replace")){
                return value.replace(args[1], args[2]);
            }else if(args[0].equalsIgnoreCase("replaceLines")){
                return value.replace("\n", args[1]);
            }else if(args[0].equalsIgnoreCase("replaceFromXml")){
                Properties prop = new Properties();
                try {
                    prop.loadFromXML(new FileInputStream(args[1]));
                    value = prop.getProperty(value, value);
                } catch (IOException ex) {
                    Logger.getLogger(FieldMapping.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(FieldMapping.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return value;
    }

    public static FieldMapping CreateFieldMapping(Node node) {

        String range = "";

        if (node.getAttributes().getNamedItem("range") != null) {
            range = node.getAttributes().getNamedItem("range").getNodeValue();
        }
        String fast = node.getAttributes().getNamedItem("fast")!=null ?
            node.getAttributes().getNamedItem("fast").getNodeValue() :
            node.getAttributes().getNamedItem("source").getNodeValue();
        FieldMapping fm = new FieldMapping(fast,
                node.getAttributes().getNamedItem("source").getNodeValue(),
                FieldMappingType.parseString(node.getAttributes().getNamedItem("type").getNodeValue()), range);

        if (node.getAttributes().getNamedItem("booleanTrueIs") != null) {
            fm.booleanTrueIs = node.getAttributes().getNamedItem("booleanTrueIs").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("booleanFalseIs") != null) {
            fm.booleanFalseIs = node.getAttributes().getNamedItem("booleanFalseIs").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("separator") != null) {
            fm.separator = node.getAttributes().getNamedItem("separator").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("dateformat") != null) {
            fm.dateFormat = node.getAttributes().getNamedItem("dateformat").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("list") != null) {
            fm.list = node.getAttributes().getNamedItem("list").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("transform") != null) {
            fm.transform = node.getAttributes().getNamedItem("transform").getNodeValue();
        }
        if (node.getAttributes().getNamedItem("isMultiple") != null) {
            fm.isMultiple = Boolean.parseBoolean(node.getAttributes().getNamedItem("isMultiple").getNodeValue());
        }


        return fm;
    }

}
