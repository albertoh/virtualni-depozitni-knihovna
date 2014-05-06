package cz.incad.vdkcr.server;

import cz.incad.vdkcr.server.functions.IndexDb;
import cz.incad.vdkcr.server.functions.RegenerateMD5;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.aplikator.server.ApplicationLoaderServlet;
import org.aplikator.server.descriptor.Application;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Menu;
import org.aplikator.server.processes.ProcessManager;
import org.quartz.SchedulerException;

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
            admin.addView(Structure.knihovna.view());
            admin.addView(Structure.offer.view());
            Function globalFunction = new Function("Reindex solr", "Reindex solr", new IndexDb());
            admin.addFunction(globalFunction);
            Function md5Function = new Function("Regenerate MD5", "Regenerate MD5", new RegenerateMD5());
            admin.addFunction(md5Function);
            struct.addMenu(admin);
            Structure.zdroj.setCron();
            LOG.info("vdkcr Loader finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "vdkcr Loader error:", ex);
            throw new ServletException("vdkcr Loader error: ", ex);
        }
    }

    @Override
    public void destroy() {
        try {
            Structure.zdroj.stopCron();
            ProcessManager.getManager().shutDown();
        } catch (SchedulerException ex) {
            Logger.getLogger(VdkcrLoaderServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
