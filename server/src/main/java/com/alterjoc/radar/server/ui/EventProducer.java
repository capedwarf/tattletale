package com.alterjoc.radar.server.ui;

import com.alterjoc.radar.server.domain.Topic;

/**
 * Produce event.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface EventProducer
{
   /**
    * Submit system event.
    *
    * @param topic the event's topic
    * @param title the event title
    * @param comment the comment
    * @param address the address
    */
   void submitSystemEvent(Topic topic, String title, String comment, String address);
}
