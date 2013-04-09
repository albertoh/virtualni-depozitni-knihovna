/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.rdcz;

/**
 *
 * @author Administrator
 */
public class ConfigQuery {

    public String query;
    public String description;

    public ConfigQuery(String _query, String _description) {
        query = _query;
        description = _description;
    }
}
