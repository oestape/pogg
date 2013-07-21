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

public class KateCurveType {
  public static final KateCurveType kate_curve_none = new KateCurveType ();
  public static final KateCurveType kate_curve_static = new KateCurveType ();
  public static final KateCurveType kate_curve_linear = new KateCurveType ();
  public static final KateCurveType kate_curve_catmull_rom_spline = new KateCurveType ();
  public static final KateCurveType kate_curve_bezier_cubic_spline = new KateCurveType ();
  public static final KateCurveType kate_curve_bspline = new KateCurveType ();

  private static final KateCurveType[] list = {
    kate_curve_none,
    kate_curve_static,
    kate_curve_linear,
    kate_curve_catmull_rom_spline,
    kate_curve_bezier_cubic_spline,
    kate_curve_bspline,
  };

  private KateCurveType() {
  }

  /**
   * Create a KateCurveType object from an integer.
   */
  public static KateCurveType CreateCurveType(int idx) throws KateException {
    if (idx < 0 || idx >= list.length)
      throw new KateException("Curve type "+idx+" out of bounds");
    return list[idx];
  }
}
