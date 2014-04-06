package codepath.apps.twitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;
import android.widget.TextView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.fragments.UserTimelineFragment;
import codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONObject;

/**
 * ProfileActivity - profile screen
 */
public class ProfileActivity extends FragmentActivity {
	/** views */
	private ImageView ivProfPic;
	private TextView tvProfRealName;
	private TextView tvProfScreenName;
	private TextView tvNumTweets;
	private TextView tvNumFollowing;
	private TextView tvNumFollowers;
	private UserTimelineFragment fragUserTimeline;

	/** user of this profile */
	private User profileUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		fragUserTimeline = (UserTimelineFragment) getSupportFragmentManager().findFragmentById(R.id.fragUserTimeline);
		// setup views
		setupViews();
		// set values
		Intent i = getIntent();
		profileUser = (User) i.getSerializableExtra(TimelineActivity.USER_EXTRA);
		if (profileUser != null) {
			fragUserTimeline.handleUserId(profileUser.getUserId());
			setUserInfo();
		} else {
			loadProfileInfo(Long.parseLong(i.getStringExtra(TimelineActivity.USER_ID_EXTRA)));
		}
	}

	/** setups the views */
	private void setupViews() {
		ivProfPic = (ImageView) findViewById(R.id.ivProfPic);
		tvProfRealName = (TextView) findViewById(R.id.tvProfRealName);
		tvProfScreenName = (TextView) findViewById(R.id.tvProfScreenName);
		tvNumTweets = (TextView) findViewById(R.id.tvNumTweets);
		tvNumFollowing = (TextView) findViewById(R.id.tvNumFollowing);
		tvNumFollowers = (TextView) findViewById(R.id.tvNumFollowers);
	}

	/**
	 * loads any user's profile info
	 * @param userId	id of user to fetch info for
	 */
	private void loadProfileInfo(long userId) {
		TwitterApp.getRestClient().getAnyUserAccount(userId, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject jsonUser) {
				profileUser = User.fromJson(jsonUser);
				fragUserTimeline.handleUserId(profileUser.getUserId());
				setUserInfo();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {

			}
		});
	}

	/** sets the user info in the views */
	private void setUserInfo() {
		ImageLoader.getInstance().displayImage(profileUser.getProfileImageUrl(), ivProfPic);
		tvProfRealName.setText(profileUser.getName());
		tvProfScreenName.setText("@"+profileUser.getScreenName());
		tvNumTweets.setText(Integer.toString(profileUser.getNumTweets()));
		tvNumFollowing.setText(Integer.toString(profileUser.getNumFollowing()));
		tvNumFollowers.setText(Integer.toString(profileUser.getNumFollowers()));
	}
}