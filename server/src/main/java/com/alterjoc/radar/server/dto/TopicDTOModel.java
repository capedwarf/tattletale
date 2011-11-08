package com.alterjoc.radar.server.dto;

import javax.inject.Inject;

import com.alterjoc.radar.common.data.TopicInfo;
import com.alterjoc.radar.server.domain.Topic;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.server.api.io.BlobService;

/**
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicDTOModel implements DTOModel<Topic>
{
   private BlobService blobService;

   public Object toDTO(Topic entity)
   {
      TopicInfo ti = new TopicInfo();
      ti.setId(entity.getId());
      ti.setName(entity.getName());
      ti.setType(entity.getType());
      ti.setMode(entity.getMode());
      ti.setPhoto(blobService.loadBytes(entity.getImage()));
      long ts = entity.getTimestamp();
      ti.setTimestamp(ts);
      ti.setExpirationDelta(entity.getExpirationTime() - ts);
      return ti;
   }

   @Inject
   public void setBlobService(BlobService blobService)
   {
      this.blobService = blobService;
   }
}
