package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import com.alterjoc.radar.server.utils.Initialization;
import org.jboss.capedwarf.server.api.mvc.ActionController;

/**
 * Topic request.
 *
 * @author Ales Justin
 */
public class TopicController extends ActionController
{
   private Initialization initialization;
   private AllSinceTsTopicsAction findAllSinceTs;
   private CountSubscribedAction countAction;
   private CountSubscribedMultiAction countMultiAction;
   private TopicCommentsAction topicCommentsTs;

   @Override
   protected void doInitialize(ServletContext context)
   {
      super.doInitialize(context);

      // init
      initialization.initializeTopics();

      // initialize actions
      findAllSinceTs.initialize(context);
      countAction.initialize(context);
      topicCommentsTs.initialize(context);

      addAction("find-all-since-ts", findAllSinceTs);
      addAction("count-subscribed", countAction);
      addAction("count-subscribed-multi", countMultiAction);
      addAction("topic-comments-ts", topicCommentsTs);
   }

   @Inject
   public void setInitialization(Initialization initialization)
   {
      this.initialization = initialization;
   }

   @Inject
   public void setFindAllSinceTs(AllSinceTsTopicsAction findAllSinceTs)
   {
      this.findAllSinceTs = findAllSinceTs;
   }

   @Inject
   public void setCountAction(CountSubscribedAction countAction)
   {
      this.countAction = countAction;
   }

   @Inject
   public void setCountMultiAction(CountSubscribedMultiAction countMultiAction)
   {
      this.countMultiAction = countMultiAction;
   }

   @Inject
   public void setTopicCommentsTs(TopicCommentsAction topicCommentsTs)
   {
      this.topicCommentsTs = topicCommentsTs;
   }
}
