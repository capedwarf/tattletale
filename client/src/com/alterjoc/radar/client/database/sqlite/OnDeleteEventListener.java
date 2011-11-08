package com.alterjoc.radar.client.database.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.alterjoc.radar.common.data.CommentInfo;
import com.alterjoc.radar.common.data.EventInfo;
import org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper;
import org.jboss.capedwarf.sqlite.EntityListener;
import org.jboss.capedwarf.sqlite.Phase;

/**
 * Delete event's comments.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class OnDeleteEventListener implements EntityListener<SQLiteDatabase, EventInfo>
{
   public void action(SQLiteDatabase db, EventInfo entity, Phase phase)
   {
      if (phase == Phase.AFTER)
      {
         AbstractSQLiteOpenHelper.delete(db, new CommentInfo(), "eventId = ?", entity.getId());
         AbstractSQLiteOpenHelper.delete(db, new SQLiteImageInfo(), "pk = ?", entity.getSmallPhotoPk());
         AbstractSQLiteOpenHelper.delete(db, new SQLiteImageInfo(), "pk = ?", entity.getBigPhotoPk());         
      }
   }
}
