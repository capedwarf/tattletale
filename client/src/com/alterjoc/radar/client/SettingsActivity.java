package com.alterjoc.radar.client;

import android.content.Intent;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;
import com.alterjoc.radar.client.login.LostPasswordWizardActivity;
import com.alterjoc.radar.client.login.UserWizardActivity;

import static com.alterjoc.radar.client.Preferences.PreferenceKey.*;

/**
 * @author Marko Strukelj
 */
public class SettingsActivity extends PreferenceActivity {
   
    private PreferenceScreen userLoginPref;
    private EditTextPreference customPeriodPref;
    private EditTextPreference customRadiusPref;
    private CheckBoxPreference useSoundPref;
    private CheckBoxPreference useVibratorPref;
    private Preferences prefs;
    
    private long lastKnownEffectiveRadius;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        super.onCreate(savedInstanceState);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        prefs = Application.getInstance().getPreferences(this);

        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);

        PreferenceCategory settingsPrefCat = new PreferenceCategory(this);
        settingsPrefCat.setTitle(R.string.event_update);
        root.addPreference(settingsPrefCat);

        // List preference
        final ListPreference eventsUpdatePeriodPref = new ListPreference(this);
        eventsUpdatePeriodPref.setEntries(prefs.getEventsUpdatePeriodLabels());
        eventsUpdatePeriodPref.setEntryValues(prefs.getEventsUpdatePeriodValues());
        eventsUpdatePeriodPref.setDialogTitle(R.string.event_update);
        eventsUpdatePeriodPref.setKey(prefs.getKey(EVENTS_UPDATE_PERIOD));
        eventsUpdatePeriodPref.setTitle(prefs.getEventsUpdatePeriodSummaryValue());
        settingsPrefCat.addPreference(eventsUpdatePeriodPref);

        eventsUpdatePeriodPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {                
                eventsUpdatePeriodPref.setTitle(prefs.getEventsUpdatePeriodSummaryValue((String) newValue));

                boolean isCustom = "0".equals(newValue);
                if (!isCustom)
                {
                   reinitSyncTimer(newValue, -1);                  
                }
                else
                {
                   // give user a minute to change his mind
                   reinitSyncTimer(prefs.getCustomEventUpdatePeriodValue(), 60000);
                }

                customPeriodPref.setEnabled(isCustom);
                customPeriodPref.setTitle(prefs.getCustomEventUpdatePeriodTitleValueConditional((String) newValue));
                customPeriodPref.setSummary(prefs.getCustomEventUpdatePeriodSummaryValueConditional((String) newValue));

                return true;
            }
                        
        });

        customPeriodPref = new EditTextPreference(this);
        customPeriodPref.setDialogTitle(R.string.event_update_period);
        customPeriodPref.setKey(prefs.getKey(EVENTS_CUSTOM_UPDATE_PERIOD));
        customPeriodPref.setTitle(prefs.getCustomEventUpdatePeriodTitleValueDep());
        customPeriodPref.setSummary(prefs.getCustomEventUpdatePeriodSummaryValueDep());
        boolean customPeriodEnabled = "0".equals(prefs.getEventsUpdatePeriodValue());
        customPeriodPref.setEnabled(customPeriodEnabled);
        settingsPrefCat.addPreference(customPeriodPref);

        customPeriodPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

           public boolean onPreferenceChange(Preference preference,
                                             Object newValue) {
              customPeriodPref.setTitle(prefs.getCustomEventUpdatePeriodSummaryValue((String) newValue));
              boolean err = false;
              try
              {
                 long val = Long.valueOf((String) newValue);
                 
                 if (val < 1)
                    err = true;
              }
              catch(Exception ex)
              {
                 err = true;
              }
              
              if (err)
              {
                 Toast.makeText(SettingsActivity.this, R.string.invalid_value_minutes, 5000).show();
                 return false;
              }
              reinitSyncTimer(newValue, -1);
              return true;
           }
                       
        });
        
        
        PreferenceCategory settingsRadiusCat = new PreferenceCategory(this);
        settingsRadiusCat.setTitle(R.string.radius);
        root.addPreference(settingsRadiusCat);

        // Radius
        final ListPreference radiusPref = new ListPreference(this);
        radiusPref.setEntries(prefs.radiusLabel);
        radiusPref.setEntryValues(prefs.radiusValues);
        radiusPref.setDialogTitle(R.string.radius);
        radiusPref.setKey(prefs.getKey(EVENTS_NOTIFICATION_RADIUS));
        radiusPref.setTitle(prefs.getRadiusSummaryValue());
        settingsRadiusCat.addPreference(radiusPref);

        radiusPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {
                radiusPref.setTitle(prefs.getRadiusSummaryValue((String) newValue));
                boolean isCustom = "0".equals(newValue);
                customRadiusPref.setEnabled(isCustom);
                customRadiusPref.setTitle(prefs.getCustomRadiusTitleValueConditional((String) newValue));
                customRadiusPref.setSummary(prefs.getCustomRadiusSummaryValueConditional((String) newValue));
                return true;
            }
        });
        
        customRadiusPref = new EditTextPreference(this);
        customRadiusPref.setDialogTitle(R.string.radius_km);
        customRadiusPref.setKey(prefs.getKey(EVENTS_CUSTOM_RADIUS));
        customRadiusPref.setTitle(prefs.getCustomRadiusTitleValueDep());
        customRadiusPref.setSummary(prefs.getCustomRadiusSummaryValueDep());
        boolean customRadiusEnabled = "0".equals(prefs.getEventNotificationRadius());
        customRadiusPref.setEnabled(customRadiusEnabled);
        settingsRadiusCat.addPreference(customRadiusPref);

        customRadiusPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

           public boolean onPreferenceChange(Preference preference,
                                             Object newValue) {                
              customRadiusPref.setTitle(prefs.getCustomRadiusSummaryValue((String) newValue));
              boolean err = false;
              try
              {
                 double val = DoubleKmConverter.fromValueStringToKmDouble((String) newValue);
                 if (val <= 0)
                    err = true;
              }
              catch(Exception ex)
              {
                 err = true;
              }
              
              if (err)
              {
                 Toast.makeText(SettingsActivity.this, R.string.invalid_value_radius, 5000).show();
                 return false;
              }
              return true;
           }
                       
        });
        
        PreferenceCategory notificationPropertiesCat = new PreferenceCategory(this);
        notificationPropertiesCat.setTitle(R.string.informing_attributes);
        root.addPreference(notificationPropertiesCat);

        useSoundPref = new CheckBoxPreference(this);
        useSoundPref.setKey(prefs.getKey(EVENTS_NOTIFICATION_SOUND));
        useSoundPref.setTitle(R.string.informing_with_sound);
        notificationPropertiesCat.addPreference(useSoundPref);
        
        useVibratorPref = new CheckBoxPreference(this);
        useVibratorPref.setKey(prefs.getKey(EVENTS_NOTIFICATION_VIBRATE));
        useVibratorPref.setTitle(R.string.informing_with_vibrating);
        notificationPropertiesCat.addPreference(useVibratorPref);

        PreferenceCategory settingsLoginCat = new PreferenceCategory(this);
        settingsLoginCat.setTitle(R.string.login);
        root.addPreference(settingsLoginCat);

        userLoginPref = getPreferenceManager().createPreferenceScreen(this);
        userLoginPref.setIntent(new Intent(this, UserWizardActivity.class));
        userLoginPref.setTitle(R.string.new_login);
        updateUserLoginPref();
        settingsLoginCat.addPreference(userLoginPref);

        PreferenceScreen lostPassPref = getPreferenceManager().createPreferenceScreen(this);
        lostPassPref.setIntent(new Intent(this, LostPasswordWizardActivity.class));
        lostPassPref.setTitle(R.string.lost_password);
        updateUserLoginPref();
        settingsLoginCat.addPreference(lostPassPref);
                

/*        
        PreferenceCategory settingsDevelCat = new PreferenceCategory(this);
        settingsDevelCat.setTitle("Razvoj");
        root.addPreference(settingsDevelCat);

        PreferenceScreen facebookPref = getPreferenceManager().createPreferenceScreen(this);
        facebookPref.setIntent(new Intent(this, FacebookTestActivity.class));
        facebookPref.setTitle("Facebook Test");
        settingsDevelCat.addPreference(facebookPref);
*/
        setPreferenceScreen(root);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Reset proximity alerts
        long currentEffectiveRadius = Long.parseLong(prefs.getEffectiveRadiusValue());
        if (lastKnownEffectiveRadius != currentEffectiveRadius)
        {
           lastKnownEffectiveRadius = currentEffectiveRadius;
           Application.getInstance().getMasterLocationListener().resetProximityAlerts(this);   
        }
    }

    private void reinitSyncTimer(Object newValue, long startDelay)
    {
       TozibabaService svc = TozibabaService.getInstance(null);
       if (svc != null)
       {
          svc.reinitSyncTimer(60000 * Long.valueOf((String) newValue), startDelay);
       }
    }

    private void updateUserLoginPref() {
        String login = Application.getInstance().getPreferences(this).getUserLogin();
        userLoginPref.setSummary(login == null ? getResources().getString(R.string.not_logged_in)  :  getResources().getString(R.string.logged_in_as) +  ": '" + login + "'");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserLoginPref();
        lastKnownEffectiveRadius = Long.parseLong(prefs.getEffectiveRadiusValue());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Tools.buildMainMenu(menu, this, false, true, false, false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       return Tools.processOptionsItemSelected(item, this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
       if (resultCode == Application.RESULT_EXIT)
       {
          setResult(Application.RESULT_EXIT);
          finish();
       }
    }    
}
