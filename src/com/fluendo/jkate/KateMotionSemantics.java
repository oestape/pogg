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

public class KateMotionSemantics {
  public static final KateMotionSemantics kms_time = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_z = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_region_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_region_size = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_alignment_int = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_alignment_ext = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_size = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker1_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker2_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker3_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker4_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_1 = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_2 = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_3 = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_4 = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_color_rg = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_color_ba = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_background_color_rg = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_background_color_ba = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_draw_color_rg = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_draw_color_ba = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_style_morph = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_path = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_path_section = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_draw = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_text_visible_section = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_horizontal_margins = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_vertical_margins = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_bitmap_position = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_bitmap_size = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker1_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker2_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker3_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_marker4_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_1_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_2_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_3_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_glyph_pointer_4_bitmap = new KateMotionSemantics ();
  public static final KateMotionSemantics kms_draw_width = new KateMotionSemantics ();

  private static final KateMotionSemantics[] list = {
    kms_time,
    kms_z,
    kms_region_position,
    kms_region_size,
    kms_text_alignment_int,
    kms_text_alignment_ext,
    kms_text_position,
    kms_text_size,
    kms_marker1_position,
    kms_marker2_position,
    kms_marker3_position,
    kms_marker4_position,
    kms_glyph_pointer_1,
    kms_glyph_pointer_2,
    kms_glyph_pointer_3,
    kms_glyph_pointer_4,
    kms_text_color_rg,
    kms_text_color_ba,
    kms_background_color_rg,
    kms_background_color_ba,
    kms_draw_color_rg,
    kms_draw_color_ba,
    kms_style_morph,
    kms_text_path,
    kms_text_path_section,
    kms_draw,
    kms_text_visible_section,
    kms_horizontal_margins,
    kms_vertical_margins,
    kms_bitmap_position,
    kms_bitmap_size,
    kms_marker1_bitmap,
    kms_marker2_bitmap,
    kms_marker3_bitmap,
    kms_marker4_bitmap,
    kms_glyph_pointer_1_bitmap,
    kms_glyph_pointer_2_bitmap,
    kms_glyph_pointer_3_bitmap,
    kms_glyph_pointer_4_bitmap,
    kms_draw_width,
  };

  private KateMotionSemantics() {
  }

  /**
   * Create a KateMotionSemantics object from an integer.
   */
  public static KateMotionSemantics CreateMotionSemantics(int idx) throws KateException {
    if (idx < 0 || idx >= list.length)
      throw new KateException("Motion semantics "+idx+" out of bounds");
    return list[idx];
  }
}
