package com.primewebtech.darts.gallery;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import eu.davidea.flexibleadapter.items.IHeader;

/**
 * Created by benebsworth on 21/5/17.
 */

public class GalleryDatabaseService {

    private static final String TAG = GalleryDatabaseService.class.getSimpleName();
    private static GalleryDatabaseService mInstance;
    private List<AbstractFlexibleItem> mItems = new ArrayList<AbstractFlexibleItem>();
    private File pictureDirectory;
    private DateOrganiser mDateOrganiser;
    private Context mContext;


    private GalleryDatabaseService(Context context) {
        mContext = context;
    }

    public void createHeadersSectionsGalleryDataset() {
        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");
        mDateOrganiser = new DateOrganiser(pictureDirectory);
        HeaderItem header = null;
        Date dow = new Date();
        int itemIndex = 0;
        int lastHeaderIndex = 0;
        Log.v(TAG, "createHeadersSectionsGalleryDataset");
        for (File file : mDateOrganiser.sortedFiles()) {

            if (!dow.equals(mDateOrganiser.getDay(file))) {
                dow = mDateOrganiser.getDay(file);
                Log.v(TAG, "dow:"+dow.toString());
                header = newHeader(++lastHeaderIndex, dow);
                Log.v(TAG, header.toString());
                mItems.add(newPhotoItem(itemIndex + 1, file, header));
                Log.v(TAG, newPhotoItem(itemIndex + 1, file, header).toString());
            } else {
                mItems.add(newPhotoItem(itemIndex + 1, file, header));
                Log.v(TAG, newPhotoItem(itemIndex + 1, file, header).toString());
            }
            itemIndex++;
        }
    }

    public static GalleryDatabaseService getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GalleryDatabaseService(context);
        }
        return mInstance;
    }

    /*-------------------*/
	/* DATABASE CREATION */
	/*-------------------*/

    public void clear() {
        mItems.clear();
    }

    /*---------------*/
	/* ITEM CREATION */
	/*---------------*/

    /*
     * Creates a Header item.
     */
    public HeaderItem newHeader(int index, Date date) {
        HeaderItem header = new HeaderItem("H"+ index);
        header.setTitle(new SimpleDateFormat("EEE, MMM d", Locale.US).format(date));
        //header is hidden and un-selectable by default!
        return header;
    }

    /*
	 * Creates a normal item with a Header linked.
	 */

    public PhotoItem newPhotoItem(int i, File file, IHeader header) {
        PhotoItem item = new PhotoItem("I" + i,file, (HeaderItem) header);
        item.setTitle("Simple Item " + i);
        item.setFile(file);
        item.setDescription(getDescription(file));
        return item;
    }



    /*
 	 * Creates a staggered item with a Header linked.
 	 */
    private HeaderHolder newHeaderHolder(int i, Date date) {
        HeaderModel model = new HeaderModel("H" + i);
        model.setTitle(new SimpleDateFormat("EEE, MMM d", Locale.US).format(date));
        return new HeaderHolder(model);
    }
    private ItemHolder newItemHolder(int i, File file, HeaderHolder header) {
        ItemModel model = new ItemModel("I" + i);
        model.setFile(file);
        model.setTitle("Holder Item " + i);
        model.setSubtitle("Subtitle " + i);
        return new ItemHolder(model, header);
    }

    /*-----------------------*/
	/* MAIN DATABASE METHODS */
	/*-----------------------*/

    /**
     * @return Always a copy of the original list.
     */
    public List<AbstractFlexibleItem> getDatabaseList() {
        Log.i(TAG, "getDatabaseList");
        //Return a copy of the DB: we will perform some tricky code on this list.
        return new ArrayList<>(mItems);
    }

    public String getDescription(File file) {

        Cursor mCursor;
        String[] mProjection = {
                MediaStore.Images.ImageColumns.TITLE,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.DESCRIPTION
        };

        String selectionClause = MediaStore.Images.ImageColumns.DATA + " = ?";
        String[] selectionArgs = { file.getPath() };
        Log.d(TAG, "getDescription:file:path:"+file.getPath());
        mCursor = mContext.getContentResolver().query(
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
            return null;
        }
        else if (mCursor.getCount() < 1) {
            // No matches found
            return null;
        }
        else {

            while (mCursor.moveToFirst()) {
                String title = mCursor.getString(titleIndex);
                String date = mCursor.getString(dateIndex);
                String description = mCursor.getString(descriptionIndex);

                // Dumps "ID: 1 Word: NewWord Locale: en_US"
                // I added this via Settings > Language & Input > Personal Dictionary
                Log.d(TAG, "Title = " + title +
                        ", Date=" + date +
                        ", Descriptione= "+ description);
                return description;
            }

        }
        return null;
    }
    public void removeItem(IFlexible item) {
        mItems.remove(item);
    }
    public void removeAll() {
        mItems.clear();
    }
    public static void onDestroy() {
        mInstance = null;
    }

    public static List<File> sortByLastModified(File dirPath) {
        List<File> files = Arrays.asList(dirPath.listFiles());
        Collections.sort(files, Collections.reverseOrder());
        return files;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static class DateOrganiser {
        NavigableMap<Date, File> mDirectoryMap;
        File mDirectory;
        List<File> mSortedFiles;

        public DateOrganiser(File fileDir) {
            mDirectory = fileDir;
            mDirectoryMap = new TreeMap<>();
            for (File file : fileDir.listFiles()) {
                mDirectoryMap.put(new Date(file.lastModified()), file);
            }
            mSortedFiles = sortedFiles();

        }
        public List<File> sortedFiles() {
            List<File> files = Arrays.asList(mDirectory.listFiles());
            Collections.sort(files, Collections.reverseOrder());
            return files;
        }
        public Date getDay(File file) {
            return roundDay(new Date(file.lastModified()));
        }

        public SortedMap<Date, File> thisWeeksFiles() {
            Calendar calendar = Calendar.getInstance();
            SortedMap<Date, File> files = mDirectoryMap.subMap(roundWeek(calendar.getTime()), roundDay(calendar.getTime()));
            Log.d(TAG, "thisWeeksFiles:"+files.toString());
            return files;
        }
        public static Date roundDay(Date d) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                return sdf.parse(sdf.format(d));
            } catch (ParseException ex) {
                //This exception will never be thrown, because sdf parses what it   formats
                return d;
            }
        }
        public static Date roundWeek(Date d) {
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(d);
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                return calendar.getTime();
            } catch (Exception ex) {
                //This exception will never be thrown, because sdf parses what it   formats
                return d;
            }
        }


    }

    public boolean isEmpty() {
        return mItems == null || mItems.isEmpty();
    }
    /**
     * A simple item comparator by Id.
     */


}
