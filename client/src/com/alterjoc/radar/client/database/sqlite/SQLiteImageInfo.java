package com.alterjoc.radar.client.database.sqlite;

import com.alterjoc.radar.common.data.ImageInfo;
import org.jboss.capedwarf.common.sql.Column;

/**
 * SQLite ImageInfo impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class SQLiteImageInfo extends ImageInfo
{
   private byte[] image;

   @Column
   public byte[] getImage()
   {
      return image;
   }

   public void setImage(byte[] image)
   {
      this.image = image;
   }

   public void readFromBytesIntoDB(byte[] byteArray)
   {
      setImage(byteArray);
   }

   public byte[] readFromDBIntoArray()
   {
      return getImage();
   }
}
