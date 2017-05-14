package com.primewebtech.darts.gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
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

/**
 * Created by benebsworth on 7/5/17.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static List<File> sortByLastModified(File dirPath) {
        List<File> files = Arrays.asList(dirPath.listFiles());
        Collections.sort(files, Collections.reverseOrder());
        return files;
    }
    public List<File> getTodaysImages(File dirPath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd");
        Date now = new Date();
        List<File> todaysImages = new ArrayList<>();
        List<File> allFiles = Arrays.asList(dirPath.listFiles());
        for ( File file : allFiles) {
            if ( isSameDay(new Date(file.lastModified()), now)) {
                todaysImages.add(file);
            }
        }
        return todaysImages;
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
    public static List<File> getHeaderIndexes(File fileDir) {
        List<File> mSortedFiles;
        NavigableMap<Date, File> dateMap = new TreeMap<>();
        mSortedFiles = Util.sortByLastModified(fileDir);
        for ( File file : mSortedFiles) {
            Log.d(TAG, ":file:"+file.getPath());
            dateMap.put(new Date(file.lastModified()), file);
        }
        return mSortedFiles;
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
