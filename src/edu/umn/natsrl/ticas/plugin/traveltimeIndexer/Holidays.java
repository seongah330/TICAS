package edu.umn.natsrl.ticas.plugin.traveltimeIndexer;

/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * @author Chongmyung Park
 */
import java.util.*;
import java.text.*;

public class Holidays {

    public static Date NewYearsDayObserved(int nYear) {
        Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                return (new GregorianCalendar(--nYear, Calendar.DECEMBER, 31)).getTime();
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 2)).getTime();
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            default:
                return cal.getTime();
        }
    }

    public static Date MartinLutherKing(int nYear) {
        // Third Monday in January
        Calendar cal = new GregorianCalendar(nYear, Calendar.JANUARY, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 16)).getTime();
            case Calendar.MONDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 15)).getTime();
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 21)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 20)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 19)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 18)).getTime();
            default: // Saturday
                return (new GregorianCalendar(nYear, Calendar.JANUARY, 17)).getTime();
        }
    }

    public static Date PresidentsDay(int nYear) {
// Third Monday in February
        Calendar cal = new GregorianCalendar(nYear, Calendar.FEBRUARY, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 16)).getTime();
            case Calendar.MONDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 15)).getTime();
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 21)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 20)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 19)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 18)).getTime();
            default: // Saturday
                return (new GregorianCalendar(nYear, Calendar.FEBRUARY, 17)).getTime();
        }
    }

    public static Date MemorialDay(int nYear) {
// Last Monday in May
        Calendar cal = new GregorianCalendar(nYear, Calendar.MAY, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 30)).getTime();
            case Calendar.MONDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 29)).getTime();
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 28)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 27)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 26)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.MAY, 25)).getTime();
            default: // Saturday
                return (new GregorianCalendar(nYear, Calendar.MAY, 31)).getTime();
        }
    }

    public static Date IndependenceDayObserved(int nYear) {
        Calendar cal = new GregorianCalendar(nYear, Calendar.JULY, 4);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                return (new GregorianCalendar(nYear, Calendar.JULY, 3)).getTime();
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.JULY, 5)).getTime();
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            default:
                return cal.getTime();
        }
    }

    public static Date LaborDay(int nYear) {
// The first Monday in September
        Calendar cal = new GregorianCalendar(nYear, Calendar.SEPTEMBER, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 7)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 6)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 5)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 4)).getTime();
            case Calendar.SATURDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 3)).getTime();
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.SEPTEMBER, 2)).getTime();
            case Calendar.MONDAY:
            default:
                return cal.getTime();
        }
    }

    public static Date ColumbusDay(int nYear) {
// Second Monday in October
        Calendar cal = new GregorianCalendar(nYear, Calendar.OCTOBER, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 9)).getTime();
            case Calendar.MONDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 8)).getTime();
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 14)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 13)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 12)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 11)).getTime();
            default:
                return (new GregorianCalendar(nYear, Calendar.OCTOBER, 10)).getTime();
        }
    }

    public static Date VeteransDayObserved(int nYear) {
//November 11th
        Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 11);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 10)).getTime();
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 12)).getTime();
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            default:
                return cal.getTime();
        }
    }

    public static Date Thanksgiving(int nYear) {
        Calendar cal = new GregorianCalendar(nYear, Calendar.NOVEMBER, 1);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 26)).getTime();
            case Calendar.MONDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 25)).getTime();
            case Calendar.TUESDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 24)).getTime();
            case Calendar.WEDNESDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 23)).getTime();
            case Calendar.THURSDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 22)).getTime();
            case Calendar.FRIDAY:
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 28)).getTime();
            default: // Saturday
                return (new GregorianCalendar(nYear, Calendar.NOVEMBER, 27)).getTime();
        }
    }

    public static Date ChristmasDayObserved(int nYear) {
        Calendar cal = new GregorianCalendar(nYear, Calendar.DECEMBER, 25);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SATURDAY:
                return (new GregorianCalendar(nYear, Calendar.DECEMBER, 24)).getTime();
            case Calendar.SUNDAY:
                return (new GregorianCalendar(nYear, Calendar.DECEMBER, 26)).getTime();
            case Calendar.MONDAY:
            case Calendar.TUESDAY:
            case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:
            case Calendar.FRIDAY:
            default:
                return cal.getTime();
        }
    }

    public static void main(String[] args) {

        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM d");
        for (int nYear = 1997; nYear <= 2020; nYear++) {
            System.out.println("Federal Holidays for " + nYear + ":");
            System.out.printf("%-23s = %s%n", df.format(Holidays.NewYearsDayObserved(nYear)), "New Year's Day (observed)");
            System.out.printf("%-23s = %s%n", df.format(Holidays.MartinLutherKing(nYear)), "ML King Day");
            System.out.printf("%-23s = %s%n", df.format(Holidays.PresidentsDay(nYear)), "President's Day");
            System.out.printf("%-23s = %s%n", df.format(Holidays.MemorialDay(nYear)), "Memorial Day (observed)");
            System.out.printf("%-23s = %s%n", df.format(Holidays.IndependenceDayObserved(nYear)), "Independence Day (observed)");
            System.out.printf("%-23s = %s%n", df.format(Holidays.LaborDay(nYear)), "Labor Day");
            System.out.printf("%-23s = %s%n", df.format(Holidays.ColumbusDay(nYear)), "Columbus Day");
            System.out.printf("%-23s = %s%n", df.format(Holidays.VeteransDayObserved(nYear)), "Veterans Day (observed)");
            System.out.printf("%-23s = %s%n", df.format(Holidays.Thanksgiving(nYear)), "Thanksgiving Day");
            System.out.printf("%-23s = %s%n", df.format(Holidays.ChristmasDayObserved(nYear)), "Christmas Day (observed)");
            System.out.println();
        }
    }
}