package com.darsh.couponstracker;

import com.darsh.couponstracker.util.Utilities;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by darshan on 14/3/17.
 */

public class UtilitiesTest {
    @Test
    public void getLongDateTest() {
        int[][] dates = new int[][]{
                { 2017, 3, 4 },
                { 2017, 10, 6 },
                { 2019, 10, 20 },
                { 2134, 2, 21 }
        };
        long[] expectedLongDates = new long[]{
                20170304,
                20171006,
                20191020,
                21340221
        };
        for (int i = 0; i < dates.length; i++) {
            System.out.println(Utilities.getLongDate(dates[i][0], dates[i][1], dates[i][2]));
            Assert.assertEquals(expectedLongDates[i], Utilities.getLongDate(dates[i][0], dates[i][1], dates[i][2]));
        }
    }

    @Test
    public void getStringDateTest() {
        long[] dates = new long[]{
                20170304,
                20171006,
                20191020,
                21340221
        };
        String[] expectedDates = new String[]{
                "04 Mar, 2017",
                "06 Oct, 2017",
                "20 Oct, 2019",
                "21 Feb, 2134"
        };
        for (int i = 0; i < dates.length; i++) {
            System.out.println(Utilities.getStringDate(dates[i]));
            Assert.assertEquals(expectedDates[i], Utilities.getStringDate(dates[i]));
        }
    }
}
