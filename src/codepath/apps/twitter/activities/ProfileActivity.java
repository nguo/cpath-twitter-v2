package codepath.apps.twitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import codepath.apps.twitter.R;
import codepath.apps.twitter.fragments.UserTimelineFragment;
import codepath.apps.twitter.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DecimalFormat;

/**
 * ProfileActivity - profile screen
 */
public class ProfileActivity extends BaseTimelineActivity {
	/** views */
	private LinearLayout llInfo;
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
		profileUser = (User) i.getSerializableExtra(BaseTimelineActivity.USER_EXTRA);
		if (profileUser != null) {
			setUserInfo();
			createFragment();
		} else {
			Toast.makeText(this, "Invalid User", Toast.LENGTH_LONG).show();
		}
	}

	/** setups the views */
	private void setupViews() {
		llInfo = (LinearLayout) findViewById(R.id.llInfo);
		ivProfPic = (ImageView) findViewById(R.id.ivProfPic);
		ivProfBanner = (ImageView) findViewById(R.id.ivProfBanner);
		tvProfDesc = (TextView) findViewById(R.id.tvProfDesc);
		tvProfRealName = (TextView) findViewById(R.id.tvProfRealName);
		tvProfScreenName = (TextView) findViewById(R.id.tvProfScreenName);
		tvNumTweets = (TextView) findViewById(R.id.tvNumTweets);
		tvNumFollowing = (TextView) findViewById(R.id.tvNumFollowing);
		tvNumFollowers = (TextView) findViewById(R.id.tvNumFollowers);
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

		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)llInfo.getLayoutParams();
		if (profileUser.getDescription().length() > 0) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			llInfo.setGravity(Gravity.LEFT);
		} else {
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			llInfo.setGravity(Gravity.CENTER);
		}
		llInfo.setLayoutParams(params);
	}

	@Override
	protected User getUser() {
		return profileUser;
	}

	@Override
	public void onUserImageClick(View v) {
		return; // do nothing because we're already on this user's profile
	}
}