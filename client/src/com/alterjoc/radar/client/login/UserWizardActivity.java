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
public class UserWizardActivity extends WizardActivity
{
   @Override
   protected void onCreate(Bundle savedBundleInstance)
   {
      super.onCreate(savedBundleInstance);
      
      // Vertical linear layout is our content view
      setContentView(R.layout.user_wizard);
      Button newUserButton = (Button) findViewById(R.id.new_user_button);
      newUserButton.setOnClickListener(new OnClickListener() {

         public void onClick(View button)
         {
            startActivityForResult(new Intent(UserWizardActivity.this, NewUserActivity.class), 0);
         }
      });

      Button loginButton = (Button) findViewById(R.id.login_button);
      loginButton.setOnClickListener(new OnClickListener() {

         public void onClick(View button)
         {
            startActivityForResult(new Intent(UserWizardActivity.this, LoginActivity.class), 0);
         }
      });      
   }
   
   public void onActivityResult(int requestCode, int resultCode, Intent intent)
   {
      if (resultCode == RESULT_OK)
         finish();
   }
}
