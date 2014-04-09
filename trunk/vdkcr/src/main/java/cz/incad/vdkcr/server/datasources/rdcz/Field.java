/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.rdcz;

import java.util.ArrayList;

/**
 *
 * @author alberto
 */
public class Field {

    public ArrayList<Object> values;
    public String name;
    public FieldMappingType type;
    private String separator;

    Field(String name, Object value, FieldMappingType fieldMappingType, String separator) {
        this.name = name;
        this.separator = separator;
        this.type = fieldMappingType;
//        if (fieldMappingType == FieldMappingType.STRING) {
//            values = new ArrayList<String>();
//        } else if (fieldMappingType == FieldMappingType.INTEGER) {
//            values = new ArrayList<Integer>();
//        } else if (fieldMappingType == FieldMappingType.BOOLEAN) {
//            values = new ArrayList<Boolean>();
//        } else if (fieldMappingType == FieldMappingType.DATE) {
//            values = new ArrayList<Date>();
//        } else if (fieldMappingType == FieldMappingType.BINARY) {
//            values = new ArrayList<Object>();
//        }
        values = new ArrayList<Object>();
        values.add(value);

    }

    public String stringValue() {
        if (type != FieldMappingType.STRING) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            sb.append((String) values.get(i));
            if (i < values.size() - 1) {
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    /**
     * @return the value
     */
    public ArrayList<Object> getValue() {
        return values;
    }

    /**
     * @param value the value to set
     */
    public void addValue(Object value) {
        this.values.add(value);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
