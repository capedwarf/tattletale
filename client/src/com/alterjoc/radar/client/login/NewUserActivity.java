package com.alterjoc.radar.client.login;

import static com.alterjoc.radar.client.Application.NULL_CLICK_LISTENER;
import static com.alterjoc.radar.common.Constants.TAG_LOGIN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.Size;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import com.alterjoc.radar.client.Application;
import com.alterjoc.radar.client.Preferences;
import com.alterjoc.radar.client.R;
import com.alterjoc.radar.client.Tools;
import com.alterjoc.radar.client.TozibabaService;
import com.alterjoc.radar.client.log.Log;
import org.jboss.capedwarf.common.data.LoginInfo;
import org.jboss.capedwarf.common.data.Status;
import org.jboss.capedwarf.common.data.UserInfo;

/**
 * @author <a href="mailto:marko.strukelj@gmail.com">Marko Strukelj</a>
 */
public class NewUserActivity extends WizardActivity {

    private final static int PROGRESS_WAIT = 1000;
    private final static int USER_EXISTS_ALREADY = 1001;
    private final static int VALIDATION_ERROR = 1002;
    private final static int SUCCESS = 1003;
    private final static int BAD_PASSWORD = 1004;
    private final static int BAD_TOKEN = 1005;
    private final static int UNKNOWN_ERROR = 1006;

    private ProgressDialog progressDlg;
    private boolean success;
    private UserCreator userCreator;
    private String errorMessage;

    private EditText usernameEdit;
    private EditText passwordEdit;
    private EditText passwordRepeatEdit;
    private EditText emailEdit;

    private Mode mode = Mode.CREATE;
    private EditText recoveryTokenEdit;

    private Button nextButton;

    static enum Mode {
        LOGIN,
        CREATE,
        RECOVER
    }


    protected void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_WAIT:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getProgressMessageForMode());
                dialog.setIndeterminate(true);
                dialog.setOnDismissListener(new OnDismissListener() {

                    public void onDismiss(DialogInterface dialog) {
                        if (success) {
                            setResult(RESULT_OK);
                            finish();
                            return;
                        } else {
                            if (userCreator.retInfo != null) {
                                if (userCreator.retInfo.getStatus() == Status.DUPLICATE) {
                                    if (mode == Mode.CREATE) {
                                        dialog(USER_EXISTS_ALREADY);
                                        return;
                                    } else if (mode == Mode.LOGIN) {
                                        dialog(BAD_PASSWORD);
                                        return;
                                    }
                                } else if (userCreator.retInfo.getStatus() == Status.NO_SUCH_ENTITY) {
                                    if (mode == Mode.RECOVER) {
                                        dialog(BAD_TOKEN);
                                        return;
                                    }

                                    dialog(BAD_PASSWORD);
                                    return;
                                }
                            }
                            
                            if (errorMessage != null)
                            {
                               dialog(VALIDATION_ERROR);
                               return;
                            }
                        }
                        if (Application.isDebug())
                            Log.e(TAG_LOGIN, "Unknown error: retInfo: " + (userCreator == null ? null :
                                    userCreator.retInfo == null ? null : userCreator.retInfo.toShortString()));
                        dialog(UNKNOWN_ERROR);
                    }
                });
                this.progressDlg = dialog;
                return dialog;

            case BAD_PASSWORD:
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                        R.string.wrong_user)
                        .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
            case BAD_TOKEN:
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                        R.string.wrong_password_or_user)
                        .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
            case USER_EXISTS_ALREADY:
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                        R.string.username + "'" +  userCreator.username + "'" +  R.string.not_available)
                        .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
            case VALIDATION_ERROR:
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(errorMessage)
                        .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();
            case SUCCESS:
                success = true;
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.new_user).setMessage(
                        getSuccessMessageForMode())
                        .setPositiveButton(R.string.m_continue, NULL_CLICK_LISTENER).create();
            case UNKNOWN_ERROR:
                return new AlertDialog.Builder(this).setIcon(
                        R.drawable.alert_dialog_icon).setTitle(R.string.error).setMessage(
                        R.string.error_check_connection)
                        .setPositiveButton(R.string.back, NULL_CLICK_LISTENER).create();

        }
        return null;
    }

    private CharSequence getSuccessMessageForMode() {
        return mode == Mode.CREATE ? getResources().getString(R.string.user_created):
                mode == Mode.LOGIN || mode == Mode.RECOVER ? getResources().getString(R.string.login_success) : "Eeeeck";
    }

    private CharSequence getProgressMessageForMode() {
        return mode == Mode.CREATE ? getResources().getString(R.string.creating_user) :
                mode == Mode.LOGIN || mode == Mode.RECOVER ? getResources().getString(R.string.login) + "..." : "Eeeeck";
    }

    @Override
    protected void onCreate(Bundle savedBundleInstance) {
        super.onCreate(savedBundleInstance);

        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(5, 5, 5, 5);

        setContentView(layout);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.tozibaba16);

        usernameEdit = new EditText(this);
        usernameEdit.setHint(R.string.username_hint);
        usernameEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        usernameEdit.setSingleLine(true);
        usernameEdit.setLines(1);
        usernameEdit.setTextSize(Tools.getButtonsFontSize(this));
        /*
        usernameEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                nextButton.setEnabled(validate());
                return false;
            }
        });
        */
        layout.addView(usernameEdit);

        if (mode == Mode.RECOVER) {
            recoveryTokenEdit = new EditText(this);
            recoveryTokenEdit.setHint(R.string.one_time_password_hint);
            recoveryTokenEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            recoveryTokenEdit.setSingleLine(true);
            recoveryTokenEdit.setLines(1);
            recoveryTokenEdit.setTextSize(Tools.getButtonsFontSize(this));
            layout.addView(recoveryTokenEdit);
        }

        passwordEdit = new EditText(this);
        passwordEdit.setHint(getPasswordLabelForMode() + " " +  getResources().getString(R.string.password_hint));
        passwordEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        passwordEdit.setTextSize(Tools.getButtonsFontSize(this));
        layout.addView(passwordEdit);

        if (mode == Mode.CREATE || mode == Mode.RECOVER) {
            passwordRepeatEdit = new EditText(this);
            passwordRepeatEdit.setHint(getRepeatPasswordLabelForMode());
            passwordRepeatEdit.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordRepeatEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordRepeatEdit.setTextSize(Tools.getButtonsFontSize(this));
            layout.addView(passwordRepeatEdit);
        }

        if (mode == Mode.CREATE) {
            emailEdit = new EditText(this);
            emailEdit.setHint(R.string.email_hint);
            emailEdit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            emailEdit.setSingleLine(true);
            emailEdit.setLines(1);
            emailEdit.setTextSize(Tools.getButtonsFontSize(this));
            layout.addView(emailEdit);
        }

        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 0));
        buttonsLayout.setGravity(Gravity.CENTER);
        buttonsLayout.setPadding(0, 5, 0, 0);

        nextButton = new Button(this);
        nextButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT, 0));
        nextButton.setText(R.string.m_continue);
        nextButton.setPadding(50, 5, 50, 8);
        buttonsLayout.addView(nextButton);

        nextButton.setOnClickListener(new OnClickListener() {

            public void onClick(View button) {
                if (validate() == false) {
                    dialog(VALIDATION_ERROR);
                    return;
                }

                String user = usernameEdit.getText().toString();
                String pass = passwordEdit.getText().toString();

                String recovery = null;
                if (mode == Mode.RECOVER) {
                    recovery = recoveryTokenEdit.getText().toString();
                }

                userCreator = new UserCreator(user, pass, recovery);
                UserInfo userInfo = new UserInfo(user, pass);
                if (mode == Mode.CREATE) {
                    userInfo.setEmail(emailEdit.getText().toString());
                } else if (mode == Mode.RECOVER) {
                    userInfo.setStatus(Status.RECOVERY);
                    userInfo.setRecovery(recovery);
                } else {
                    userInfo.setStatus(Status.LOGIN);
                }

                TozibabaService svc = TozibabaService.getInstance(NewUserActivity.this);
                errorMessage = null;
                svc.createUserAsync(userInfo, userCreator);
                dialog(PROGRESS_WAIT);
            }
        });

        layout.addView(buttonsLayout);
        layout.requestFocus();
    }

    private CharSequence getRepeatPasswordLabelForMode() {
        return mode == Mode.RECOVER ? getResources().getString(R.string.repeat_new_password) : getResources().getString(R.string.repeat_password);
    }

    private CharSequence getPasswordLabelForMode() {
        return mode == Mode.RECOVER ? getResources().getString(R.string.new_password)  : getResources().getString(R.string.password);
    }

    protected boolean validate() {
        if (mode == Mode.CREATE || mode == Mode.RECOVER)
            return checkNotEmpty() && checkPasswordsMatch() && checkFieldLengths();
        else
            return checkNotEmpty() && checkFieldLengths();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class UserCreator implements TozibabaService.AsyncCallback {

        String token;
        String username;
        String pass;
        String recovery;
        long id;
        LoginInfo retInfo;

        UserCreator(String user, String pass, String recovery) {
            this.username = user;
            this.pass = pass;
            this.recovery = recovery;
        }

        public void onSuccess(final Object result) {
            NewUserActivity.this.runOnUiThread(new Runnable() {

                public void run() {
                    retInfo = (LoginInfo) result;
                    final LoginInfo info = retInfo;

                    boolean ok = false;

                    if (mode == Mode.CREATE || mode == Mode.RECOVER) {
                        ok = info.getStatus() == Status.OK;
                    } else if (mode == Mode.LOGIN) {
                        ok = info.getStatus() == Status.DUPLICATE && info.getToken() != null && info.getToken().length() > 0;
                    }

                    if (ok) {
                        id = info.getId();
                        token = info.getToken();

                        Preferences prefs = Application.getInstance().getPreferences(NewUserActivity.this);
                        prefs.setUserToken(token);
                        prefs.setUserId(id);
                        if (mode == Mode.RECOVER) {
                            prefs.setUserLogin(info.getUsername());
                        } else {
                            prefs.setUserLogin(username);
                        }
                        success = true;
                    }

                    if (progressDlg != null) {
                        progressDlg.dismiss();
                        progressDlg = null;
                    }
                }
            });
        }

        public void onError(final Throwable th) {
            Log.e(TAG_LOGIN, "Failed to create user: " + username, th);

            boolean ok = true;
            
            if (th instanceof ConstraintViolationException)
            {
               ok = false;
               Set<ConstraintViolation<?>> violations = ((ConstraintViolationException) th).getConstraintViolations();
               if (violations.size() > 0)
               {
                  final ConstraintViolation<?> violation = violations.iterator().next();                  
                  errorMessage = violation.getMessage();                         
               }
            }
            try {
               
                if (ok && mode == Mode.CREATE || mode == Mode.RECOVER) {
                    // we should try execute login with the remembered username and password
                    // if login succeeds, then status is OK
                    // since we are in job handler we can do this synchronously from here
                    
                       UserInfo userInfo = new UserInfo(username, pass);
                       userInfo.setStatus(Status.LOGIN);
   
                       Log.d(TAG_LOGIN, "Attempting to log in with remembered credentials - username: " + username);
                       LoginInfo ret = TozibabaService.getInstance(null).createUser(userInfo);
                       if (ret.getStatus() == Status.DUPLICATE) {                           
                           String token = ret.getToken();
                           if (token != null && token.length() > 0)
                               UserCreator.this.token = token;
                           else
                               ok = false;
   
                           long id = ret.getId();
                           if (id > 0)
                               UserCreator.this.id = id;
                           else
                               ok = false;
   
                           if (ok) {
                               success = true;
   
                               Preferences prefs = Application.getInstance().getPreferences(NewUserActivity.this);
                               prefs.setUserToken(token);
                               prefs.setUserId(id);
                               prefs.setUserLogin(username);
                           }
                       }
                }
            }
            catch (Exception e) {
                Log.e(TAG_LOGIN, "Failed miserably: ", e);
            }

            NewUserActivity.this.runOnUiThread(new Runnable() {

                public void run() {
                    if (progressDlg != null) {
                        progressDlg.dismiss();
                        progressDlg = null;
                    }
                }
            });
        }
    }

    private boolean checkPasswordsMatch() {
        String pass = passwordEdit.getText().toString();
        String repeatPass = passwordRepeatEdit.getText().toString();

        if (pass.equals(repeatPass) == false) {
            errorMessage = getResources().getString(R.string.passwords_dont_match);
            return false;
        }
        return true;
    }

    private boolean checkNotEmpty() {
        boolean empty = false;
        String text = null;

        text = usernameEdit.getText().toString();
        if (text.length() == 0)
            empty = true;

        if (mode == Mode.RECOVER) {
            text = recoveryTokenEdit.getText().toString();
            if (text.length() == 0)
                empty = true;
        }

        text = passwordEdit.getText().toString();
        if (text.length() == 0)
            empty = true;

        if (mode == Mode.CREATE || mode == Mode.RECOVER) {
            text = passwordRepeatEdit.getText().toString();
            if (text.length() == 0)
                empty = true;
        }

        if (empty) {
            errorMessage = getResources().getString(R.string.fill_all);
            return false;
        }
        return true;
    }

    private boolean checkMinMaxLength(Method m, String text, String errMsg) {
        Annotation[] anns = m.getAnnotations();
        for (Annotation a : anns) {
            if (a instanceof Size) {
                int min = ((Size) a).min();
                int max = ((Size) a).max();
                if (text.length() < min || text.length() > max) {
                    errorMessage = new Formatter().format(errMsg, min, max).toString();
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkMinMaxLength(String getterName, EditText ctrl, String fieldName) throws Exception {
        return checkMinMaxLength(UserInfo.class.getMethod(getterName),
                ctrl.getText().toString(),
                fieldName + " " + R.string.should_contain + "%1$d " + R.string.and + " %2$d " +  R.string.characters + ".");
    }

    private boolean checkFieldLengths() {
        try {
            String text = null;

            boolean check = checkMinMaxLength("getUsername", usernameEdit, getResources().getString(R.string.username));
            if (!check)
                return false;

            check = checkMinMaxLength("getPassword", passwordEdit, getResources().getString(R.string.password));
            if (!check)
                return false;

        }
        catch (Exception e) {
            Log.w(TAG_LOGIN, "Exception while validating login data: ", e);
        }
        return true;
    }

    protected void dialog(int dialogId) {
        removeDialog(dialogId);
        showDialog(dialogId);
    }
}
