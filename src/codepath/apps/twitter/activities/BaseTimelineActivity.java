package codepath.apps.twitter.activities;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.helpers.Util;
import codepath.apps.twitter.models.ImageButtonData;
import codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONObject;

/**
 * BaseTimelineActivity - base activity for activities that have timelines
 */
public abstract class BaseTimelineActivity extends FragmentActivity {

	/** request code for compose activity */
	public static final int COMPOSE_REQUEST_CODE = 7;
	/** name of intent bundle that contains posted tweet's contents */
	public static final String POSTED_TWEET_EXTRA = "posted_tweet";
	/** name of intent bundle that contains user's name */
	public static final String USER_NAME_EXTRA = "user_name";
	/** name of intent bundle that contains user's screenname */
	public static final String USER_SCREEN_NAME_EXTRA = "user_screen_name";
	/** name of the intent bundle that contains user's profile image url */
	public static final String USER_PROFILE_IMAGE_URL_EXTRA = "user_profile_image_url";
	/** name of the intent bundle that contains a user object */
	public static final String USER_EXTRA = "user";
	/** name of the intent bundle that contains a user id */
	public static final String USER_ID_EXTRA = "user_id";
	/** name of the intent bundle that contains some string to prepopulate into the text field */
	public static final String PREPOPULATED_STRING_EXTRA = "prepopulated_string_extra";

	/** Gets the user associated with this activity */
	protected abstract User getUser();

	/** Callback for when a user's image on the timeline is clicked */
	public abstract void onUserImageClick(View v);

	/**
	 * callback when unfavorited button is hit (means user wants to favorite)
	 * @param v		unfavorite button view
	 */
	public void onFavorite(final View v) {
		ImageButtonData tag = (ImageButtonData) v.getTag();
		TwitterApp.getRestClient().favorite(tag.getTweet().getTweetId(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				// show/hide corresponding buttons
				ImageButtonData tag = (ImageButtonData) v.getTag();
				v.setVisibility(View.INVISIBLE);
				tag.getRelatedImageButton().setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Util.failureToastHelper(getBaseContext(), "Failed to favorite tweet: ", jsonObject);
			}
		});
	}

	/**
	 * callback when favorited button is hit (means user wants to unfavorite)
	 * @param v		favorite button view
	 */
	public void onUnfavorite(final View v) {
		ImageButtonData tag = (ImageButtonData) v.getTag();
		TwitterApp.getRestClient().unfavorite(tag.getTweet().getTweetId(), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				// show/hide corresponding buttons
				ImageButtonData tag = (ImageButtonData) v.getTag();
				v.setVisibility(View.INVISIBLE);
				tag.getRelatedImageButton().setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Util.failureToastHelper(getBaseContext(), "Failed to unfavorite tweet: ", jsonObject);
			}
		});
	}

	/** Callback for when the reply button on a tweet in the timeline is clicked */
	public void onReply(View v) {
		composeHelper("@"+v.getTag()+" ");
	}

	/** helper function that opens the compose activity */
	protected void composeHelper(String prepopulatedStr) {
		Intent i = new Intent(this, ComposeActivity.class);
		i.putExtra(USER_NAME_EXTRA, getUser().getName());
		i.putExtra(USER_SCREEN_NAME_EXTRA, getUser().getScreenName());
		i.putExtra(USER_PROFILE_IMAGE_URL_EXTRA, getUser().getProfileImageUrl());
		i.putExtra(PREPOPULATED_STRING_EXTRA, prepopulatedStr);
		startActivityForResult(i, COMPOSE_REQUEST_CODE);
	}
}
