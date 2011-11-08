package com.alterjoc.test.client.server.test;

import com.alterjoc.radar.connect.server.ServerProxy;
import org.jboss.capedwarf.connect.config.Configuration;

/**
 * Server test delegate.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public interface ServerTestDelegate
{
   /**
    * Get server.
    *
    * @return the server
    */
   String server();

   /**
    * Get configuration.
    *
    * @return the configuration
    */
   Configuration<ServerProxy> configuration();

   /**
    * Get username.
    *
    * @return the username
    */
   String username();

   /**
    * Get password.
    *
    * @return the password
    */
   String password();
}
