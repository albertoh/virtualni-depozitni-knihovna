/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.data;

import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.ListProvider;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author alberto
 */

public class SklizenStatus {
    
    static class DefaultListItem implements ListItem{
        private String name;
        private String value;



        public DefaultListItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

        
        @Override
        public String getValue() {
            return this.value;
        }

        @Override
        public String getName() {
            return this.name;
        }
    } 
    
    public enum Stav  {
        ZAHAJEN("zahajen"), UKONCEN("ukoncen"), CHYBA("chyba");

        private Stav(String value){
            this.value = value;
        }
        private String value;
        
        public String getValue() {
            return value;
        }

        public String getName() {
            return value;
        }

    }
    
    //private Map<Stav, String> map = new HashMap<Stav, String>();
    
    public static ListProvider getGroupList() {
        List<ListItem> returnsList = new ArrayList<ListItem>();
        for(Stav s: Stav.values()){
            returnsList.add(new DefaultListItem(s.getName(), s.getValue()));
        }
        return new ListProvider.Default(returnsList);
    }
}
