package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.common.dto.DTOModelFactory;
import org.jboss.capedwarf.common.serialization.JSONCollectionSerializator;
import org.jboss.capedwarf.common.serialization.Serializator;
import org.jboss.capedwarf.server.api.mvc.AbstractCollectionsAction;

import javax.inject.Inject;

/**
 * Abstract events action.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractEventsAction extends AbstractCollectionsAction<Event>
{
   private static final Serializator ts = new JSONCollectionSerializator(EventInfo.class);
   private DTOModel<Event> dtoModel;

   protected Serializator getSerializator()
   {
      return ts;
   }

   protected DTOModel<Event> getDtoModel()
   {
      return dtoModel;
   }

   @Inject
   public void setDTOModelFactory(DTOModelFactory factory)
   {
      dtoModel = factory.createModel(Event.class);
   }
}
