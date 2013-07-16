package cz.incad.vdkcr.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Menu;


@SuppressWarnings("serial")
public class VdkcrLoaderServlet extends ApplicationLoaderServlet {

    private static final Logger LOG = Logger.getLogger(VdkcrLoaderServlet.class.getName());

    Structure struct;


    @Override
    public void init() throws ServletException {
        try {
            LOG.info("vdkcr Loader started");
            struct = (Structure) Application.get();
            Menu records = new Menu("Zaznamy");
            records.addView(Structure.zaznam.view());
            struct.addMenu(records);
            Menu admin = new Menu("Admin");
            admin.addView(Structure.zdroj.view());
            admin.addView(Structure.status.view());
            //Function globalFunction = new Function("GlobalFunction", "GlobalFunction", new ReindexFast());
            //admin.addFunction(globalFunction);
            struct.addMenu(admin);

            LOG.info("vdkcr Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "vdkcr Loader error:", ex);
            throw new ServletException("vdkcr Loader error: ", ex);
        }
    }


}
