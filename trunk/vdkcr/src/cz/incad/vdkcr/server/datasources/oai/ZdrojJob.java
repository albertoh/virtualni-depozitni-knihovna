/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.vdkcr.server.datasources.oai;

import cz.incad.vdkcr.server.VdkcrLoaderServlet;
import cz.incad.vdkcr.server.functions.SkliditZdroj;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.client.shared.rpc.AplikatorService;
import org.aplikator.server.function.FunctionResult;
import org.aplikator.server.impl.ContextImpl;
import org.aplikator.server.rpc.AplikatorServiceImpl;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.SchedulerException;

/**
 *
 * @author alberto
 */
public class ZdrojJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(ZdrojJob.class.getName());

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            String jobKey = jec.getJobDetail().getKey().toString();
            int i = 0;
            for (JobExecutionContext j : jec.getScheduler().getCurrentlyExecutingJobs()) {
                if (jobKey.equals(j.getJobDetail().getKey().toString())) {
                    i++;
                }
            }
            if (i > 1) {
                LOGGER.log(Level.INFO, "jobKey {0} is still running. Nothing to do.", jobKey);
                return;
            }

            AplikatorService service = new AplikatorServiceImpl();
            JobDataMap data = jec.getJobDetail().getJobDataMap();
            Record rec = (Record) data.get("record");
            //LOGGER.info(jobKey + " executing at " + new Date());
            LOGGER.log(Level.INFO, "jobKey: {0}", jobKey);

            SkliditZdroj f = new SkliditZdroj();
            FunctionResult fr = f.run(rec, new ContextImpl(null, null, service), true);

        } catch (SchedulerException ex) {
            Logger.getLogger(ZdrojJob.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
