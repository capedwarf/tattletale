package com.alterjoc.radar.server.mvc.impl;

import org.jboss.capedwarf.server.api.mvc.ActionController;

import javax.inject.Inject;
import javax.servlet.ServletContext;


/**
 * Event request.
 */
public class EventController extends ActionController
{
   private TopicEventsTsAction topicEventsTsAction;
   private EventCommentsTsAction eventCommentsTsAction;
   private OnDemandPhotoAction onDemandPhotoAction;

   @Override
   protected void doInitialize(ServletContext context)
   {
      super.doInitialize(context);    //To change body of overridden methods use File | Settings | File Templates.

      // initialize actions
      topicEventsTsAction.initialize(context);
      eventCommentsTsAction.initialize(context);
      onDemandPhotoAction.initialize(context);

      addAction("topic-events-ts", topicEventsTsAction);
      addAction("event-comments-ts", eventCommentsTsAction);
      addAction("on-demand-photo", onDemandPhotoAction);
   }

   @Inject
   public void setTopicEventsTsAction(TopicEventsTsAction topicEventsTsAction)
   {
      this.topicEventsTsAction = topicEventsTsAction;
   }

   @Inject
   public void setEventCommentsTsAction(EventCommentsTsAction eventCommentsTsAction)
   {
      this.eventCommentsTsAction = eventCommentsTsAction;
   }

   @Inject
   public void setOnDemandPhotoAction(OnDemandPhotoAction onDemandPhotoAction)
   {
      this.onDemandPhotoAction = onDemandPhotoAction;
   }
}
