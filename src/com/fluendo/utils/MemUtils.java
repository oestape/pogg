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

/**
 * This class is a clean room implementation of the original 
 * GPL com.fluendo.utils.MemUtils by Fluendo S.L.
 * The package name has been kept just for compatibility.
 *
 * @author Octavi Estape
 */
public class MemUtils {

    public static boolean startsWith(byte[] data, int offset, int length, byte[] data2) {

        for(int i=0; i<length && i<data2.length; i++) {
            if(data[i+offset] != data2[i]) return false;
        }
        return true;
    }

    public static void set(short[] dst, int offset, short value, int length) {
        int end = offset+length;
        for(int i=offset; i<end; i++) {
            dst[i] = value;
        }
    }

    public static void set(int[] dst, int offset, int value, int length) {
        int end = offset+length;
        for(int i=offset; i<end; i++) {
            dst[i] = value;
        }
    }

    public static void set(byte[] dst, int offset, int value, int length) {
        int end = offset+length;
        for(int i=offset; i<end; i++) {
            dst[i] = (byte)value;
        }
    }

    public static void set(Object[] dst, int offset, Object value, int length) {
        int end = offset+length;
        for(int i=offset; i<end; i++) {
            dst[i] = value;
        }
    }
}
