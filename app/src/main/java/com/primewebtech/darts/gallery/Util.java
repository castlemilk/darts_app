package com.primewebtech.darts.gallery;

import android.os.Build;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by benebsworth on 7/5/17.
 */

public class Util {

    public static List<File> sortByLastModified(File dirPath) {
        List<File> files = Arrays.asList(dirPath.listFiles());

        Collections.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return Long.compare(o1.lastModified(), o2.lastModified());
                } else {
                    return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
                }
            }
        });
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
}
