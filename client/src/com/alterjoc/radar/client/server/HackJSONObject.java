package com.alterjoc.radar.client.server;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Modified json object.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class HackJSONObject extends JSONObject
{
   HackJSONObject()
   {
   }

   HackJSONObject(JSONTokener jsonTokener) throws JSONException
   {
       super(jsonTokener);
   }

   public JSONObject putOpt(String key, Object value) throws JSONException
   {
      if (value instanceof String)
      {
         String string = (String) value;
         if (string == null || string.trim().length() == 0)
            return this;
      }
      return super.putOpt(key, value);
   }
}