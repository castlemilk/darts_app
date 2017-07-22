package com.primewebtech.darts;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by benebsworth on 22/7/17.
 */
@RunWith(AndroidJUnit4.class)
public class MediaTests {
    private static final String TAG = MediaTests.class.getSimpleName();
    Cursor mCursor;


    @Test
    public void TestContentProviderQuery() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();

        String[] mProjection = {
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.DESCRIPTION
        };
        String selectionClause = MediaStore.Images.ImageColumns.MIME_TYPE + " = ?";
        String[] selectionArgs = { "image/jpeg" };

        mCursor = appContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                mProjection,
                selectionClause,
                selectionArgs,
                null
        );

        int titleIndex = mCursor.getColumnIndex(MediaStore.Images.ImageColumns.TITLE);
        int dateIndex = mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
        int descriptionIndex = mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DESCRIPTION);
        if (mCursor == null) {
            // Some providers return null if an error occurs whereas others throw an exception
        }
        else if (mCursor.getCount() < 1) {
            // No matches found
        }
        else {

            while (mCursor.moveToNext()) {
                String title = mCursor.getString(titleIndex);
                String date = mCursor.getString(dateIndex);
                String description = mCursor.getString(descriptionIndex);

                // Dumps "ID: 1 Word: NewWord Locale: en_US"
                // I added this via Settings > Language & Input > Personal Dictionary
                Log.d(TAG, "Title = " + title +
                        ", Date=" + date +
                        ", Descriptione= "+ description);
            }

        }
    }
}
