package com.alterjoc.radar.server.dao.impl;

import javax.persistence.Query;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.server.dao.AuditLogDAO;
import com.alterjoc.radar.server.domain.AuditLog;
import org.jboss.capedwarf.jpa.ProxyingEnum;
import org.jboss.capedwarf.server.api.cache.CacheKey;
import org.jboss.capedwarf.server.api.cache.CacheMode;
import org.jboss.capedwarf.server.api.cache.Cacheable;
import org.jboss.capedwarf.server.api.dao.impl.AbstractGenericDAO;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;
import org.jboss.capedwarf.server.api.persistence.Proxying;
import org.jboss.capedwarf.server.api.tx.TransactionPropagationType;
import org.jboss.capedwarf.server.api.tx.Transactional;

/**
 * Audit log DAO impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AuditLogDAOImpl extends AbstractGenericDAO<AuditLog> implements AuditLogDAO
{
   protected Class<AuditLog> entityClass()
   {
      return AuditLog.class;
   }

   @Transactional(TransactionPropagationType.SUPPORTS)
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "AuditLogCache")
   @CacheKey(Constants.AUDIT_LOG)
   public AuditLog getLast()
   {
      Query query = getEM().createQuery("select al from AuditLog al order by al.timestamp desc");
      return getSingleResult(query);
   }

   @Transactional
   @Proxying(ProxyingEnum.DISABLE)
   @Cacheable(name = "AuditLogCache", mode = CacheMode.REMOVE)
   @CacheKey(Constants.AUDIT_LOG)
   public void setLast(TimestampedEntity entity)
   {
      AuditLog last = new AuditLog();
      last.setTimestamp(entity.getTimestamp());
      last.setKind(entity.getClass().getSimpleName());
      last.setKey(entity.getId());
      save(last);
   }
}
