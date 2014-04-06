package codepath.apps.twitter.fragments;

import android.os.Bundle;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * HomeTimelineFragment - fragment specific to the home timeline
 */
public class HomeTimelineFragment extends TweetsListFragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public void processComposedTweet(Tweet t) {
			trySaveTweet(t);
			tweetsList.addFirst(t);
			adapter.notifyDataSetChanged();
			lastPostedTweetIds.add(t.getTweetId());
	}

	@Override
	protected void requestOldTweets() {
		TwitterApp.getRestClient().getHomeTimeline(currentOldestTweetId, -1, new JsonHttpResponseHandler() {
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
		TwitterApp.getRestClient().getHomeTimeline(-1, currentNewestTweetId, new JsonHttpResponseHandler() {
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
}
