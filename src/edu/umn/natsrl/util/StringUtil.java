/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
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

package edu.umn.natsrl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Chongmyung Park
 */
public class StringUtil {

    public static String join(String[] s, String delimiter) {
        return join(Arrays.asList(s), delimiter);
    }
    
    public static String join(Collection s, String delimiter) {
        StringBuilder buffer = new StringBuilder();
        Iterator iter = s.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
    
    public static boolean isNumber(String str){
        if(str == null) return false;
        Pattern p = Pattern.compile("([\\p{Digit}]+)?");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
