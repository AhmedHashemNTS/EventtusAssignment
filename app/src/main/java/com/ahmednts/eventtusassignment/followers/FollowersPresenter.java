package com.ahmednts.eventtusassignment.followers;

import android.support.annotation.NonNull;

import com.ahmednts.eventtusassignment.data.MyTwitterApiClient;
import com.ahmednts.eventtusassignment.data.responses.FollowersResponse;
import com.ahmednts.eventtusassignment.utils.Logger;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiException;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/**
 * Created by AhmedNTS on 6/6/2017.
 */

public class FollowersPresenter implements FollowersContract.Presenter {
    private static final String TAG = FollowersPresenter.class.getSimpleName();

    @NonNull
    private final MyTwitterApiClient apiClient;

    @NonNull
    private final FollowersContract.View followersView;

    private final TwitterSession activeSession;

    private Call<FollowersResponse> followersResponseCall;
    private long nextCursor = -1;
    private List<User> followers;

    public FollowersPresenter(@NonNull TwitterSession activeSession, @NonNull MyTwitterApiClient apiClient, @NonNull FollowersContract.View followersView) {
        this.activeSession = activeSession;
        this.apiClient = apiClient;
        this.followersView = followersView;

        followers = new ArrayList<>(0);

        this.followersView.setTitle(activeSession.getUserName());
    }

    @Override
    public void loadFollowersList(boolean reload) {
        Logger.getInstance().withTag(TAG).log("loadFollowersList: userId=" + activeSession.getUserId());

        if (reload) {
            nextCursor = -1;
            followersView.showIndicator();
            followers.clear();
        }

        if (nextCursor == 0) {
            followersView.hideIndicator();
            return;
        }

        Logger.getInstance().withTag(TAG).log("loadFollowersList: nextCursor=" + nextCursor);

        followersResponseCall = apiClient.getTwitterCustomService().followers(activeSession.getUserId(), nextCursor);
        followersResponseCall.enqueue(new Callback<FollowersResponse>() {
            @Override
            public void success(Result<FollowersResponse> result) {
                if (result != null) {
                    if (result.data.users != null && result.data.users.size() > 0) {
                        nextCursor = result.data.next_cursor;
                        Logger.getInstance().withTag(TAG).log("loadFollowersList: nextCursor=" + nextCursor);

                        followers.addAll(result.data.users);

                        followersView.hideIndicator();
                        followersView.showFollowersList(followers);
                    } else if (followers.size() == 0) {
                        followersView.hideIndicator();
                        followersView.showNoResultMessage();
                    }
                }
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
                if (followersResponseCall.isCanceled()) return;

                followersView.hideIndicator();

                if (exception instanceof TwitterApiException) {
                    TwitterApiException apiException = (TwitterApiException) exception;
                    if (apiException.getErrorCode() == 429)
                        followersView.showApiLimitMessage();
                    else
                        followersView.showToastMessage(apiException.getErrorMessage());
                } else if (exception.getCause() instanceof UnknownHostException) {
                    followersView.showNoNetworkMessage();
                }
            }
        });
    }

    @Override
    public void openFollowerDetails(User follower) {
        followersView.openFollowerDetailsUI(follower);
    }

    @Override
    public void stop() {
        followersResponseCall.cancel();
    }
}
