/* 
 * Copyright (C) 2009 Octavi Estape
 *  
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
   
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is a clean room implementation of the original
 * GPL com.fluendo.utils.Debug by Fluendo S.L.
 * The package name has been kept just for compatibility.
 * 
 * @author Octavi Estape
 */
public final class Debug {
    public static final int NOLOG = 4;
    public static final int INFO = 3;
    public static final int ERROR = 2;
    public static final int WARNING = 1;
    public static final int DEBUG = 0;
    public static int lastId = 0;

    private static String[] prefix;

    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    
    private static int printLevel = NOLOG;

    static {
        prefix = new String[4];
        prefix[DEBUG]   = "[DEBUG] ";
        prefix[WARNING] = "[WARN ] ";
        prefix[ERROR]   = "[ERROR] ";
        prefix[INFO]   = "[INFO ] ";
    }
    
    static final public void setLevel(int level) {
        Debug.printLevel = level;
    }

    static final public void log(int level, String str) {
        if(level>=printLevel) {
            System.out.println(time() + prefix[level] + str);
        }
    }

    static final public void log(int level, String str, Exception ex) {
        if(level>=printLevel) {
            System.out.println(time() + prefix[level] + str);
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }

    static final public String time() {
        return sdf.format(Calendar.getInstance().getTime()) + " ";
    }

    static final public void info(String str) {
        log(INFO, str);
    }

    static final public void error(String str) {
        log(ERROR, str);
    }

    static final public void error(String str, Exception ex) {
        log(ERROR, str, ex);
    }

    static final public void warning(String str) {
        log(WARNING, str);
    }

    static final public void warn(String str) {
        log(WARNING, str);
    }

    static final public void debug(String str) {
        log(DEBUG, str);
    }

    static final public int genId() {
        lastId++;
        return lastId;
    }

}
