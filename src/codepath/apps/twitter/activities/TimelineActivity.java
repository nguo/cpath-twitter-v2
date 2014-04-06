package codepath.apps.twitter.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.fragments.HomeTimelineFragment;
import codepath.apps.twitter.fragments.MentionsFragment;
import codepath.apps.twitter.helpers.Util;
import codepath.apps.twitter.models.ImageButtonData;
import codepath.apps.twitter.models.Tweet;
import codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONObject;

/**
 * TimelineActivity - home timeline screen
 */
public class TimelineActivity extends FragmentActivity implements TabListener {
	/** home timeline tab tag name */
	public static final String HOME_TIMELINE_TAB_TAG_NAME = "HomeTimelineFragment";
	/** mentions tab tag name */
	public static final String MENTIONS_TAB_TAG_NAME = "MentionsFragment";
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

	/** views */
	private MenuItem miCompose; // hide before user info is retrieved because the compose activity needs it
	private MenuItem miProfile; // hide before user info is retrieved because the profile activity needs it
	private LinearLayout llCompose; // hide before user info is retrieved because the compose activity needs it
	private HomeTimelineFragment fragHomeTimeline;
	private MentionsFragment fragMentions;

	/** account uer's info */
	private User accountUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		if (savedInstanceState == null) {
			fragHomeTimeline = new HomeTimelineFragment();
			fragMentions = new MentionsFragment();
		}
		setupViews();
		setupNavigationTabs();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.timeline, menu);
		miCompose = menu.findItem(R.id.miCompose);
		miCompose.setEnabled(false);
		miProfile = menu.findItem(R.id.miProfile);
		miProfile.setEnabled(false);
		getUserInfo();
		return true;
	}

	/** setups navigation tabs */
	private void setupNavigationTabs() {
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);
		Tab tabHome = actionBar.newTab()
				.setText("Home")
				.setTag(HOME_TIMELINE_TAB_TAG_NAME)
				.setTabListener(this);
		Tab tabMentions = actionBar.newTab()
				.setText("Mentions")
				.setTag(MENTIONS_TAB_TAG_NAME)
				.setTabListener(this);
		actionBar.addTab(tabHome);
		actionBar.addTab(tabMentions);
		actionBar.selectTab(tabHome);
	}

	/** setups the views */
	private void setupViews() {
		llCompose = (LinearLayout) findViewById(R.id.llCompose);
		llCompose.setVisibility(View.INVISIBLE);
	}

	/** gets the user's account info */
	private void getUserInfo() {
		// if there's no internet, don't try to get user info
		if (!Util.isNetworkAvailable(this)) {
			Toast.makeText(this, "Please connect to a network.", Toast.LENGTH_LONG).show();
			return;
		}

		// proceed when network is available...
		TwitterApp.getRestClient().getUserAccount(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonObject) {
				accountUser = User.fromJson(jsonObject);
				// enable compose elements
				llCompose.setVisibility(View.VISIBLE);
				miCompose.setEnabled(true);
				miCompose.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
				miProfile.setEnabled(true);
				miProfile.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {
				Util.failureToastHelper(getBaseContext(), "Failed to retrieve user info: ", jsonObject);
			}
		});
	}

	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// do nothing
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
		FragmentTransaction fts = getSupportFragmentManager().beginTransaction();
		if (tab.getTag().equals(HOME_TIMELINE_TAB_TAG_NAME)) {
			// set fragment to home timeline
			fts.replace(R.id.flayoutFrag, fragHomeTimeline);
		} else {
			// set fragment to mentions timeline
			fts.replace(R.id.flayoutFrag, fragMentions);
		}
		fts.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// do nothing
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == COMPOSE_REQUEST_CODE) {
			Tweet t = (Tweet) data.getSerializableExtra(POSTED_TWEET_EXTRA);
			fragHomeTimeline.processComposedTweet(t);
		}
	}

	/** Callback for when the profile button is pressed */
	public void onProfileView(MenuItem mi) {
		Intent i = new Intent(this, ProfileActivity.class);
		i.putExtra(USER_EXTRA, accountUser);
		startActivity(i);
	}

	/** Callback for when the compose button is pressed */
	public void onCompose(View v) {
		composeHelper("");
	}

	/** Callback for when the compose button is pressed from the action bar */
	public void onCompose(MenuItem mi) {
		composeHelper("");
	}

	/** Callback for when the reply button on a tweet in the timeline is clicked */
	public void onReply(View v) {
		composeHelper("@"+v.getTag()+" ");
	}

	/** helper function that opens the compose activity */
	public void composeHelper(String prepopulatedStr) {
		Intent i = new Intent(this, ComposeActivity.class);
		i.putExtra(USER_NAME_EXTRA, accountUser.getName());
		i.putExtra(USER_SCREEN_NAME_EXTRA, accountUser.getScreenName());
		i.putExtra(USER_PROFILE_IMAGE_URL_EXTRA, accountUser.getProfileImageUrl());
		i.putExtra(PREPOPULATED_STRING_EXTRA, prepopulatedStr);
		startActivityForResult(i, COMPOSE_REQUEST_CODE);
	}

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
}