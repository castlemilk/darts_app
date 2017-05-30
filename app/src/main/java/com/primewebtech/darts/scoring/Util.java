package com.primewebtech.darts.scoring;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benebsworth on 30/5/17.
 */

public class Util {

    public static List<Integer> makeSequence(int begin, int end) {
        List<Integer> ret = new ArrayList<>(end - begin + 1);
        for (int i=begin; i<=end; i++) {
            ret.add(i);
        }
        return ret;
    }
}
