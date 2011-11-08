package com.alterjoc.test.radar.server;

import java.net.URL;

/**
 * Abstract test class.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public abstract class AbstractTest
{
   /**
    * Get resource.
    *
    * @param name the resource name
    * @return the resource
    */
   protected static URL getResource(String name)
   {
      return AbstractTest.class.getResource(name);
   }
}
