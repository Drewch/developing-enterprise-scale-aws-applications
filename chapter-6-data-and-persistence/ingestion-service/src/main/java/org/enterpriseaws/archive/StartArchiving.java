package org.enterpriseaws.archive;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.quartz.JobBuilder;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;


public class StartArchiving extends HttpServlet {

  private final String APPLICATION_JSON   = "application/json";
  private SchedulerFactory sf;

  public void init(ServletConfig config) {
    sf = new StdSchedulerFactory();
    try {
      sf.getScheduler().start();
    } catch (SchedulerException se) {
      se.printStackTrace();
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    try {

     // Schedule hot archive job
     new StdSchedulerFactory().getScheduler().scheduleJob(
         JobBuilder.newJob()
           .ofType(HotArchiveJob.class)
           .withIdentity("hot-archive-job")
           .build(),
         TriggerBuilder.newTrigger()
           .withSchedule(SimpleScheduleBuilder
               .simpleSchedule()
                 .withIntervalInHours(144)
                 .repeatForever()
               )
           .build());

     // Schedule warm archive job
     new StdSchedulerFactory().getScheduler().scheduleJob(
         JobBuilder.newJob()
           .ofType(WarmArchiveJob.class)
           .withIdentity("warm-archive-job")
           .build(),
         TriggerBuilder.newTrigger()
           .withSchedule(SimpleScheduleBuilder
               .simpleSchedule()
                 .withIntervalInMinutes(1)
                 .repeatForever()
               )
           .build());

       // Schedule cold archive job
       new StdSchedulerFactory().getScheduler().scheduleJob(
           JobBuilder.newJob()
             .ofType(ColdArchiveJob.class)
             .withIdentity("cold-archive-job")
             .build(),
           TriggerBuilder.newTrigger()
             .withSchedule(SimpleScheduleBuilder
                 .simpleSchedule()
                   .withIntervalInMinutes(2)
                   .repeatForever()
                 )
             .build());

    } catch( SchedulerException se) {
      se.printStackTrace();
    }

  }

  public void destroy() {
    try {
      sf.getScheduler().shutdown();
    } catch (SchedulerException se) {
      se.printStackTrace();
    }
  }

}
