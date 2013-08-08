/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.data;

import java.util.ArrayList;
import java.util.List;
import org.aplikator.client.shared.data.ListItem;
import org.aplikator.server.descriptor.ListProvider;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *
 * @author alberto
 */
@Portable
public class SklizenStatus {
    
    static class DefaultListItem implements ListItem<String>{
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
    
    public static ListProvider<String> getGroupList() {
        List<ListItem<String>> returnsList = new ArrayList<ListItem<String>>();
        for(Stav s: Stav.values()){
            returnsList.add(new DefaultListItem(s.getName(), s.getValue()));
        }
        return new ListProvider.Default<String>(returnsList);
    }
}