package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.server.domain.Comment;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.common.dto.DTOModelFactory;
import org.jboss.capedwarf.common.serialization.JSONCollectionSerializator;
import org.jboss.capedwarf.common.serialization.Serializator;
import org.jboss.capedwarf.server.api.mvc.AbstractCollectionsAction;

import javax.inject.Inject;

/**
 * Abstract comments action.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractCommentsAction extends AbstractCollectionsAction<Comment>
{
   private static final Serializator ts = new JSONCollectionSerializator(CommentInfo.class);
   private DTOModel<Comment> dtoModel;

   protected Serializator getSerializator()
   {
      return ts;
   }

   protected DTOModel<Comment> getDtoModel()
   {
      return dtoModel;
   }

   @Inject
   public void setDTOModelFactory(DTOModelFactory factory)
   {
      dtoModel = factory.createModel(Comment.class);
   }
}
