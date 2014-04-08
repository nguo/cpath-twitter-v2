package codepath.apps.twitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;
import android.widget.TextView;
import codepath.apps.twitter.R;
import codepath.apps.twitter.TwitterApp;
import codepath.apps.twitter.fragments.UserTimelineFragment;
import codepath.apps.twitter.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * ProfileActivity - profile screen
 */
public class ProfileActivity extends FragmentActivity {
	/** views */
	private ImageView ivProfBanner;
	private ImageView ivProfPic;
	private TextView tvProfDesc;
	private TextView tvProfRealName;
	private TextView tvProfScreenName;
	private TextView tvNumTweets;
	private TextView tvNumFollowing;
	private TextView tvNumFollowers;

	/** user of this profile */
	private User profileUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		// setup views
		setupViews();
		// set values
		Intent i = getIntent();
		profileUser = (User) i.getSerializableExtra(TimelineActivity.USER_EXTRA);
		if (profileUser != null) {
			setUserInfo();
			createFragment();
		} else {
			loadProfileInfo(i.getLongExtra(TimelineActivity.USER_ID_EXTRA, -1));
		}
	}

	/** setups the views */
	private void setupViews() {
		ivProfPic = (ImageView) findViewById(R.id.ivProfPic);
		ivProfBanner = (ImageView) findViewById(R.id.ivProfBanner);
		tvProfDesc = (TextView) findViewById(R.id.tvProfDesc);
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
				setUserInfo();
				createFragment();
			}

			@Override
			public void onFailure(Throwable throwable, JSONObject jsonObject) {

			}
		});
	}

	/** creates the user timeline fragment */
	private void createFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		UserTimelineFragment fragUserTimeline = UserTimelineFragment.newInstance(profileUser.getUserId());
		ft.replace(R.id.flayoutFragProfile, fragUserTimeline);
		ft.commit();
	}

	/** sets the user info in the views */
	private void setUserInfo() {
		ImageLoader.getInstance().displayImage(profileUser.getProfileImageUrl(), ivProfPic);
		ImageLoader.getInstance().displayImage(profileUser.getProfileBannerUrl(), ivProfBanner);
		tvProfDesc.setText(profileUser.getDescription());
		tvProfRealName.setText(profileUser.getName());
		tvProfScreenName.setText("@"+profileUser.getScreenName());
		DecimalFormat formatter = new DecimalFormat("###,###,###,###");
		tvNumTweets.setText(formatter.format(profileUser.getNumTweets()));
		tvNumFollowing.setText(formatter.format(profileUser.getNumFollowing()));
		tvNumFollowers.setText(formatter.format(profileUser.getNumFollowers()));
	}
}