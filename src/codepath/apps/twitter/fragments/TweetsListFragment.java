package codepath.apps.twitter.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import codepath.apps.twitter.R;
import codepath.apps.twitter.adapters.TweetsAdapter;
import codepath.apps.twitter.helpers.EndlessScrollListener;
import codepath.apps.twitter.helpers.Util;
import codepath.apps.twitter.models.Tweet;
import eu.erikw.PullToRefreshListView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * TweetsListFragment - generic abstract fragment designed to show a list of tweets
 */
abstract public class TweetsListFragment extends Fragment {
	/** views */
	protected ProgressBar pbCenter;
	protected PullToRefreshListView lvTweets;
	protected View footerView;

	/** list of tweets */
	protected LinkedList<Tweet> tweetsList = new LinkedList<Tweet>();
	/** tweets adapter */
	protected TweetsAdapter adapter;

	/** the current id of the oldest tweet we pulled (must be positive) */
	protected long currentOldestTweetId = -1;
	/** the current id of the newest tweet we pulled (must be positive) */
	protected long currentNewestTweetId = -1;
	/** if true, then we are still awaiting a response from the previous tweets request */
	protected boolean isFetchingTweets = false;
	/** if true, then we want to request more (older) tweets */
	protected boolean areOlderTweetsWanted = false;
	/** the id of the tweet that we just posted */
	protected ArrayList<Long> lastPostedTweetIds = new ArrayList<Long>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_tweets_list, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setupViews();
		setupListeners();
	}

	/** setups the views */
	protected void setupViews() {
		pbCenter = (ProgressBar) getActivity().findViewById(R.id.pbCenter);
		lvTweets = (PullToRefreshListView) getActivity().findViewById(R.id.lvTweets);
		footerView = getActivity().getLayoutInflater().inflate(R.layout.lv_footer_item, null);
		adapter = new TweetsAdapter(getActivity(), tweetsList);
		lvTweets.setAdapter(adapter);
		toggleCenterProgressBar(true);
	}

	/** toggles the visibility of the listview and the progress bar */
	protected void toggleCenterProgressBar(boolean showPb) {
		if (showPb) {
			lvTweets.setVisibility(View.INVISIBLE);
			pbCenter.setVisibility(View.VISIBLE);
		} else {
			pbCenter.setVisibility(View.INVISIBLE);
			lvTweets.setVisibility(View.VISIBLE);
		}
	}

	/** setups the listeners on the views */
	protected void setupListeners() {
		lvTweets.setOnScrollListener(new EndlessScrollListener() {
			@Override
			public void onLoadMore(int page, int totalItemsCount) {
				getOldTweets();
			}
		});
		// Set a listener to be invoked when the list should be refreshed.
		lvTweets.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				getNewTweets();
			}
		});
	}

	/**
	 * On the initial tweets retrieval, try to get the tweets from db if applicable
	 * @return		true if we succeeded in getting tweets from db
	 */
	abstract protected boolean handleInitialTweetsFromDb();

	/**
	 * tries to save the given tweet into the db
	 * @param tweet		tweet to save
	 */
	abstract protected void trySaveTweet(Tweet tweet);

	/** makes request to get old tweets */
	protected void getOldTweets() {
		if (handleInitialTweetsFromDb()) {
			return; // no need to do anything more because we got the tweets from the db
		} else if (!Util.isNetworkAvailable(getActivity())) {
			// don't try to make requests and let the user know that there's no internet connection
			Toast.makeText(getActivity(), "Please connect to a network.", Toast.LENGTH_LONG).show();
		} else if (isFetchingTweets && currentOldestTweetId >= 0) {
			areOlderTweetsWanted = true; // can't make request because we're still waiting for previous request to come back
		} else if (!isFetchingTweets) {
			// if no pending fetches, then make the request
			isFetchingTweets = true;
			lvTweets.addFooterView(footerView);
			requestOldTweets();
		}
	}

	/** makes the rest client request for getting old tweets */
	abstract protected void requestOldTweets(); // override in subclasses

	/**
	 * callback when old tweets request succeeds
	 * @param jsonTweets	array containing a list of json tweets
	 */
	protected void onOldTweetsSuccess(JSONArray jsonTweets) {
		ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
		long lastOldestTweetId = currentOldestTweetId;
		// oldest tweet ID should be the lowest ID value
		Iterator<Tweet> iter = tweets.iterator();
		while (iter.hasNext()) {
			Tweet currTweet = iter.next();
			long id = currTweet.getTweetId();
			if (id == lastOldestTweetId) {
				iter.remove(); // duplicate entry. remove. don't want to handle it further
				continue;
			}
			// try to save the current tweet into the db
			trySaveTweet(currTweet);
			if (currentOldestTweetId < 0 || id < currentOldestTweetId) {
				currentOldestTweetId = id; // otherwise set this id as the oldest
			}
			// also update newest tweet ID so we can use this later when we get new tweets
			if (id > currentNewestTweetId) {
				currentNewestTweetId = id;
			}
		}
		// hide progress bars and refresh adapter
		toggleCenterProgressBar(false);
		lvTweets.removeFooterView(footerView);
		adapter.addAll(tweets);
		// reset flag because we're no longer waiting for a response
		isFetchingTweets = false;
		if (areOlderTweetsWanted) {
			// if we previously wanted to get more tweets but was denied because of another request in process,
			// then we can now make the call to get more tweets
			getOldTweets();
			areOlderTweetsWanted = false;
		}
	}

	/**
	 * callback when the old tweets request fails
	 * @param jsonObject	object containing the error messsage
	 */
	protected void onOldTweetsFailure(JSONObject jsonObject) {
		Util.failureToastHelper(getActivity(), "Failed to retrieve tweets: ", jsonObject);
	}

	/** fetches newer tweets */
	protected void getNewTweets() {
		// if there's no internet, don't try to get tweets
		if (!Util.isNetworkAvailable(getActivity())) {
			Toast.makeText(getActivity(), "Please connect to a network.", Toast.LENGTH_LONG).show();
			return;
		}
		// proceed when network is available...
		requestNewTweets();
	}

	/** makes the rest client request for getting new tweets */
	abstract protected void requestNewTweets(); // override in subclasses

	/**
	 * callback when new tweets request succeeds
	 * @param jsonTweets	array containing a list of json tweets
	 */
	protected void onNewTweetsSuccess(JSONArray jsonTweets) {
		ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
		// newest tweet ID should have the highest ID value
		Iterator<Tweet> iter = tweets.iterator();
		while (iter.hasNext()) {
			Tweet currTweet = iter.next();
			long id = currTweet.getTweetId();
			if (lastPostedTweetIds.indexOf(id) >= 0) {
				// because we updated the timeline with the last posted tweet without making a request,
				// we need to remove the same tweet from the newest tweets retrieved from the server so that the tweet isn't duplicated
				iter.remove();
				continue; // don't need to handle further
			}
			// try to save the current tweet into the db
			trySaveTweet(currTweet);
			if (id > currentNewestTweetId) {
				currentNewestTweetId = id;
			}
		}
		toggleCenterProgressBar(false);
		tweetsList.addAll(0, tweets);
		adapter.notifyDataSetChanged();
		lvTweets.onRefreshComplete();
	}

	/**
	 * callback when the old tweets request fails
	 * @param jsonObject	object containing the error messsage
	 */
	protected void onNewTweetsFailure(JSONObject jsonObject) {
		lvTweets.onRefreshComplete();
		Util.failureToastHelper(getActivity(), "Failed to retrieve new tweets: ", jsonObject);
	}
}
