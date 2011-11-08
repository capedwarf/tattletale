package com.alterjoc.radar.client.login;

import com.alterjoc.radar.client.R;

import android.app.LauncherActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class LostPasswordWizardActivity extends WizardActivity
{
   @Override
   protected void onCreate(Bundle savedBundleInstance)
   {
      super.onCreate(savedBundleInstance);
      
      // Vertical linear layout is our content view
      setContentView(R.layout.recovery_wizard);
      Button newUserButton = (Button) findViewById(R.id.request_recovery_button);
      newUserButton.setOnClickListener(new OnClickListener() {

         public void onClick(View button)
         {
            startActivityForResult(new Intent(LostPasswordWizardActivity.this, RequestPassRecoveryActivity.class), 0);
         }
      });

      Button loginButton = (Button) findViewById(R.id.recover_button);
      loginButton.setOnClickListener(new OnClickListener() {

         public void onClick(View button)
         {
            startActivityForResult(new Intent(LostPasswordWizardActivity.this, RecoverPasswordActivity.class), 0);
         }
      });            
   }
   
   public void onActivityResult(int requestCode, int resultCode, Intent intent)
   {
      if (resultCode == RESULT_OK)
         finish();
   }
}
