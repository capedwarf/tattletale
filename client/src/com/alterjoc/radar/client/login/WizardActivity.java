package com.alterjoc.radar.client.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class WizardActivity extends Activity
{

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      // turn off title bar
      // requestWindowFeature(Window.FEATURE_NO_TITLE);
       requestWindowFeature(Window.FEATURE_LEFT_ICON);
   }

   @Override
   protected void onDestroy()
   {
      // TODO Auto-generated method stub
      super.onDestroy();
   }

   @Override
   protected void onPause()
   {
      // TODO Auto-generated method stub
      super.onPause();
   }

   @Override
   protected void onRestart()
   {
      // TODO Auto-generated method stub
      super.onRestart();
   }

   @Override
   protected void onResume()
   {
      // TODO Auto-generated method stub
      super.onResume();
   }

   @Override
   protected void onStart()
   {
      // TODO Auto-generated method stub
      super.onStart();
   }

   @Override
   protected void onStop()
   {
      // TODO Auto-generated method stub
      super.onStop();
   }

}
