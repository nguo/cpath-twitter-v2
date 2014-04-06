package codepath.apps.twitter.helpers;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Util - util class containing some helper functions that are needed throughout the codebase
 */
public class Util {

	/** @return true if there is network connection */
	public static boolean isNetworkAvailable(Activity activity) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Helper function that shows a toast when an onFailure result comes back from a request
	 * @param context		context
	 * @param prefix		prefix of the toast text
	 * @param jsonObject	contains info about the error text
	 */
	public static void failureToastHelper(Context context, String prefix, JSONObject jsonObject) {
		try {
			JSONArray errorsArray = jsonObject.getJSONArray("errors");
			Toast.makeText(context, prefix + ((JSONObject) errorsArray.get(0)).getString("message"), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {}
	}

}
