package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.common.serialization.GzipOptionalSerializator;

/**
 * Test disabled gzip server.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GzipDisabledServerTestCase extends ServerTestCase
{
   protected ServerProxy create() throws Exception
   {
      GzipOptionalSerializator.disableGzip();
      return super.create();
   }

   protected void doDestroy()
   {
      GzipOptionalSerializator.enableGzip();
      super.doDestroy();
   }
}
