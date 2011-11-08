package com.alterjoc.radar.server.domain;

import com.alterjoc.radar.common.data.CommentInfo;
import org.jboss.capedwarf.common.dto.DTOClass;
import org.jboss.capedwarf.common.dto.DTOModel;
import org.jboss.capedwarf.common.social.SocialEvent;
import org.jboss.capedwarf.jpa.ManyToOne;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Keep track of all event's comments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
@DTOClass(model = Comment.CommentDTOModel.class)
public class Comment extends TimestampedEntity implements SocialEvent
{
   private static long serialVersionUID = 4l;

   // user that published this comment
   private Long publisherId;
   private transient Client publisher;
   private String username; // client's username
   // topic
   private Long topicId;
   private transient Topic topic;
   // event
   private Long eventId;
   private transient Event event;
   // extra info
   private String comment;

   public Long userId()
   {
      return getPublisherId();
   }

   public String content()
   {
      return getComment();
   }

   public Long parentId()
   {
      return getEventId();
   }

   public Long getPublisherId()
   {
      return publisherId;
   }

   public void setPublisherId(Long publisherId)
   {
      this.publisherId = publisherId;
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

   @NotNull
   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
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

   public Long getEventId()
   {
      return eventId;
   }

   public void setEventId(Long eventId)
   {
      this.eventId = eventId;
   }

   @Transient
   @ManyToOne
   public Event getEvent()
   {
      return event;
   }

   @ManyToOne
   public void setEvent(Event event)
   {
      this.event = event;
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

   protected void addInfo(StringBuilder builder)
   {
      super.addInfo(builder);
      builder.append(", comment=").append(comment);
      builder.append(", eventId=").append(eventId);
      builder.append(", username=").append(username);
   }

   public static class CommentDTOModel implements DTOModel<Comment>
   {
      public Object toDTO(Comment entity)
      {
         CommentInfo ci = new CommentInfo(
               entity.getComment(),
               entity.getTopicId(),
               entity.getEventId()
         );
         ci.setId(entity.getId());
         ci.setUsername(entity.getUsername());
         long ts = entity.getTimestamp();
         ci.setTimestamp(ts);
         ci.setExpirationDelta(entity.getExpirationTime() - ts);
         return ci;
      }
   }
}
