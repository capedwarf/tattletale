package com.alterjoc.radar.client;

import java.util.Formatter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

import static com.alterjoc.radar.client.Preferences.PreferenceKey.*;

public class Preferences
{
   
   public enum PreferenceKey
   {
      USER_LOGIN,
      USER_TOKEN,
      USER_ID,
      FIRST_RUN,
      EVENTS_UPDATE_PERIOD,
      EVENTS_NOTIFICATION_RADIUS,
      EVENTS_CUSTOM_UPDATE_PERIOD,
      EVENTS_CUSTOM_RADIUS,
      EVENTS_NOTIFICATION_SOUND,
      EVENTS_NOTIFICATION_VIBRATE,
      EVENTS_BACKLOG
   }

   private String labelCustom = "Custom";
   private String templateUpdate = "Every %1 min";

   private String[] eventsUpdatePeriodValues;
   private String[] eventsUpdatePeriodLabels;

   public String[] radiusValues;
   public String[] radiusLabel;

   private SharedPreferences prefs;

   Preferences(SharedPreferences prefs, Resources res)
   {
      this.prefs = prefs;
      
      labelCustom = res.getString(R.string.custom);
      templateUpdate = res.getString(R.string.every_x_min);

      eventsUpdatePeriodValues = new String [] { "1", "5", "60", "0" };
      eventsUpdatePeriodLabels = new String [] { "1 min", "5 min", "60 min", labelCustom };

      radiusValues = new String [] { "100", "1000", "10000", "100000", "0" };
      radiusLabel = new String [] { "100 m", "1 km", "10 km", "100 km", labelCustom };
      
      init();
   }

   public static String getKey(PreferenceKey key)
   {
      return key.toString();      
   }
   
   private void init()
   {
      Editor edit = prefs.edit();

      String val = prefs.getString(EVENTS_UPDATE_PERIOD.toString(), null);
      if (val == null)
      {
         edit.putString(EVENTS_UPDATE_PERIOD.toString(), getEventsUpdatePeriodValues()[1]);
      }

      val = prefs.getString(EVENTS_NOTIFICATION_RADIUS.toString(), null);
      if (val == null)
      {
         edit.putString(EVENTS_NOTIFICATION_RADIUS.toString(), radiusValues[2]);
      }

      val = prefs.getString(EVENTS_CUSTOM_UPDATE_PERIOD.toString(), null);
      if (val == null)
      {
         edit.putString(EVENTS_CUSTOM_UPDATE_PERIOD.toString(), "10");
      }

      val = prefs.getString(EVENTS_CUSTOM_RADIUS.toString(), null);
      if (val == null)
      {
         edit.putString(EVENTS_CUSTOM_RADIUS.toString(), "5");
      }

      prefs.registerOnSharedPreferenceChangeListener(
            new SharedPreferences.OnSharedPreferenceChangeListener() {

         private boolean equals(String val, PreferenceKey key)
         {
            return val.equals(key.toString());
         }
         
         private long lastValue = -1
         ; 
         public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
         {
            if (equals(key, PreferenceKey.EVENTS_NOTIFICATION_RADIUS) 
                  || equals(key, PreferenceKey.EVENTS_CUSTOM_RADIUS))
            {
               String val = getEffectiveRadiusValue();
               long newVal = Long.parseLong(val);
               if (newVal != lastValue)
               {
                  lastValue = newVal;
               }
            }
         }
      });
      edit.commit();
   }

   public String[] getEventsUpdatePeriodLabels()
   {
      return eventsUpdatePeriodLabels;
   }

   public String[] getEventsUpdatePeriodValues()
   {
      return eventsUpdatePeriodValues;
   }

   public String getEventsUpdatePeriodValue()
   {
      return prefs.getString(EVENTS_UPDATE_PERIOD.toString(), null);
   }

   public String getEventsUpdatePeriodSummaryValue()
   {
      return getEventsUpdatePeriodSummaryValue(getEventsUpdatePeriodValue());
   }

   public String getEventsUpdatePeriodSummaryValue(String val)
   {
      return getValueForKey(getEventsUpdatePeriodValues(),
            getEventsUpdatePeriodLabels(), val);
   }

   public void setEventNotificationRadius(long radius)
   {
      setLongValue(EVENTS_NOTIFICATION_RADIUS.toString(), radius);
   }

   public String getEventNotificationRadius()
   {
      return prefs.getString(EVENTS_NOTIFICATION_RADIUS.toString(), null);
   }

   public String getRadiusSummaryValue()
   {
      return getRadiusSummaryValue(getEventNotificationRadius());
   }

   public String getRadiusSummaryValue(String val)
   {
      return getValueForKey(radiusValues, radiusLabel, val);
   }

   public void setUserLogin(String userLogin)
   {
      setStringValue(USER_LOGIN.toString(), userLogin);
   }

   public String getUserLogin()
   {
      return prefs.getString(USER_LOGIN.toString(), null);
   }

   public void setUserToken(String userToken)
   {
      setStringValue(USER_TOKEN.toString(), userToken);
   }

   public String getUserToken()
   {
      return prefs.getString(USER_TOKEN.toString(), null);
   }

   public void setUserId(Long id)
   {
      if (id == null)
         throw new IllegalArgumentException("User id should not be null.");

      setLongValue(USER_ID.toString(), id);
   }

   public long getUserId()
   {
      return prefs.getLong(USER_ID.toString(), 0);
   }

   public String getCustomEventUpdatePeriodValue()
   {
      return prefs.getString(EVENTS_CUSTOM_UPDATE_PERIOD.toString(), null);
   }

   public String getCustomEventUpdatePeriodSummaryValue()
   {
      return getCustomEventUpdatePeriodSummaryValue(getCustomEventUpdatePeriodValue());
   }

   public String getEffectiveUpdatePeriodValue()
   {
      String val = getEventsUpdatePeriodValue();
      if ("0".equals(val))
         val = getCustomEventUpdatePeriodValue();
      return val;
   }

   public String getCustomRadiusValue()
   {
      return prefs.getString(EVENTS_CUSTOM_RADIUS.toString(), null);
   }

   public String getCustomRadiusSummaryValue()
   {
      return Preferences.getCustomRadiusSummaryValue(getCustomRadiusValue());
   }

   public String getEffectiveRadiusValue()
   {
      String val = getEventNotificationRadius();
      if ("0".equals(val))
      {
         long lVal = (long) (DoubleKmConverter
               .fromValueStringToKmDouble(getCustomRadiusValue()) * 1000);
         val = String.valueOf(lVal);
      }
      return val;
   }

   public String getCustomEventUpdatePeriodSummaryValue(String newValue)
   {
      return new Formatter().format(templateUpdate, newValue).toString();
   }

   public static String getCustomRadiusSummaryValue(String val)
   {
      return DoubleKmConverter.toUnitString(val);
   }

   public String getCustomEventUpdatePeriodTitleValueConditional(String newValue)
   {
      if ("0".equals(newValue))
         return getCustomEventUpdatePeriodSummaryValue();
      else
         return labelCustom;
   }

   public String getCustomEventUpdatePeriodSummaryValueConditional(
         String newValue)
   {
      if ("0".equals(newValue))
         return "";
      else
         return getCustomEventUpdatePeriodSummaryValue();
   }

   public String getCustomEventUpdatePeriodSummaryValueDep()
   {
      return getCustomEventUpdatePeriodSummaryValueConditional(getEventsUpdatePeriodValue());
   }

   public String getCustomEventUpdatePeriodTitleValueDep()
   {
      return getCustomEventUpdatePeriodTitleValueConditional(getEventsUpdatePeriodValue());
   }

   public String getCustomRadiusTitleValueConditional(String newValue)
   {
      if ("0".equals(newValue))
         return getCustomRadiusSummaryValue();
      else
         return labelCustom;
   }

   public String getCustomRadiusSummaryValueConditional(String newValue)
   {
      if ("0".equals(newValue))
         return "";
      else
         return getCustomRadiusSummaryValue();
   }

   public String getCustomRadiusTitleValueDep()
   {
      return getCustomRadiusTitleValueConditional(getEventNotificationRadius());
   }

   public String getCustomRadiusSummaryValueDep()
   {
      return getCustomRadiusSummaryValueConditional(getEventNotificationRadius());
   }
   
   private void setStringValue(String key, String value)
   {
      Editor editor = prefs.edit();
      editor.putString(key, value);
      editor.commit();
   }

   private void setLongValue(String key, Long value)
   {
      Editor editor = prefs.edit();
      editor.putLong(key, value);
      editor.commit();
   }

   private void setBooleanValue(String key, Boolean value)
   {
      Editor editor = prefs.edit();
      editor.putBoolean(key, value);
      editor.commit();
   }
   
   private static String getValueForKey(String[] keys, String[] values,
         String key)
   {
      for (int i = 0; i < keys.length; i++)
      {
         if (keys[i].equals(key))
         {
            return values[i];
         }
      }
      return "?";
   }

   public boolean isFirstRun()
   {
      return prefs.getLong(FIRST_RUN.toString(), 0) == 0;
   }
   
   public void setHasRun()
   {
      setLongValue(FIRST_RUN.toString(), 1L);
   }
   
   public boolean isUsingSoundNotification()
   {
      return prefs.getBoolean(EVENTS_NOTIFICATION_SOUND.toString(), false);
   }
   
   public void setUsingSoundNotification(boolean val)
   {
      setBooleanValue(EVENTS_NOTIFICATION_SOUND.toString(), val);
   }
   
   public boolean isUsingVibrateNotification()
   {
      return prefs.getBoolean(EVENTS_NOTIFICATION_VIBRATE.toString(), false);
   }
   
   public void setUsingVibrateNotification(boolean val)
   {
      setBooleanValue(EVENTS_NOTIFICATION_VIBRATE.toString(), val);
   }

   public long getEventBackLogMillis()
   {
      return prefs.getLong(EVENTS_BACKLOG.toString(), 7*24*3600*1000); // 7 dni
   }
}
