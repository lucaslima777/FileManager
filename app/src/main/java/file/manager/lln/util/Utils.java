package file.manager.lln.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;


@SuppressWarnings("unused")
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;
    private static int sInputImageWidth = 0;
    private static int sInputImageHeight = 0;
    private static final String ERROR_CREATE_SHA1_MESSAGE = "Falha em criar SHA1.";
    private static final String REQUEST_GET_METHOD = "GET";
    private static final String ELLIPSIZE_TEXT_VALUE = "…";

    private Utils() {
        // unused
    }

    static int getsInputImageWidth() {
        return sInputImageWidth;
    }

    static int getsInputImageHeight() {
        return sInputImageHeight;
    }

    public static Matrix getMatrixFromExifOrientation(int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180.0f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90.0f);
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90.0f);
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90.0f);
                break;
            default:
                break;
        }
        return matrix;
    }

    public static int getExifOrientationFromAngle(int angle) {
        int normalizedAngle = angle % 360;
        switch (normalizedAngle) {
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Uri ensureUriPermission(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
        return uri;
    }

    public static void generateFileFromInputStream(InputStream inputStream, File file) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len1;
            while ((len1 = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len1);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Erro ao gerar inputStream - arquivo não encontrado");
        } catch (IOException e) {
            Log.e(TAG, "Erro ao gerar inputStream - I/O");
        } finally {
            tryClose(fileOutputStream);
        }
    }

    private static void tryClose(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao fechar stream");
            }
        }
    }

    public static HttpURLConnection getConnectionFromUrl(String url) {
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(REQUEST_GET_METHOD);
            c.setDoOutput(false);
            c.connect();

            return c;
        } catch (ProtocolException e) {
            Log.e(TAG, "Erro recuperar conexão - erro de protocolo", e);
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Erro recuperar conexão - I/O", e);
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri sourceUri,
                                                    int requestSize) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(sourceUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Utils.calculateInSampleSize(context, sourceUri, requestSize);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private static int calculateInSampleSize(Context context, Uri sourceUri, int requestSize) {
        InputStream is = null;
        // check image size
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = context.getContentResolver().openInputStream(sourceUri);
            BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException ignored) {
            Log.e(TAG, ignored.getMessage());
        } finally {
            closeQuietly(is);
        }
        int inSampleSize = 1;
        sInputImageWidth = options.outWidth;
        sInputImageHeight = options.outHeight;
        while (options.outWidth / inSampleSize > requestSize
                || options.outHeight / inSampleSize > requestSize) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    public static Bitmap getScaledBitmapForHeight(Bitmap bitmap, int outHeight) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outWidth = Math.round(outHeight * ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    public static Bitmap getScaledBitmapForWidth(Bitmap bitmap, int outWidth) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outHeight = Math.round(outWidth / ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    static Bitmap getScaledBitmap(Bitmap bitmap, int outWidth, int outHeight) {
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(
                (float) outWidth / (float) currentWidth,
                (float) outHeight / (float) currentHeight
        );
        return Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, scaleMatrix, true);
    }

    public static int getMaxSize() {
        int maxSize = SIZE_DEFAULT;
        int[] arr = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, arr, 0);
        if (arr[0] > 0) {
            maxSize = Math.min(arr[0], SIZE_LIMIT);
        }
        return maxSize;
    }

    static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Gera um hash SHA1 a partir da string passada
     *
     * @param input string base para gerar o hash
     * @return hash SHA1
     */
    public static String makeSHA1Hash(String input) {
        StringBuilder hexStr = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            md.reset();
            byte[] buffer = input.getBytes("UTF-8");
            md.update(buffer);
            byte[] digest = md.digest();

            for (byte aDigest : digest) {
                hexStr.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            Log.e(ERROR_CREATE_SHA1_MESSAGE, e.getMessage());
        }
        return hexStr.toString();
    }

    /**
     * Método utilizado para configurar um componente de text para não exibir ellipse com espaço
     * entre as palavras e o sinal de continuidade de texto(…).
     *
     * @param textView TextView desejado
     */
    public static void ellipsizeTextWithoutSpace(final TextView textView) {
        removeSpaceFromEllipsizeText(textView);
        textView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeSpaceFromEllipsizeText((TextView) v);
            }
        });
    }

    private static void removeSpaceFromEllipsizeText(TextView v) {
        final String ellipsiWhiteWhiteSpace = " " + ELLIPSIZE_TEXT_VALUE;
        if (v.getLayout() != null) {
            CharSequence charSequence = v.getLayout().getText();
            if (charSequence.toString().contains(ellipsiWhiteWhiteSpace)) {
                v.setText(charSequence.toString().replace(ellipsiWhiteWhiteSpace,
                        ELLIPSIZE_TEXT_VALUE));
            }
        }
    }

    public static String hashMapToString(Map<String, Object> map, boolean useTab) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (useTab) {
                sb.append("\t");
            }
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }
}
