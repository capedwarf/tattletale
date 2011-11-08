package com.alterjoc.radar.server.mvc.impl;

import com.alterjoc.radar.server.dao.AuditLogDAO;
import com.alterjoc.radar.server.domain.AuditLog;
import org.jboss.capedwarf.server.api.mvc.AbstractAction;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Modification action.
 *
 * @author Ales Justin
 */
public class ModificationAction extends AbstractAction
{
   private AuditLogDAO auditLogDAO;

   public void handle(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      AuditLog last = auditLogDAO.getLast();
      writeResult(resp, last != null ? last.getTimestamp() : 0L);
   }

   @Inject
   public void setAuditLogDAO(AuditLogDAO auditLogDAO)
   {
      this.auditLogDAO = auditLogDAO;
   }
}
