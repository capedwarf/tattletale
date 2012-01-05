package com.alterjoc.radar.server.domain;

import com.alterjoc.radar.common.Constants;
import org.jboss.capedwarf.jpa.OneToMany;
import org.jboss.capedwarf.server.api.domain.TimestampedEntity;
import org.jboss.capedwarf.server.api.domain.Version;
import org.jboss.capedwarf.server.api.validation.JpaEmail;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Android clients.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Entity
public class Client extends TimestampedEntity
{
   private static long serialVersionUID = 4l;

   // user info
   private String username;
   private String password;
   private String token;
   private String email;
   private String recovery;

   // lower case
   private String lowercaseUsername;

   // app reg id
   private String registrationId;
   // android version; 2.2+ or 2.1, ...
   private Version version = Version.DEFAULT_VERSION;

   @Size(min = 3, max = 20)
   @Pattern(regexp = Constants.USERNAME_REGEXP)
   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
      if (username != null)
      {
         lowercaseUsername = username.toLowerCase();
      }
      else
      {
         lowercaseUsername = null;
      }
   }

   public String getLowercaseUsername()
   {
      return lowercaseUsername;
   }

   public void setLowercaseUsername(String lowercaseUsername)
   {
      this.lowercaseUsername = lowercaseUsername;
   }

   @Size(min = 6)
   @Pattern(regexp = Constants.PASSWORD_REGEXP)
   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }

   public String getToken()
   {
      return token;
   }

   public void setToken(String token)
   {
      this.token = token;
   }

   @JpaEmail
   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public String getRecovery()
   {
      return recovery;
   }

   public void setRecovery(String recovery)
   {
      this.recovery = recovery;
   }

   public String getRegistrationId()
   {
      return registrationId;
   }

   public void setRegistrationId(String registrationId)
   {
      this.registrationId = registrationId;
   }

   @Embedded
   public Version getVersion()
   {
      return version;
   }

   public void setVersion(Version version)
   {
      this.version = version;
   }

   @Transient
   @OneToMany(join = "publisherId")
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
      builder.append(", username=").append(username);
      builder.append(", token=").append(token);
      builder.append(", email=").append(email);
   }
}
