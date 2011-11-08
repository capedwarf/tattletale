package com.alterjoc.radar.client.database.sqlite;

import android.content.Context;
import com.alterjoc.radar.client.database.data.SyncInfo;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper;

/**
 * Custom SQLite open helper
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CustomSQLiteOpenHelper extends AbstractSQLiteOpenHelper
{
   private static final String DATABASE_NAME = "TozibabaDB";
   private static final int DATABASE_VERSION = 1;

   public CustomSQLiteOpenHelper(Context context)
   {
      super(context, DATABASE_NAME, DATABASE_VERSION);
      // Build entities
      getEntityModel(SyncInfo.class);
      getEntityModel(TopicInfo.class);
      getEntityModel(EventInfo.class);
      getEntityModel(SQLiteImageInfo.class);
      getEntityModel(CommentInfo.class);
   }
}
