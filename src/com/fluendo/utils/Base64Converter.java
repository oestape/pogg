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
 * GPL com.fluendo.utils.Base64Converter by Fluendo S.L.
 * The package name has been kept just for compatibility.
 *
 * @author Octavi Estape
 */
public class Base64Converter {
    public static String encode(byte[] bytes) {
        return Base64.encodeBytes(bytes);
    }
}
