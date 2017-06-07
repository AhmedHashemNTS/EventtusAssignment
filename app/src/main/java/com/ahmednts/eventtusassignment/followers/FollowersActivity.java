package com.ahmednts.eventtusassignment.followers;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ahmednts.eventtusassignment.R;
import com.ahmednts.eventtusassignment.data.MyTwitterApiClient;
import com.ahmednts.eventtusassignment.utils.EndlessRecyclerViewScrollListener;
import com.ahmednts.eventtusassignment.utils.UIUtils;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

public class FollowersActivity extends AppCompatActivity implements FollowersContract.View {
    private static final String TAG = FollowersActivity.class.getSimpleName();

    private TextView toolbarTitle;
    private TextView errorMessage;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private FollowersContract.Presenter followersPresenter;

    long userId;

    boolean isLoading;
    int pageIndex = 1;
    int pageSize = 50;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        initUI();

        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (session != null) {
            MyTwitterApiClient myTwitterApiClient = new MyTwitterApiClient(session);
            TwitterCore.getInstance().addApiClient(session, myTwitterApiClient);

            followersPresenter = new FollowersPresenter(myTwitterApiClient, this);
            followersPresenter.loadFollowersList(userId = session.getUserId());
        }
    }

    void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }

        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Followers");
        errorMessage = (TextView) findViewById(R.id.errorMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        UIUtils.setProgressBarColor(this, progressBar, R.color.colorAccent);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.popularMoviesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(false);
        recyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout.setOnRefreshListener(() -> {
                    followersPresenter.loadFollowersList(userId);
                }
        );

    }

    @Override
    public void showFollowers(List<User> users) {
        FollowersAdapter rcAdapter = new FollowersAdapter(users, followerItemClickListener);
        recyclerView.setAdapter(rcAdapter);
    }

    @Override
    public void openFollowerDetailsUI() {

    }

    @Override
    public void showIndicator() {
        isLoading = true;
        recyclerView.setVisibility(android.view.View.GONE);
        errorMessage.setVisibility(android.view.View.GONE);
        progressBar.setVisibility(android.view.View.VISIBLE);
    }

    @Override
    public void hideIndicator() {
        isLoading = false;
        progressBar.setVisibility(android.view.View.GONE);
        errorMessage.setVisibility(android.view.View.GONE);
        recyclerView.setVisibility(android.view.View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showErrorMessage(String msg) {
        progressBar.setVisibility(android.view.View.GONE);
        recyclerView.setVisibility(android.view.View.GONE);
        errorMessage.setVisibility(android.view.View.VISIBLE);
        errorMessage.setText(msg);
    }

    FollowersAdapter.FollowerItemClickListener followerItemClickListener = follower -> {
        followersPresenter.openFollowerDetails(follower);
    };
}
