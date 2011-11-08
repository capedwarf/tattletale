package com.alterjoc.radar.client.database.sqlite;

import android.database.sqlite.SQLiteDatabase;
import com.alterjoc.radar.common.data.ImageInfo;
import com.alterjoc.radar.common.data.TopicInfo;
import org.jboss.capedwarf.sqlite.AbstractSQLiteOpenHelper;
import org.jboss.capedwarf.sqlite.EntityListener;
import org.jboss.capedwarf.sqlite.Phase;

/**
 * Load topic's image.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TopicLoadImageListener implements EntityListener<SQLiteDatabase, TopicInfo>
{
   public void action(SQLiteDatabase db, TopicInfo entity, Phase phase)
   {
      if (phase == Phase.AFTER)
      {
         Long imagePk = entity.getImagePk();
         if (imagePk != null)
         {
            ImageInfo image = AbstractSQLiteOpenHelper.load(db, SQLiteImageInfo.class, imagePk);
            if (image != null)
               entity.setImage(image);
         }
      }
   }
}
