package com.alterjoc.radar.client.database.sqlite;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import com.alterjoc.radar.client.database.AbstractDBAdapter;
import com.alterjoc.radar.client.database.data.SyncInfo;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.common.data.Type;
import org.jboss.capedwarf.common.dto.Identity;
import org.jboss.capedwarf.common.dto.Timestamped;
import org.jboss.capedwarf.common.sql.SQLObject;
import org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper;
import org.jboss.capedwarf.sqlite.ColumnMapper;

/**
 * SQLite DBAdapter.
 * 
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SQLiteDBAdapter extends AbstractDBAdapter
{
   private AbstractSQLiteOpenHelper helper;

   public SQLiteDBAdapter(Context context)
   {
      super(context);
   }

   protected synchronized AbstractSQLiteOpenHelper getHelper()
   {
      if (helper == null)
         helper = new CustomSQLiteOpenHelper(getContext());

      return helper;
   }

   public synchronized void close()
   {
      if (helper != null)
      {
         helper.close();
         helper = null;
      }
   }

   protected <T> List<T> select(Class<T> entityClass, String selection, String[] selectionArgs, String orderBy, String limit)
   {
      return getHelper().select(entityClass, selection, selectionArgs, orderBy, limit);
   }

   @SuppressWarnings({"unchecked"})
   protected <T, U> List<U> select(Class<T> entityClass, ColumnMapper<U> mapper, String selection, String[] selectionArgs, String orderBy, String limit)
   {
      return getHelper().select(entityClass, Collections.<ColumnMapper>singletonList(mapper), selection, selectionArgs, orderBy, limit);
   }

   protected <T> List select(Class<T> entityClass, List<ColumnMapper> mappers, String selection, String[] selectionArgs, String orderBy, String limit)
   {
      return getHelper().select(entityClass, mappers, selection, selectionArgs, orderBy, limit);
   }

   protected <T> List<Long> pks(Class<T> entityClass, String selection, String[] selectionArgs, String orderBy, String limit)
   {
      return getHelper().pks(entityClass, selection, selectionArgs, orderBy, limit);
   }

   protected <T> T single(Class<T> entityClass, String selection, String[] selectionArgs)
   {
      return getSingleResult(select(entityClass, selection, selectionArgs, null, "1"));
   }

   protected int count(Class<?> entityClass, String selection, String[] selectionArgs)
   {
      return getHelper().count(entityClass, selection, selectionArgs);
   }

   protected int countAll(Class<?> entityClass)
   {
      return count(entityClass, null, null);
   }

   protected <T extends Identity> T findEntity(Long id, Class<T> entityClass)
   {
      return single(entityClass, "id = ?", AbstractSQLiteOpenHelper.toSelectionArgs(id));
   }

   protected <T> List<T> getAll(Class<T> entityClass)
   {
      return select(entityClass, null, null, null, null);
   }

   protected <T extends Timestamped> T getLatestEntity(Class<T> entityClass)
   {
      return getSingleResult(select(entityClass, null, null, "timestamp DESC", "1"));
   }

   public boolean initialize(int currentVersion)
   {
      return getHelper().initialize(currentVersion);
   }

   protected void begin()
   {
      getHelper().begin();
   }

   protected void end()
   {
      getHelper().end();
   }

   protected void commit()
   {
      getHelper().commit();
   }

   protected void rollback()
   {
      getHelper().rollback();
   }

   protected long store(SQLObject entity)
   {
      return AbstractSQLiteOpenHelper.persist(entity);
   }

   protected int update(SQLObject entity)
   {
      return AbstractSQLiteOpenHelper.update(entity);
   }

   protected int delete(SQLObject entity)
   {
      if (entity == null || entity.getPk() == null)
         return 0;

      return AbstractSQLiteOpenHelper.delete(entity);
   }

   public List<CommentInfo> readComments(long eventId)
   {
      return select(CommentInfo.class, "eventId = ?", AbstractSQLiteOpenHelper.toSelectionArgs(eventId), null, null);
   }

   public int countComments(long eventId)
   {
      return count(CommentInfo.class, "eventId = ?", AbstractSQLiteOpenHelper.toSelectionArgs(eventId));
   }

   public ImageInfo createImageInfo()
   {
      return new SQLiteImageInfo();
   }

   public ImageInfo readImageInfo(long pk)
   {
      return getHelper().load(SQLiteImageInfo.class, pk);
   }

   public SyncInfo getSyncInfo(Class<?> type)
   {
      return single(SyncInfo.class, "className = ?", AbstractSQLiteOpenHelper.toSelectionArgs(type.getName()));
   }

   public List<EventInfo> getEvents(Boolean archived)
   {
      return select(EventInfo.class, "archived = ?", AbstractSQLiteOpenHelper.toSelectionArgs(archived), null, null);
   }

   public List<EventInfo> getNonFilteredEvents(Boolean archived)
   {
      ColumnMapper<Long> mapper = new ColumnMapper<Long>()
      {
         public String column()
         {
            return "id";
         }

         public Long value(Cursor cursor, int index)
         {
            return cursor.getLong(index);
         }
      };
      List<Long> ids = select(
            TopicInfo.class,
            mapper,
            "userSubscribed = ? and filterApplied = ? and (type is null or type <> ?)",
            AbstractSQLiteOpenHelper.toSelectionArgs(true, false, Type.SYSTEM),
            mapper.column(), 
            null
      );

      if (ids.isEmpty())
         return Collections.emptyList();

      String selectionArgs[] = AbstractSQLiteOpenHelper.toSelectionArgs(archived, ids);
      return select(EventInfo.class, "archived = ? and topicId in (" + AbstractSQLiteOpenHelper.toQuery(ids) + ")", selectionArgs, null, null);
   }
   
   /**
    * Return all events that are neither archived, nor unarchived.
    * These events can - if additional conditions are fulfilled - be archived
    * 
    * @return list containing only events that may become archived
    */
   public List<EventInfo> getEventsForArchiving()
   {
      return select(EventInfo.class, "archived = ? and unarchived = ?", new String[] {"0", "0"}, null, null);
   }

   public List<TopicInfo> getSubscribedTopics()
   {
      return select(TopicInfo.class, "userSubscribed = ? and (type is null or type <> ?)", AbstractSQLiteOpenHelper.toSelectionArgs(true, Type.SYSTEM), null, null);
   }

   public List<TopicInfo> getNonSystemTopics()
   {
      return select(TopicInfo.class, "type is null or type <> ?", AbstractSQLiteOpenHelper.toSelectionArgs(Type.SYSTEM), null, null);
   }

   public List<TopicInfo> getSubscribedTopicsForSynch()
   {
      return select(TopicInfo.class, "userSubscribed = ?", AbstractSQLiteOpenHelper.toSelectionArgs(true), null, null);
   }

   public List<TopicInfo> getTopicsForUpSync()
   {
      return select(TopicInfo.class, "userSubscribed <> userSubscribedOnSrv", null, null, null);
   }

   public int countAllTopics()
   {
      return countAll(TopicInfo.class);
   }

   public int countSubscribedTopics()
   {
      return count(TopicInfo.class, "userSubscribed = ?", AbstractSQLiteOpenHelper.toSelectionArgs(true));
   }

   public int countAllEvents()
   {
      return countAll(EventInfo.class);
   }

   public int countArchivedEvents()
   {
      return count(EventInfo.class, "archived = ?", AbstractSQLiteOpenHelper.toSelectionArgs(true));
   }
}
