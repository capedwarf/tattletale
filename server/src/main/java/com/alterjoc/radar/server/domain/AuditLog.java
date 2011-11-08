package com.alterjoc.radar.server.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

/**
 * Keep track of all tx actions.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
public class AuditLog extends TimestampedEntity
{
   private static long serialVersionUID = 1l;

   private String kind;
   private Long key;

   public String getKind()
   {
      return kind;
   }

   public void setKind(String kind)
   {
      this.kind = kind;
   }

   @Column(name = "akey")
   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }
}
