package com.alterjoc.radar.client.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.client.TozibabaService;
import com.alterjoc.radar.client.log.Log;
import org.jboss.capedwarf.common.data.Status;

import static com.alterjoc.radar.client.Application.NULL_CLICK_LISTENER;
import static com.alterjoc.radar.common.Constants.TAG_LOGIN;

public class RequestPassRecoveryActivity extends WizardActivity
{

   private final static int PROGRESS_WAIT = 1000;
   private final static int VALIDATION_ERROR = 1002;
   private final static int SUCCESS = 1003;
   private final static int REMOTE_ERROR = 1005;
   private final static int UNKNOWN_ERROR = 1006;
   
   private ProgressDialog progressDlg;
   private boolean success;
   private String errorMessage;
   private PassRecovery passRecovery;   
   private EditText usernameEdit;
   
   @Override
   protected Dialog onCreateDialog(int id)
   {
      switch (id)
      {
         case PROGRESS_WAIT:
            ProgressDialog dialog = new ProgressDialog(this);            
            dialog.setMessage(getProgressMessageForMode());
            dialog.setIndeterminate(true);
            dialog.setOnDismissListener(new OnDismissListener() {

               public void onDismiss(DialogInterface dialog)
               {
                  if (success)
                  {
                     //setResult(RESULT_OK); // let's not close all the parent                     
                     finish();                     
                     return;
                  }
                  
                  if (passRecovery != null && passRecovery.status == Status.NO_SUCH_ENTITY)
                  {
                     errorMessage = getResources().getString(R.string.user_unknown);
                     dialog(REMOTE_ERROR);
                     return;                           
                  }
                  
                  if (passRecovery != null && passRecovery.status == Status.INVALID_EMAIL)
                  {
                     errorMessage = getResources().getString(R.string.recovery_not_possible);
                     dialog(REMOTE_ERROR);
                     return;                                                
                  }

                  dialog(UNKNOWN_ERROR);                  
               }});
            this.progressDlg = dialog;
            return dialog;

         case VALIDATION_ERROR:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(errorMessage)
                  .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
         case REMOTE_ERROR:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(errorMessage)
                  .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
         case SUCCESS:
            success = true;
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.password_recovery).setMessage(
                  getSuccessMessageForMode())
                  .setPositiveButton("" + R.string.error, NULL_CLICK_LISTENER).create();
         case UNKNOWN_ERROR:
            return new AlertDialog.Builder(this).setIcon(
                  R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                  R.string.error_check_connection)
                  .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
      }
      return null;
   }   
      
   private CharSequence getSuccessMessageForMode()
   {
      return getResources().getString(R.string.email_notification);
   }

   private CharSequence getProgressMessageForMode()
   {
      return R.string.password_recovery + "...";
   }

   @Override
   protected void onCreate(Bundle savedBundleInstance)
   {
      super.onCreate(savedBundleInstance);
      
      requestWindowFeature(Window.FEATURE_LEFT_ICON);
      LinearLayout layout = new LinearLayout(this);      
      layout.setOrientation(LinearLayout.VERTICAL);
      layout.setPadding(5, 5, 5, 5);
      
      setContentView(layout);
      setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

      usernameEdit = new EditText(this);
      usernameEdit.setHint(R.string.username_or_email);
      usernameEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
      usernameEdit.setSingleLine(true);
      usernameEdit.setLines(1);
      usernameEdit.setTextSize(Tools.getButtonsFontSize(this));
      layout.addView(usernameEdit);

      
      LinearLayout buttonsLayout = new LinearLayout(this);
      buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
      buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT, 
            LinearLayout.LayoutParams.WRAP_CONTENT, 0));
      buttonsLayout.setGravity(Gravity.CENTER);
      buttonsLayout.setPadding(0, 5, 0, 0);
      

      Button nextButton = (Button) new Button(this);
      nextButton.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, 
            LinearLayout.LayoutParams.FILL_PARENT, 0));
      nextButton.setText(R.string.m_continue);
      nextButton.setPadding(50, 5, 50, 8);            
      buttonsLayout.addView(nextButton);
            
      nextButton.setOnClickListener(new OnClickListener() {

         public void onClick(View button)
         {
            if (validate() == false)
            {
               dialog(VALIDATION_ERROR);
               return;
            }
                        
            TozibabaService svc = TozibabaService.getInstance(RequestPassRecoveryActivity.this);
            String user = usernameEdit.getText().toString();
            passRecovery = new PassRecovery();
            svc.requestPasswordRecoveryAsync(user, passRecovery);
            dialog(PROGRESS_WAIT);                  
         }
      });

      layout.addView(buttonsLayout);
      layout.requestFocus();
   }

   protected boolean validate()
   {
      return checkNotEmpty(); 
   }

   @Override
   protected void onDestroy()
   {
      super.onDestroy();
   }
   
   class PassRecovery implements TozibabaService.AsyncCallback<Status>
   {  
      Status status;
      
      public void onSuccess(final Status result)
      {         
         RequestPassRecoveryActivity.this.runOnUiThread(new Runnable() {

            public void run()
            {
               status = result;
               success = result == Status.OK;

               if (progressDlg != null)
               {
                  progressDlg.dismiss();
                  progressDlg = null;
               }               
            }            
         });         
      }
      
      public void onError(final Throwable th)
      {
         RequestPassRecoveryActivity.this.runOnUiThread(new Runnable() {

            public void run()
            {         
               Log.e(TAG_LOGIN, "Request to recover password failed: ", th);
               if (progressDlg != null)
               {
                  progressDlg.dismiss();
                  progressDlg = null;
               }
            }
         });
      }
   }
   
   private boolean checkNotEmpty()
   {      
      boolean empty = false;
      String text = usernameEdit.getText().toString();      
      if (text.length() == 0)
         empty = true;

      if (empty)
      {
         errorMessage = getResources().getString(R.string.please_fill_field);
         return false;
      }
      return true;
   }
      
   protected void dialog(int dialogId)
   {
      removeDialog(dialogId);
      showDialog(dialogId);
   }
}
