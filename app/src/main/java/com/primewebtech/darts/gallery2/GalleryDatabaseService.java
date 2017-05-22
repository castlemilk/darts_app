package com.primewebtech.darts.gallery2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private Map<StaggeredItemStatus, StaggeredHeaderItem> headers;
    private File pictureDirectory;
    private DateOrganiser mDateOrganiser;


    private GalleryDatabaseService() {


    }

    public void createHolderGalleryDataset() {
        pictureDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Darts/");
        mDateOrganiser = new DateOrganiser(pictureDirectory);
        HeaderHolder header = null;
        Date dow = new Date();
        int index = 0;
        for (File file : mDateOrganiser.sortedFiles()) {
            dow = mDateOrganiser.getDay(file);
            header = !dow.equals(mDateOrganiser.getDay(file)) ? newHeaderHolder(index, dow) : header;
            mItems.add(newItemHolder(index + 1, file, header));
            index++;

        }
    }

    public List<File> getSelectedItems(List<Integer> itemPositions) {
        List<File> items = new ArrayList<>();
        Log.d(TAG, "getSelectedItems:itemPositions:"+itemPositions.toString());
        for ( Integer position: itemPositions) {
            items.add(((PhotoItem) mItems.get(position - 1)).getFile());
            Log.d(TAG, "getSelectedItems:file:"+((PhotoItem) mItems.get(position - 1)).getFile().getPath());
        }
        return items;
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

    public static GalleryDatabaseService getInstance() {
        if (mInstance == null) {
            mInstance = new GalleryDatabaseService();
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
    public static HeaderItem newHeader(int index, Date date) {
        HeaderItem header = new HeaderItem("H"+ index);
        header.setTitle(new SimpleDateFormat("EEE, MMM d", Locale.US).format(date));
        //header is hidden and un-selectable by default!
        return header;
    }

    /*
	 * Creates a normal item with a Header linked.
	 */

    public static PhotoItem newPhotoItem(int i, File file, IHeader header) {
        PhotoItem item = new PhotoItem("I" + i,file,  (HeaderItem) header);
        item.setTitle("Simple Item " + i);
        item.setFile(file);
        return item;
    }



    /*
 	 * Creates a staggered item with a Header linked.
 	 */
    public static StaggeredItem newStaggeredItem(int i, StaggeredHeaderItem header) {
        return new StaggeredItem(i, header);
    }
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

    private boolean isSameDay(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        boolean sameYear = calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
        boolean sameMonth = calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
        boolean sameDay = calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
        return (sameDay && sameMonth && sameYear);
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
    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static class DateOrganiser {
        NavigableMap<Date, File> mDirectoryMap;
        File[] mfileList;
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
        public List<File> groupByDay() {
            List<File> files = mSortedFiles;
            Collections.sort(files, new Comparator<File>() {
                public int compare(File o1, File o2) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        int result = getDay(o1) - getDay(o1);
//                        return Long.compare(o1.lastModified(), o2.lastModified());
                        return getDay(o1).compareTo(getDay(o2));
                    } else {
                        return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                    }
                }
            });
            return files;
        }
        public Date getDay(File file) {
            return roundDay(new Date(file.lastModified()));
        }

        public SortedMap<Date, File> todaysFiles() {
            Calendar calendar = Calendar.getInstance();
//            Map.Entry<Date, File> files = mDirectoryMap.floorEntry(round(calendar.getTime()));
            SortedMap<Date, File> files = mDirectoryMap.tailMap(roundDay(calendar.getTime()));
            Log.d(TAG, "todaysFiles:"+files.toString());
            return files;
        }
        public boolean todaysFilesExist() {
            Calendar calendar = Calendar.getInstance();
//            Map.Entry<Date, File> files = mDirectoryMap.floorEntry(round(calendar.getTime()));
            SortedMap<Date, File> files = mDirectoryMap.tailMap(roundDay(calendar.getTime()));
            Log.d(TAG, "todaysFiles:"+files.toString());
            if (!files.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        public SortedMap<Date, File> thisWeeksFiles() {
            Calendar calendar = Calendar.getInstance();
            SortedMap<Date, File> files = mDirectoryMap.subMap(roundWeek(calendar.getTime()), roundDay(calendar.getTime()));
            Log.d(TAG, "thisWeeksFiles:"+files.toString());
            return files;
        }
        public boolean thisWeeksFilesExist() {
            SortedMap<Date, File> files = thisWeeksFiles();
            Log.d(TAG, "thisWeeksFiles:"+files.toString());
            if (!files.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        public List<Map.Entry<Integer, String>> getDayIndices() {
            List<Map.Entry<Integer, String>> indices = new ArrayList<>();
            List<File> sortedFiles = mSortedFiles;
            Date dow = new Date();
            int index = 0;
            try {
                for (File file : sortedFiles) {
                    if (!dow.equals(getDay(file))) {
                        dow = getDay(file);
                        Log.d(TAG, "day: " + dow.toString());
                        Map.Entry<Integer, String> item = new AbstractMap.SimpleEntry<>(index,
                                new SimpleDateFormat("EEE, MMM d", Locale.US).format(dow));
                        indices.add(item);
                    }
                    Log.d(TAG, new Date(file.lastModified()).toString());
                    index++;
                }
                return indices;
            } catch (Exception e) {
                return null;
            }
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

    /**
     * A simple item comparator by Id.
     */
    public static class ItemComparatorById implements Comparator<AbstractFlexibleItem> {
        @Override
        public int compare(AbstractFlexibleItem lhs, AbstractFlexibleItem rhs) {
            return ((StaggeredItem) lhs).getId() - ((StaggeredItem) rhs).getId();
        }
    }

    /**
     * A complex item comparator able to compare different item types for different view types:
     * Sort by HEADER than by ITEM.
     * <p>In this way items are always displayed in the corresponding section and position.</p>
     */
    public static class ItemComparatorByGroup implements Comparator<IFlexible> {
        @Override
        public int compare(IFlexible lhs, IFlexible rhs) {
            int result = 0;
            if (lhs instanceof StaggeredHeaderItem && rhs instanceof StaggeredHeaderItem) {
                result = ((StaggeredHeaderItem) lhs).getOrder() - ((StaggeredHeaderItem) rhs).getOrder();
            } else if (lhs instanceof StaggeredItem && rhs instanceof StaggeredItem) {
                result = ((StaggeredItem) lhs).getHeader().getOrder() - ((StaggeredItem) rhs).getHeader().getOrder();
                if (result == 0)
                    result = ((StaggeredItem) lhs).getId() - ((StaggeredItem) rhs).getId();
            } else if (lhs instanceof StaggeredItem && rhs instanceof StaggeredHeaderItem) {
                result = ((StaggeredItem) lhs).getHeader().getOrder() - ((StaggeredHeaderItem) rhs).getOrder();
            } else if (lhs instanceof StaggeredHeaderItem && rhs instanceof StaggeredItem) {
                result = ((StaggeredHeaderItem) lhs).getOrder() - ((StaggeredItem) rhs).getHeader().getOrder();
            }
            return result;
        }
    }


}
