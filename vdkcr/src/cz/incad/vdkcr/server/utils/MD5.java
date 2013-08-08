/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CheckedInputStream;

import java.util.zip.Adler32;
 
import java.io.*;
/**
 * 
 * @author alberto
 */
public class MD5 {

    /**
     * 
     * @param value
     * @return
     */
    public static String integerMD5(String value){
        //long l = checkSum(generate(value));
        //System.out.println(value + " -> " + l);
        return Long.toString(checkSum(generate(value)));
    }
    
    public static String generate(String[] params) {
        String key = "";
        for(String s: params){
            key += s.replaceAll(" ", "").toLowerCase();
        }
        return generate(key);
    }

    /**
     * 
     * @param value
     * @return
     */
    public static String generate(String value) {
        String md5val = "";
        MessageDigest algorithm = null;

        try {
            algorithm = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            return null;
        }

        byte[] defaultBytes = value.getBytes();
        algorithm.reset();
        algorithm.update(defaultBytes);
        byte messageDigest[] = algorithm.digest();
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < messageDigest.length; i++) {
            String hex = Integer.toHexString(0xFF & messageDigest[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        md5val = hexString.toString();
        return md5val;
    }

    /**
     * 
     * @param value
     * @return
     */
    public static long checkSum(String value) {
        try {
            byte buffer[] = value.getBytes();
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            CheckedInputStream cis = new CheckedInputStream(bais, new Adler32());
            byte readBuffer[] = new byte[5];
            if (cis.read(readBuffer) >= 0) {
                return cis.getChecksum().getValue();
            }else{
                return 0;
            }
        } catch (Exception e) {
            System.out.println("Exception has been caught" + e);
            return 0;
        }
    }
}


