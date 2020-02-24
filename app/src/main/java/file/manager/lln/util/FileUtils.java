package file.manager.lln.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import file.manager.lln.BuildConfig;

public class FileUtils {
    public static final String MIME_TYPE_IMAGE = "image/*";
    public static final String IMAGE_TYPE = "image";
    public static final String VIDEO_TYPE = "video";
    public static final String EXTENSION_TYPE_IMAGE = "jpg";
    public static final String EXTENSION_TYPE_PDF = "pdf";
    private static final String ERROR_CLOSE_FILE = "Erro ao fechar arquivo";
    private static final String ERROR_SAVE_FILE = "Erro ao salvar arquivo";
    private static final String PROVIDER_DOWNLOAD_FILE = "com.android.providers.downloads.documents";
    private static final String EXTERNAL_STORAGE_DOCUMENT = "com.android.externalstorage.documents";
    private static final String ANDROID_PHOTOS_CONTENT = "com.google.android.apps.photos.content";
    private static final String ANDROID_MEDIA_DOCUMENTS = "com.android.providers.media.documents";
    private static final String SHARE_FILE_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";
    private static final String AUDIO_TYPE = "audio";
    private static final String PRIMARY_TYPE = "primary";
    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";

    private FileUtils() {

    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return ANDROID_MEDIA_DOCUMENTS.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return ANDROID_PHOTOS_CONTENT.equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                if (BuildConfig.DEBUG) {
                    DatabaseUtils.dumpCursor(cursor);
                }

                final int columnIndex = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                return getDocumentProvider(uri);
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                return getDownloadProvider(context, uri);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                return getMediaProvider(context, uri);
            }
        }
        // MediaStore (and general)
        else if (SCHEME_CONTENT.equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if (SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getDownloadProvider(Context context, Uri uri) {
        final String id = DocumentsContract.getDocumentId(uri);
        final Uri contentUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

        return getDataColumn(context, contentUri, null, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getDocumentProvider(Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        if (PRIMARY_TYPE.equalsIgnoreCase(type)) {
            return Environment.getExternalStorageDirectory() + "/" + split[1];
        }

        return "";
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getMediaProvider(Context context, Uri uri) {
        final String docId = DocumentsContract.getDocumentId(uri);
        final String[] split = docId.split(":");
        final String type = split[0];

        Uri contentUri = null;
        if (IMAGE_TYPE.equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (VIDEO_TYPE.equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (AUDIO_TYPE.equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }

        final String selection = "_id=?";
        final String[] selectionArgs = new String[]{
                split[1]
        };

        return getDataColumn(context, contentUri, selection, selectionArgs);
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return EXTERNAL_STORAGE_DOCUMENT.equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return PROVIDER_DOWNLOAD_FILE.equals(uri.getAuthority());
    }

    public static boolean deleteFile(String path) {
        return !android.text.TextUtils.isEmpty(path) && deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        return !file.exists() || file.delete();
    }

    public static boolean fixMediaDir() {
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            File mediaDir = new File(sdcard, "DCIM/Camera");
            File pictureDir = new File(sdcard, "Pictures");
            if (!mediaDir.exists()) {
                return mediaDir.mkdirs();
            }
            if (!pictureDir.exists()) {
                return pictureDir.mkdirs();
            }
        }

        return false;
    }

    /**
     * Copia um arquivo para um diretório
     *
     * @param file O arquivo original
     * @param dir  O diretório que será copiado
     * @return O arquivo copiado ou null em caso de erro
     */
    public static File copyFile(File file, File dir, String fileName) {
        File newFile = new File(dir, fileName);
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            fileOutputStream = new FileOutputStream(newFile);
            if (!copyStream(fileInputStream, fileOutputStream)) {
                newFile = null;
            }
        } catch (IOException ex) {
            Log.e("ERRO", ERROR_SAVE_FILE);
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                    Log.e("ERRO", ERROR_CLOSE_FILE);
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ex) {
                    Log.e("ERRO", ERROR_CLOSE_FILE);
                }
            }
        }

        return newFile;
    }

    /**
     * Retorna um nome de arquivo baseado no timestamp com extensão opcional.
     *
     * @param extension (Opcional) A extensão desejada com pelo menos 3 caracteres. Ex: "jpg"
     * @return O nome de arquivo único
     */
    public static String getUniqueFilename(String extension) {
        String fileName = ((Long) System.currentTimeMillis()).toString();
        if (extension != null && extension.length() > 2) {
            fileName += "." + extension;
        }
        return fileName;
    }

    private static boolean copyStream(
            FileInputStream fileInputStream,
            FileOutputStream fileOutputStream) {
        FileChannel outputChannel;
        FileChannel inputChannel;
        if (fileInputStream != null && fileOutputStream != null) {
            try {
                outputChannel = fileOutputStream.getChannel();
                inputChannel = fileInputStream.getChannel();
                inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            } catch (IOException ex) {
                Log.e("ERRO", ERROR_SAVE_FILE);
                return false;
            }
            try {
                inputChannel.close();
                outputChannel.close();
            } catch (IOException ex) {
                Log.e("ERRO", ERROR_CLOSE_FILE);
            }
        }
        return true;
    }

    // Vai fechar o aplicativo, pois vai apagar as preferencias do app
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void deletePreferences(Context context) {
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .clearApplicationUserData();

        //Remocao do barramento 'shared_pref' (xmls)
        File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
        String[] children = dir.list();
        //Remocao de xmls
        for (int i = 0; i < children.length; i++) {
            context.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit();
        }
        //Remocao de arquivos em cache
        for (int i = 0; i < children.length; i++) {
            //Deleta o arquivo quando a VM sumir
            new File(dir, children[i]).deleteOnExit();
        }
    }

    /**
     * Método para ler um arquivo Json da pasta raw dentro do res
     *
     * @param id      id do arquivo a ser lido
     * @param context contexto da tela
     * @return String do Json
     * @throws IOException InputStreamReader
     */
    public static String readFileFromResources(int id, Context context) throws IOException {
        final InputStream inputStream = context.getResources().openRawResource(id);
        Reader reader = new InputStreamReader(inputStream, "UTF-8");
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[16384];
        int len;
        while ((len = reader.read(buffer)) > 0) {
            builder.append(buffer, 0, len);
        }
        reader.close();
        return builder.toString();
    }

    public static String readFileFromAssets(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, SHARE_FILE_AUTHORITY, file);
    }
}