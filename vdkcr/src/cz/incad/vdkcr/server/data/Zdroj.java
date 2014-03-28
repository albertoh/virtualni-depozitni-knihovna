package cz.incad.vdkcr.server.data;

import cz.incad.vdkcr.server.Structure;
import cz.incad.vdkcr.server.datasources.oai.ZdrojJob;
import static org.aplikator.server.descriptor.Panel.column;
import static org.aplikator.server.descriptor.Panel.row;
import static org.aplikator.server.descriptor.RepeatedForm.repeated;

import org.aplikator.server.descriptor.Collection;
import org.aplikator.server.descriptor.Entity;
import org.aplikator.server.descriptor.Function;
import org.aplikator.server.descriptor.Property;
import org.aplikator.server.descriptor.View;

import cz.incad.vdkcr.server.functions.SkliditZdroj;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aplikator.client.shared.data.Record;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.Persister;
import org.aplikator.server.persistence.PersisterFactory;
import org.aplikator.server.persistence.PersisterTriggers;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.core.jmx.JobDataMapSupport;
import org.quartz.impl.StdSchedulerFactory;

public class Zdroj extends Entity {

    private static final Logger LOGGER = Logger.getLogger(Zdroj.class.getName());
    public Property<String> typZdroje;
    public Property<String> nazev;
    public Property<String> formatXML;
    public Property<String> trida;
    public Property<String> parametry;
    public Property<String> cron;
    public Collection<Sklizen> sklizen;

    public Function skliditZdroj = new Function("SkliditZdroj", "SkliditZdroj", new SkliditZdroj());

    Scheduler sched;

    public Zdroj() {
        super("Zdroj", "Zdroj", "Zdroj_ID");
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            sched = sf.getScheduler();
        } catch (SchedulerException ex) {
            Logger.getLogger(Zdroj.class.getName()).log(Level.SEVERE, null, ex);
        }
        initFields();
//        setCron();
    }

    public void setCron() throws SchedulerException {

        View retval = new View(this, "cron");
        retval.addProperty(nazev).addProperty(cron).addProperty(trida).addProperty(parametry);

        Persister persister = PersisterFactory.getPersister();
        //Connection conn = persister.getJDBCConnection();
        List<Record> lr = persister.getRecords(retval, null, null, null, null, 0, 0, null);
        for (Record rec : lr) {
            String cronVal = cron.getValue(rec);
            if (cronVal != null) {
                addJob(rec, cronVal);
            }
        }
        sched.start();
    }

    private void addJob(Record rec, String cronVal) throws SchedulerException {

        String name = rec.getPrimaryKey().getSerializationString();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("record", rec);
        JobDataMap data = JobDataMapSupport.newJobDataMap(map);

        JobDetail job = newJob(ZdrojJob.class)
                .withIdentity("job_" + name, "Zdroj")
                .setJobData(data)
                .build();

        CronTrigger trigger = newTrigger()
                .withIdentity("trigger_" + name, "Zdroj")
                .withSchedule(cronSchedule(cronVal))
                .build();
        if (sched.checkExists(job.getKey())) {
            sched.deleteJob(job.getKey());
        }
        sched.scheduleJob(job, trigger);
        LOGGER.log(Level.INFO, "Cron for " + name + " scheduled with " + cronVal);
    }

    public void stopCron() throws SchedulerException {
        if (sched != null) {
            sched.shutdown(true);
        }
    }

    private void initFields() {
        typZdroje = stringProperty("typZdroje");
        nazev = stringProperty("nazev");
        formatXML = stringProperty("formatXML");
        trida = stringProperty("trida");
        parametry = stringProperty("parametry");
        cron = stringProperty("cron");

        this.setPersistersTriggers(new PersisterTriggers.Default() {

            @Override
            public void onLoad(Record record, Context ctx) {
                record.setPreview("<b>" + record.getValue(Structure.zdroj.nazev.getId())
                        + "</b> (" + record.getValue(Structure.zdroj.typZdroje.getId()) + ")"
                );
            }

            @Override
            public void afterCommit(Record rec, Context ctx) {
                Object c = rec.getValue(Structure.zdroj.cron.getId());
                if (c != null) {
                    Record record = ctx.getAplikatorService().getCompleteRecord(rec.getPrimaryKey().getSerializationString(), 1, true);
                    String cronVal = c.toString();
                    try {
                        addJob(rec, cronVal);
                    } catch (SchedulerException ex) {
                        Logger.getLogger(Zdroj.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else {
                    LOGGER.log(Level.INFO, "Cron has no changed ");
                }
            }

        });
    }

    @Override
    protected View initDefaultView() {
        View retval = new View(this);
        retval.addProperty(nazev).addProperty(typZdroje);
        retval.form(column(
                row(column(nazev).setSize(4), column(typZdroje).setSize(3), column(formatXML).setSize(3)),
                row(trida.widget().setSize(4), parametry.widget().setSize(6)),
                row(cron),
                row(skliditZdroj),
                row(repeated(sklizen))
        ), false);

        return retval;
    }

}
