package com.alterjoc.radar.client.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import com.alterjoc.radar.client.R;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class WizardActivityAndDialog extends Activity
{
   private final static int PROGRESS_WAIT = 1000;
   private final static int USER_EXISTS_ALREADY = 1001;
   private final static int PASSWORDS_DONT_MATCH = 1002;
   private final static int SUCCESS = 1003;
   private final static int UNKNOWN_ERROR = 1004;
   
   private Handler handler = new ProgressHandler();
   private ProgressDialog progressDlg;
   private boolean success;

   @Override
   protected Dialog onCreateDialog(int id)
   {
      switch (id)
      {
         case PROGRESS_WAIT:
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Izdelava uporabnika ...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            this.progressDlg = dialog;
            return dialog;
            
         case USER_EXISTS_ALREADY:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle("Napaka").setMessage(
                  "Uporabnik 'janez' že obstaja!")
                  .setPositiveButton("Nazaj",
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog,
                                 int whichButton)
                           {

                              /* User clicked OK so do some stuff */
                           }
                        }).create();
         case PASSWORDS_DONT_MATCH:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle("Napaka").setMessage(
                  "Gesli se ne ujemata!")
                  .setPositiveButton("Nazaj",
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog,
                                 int whichButton)
                           {

                              /* User clicked OK so do some stuff */
                           }
                        }).create();   
         case SUCCESS:
            success = true;
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.new_user).setMessage(
                  R.string.user_created)
                  .setPositiveButton(R.string.m_continue,
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog,
                                 int whichButton)
                           {

                              /* User clicked OK so do some stuff */
                           }
                        }).create();
         case UNKNOWN_ERROR:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                  R.string.error_occurred)
                  .setPositiveButton(R.string.back,
                        new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog,
                                 int whichButton)
                           {

                              /* User clicked OK so do some stuff */
                           }
                        }).create();

      }
      return null;
   }

   
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      success = false;
      progressDlg = null;
      
      // turn off title bar
      requestWindowFeature(Window.FEATURE_NO_TITLE);

      // Vertical linear layout is our content view
      /*
       * LinearLayout mainLayout = new LinearLayout(this);
       * mainLayout.setOrientation(LinearLayout.VERTICAL);
       * 
       * mainLayout.addView(new HeadView(this), new
       * LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
       * LayoutParams.WRAP_CONTENT, 0)); mainLayout.addView(new
       * ContentView(this), new
       * LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
       * LayoutParams.WRAP_CONTENT, 1)); mainLayout.addView(new
       * FooterView(this), new
       * LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
       * LayoutParams.WRAP_CONTENT, 0)); setContentView(mainLayout);
       */
      setContentView(R.layout.user_wizard_new);
      showDialog(PROGRESS_WAIT);      
      handler.sendMessageDelayed(new Message(), 2000);
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

   
   class ProgressHandler extends Handler
   {
      @Override
      public void handleMessage(Message msg)
      {
         // TODO Auto-generated method stub
         super.handleMessage(msg);
         if (WizardActivityAndDialog.this.progressDlg != null)
         {
            progressDlg.cancel();
            progressDlg = null;
         }
         showDialog(PASSWORDS_DONT_MATCH);
      }
      
   };

}
