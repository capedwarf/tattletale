package com.alterjoc.radar.server.mvc.impl;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.jboss.capedwarf.common.env.Secure;
import org.jboss.capedwarf.server.api.mvc.ActionController;


/**
 * Secure request -- https.
 * They include user info at each request.
 *
 * @author Ales Justin
 */
@Secure
public class ProfileController extends ActionController
{
   private CreateUserAction createUserAction;
   
   @Override
   protected void doInitialize(ServletContext context)
   {
      super.doInitialize(context);

      // initialize actions
      createUserAction.initialize(context);

      // login, user handling, ...
      addAction("create-user", createUserAction);
      // topics
      addActionClass("add-new-topic", CreateNewTopicAction.class);
      addActionClass("subscribed-topics", SubscribedTopicsAction.class);
      addActionClass("add-topic", AddTopicToClientAction.class);
      addActionClass("remove-topic", RemoveTopicFromClientAction.class);
      addActionClass("client-topics", ClientsTopicsAction.class);
      // events
      addActionClass("post-event", EventInitialAction.class);
      addActionClass("client-events", ClientEventsAction.class);
      // comments
      addActionClass("add-comment", AddCommentAction.class);
   }

   @Inject
   public void setCreateUserAction(CreateUserAction createUserAction)
   {
      this.createUserAction = createUserAction;
   }
}
