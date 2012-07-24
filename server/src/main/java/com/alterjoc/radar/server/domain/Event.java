package com.alterjoc.radar.server.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.alterjoc.radar.common.Constants;
import com.alterjoc.radar.common.data.EventInfo;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.dto.DTOClass;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.common.social.SocialEvent;
import org.jboss.capedwarf.jpa.ManyToOne;
import org.jboss.capedwarf.jpa.OneToMany;
import org.jboss.capedwarf.server.api.domain.GeoPt;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

/**
 * Keep track of all events.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
@DTOClass(model = Event.EventDTOModel.class)
public class Event extends TimestampedEntity implements SocialEvent
{
   private static long serialVersionUID = 5l;

   // user that published this event
   private Long publisherId;
   private transient Client publisher;
   private String publisherUsername;
   private Type type;
   // topic
   private Long topicId;
   private transient Topic topic;
   // gps
   private GeoPt point;
   private String address;
   // extra info
   private String title;
   private String comment;
   private String smallImage;
   private String bigImage;
   private String video;
   private String sound;
   // if moderated, who confirmed it
   private Long moderatorId;
   private transient Client moderator;

   public Long userId()
   {
      return getPublisherId();
   }

   public String content()
   {
      return getTitle();
   }

   public Long parentId()
   {
      return null;
   }

   public Long getPublisherId()
   {
      return publisherId;
   }

   public void setPublisherId(Long publisher)
   {
      this.publisherId = publisher;
   }

   @Transient
   @ManyToOne
   public Client getPublisher()
   {
      return publisher;
   }

   @ManyToOne
   public void setPublisher(Client publisher)
   {
      this.publisher = publisher;
   }

   public String getPublisherUsername()
   {
      return publisherUsername;
   }

   public void setPublisherUsername(String publisherUsername)
   {
      this.publisherUsername = publisherUsername;
   }

   public Type getType()
   {
      return type;
   }

   public void setType(Type type)
   {
      this.type = type;
   }

   public Long getTopicId()
   {
      return topicId;
   }

   public void setTopicId(Long topic)
   {
      this.topicId = topic;
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

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   @Basic
   public GeoPt getPoint()
   {
      return point;
   }

   public void setPoint(GeoPt point)
   {
      this.point = point;
   }

   @Size(max = 300)
   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   @Size(max = 300)
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public String getBigImage()
   {
      return bigImage;
   }

   public void setBigImage(String bigImage)
   {
      this.bigImage = bigImage;
   }

   public String getSmallImage()
   {
      return smallImage;
   }

   public void setSmallImage(String smallImage)
   {
      this.smallImage = smallImage;
   }

   public String getVideo()
   {
      return video;
   }

   public void setVideo(String video)
   {
      this.video = video;
   }

   public String getSound()
   {
      return sound;
   }

   public void setSound(String sound)
   {
      this.sound = sound;
   }

   public Long getModeratorId()
   {
      return moderatorId;
   }

   public void setModeratorId(Long moderator)
   {
      this.moderatorId = moderator;
   }

   @Transient
   @ManyToOne
   public Client getModerator()
   {
      return moderator;
   }

   @ManyToOne
   public void setModerator(Client moderator)
   {
      this.moderator = moderator;
   }

   @Transient
   @OneToMany(type = ArrayList.class)
   public List<Comment> getComments()
   {
      return Collections.emptyList();
   }

   protected void addInfo(StringBuilder builder)
   {
      super.addInfo(builder);
      builder.append(", title=").append(title);
      builder.append(", topicId=").append(topicId);
      builder.append(", publisher=").append(publisherUsername);
   }

   public static class EventDTOModel implements DTOModel<Event>
   {
      public Object toDTO(Event entity)
      {
         EventInfo ei = new EventInfo(entity.getTitle(), entity.getComment(), entity.getTopicId());
         ei.setId(entity.getId());
         ei.setUsername(entity.getPublisherUsername());
         long ts = entity.getTimestamp();
         ei.setTimestamp(ts);
         ei.setExpirationDelta(entity.getExpirationTime() - ts);
         ei.setAddress(entity.getAddress());
         ei.setType(entity.getType());
         GeoPt pt = entity.getPoint();
         if (pt != null)
         {
            ei.setLatitude((int) (pt.getLatitude() * Constants.MIO));
            ei.setLongitude((int) (pt.getLongitude() * Constants.MIO));
         }
         // impl detail -- (x < 0) marks no small photo
         if (entity.getSmallImage() == null)
         {
            ei.setSmallPhotoPk(-1L);
         }
         return ei;
      }
   }
}
