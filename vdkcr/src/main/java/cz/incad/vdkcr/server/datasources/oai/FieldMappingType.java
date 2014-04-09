/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.oai;

/**
 *
 * @author Administrator
 */
public enum FieldMappingType {

    STRING, INTEGER, BOOLEAN, BLOB, CLOB, DATE, STRINGLIST, USER, USERLIST, CONSTANT, BINARY;

    public static FieldMappingType parseString(String s) {
        if (s.equalsIgnoreCase("string")) {
            return FieldMappingType.STRING;
        } else if (s.equalsIgnoreCase("integer")) {
            return FieldMappingType.INTEGER;
        } else if (s.equalsIgnoreCase("binary")) {
            return FieldMappingType.BINARY;
        } else if (s.equalsIgnoreCase("boolean")) {
            return FieldMappingType.BOOLEAN;
        } else if (s.equalsIgnoreCase("blob")) {
            return FieldMappingType.BLOB;
        } else if (s.equalsIgnoreCase("clob")) {
            return FieldMappingType.CLOB;
        } else if (s.equalsIgnoreCase("date")) {
            return FieldMappingType.DATE;
        } else if (s.equalsIgnoreCase("stringlist")) {
            return FieldMappingType.STRINGLIST;
        } else if (s.equalsIgnoreCase("user")) {
            return FieldMappingType.USER;
        } else if (s.equalsIgnoreCase("constant")) {
            return FieldMappingType.CONSTANT;
        } else if (s.equalsIgnoreCase("userlist")) {
            return FieldMappingType.USERLIST;
        } else {
            throw new RuntimeException("Unsupported type");
        }
    }
}
