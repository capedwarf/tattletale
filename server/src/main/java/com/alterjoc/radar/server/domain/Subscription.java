package com.alterjoc.radar.server.domain;

import org.jboss.capedwarf.jpa.ManyToOne;
import org.jboss.capedwarf.server.api.domain.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Subscription.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
public class Subscription extends AbstractEntity
{
   private static long serialVersionUID = 3l;

   private Long clientId;
   private transient Client client;
   private Long topicId;
   private transient Topic topic;
   private double radius;

   public Long getClientId()
   {
      return clientId;
   }

   public void setClientId(Long client)
   {
      this.clientId = client;
   }

   @Transient
   @ManyToOne
   public Client getClient()
   {
      return client;
   }

   @ManyToOne
   public void setClient(Client client)
   {
      this.client = client;
   }

   public Long getTopicId()
   {
      return topicId;
   }

   public void setTopicId(Long topicId)
   {
      this.topicId = topicId;
   }

   @Transient
   @ManyToOne
   public Topic getTopic()
   {
      return topic;
   }

   @ManyToOne
   public void setTopic(Topic topic)
   {
      this.topic = topic;
   }

   public double getRadius()
   {
      return radius;
   }

   public void setRadius(double radius)
   {
      this.radius = radius;
   }

   @Transient
   public String getInfo()
   {
      return "client=" + clientId + ", topicId=" + topicId;
   }
}
