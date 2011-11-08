package com.alterjoc.radar.server.dao;

import com.alterjoc.radar.server.domain.AuditLog;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

/**
 * Last modified DAO.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface AuditLogDAO
{
   /**
    * Get last.
    *
    * @return the last
    */
   AuditLog getLast();

   /**
    * Set last.
    *
    * @param entity the last tx entity
    */
   void setLast(TimestampedEntity entity);
}
