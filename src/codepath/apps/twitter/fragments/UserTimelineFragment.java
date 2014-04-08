package codepath.apps.twitter.fragments;

import android.os.Bundle;
import android.view.View;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.activities.TimelineActivity;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userId = getArguments().getLong(TimelineActivity.USER_ID_EXTRA, -1);
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
	}

	@Override
	protected void toggleCenterProgressBar(boolean showPb) {
		// tweets list will always be visible in this fragment
		pbCenter.setVisibility(View.INVISIBLE);
		lvTweets.setVisibility(View.VISIBLE);
	}

	/**
	 * Creates a new instance of this fragment
	 * @param userId	user id to save for later
	 * @return			the newly-created instance
	 */
	public static UserTimelineFragment newInstance(long userId) {
		UserTimelineFragment frag = new UserTimelineFragment();
		Bundle args = new Bundle();
		args.putLong(TimelineActivity.USER_ID_EXTRA, userId);
		frag.setArguments(args);
		return frag;
	}
}
