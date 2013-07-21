/* JKate
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JKate are based on code by Wim Taymans <wim@fluendo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.fluendo.jkate;

public final class Result {
  public static final int KATE_E_NOT_FOUND            = -1;
  public static final int KATE_E_INVALID_PARAMETER    = -2;
  public static final int KATE_E_OUT_OF_MEMORY        = -3;
  public static final int KATE_E_BAD_GRANULE          = -4;
  public static final int KATE_E_INIT                 = -5;
  public static final int KATE_E_BAD_PACKET           = -6;
  public static final int KATE_E_TEXT                 = -7;
  public static final int KATE_E_LIMIT                = -8;
  public static final int KATE_E_VERSION              = -9;
  public static final int KATE_E_NOT_KATE             = -10;
  public static final int KATE_E_BAD_TAG              = -11;
}
