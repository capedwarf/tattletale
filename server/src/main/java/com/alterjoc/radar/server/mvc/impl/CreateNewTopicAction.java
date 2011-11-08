package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.server.domain.Client;
import com.alterjoc.radar.server.topics.TopicManager;
import com.alterjoc.radar.server.users.Limits;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.StatusInfo;
import org.jboss.capedwarf.server.api.mvc.impl.StatusInfoAbstractAction;
import org.jboss.capedwarf.server.api.quilifiers.Current;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Create new topic.
 * 
 * @author Dejan Pazin
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CreateNewTopicAction extends StatusInfoAbstractAction
{
   private Limits limits;
   private TopicManager topicManager;
   private Client client;

   protected StatusInfo doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      TopicInfo ti = deserialize(req, TopicInfo.class);
      if (limits.canCreateNewTopic(client.getId()))
      {
         return topicManager.createTopic(ti, client);
      }
      else
      {
         return new StatusInfo(Status.EXCEEDED_LIMIT);
      }
   }

   @Inject
   public void setLimits(Limits limits)
   {
      this.limits = limits;
   }

   @Inject
   public void setTopicManager(TopicManager topicManager)
   {
      this.topicManager = topicManager;
   }

   @Inject
   public void setClient(@Current Client client)
   {
      this.client = client;
   }
}
