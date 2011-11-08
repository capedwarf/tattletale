package com.alterjoc.radar.server.mvc.impl;

import org.jboss.capedwarf.server.api.mvc.ActionController;

import javax.inject.Inject;
import javax.servlet.ServletContext;

/**
 * Server actions.
 *
 * @author Ales Justin
 */
public class ServerController extends ActionController
{
   private TimestampAction timestampAction;
   private ModificationAction modificationAction;
   private RecoveryAction recoveryAction;

   @Override
   protected void doInitialize(ServletContext context)
   {
      super.doInitialize(context);

      // initialize actions
      timestampAction.initialize(context);
      modificationAction.initialize(context);
      recoveryAction.initialize(context);

      // login, user handling, ...
      addAction("timestamp", timestampAction);
      addAction("modification", modificationAction);
      addAction("recovery", recoveryAction);
   }

   @Inject
   public void setTimestampAction(TimestampAction timestampAction)
   {
      this.timestampAction = timestampAction;
   }

   @Inject
   public void setModificationAction(ModificationAction modificationAction)
   {
      this.modificationAction = modificationAction;
   }

   @Inject
   public void setRecoveryAction(RecoveryAction recoveryAction)
   {
      this.recoveryAction = recoveryAction;
   }
}
