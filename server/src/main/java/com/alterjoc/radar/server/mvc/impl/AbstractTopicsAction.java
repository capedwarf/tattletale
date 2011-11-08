package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.common.dto.DTOModelFactory;
import org.jboss.capedwarf.common.serialization.JSONCollectionSerializator;
import org.jboss.capedwarf.common.serialization.Serializator;
import org.jboss.capedwarf.server.api.mvc.AbstractCollectionsAction;

import javax.inject.Inject;

/**
 * Abstract topics action.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractTopicsAction extends AbstractCollectionsAction<Topic>
{
   private static final Serializator ts = new JSONCollectionSerializator(TopicInfo.class);
   private DTOModel<Topic> dtoModel;

   protected Serializator getSerializator()
   {
      return ts;
   }

   protected DTOModel<Topic> getDtoModel()
   {
      return dtoModel;
   }

   @Inject
   public void setDTOModelFactory(DTOModelFactory factory)
   {
      dtoModel = factory.createModel(Topic.class);
   }
}
