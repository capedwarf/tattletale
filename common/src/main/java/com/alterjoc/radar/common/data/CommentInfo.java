package com.alterjoc.radar.common.data;

import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.common.sql.Column;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Dejan Pazin
 * @author Ales Justin
 */
public class CommentInfo extends Timestamped
{
   private String comment;
   private String username;
   private long topicId;
   private long eventId;

   @Deprecated
   public CommentInfo()
   {
      // deserialization only
   }

   public CommentInfo(String comment, long topicId, long eventId)
   {
      this.comment = comment;
      this.topicId = topicId;
      this.eventId = eventId;
   }

   public void writeJSONObject(JSONObject json) throws JSONException
   {
      super.writeJSONObject(json);

      json.put("comment", comment);
      json.putOpt("username", username);
      json.put("topicId", topicId);
      json.put("eventId", eventId);
   }

   public void readJSONObject(JSONObject json) throws JSONException
   {
      super.readJSONObject(json);

      comment = json.getString("comment");
      username = json.optString("username");
      topicId = json.getLong("topicId");
      eventId = json.getLong("eventId");
   }

   @NotNull
   @Size(max = 300)
   @Column
   public String getComment()
   {
      return comment;
   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   @Column
   public long getTopicId()
   {
      return topicId;
   }

   public void setTopicId(long topicId)
   {
      this.topicId = topicId;
   }

   @Column
   public long getEventId()
   {
      return eventId;
   }

   public void setEventId(long eventId)
   {
      this.eventId = eventId;
   }

   @Column
   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public String toShortString()
   {
      return "CommentInfo [id: " + getId() + ", comment: " + comment + "]";
   }
}
