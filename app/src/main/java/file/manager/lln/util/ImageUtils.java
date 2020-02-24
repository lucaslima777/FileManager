package file.manager.lln.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ImageUtils {

    private static final int DEFAULT_IMAGE_SIZE = 1024;

    private ImageUtils() {

    }

    public static void saveImage(String imagePath, Bitmap image) {
        if (image == null) {
            Log.e("ERRO","image can't be null");
            return;
        }

        FileOutputStream out;
        try {
            File file = new File(imagePath);
            if (file.exists() && !file.delete()) {
                Log.e("ERRO","Não foi possivel deletar arquivo");
            }
            out = new FileOutputStream(imagePath);
        } catch (FileNotFoundException e) {
            Log.e("ERRO", "error process facebook image");
            return;
        }

        image.compress(Bitmap.CompressFormat.PNG, 60, out);

        try {
            out.close();
        } catch (IOException e) {
            Log.e("ERRO", "error process facebook image");
        }
    }

    private static void resizeImage(String filename, String newFilename, int imageSize) throws IOException {
        int targetW = imageSize;
        int targetH = imageSize;

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(filename));

        Bitmap bitmap = BitmapFactory.decodeFile(filename, bmOptions);
        if (bitmap == null) {
            Log.e("ERRO","error loading bitmap from file=" + filename);
            return;
        }
        Bitmap rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                        true);

        FileOutputStream out;
        out = new FileOutputStream(newFilename);

        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out);

        out.close();
    }

    public static int getImageOrientation(String imagePath) {
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            Log.e("ERRO", "error on getImageOrientation");
        }
        return rotate;
    }

    private static String getFileExtension(String filePath) {
        return filePath.substring(".".lastIndexOf(filePath) + 1);
    }

    private static boolean isImage(String filePath) {
        String extension = getFileExtension(filePath);
        return extension.compareToIgnoreCase("jpg") == 0 ||
                extension.compareToIgnoreCase("jpeg") == 0 ||
                extension.compareToIgnoreCase("png") == 0;
    }

    public static boolean copyImage(String origin, String destination) throws IOException {
        if (origin == null) {
            return false;
        }

        String errorMessage = "error on copyFileToInternalStorage";

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        if (ImageUtils.isImage(origin)) {
            ImageUtils.resizeImage(origin, destination, DEFAULT_IMAGE_SIZE);
        } else {
            FileInputStream inStream = new FileInputStream(origin);
            FileOutputStream outStream = new FileOutputStream(destination);
            inChannel = inStream.getChannel();
            outChannel = outStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inStream.close();
            outStream.close();
        }

        try {
            if (inChannel != null) {
                inChannel.close();
            }
        } catch (IOException e) {
            Log.e("ERRO", errorMessage);
            return false;
        }

        try {
            if (outChannel != null) {
                outChannel.close();
            }
        } catch (IOException e) {
            Log.e("ERRO", errorMessage);
            return false;
        }

        return true;
    }

    public static Bitmap getBitmap(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getBitmap(String path, int width, int height) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPurgeable = true;

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientation(path));

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        if (bitmap != null) {
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,
                    true);
        }

        return null;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                             int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}