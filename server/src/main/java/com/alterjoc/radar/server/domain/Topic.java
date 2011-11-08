package com.alterjoc.radar.server.domain;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import com.alterjoc.radar.server.dto.TopicDTOModel;
import org.jboss.capedwarf.common.data.Mode;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.dto.DTOClass;
import org.jboss.capedwarf.common.dto.DTOProperty;
import org.jboss.capedwarf.jpa.ManyToOne;
import org.jboss.capedwarf.jpa.OneToMany;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;

/**
 * Existing topics.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
@DTOClass(model = TopicDTOModel.class)
public class Topic extends TimestampedEntity
{
   private static long serialVersionUID = 3l;

   // name
   private String name;
   // description
   private String description;
   // user that created this topic
   private Long creatorId;
   private transient Client creator;
   // user that confirmed creation of this topic
   private Long confirmedId;
   private transient Client confirmed;
   private boolean active;
   // topic type
   private Type type;
   // is the topic moderated
   private Mode mode;
   // the DS kind
   private String kind;
   // topic image
   private String image;

   @DTOProperty
   @Size(min = 5, max = 50)
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   @Size(max = 50)
   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public Long getCreatorId()
   {
      return creatorId;
   }

   public void setCreatorId(Long creator)
   {
      this.creatorId = creator;
   }

   @Transient
   @ManyToOne
   public Client getCreator()
   {
      return creator;
   }

   @ManyToOne
   public void setCreator(Client creator)
   {
      this.creator = creator;
   }

   public Long getConfirmedId()
   {
      return confirmedId;
   }

   public void setConfirmedId(Long confirmed)
   {
      this.confirmedId = confirmed;
   }

   @Transient
   @ManyToOne
   public Client getConfirmed()
   {
      return confirmed;
   }

   @ManyToOne
   public void setConfirmed(Client confirmed)
   {
      this.confirmed = confirmed;
   }

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public Type getType()
   {
      return type;
   }

   public void setType(Type type)
   {
      this.type = type;
   }

   public Mode getMode()
   {
      return mode;
   }

   public void setMode(Mode mode)
   {
      this.mode = mode;
   }

   public String getKind()
   {
      return kind;
   }

   public void setKind(String kind)
   {
      this.kind = kind;
   }

   public String getImage()
   {
      return image;
   }

   public void setImage(String image)
   {
      this.image = image;
   }

   @Transient
   @OneToMany
   public List<Event> getEvents()
   {
      return Collections.emptyList();
   }

   @Transient
   @OneToMany
   public Set<Subscription> getSubscriptions()
   {
      return Collections.emptySet();
   }

   protected void addInfo(StringBuilder builder)
   {
      super.addInfo(builder);
      builder.append(", name=").append(name);
      builder.append(", creatorId=").append(creatorId);
   }
}
