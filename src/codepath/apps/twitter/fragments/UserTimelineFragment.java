package codepath.apps.twitter.fragments;

import android.os.Bundle;
import android.view.View;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * UserTimelineFragment - fragment showing a user's tweets
 */
public class UserTimelineFragment extends TweetsListFragment {
	/** user id */
	private long userId = -1;

	/** if true, then activity as been created */
	private boolean isActivityCreated = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		isActivityCreated = true;
	}

	@Override
	protected boolean handleInitialTweetsFromDb() {
		return false;
	}

	@Override
	protected void trySaveTweet(Tweet tweet) {
		// do nothing
	}

	@Override
	protected void requestOldTweets() {
		if (userId < 0) {
			return; // userId has not been set up yet
		}
		TwitterApp.getRestClient().getUserTimeline(currentOldestTweetId, -1, userId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				onOldTweetsSuccess(jsonTweets);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				onOldTweetsFailure(jsonObject);
			}
		});
	}

	@Override
	protected void requestNewTweets() {
		if (userId < 0) {
			return; // userId has not been set up yet
		}
		TwitterApp.getRestClient().getUserTimeline(-1, currentNewestTweetId, userId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONArray jsonTweets) {
				onNewTweetsSuccess(jsonTweets);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				onNewTweetsFailure(jsonObject);
			}
		});
	}

	@Override
	public void onDetach() {
		super.onDetach();
		userId = -1;
		isActivityCreated = false;
	}

	@Override
	protected void setupViews() {
		super.setupViews();
		// progress bar will always be invisible in this fragment
		pbCenter.setVisibility(View.INVISIBLE);
		lvTweets.setVisibility(View.VISIBLE);
	}

	@Override
	protected void toggleCenterProgressBar(boolean showPb) {
		// do nothing. tweets list will always be visible in this fragment
	}

	/**
	 * handle the user id passed in
	 * @param userId
	 */
	public void handleUserId(long userId) {
		this.userId = userId;
		if (isActivityCreated) {
			requestOldTweets();
		}
	}
}
