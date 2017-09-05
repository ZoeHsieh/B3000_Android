package com.anxell.e3ak;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.anxell.e3ak.custom.FontButton;
import com.anxell.e3ak.custom.FontTextView;
import com.anxell.e3ak.custom.MyButton;
import com.anxell.e3ak.data.UserData;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity1 extends BaseActivity implements View.OnClickListener {
    private FontTextView mNoUserTV;
    private LinearLayout mUserContainer;
    private RecyclerView mRecyclerView;
    private MyButton mAddBtn;
    private MyButton mNextBtn;
    private UsersAdapter1 mAdapter;

   // private List<UserData> mUserDataList;

    private boolean mDemoShowUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_1);

        findViews();
        setListeners();

        mAdapter = new UsersAdapter1();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }





    private void findViews() {
        mNoUserTV = (FontTextView) findViewById(R.id.noUser);
        mUserContainer = (LinearLayout) findViewById(R.id.userContainer);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAddBtn = (MyButton) findViewById(R.id.add);
        mNextBtn = (MyButton) findViewById(R.id.next);
    }

    private void setListeners() {
        findViewById(R.id.skip).setOnClickListener(this);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.next).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.skip:
                openHomePage();
                break;

            case R.id.add:
                openAddUserPage();
                break;

            case R.id.next:
                openProximityReadRangePage();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransitionLeftToRight();
    }

    private void openHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void openAddUserPage() {
        Intent intent = new Intent(this, AddUserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("from", Config.FROM_USER_1_PAGE);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransitionBottomToTop();
    }

    private void openProximityReadRangePage() {
        Intent intent = new Intent(this, ProximityReadRangeActivity1.class);
        startActivity(intent);
        overridePendingTransitionRightToLeft();
    }

    private void setFooterView(RecyclerView view){
        View footer = LayoutInflater.from(this).inflate(R.layout.item_user_footer, view, false);
        footer.setOnClickListener(this);
        mAdapter.setFooterView(footer);

        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddUserPage();
            }
        });
    }

    private void showUserData() {
        mNoUserTV.setVisibility(View.GONE);
        mUserContainer.setVisibility(View.VISIBLE);
        mAddBtn.setVisibility(View.GONE);
        mNextBtn.setVisibility(View.VISIBLE);
    }
}
