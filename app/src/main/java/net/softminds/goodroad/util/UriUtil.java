package net.softminds.goodroad.util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.net.URL;

/**
 * Created by hjlee on 2017-08-03.
 */

public class UriUtil {
    private static String TAG = "UrlUtil";
    public static String getRealPathFromURI(Uri contentURI, ContentResolver contentResolver) {
        String result;
        Cursor cursor = contentResolver.query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
