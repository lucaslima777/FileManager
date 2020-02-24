package file.manager.lln.infrastructure;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * Classe de utilização exclusiva nas classes de repository
 */
public class NetworkUtil {

    private Context mContext;

    public NetworkUtil(final Context context) {
        mContext = context;
    }

    /**
     * Verify if the connection is available
     *
     * @return true if the connection is available, false otherwise
     */
    private Boolean isConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager.getActiveNetworkInfo() == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Verify if the Wifi connection is available
     *
     * @return true if the Wifi connection is available, false otherwise
     */
    private Boolean isWifiConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            return false;
        }

        return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Verify if the Mobile connection (3G) is available
     *
     * @return true if the Mobile connection (3G) is available, false otherwise
     */
    private Boolean isMobileConnectionAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null) {
            return false;
        }

        return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }
}
