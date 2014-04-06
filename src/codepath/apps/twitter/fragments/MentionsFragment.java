package codepath.apps.twitter.fragments;

import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * MentionsFragment - fragment specific to the user's mentions
 */
public class MentionsFragment extends TweetsListFragment {
	@Override
	protected void requestOldTweets() {
		TwitterApp.getRestClient().getMentions(currentOldestTweetId, -1, new JsonHttpResponseHandler() {
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
		TwitterApp.getRestClient().getMentions(-1, currentNewestTweetId, new JsonHttpResponseHandler() {
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
	protected boolean handleInitialTweetsFromDb() {
		return false; // we do not save tweets from mentions
	}

	@Override
	protected void trySaveTweet(Tweet tweet) {
		// do nothing. we do not save mentions to the db.
	}
}
