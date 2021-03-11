package com.benzveen.imagetopdf.Adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.benzveen.imagetopdf.Utils.FileComparator;

import java.io.File;

public class ImageDocument {
    private Uri mdocumentUri;
    String fileName;
    long fileSize;
    long lastModified = 0;

    public ImageDocument(Uri imageUri, Context context) {
        this.mdocumentUri = imageUri;
        String scheme = imageUri.getScheme();
        try {
            if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                fileName = imageUri.getLastPathSegment();
                File file = new File(imageUri.getPath());
                fileSize = file.length();
                lastModified = file.lastModified();
            } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {

                Cursor returnCursor =
                        context.getContentResolver().query(imageUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                fileName = returnCursor.getString(nameIndex);
                fileSize = returnCursor.getLong(sizeIndex);

                String path = FileComparator.getPath(context, imageUri);
                if (path != null) {
                    File file = new File(path);
                    if (file != null)
                        lastModified = file.lastModified();
                }

            }
        } catch (Exception ex) {
            fileName = "Input Image";
        }


    }

    public Uri getImageDocument() {
        return mdocumentUri;
    }

    public void setImageDocument(Uri value) {
        mdocumentUri = value;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getSize() {
        return fileSize;
    }
}
