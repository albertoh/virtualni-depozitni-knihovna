/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.aplikator.client.local.command.AplikatorActivity;

/**
 *
 * @author alberto
 */
public class SearchActivity extends AplikatorActivity {

    
    public SearchActivity(String id) {
        super(id);
    }
    
    private Widget contents = null;

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        if (contents == null) {
            Label l = new Label("jsem tady");
            contents = l;
        }
        panel.setWidget(contents);

    }

    
}
