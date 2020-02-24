package file.manager.lln.infrastructure;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import file.manager.lln.R;

/**
 * Classe responsável por fazer o download de um um arquivo e salvar na pasta sdcard do
 * device do usuário. Para fazer esse processo é necessário ter a permissão do usuário
 * para que o app salve o arquivo.
 */
public class DownloadFileAsyncTask extends AsyncTask<String, Integer, String> {

    public static final String DIRECTORY_TO_SAVE_FILE = Environment.getExternalStorageDirectory().getPath();

    private static final int MEGABYTE = 1024 * 1024;

    private Context mContext;
    private OnDownloadFileListener mListener;
    private String mFileName;

    public DownloadFileAsyncTask(Context mContext, String fileName, OnDownloadFileListener listener) {
        this.mContext = mContext;
        this.mFileName = fileName;
        this.mListener = listener;
    }

    @Override
    protected String doInBackground(String... fileUrl) {
        return handleHttpHttpsConnection(fileUrl[0]);
    }

    @Override
    protected void onPostExecute(String exception) {
        if (mListener == null) {
            return;
        }

        if (TextUtils.isEmpty(exception)) {
            mListener.onDownloadSuccess();
        } else {
            mListener.onDownloadFail();
        }
    }

    private String handleHttpHttpsConnection(String fileUrl) {
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection urlConnection = null;

        try {
            final URL url = new URL(fileUrl);

            urlConnection = openConnection(url);
            urlConnection.connect();

            if (urlConnection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return mContext.getString(R.string.download_file_connection_fail);
            }

            input = urlConnection.getInputStream();

            File file = new File(DIRECTORY_TO_SAVE_FILE, mFileName);
            output = new FileOutputStream(file);

            saveFile(input, output);
        } catch (Exception e) {
            Log.e(Constants.LogTag.DEFAULT_LOG_TAG, getClass().getSimpleName(), e);
            return e.toString();
        } finally {
            closeStreams(input, output, urlConnection);
        }

        return null;
    }

    private static HttpURLConnection openConnection(URL url) throws IOException, GeneralSecurityException {
        if (url.toString().toLowerCase().contains(Constants.Protocol.HTTPS)) {
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

            SSLContext sslContext = SSLContext.getInstance(Constants.Protocol.CA_TLS);
            sslContext.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());

            httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());

            return httpsURLConnection;
        }

        return (HttpURLConnection) url.openConnection();
    }

    private static void closeStreams(InputStream input, OutputStream output, HttpURLConnection urlConnection) {
        try {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) {
            Log.e(Constants.LogTag.DEFAULT_LOG_TAG, DownloadFileAsyncTask.class.getSimpleName(), ignored);
        }

        if (urlConnection != null) {
            urlConnection.disconnect();
        }
    }

    private String saveFile(InputStream input, OutputStream output) throws IOException {
        byte[] data = new byte[MEGABYTE];
        int count;

        while ((count = input.read(data)) != -1) {
            if (isCancelled()) {
                input.close();
                return null;
            }
            output.write(data, 0, count);
        }

        return null;
    }

    public interface OnDownloadFileListener {
        void onDownloadSuccess();

        void onDownloadFail();
    }

    private static class MyTrustManager implements X509TrustManager {

        private static final X509Certificate[] ACCEPTED_ISSUERS = new X509Certificate[]{};

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //Nunca lança exceção!
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            //Nunca lança exceção!
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return ACCEPTED_ISSUERS;
        }
    }
}
