package com.alterjoc.radar.client.database.sqlite;

import static org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper.getSingleResult;
import static org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper.select;
import static org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper.toSelectionArgs;

import android.database.sqlite.SQLiteDatabase;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.sqlite.EntityListener;
import org.jboss.capedwarf.sqlite.Phase;

/**
 * Load event's comments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class OnLoadEventListener implements EntityListener<SQLiteDatabase, EventInfo>
{
   public void action(SQLiteDatabase db, EventInfo entity, Phase phase)
   {
      if (phase == Phase.AFTER)
      {
         TopicInfo topicInfo = getSingleResult(select(db, TopicInfo.class, "id = ?", toSelectionArgs(entity.getTopicId()), null, "1"));
         entity.setTopicInfo(topicInfo);
      }
   }
}
