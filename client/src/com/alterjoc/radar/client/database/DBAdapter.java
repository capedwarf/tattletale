package com.alterjoc.radar.client.database;

import com.alterjoc.radar.client.database.data.SyncInfo;
import com.alterjoc.radar.client.sync.TimestampedSynchronization;
import com.alterjoc.radar.client.sync.TopicSynchronization;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.sql.SQLObject;

import java.util.List;

/**
 * DB adapter.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface DBAdapter
{
   boolean initialize(int currentVersion);

   long saveEntity(SQLObject entity);

   int updateEntity(SQLObject entity);

   void saveEntities(List<? extends SQLObject> entities);

   void deleteEntities(List<? extends SQLObject> entities);

   EventInfo readEventInfo(long id);

   List<CommentInfo> readComments(long eventId);

   int countComments(long eventId);

   ImageInfo createImageInfo();

   ImageInfo readImageInfo(long pk);

   void insertImage(ImageInfo imageInfo, byte[] bytes);

   void setEventNotified(long id);

   List<TopicInfo> getAllTopics();

   TopicInfo getTopicForId(Long id);

   SyncInfo getSyncInfo(Class<?> type);

   void putSyncInfo(SyncInfo info);

   List<TopicInfo> getSubscribedTopics();

   List<TopicInfo> getNonSystemTopics();

   List<TopicInfo> getSubscribedTopicsForSynch();

   List<EventInfo> getEvents(Boolean archived);

   List<EventInfo> getNonFilteredEvents(Boolean archived);

   public List<EventInfo> getEventsForArchiving();
   
   boolean isEventUserNotified(String id);

   TopicInfo getLatestTopicInfo();

   void updateTopics(TopicSynchronization sync, List<TopicInfo> topics) throws InterruptedException;

   EventInfo getLatestEventInfo();

   void updateEvents(TimestampedSynchronization sync, List<EventInfo> events) throws InterruptedException;

   CommentInfo getLatestCommentInfo();

   void updateComments(TimestampedSynchronization sync, List<CommentInfo> comments) throws InterruptedException;

   List<TopicInfo> getTopicsForUpSync();

   int countAllTopics();

   int countSubscribedTopics();

   int countAllEvents();

   int countArchivedEvents();
   
   void addListener(Listener lis);
   
   Listener removeListener(Listener lis);
   
   static interface ListenerVisitor
   {
      void handle(Listener l);
   }

   static interface Listener
   {
      void eventsUpdated(List<EventInfo> events);
      
      void topicsUpdated(List<TopicInfo> topics);
      
      void commentsUpdated(List<CommentInfo> comments); 
   }
}
