package com.alterjoc.radar.server.mvc.impl;

import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alterjoc.radar.common.data.Image;
import com.alterjoc.radar.common.data.PhotoInfo;
import com.alterjoc.radar.server.dao.EventDAO;
import com.alterjoc.radar.server.domain.Event;
import org.jboss.capedwarf.server.api.io.BlobService;
import org.jboss.capedwarf.server.api.mvc.JSONAwareAbstractAction;

/**
 * On demand photo action.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class OnDemandPhotoAction extends JSONAwareAbstractAction<PhotoInfo>
{
   private EventDAO eventDAO;
   private BlobService blobService;

   protected PhotoInfo doHandle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      long eventId = parseLong(req, "eventId");
      Event event = eventDAO.find(eventId);
      PhotoInfo pi = new PhotoInfo();
      pi.setEventId(eventId);
      if (event != null)
      {
         String blobKey = null;
         Image image = parseEnum(req, "type", Image.BIG, Image.values());
         if (image == Image.BIG)
            blobKey = event.getBigImage();
         else if (image == Image.SMALL)
            blobKey = event.getSmallImage();
         if (blobKey != null)
            pi.setPhoto(blobService.loadBytes(blobKey));
      }
      return pi;
   }

   protected PhotoInfo errorResult()
   {
      return new PhotoInfo();
   }

   @Inject
   public void setEventDAO(EventDAO eventDAO)
   {
      this.eventDAO = eventDAO;
   }

   @Inject
   public void setBlobService(BlobService blobService)
   {
      this.blobService = blobService;
   }
}
