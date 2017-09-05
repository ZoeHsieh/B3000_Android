package com.anxell.e3ak;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.anxell.e3ak.custom.FontEditText;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.MyButton;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;


public class PasswordActivity extends BaseActivity implements View.OnClickListener {

    private FontTextView mDeviceNameTV;
    private FontTextView mUserNameTV;
    private FontEditText mAccountET;
    private FontEditText mPasswordET;
    private FontTextView mInfoTV;
    private MyButton mNextBtn;
    private ImageView mSlidIV;

    private boolean mDemoPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        findViews();
        setListeners();
        disableNextBtn();

        showUserName("Admin");

        registerKeyboardListener();
    }

    private void findViews() {
        mDeviceNameTV = (FontTextView) findViewById(R.id.deviceView);
        mUserNameTV = (FontTextView) findViewById(R.id.userName);
        mAccountET = (FontEditText) findViewById(R.id.account);
        mPasswordET = (FontEditText) findViewById(R.id.password);
        mInfoTV = (FontTextView) findViewById(R.id.info);
        mNextBtn = (MyButton) findViewById(R.id.next);
        mSlidIV = (ImageView) findViewById(R.id.slid);
    }

    private void setListeners() {
        findViewById(R.id.skip).setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPasswordET.addTextChangedListener(passwordWatcher);

        // start demo
        findViewById(R.id.registered).setOnClickListener(this);
        findViewById(R.id.unregistered).setOnClickListener(this);
        // end demo
    }

    private void showUserName(String userName) {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.hello_user), userName);

        mUserNameTV.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skip:
                openHomePage();
                break;

            case R.id.next:

                if (!mDemoPassword) {
                    showMsg(R.string.msg_password_error);
                    mDemoPassword = true;
                } else {
                    openUsers1Page();
                }

                break;

            case R.id.registered:
                showRegisteredView();
                break;

            case R.id.unregistered:
                showUnregisteredView();
                break;
        }
    }

    private void openHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openUsers1Page() {
        Intent intent = new Intent(this, UsersActivity1.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private TextWatcher passwordWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                disableNextBtn();
            } else {
                enableNextBtn();
            }
        }
    };

    private void enableNextBtn() {
        mNextBtn.setBackground(R.drawable.green_btn);
        mNextBtn.setEnabled(true);
    }

    private void disableNextBtn() {
        mNextBtn.setBackground(R.drawable.disabled_btn);
        mNextBtn.setEnabled(false);
    }

    private void hideSlidView() {
        mSlidIV.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        0)
        );
    }

    private void showSlidView() {
        mSlidIV.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
        );
    }

    // Automatically unregistering the event on the Activity's onDestroy
    private void registerKeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if (isOpen) {
                            hideSlidView();
                        } else {
                            showSlidView();
                        }
                    }
                });
    }

    private void showRegisteredView() {
        mUserNameTV.setText(R.string.hello_1);
        mAccountET.setVisibility(View.VISIBLE);
        mInfoTV.setText(R.string.if_you_forget_your_id_or_password);
    }

    private void showUnregisteredView() {
        showUserName("Admin");
        mAccountET.setVisibility(View.GONE);
        mInfoTV.setText(R.string.you_can_find_the_password_from_the_manual);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }
}
