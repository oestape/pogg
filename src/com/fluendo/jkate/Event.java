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

public class Event
{
  public Info ki = null;

  public long start;
  public long duration;
  public long backlink;
  public double start_time;
  public double end_time;

  public int id = -1;

  public KateTextEncoding text_encoding;
  public KateTextDirectionality text_directionality;
  public KateMarkupType markup_type;
  public byte[] language;

  public Region kr = null;
  public Style ks = null;
  public Style ks2 = null;
  public Motion motions[] = null;
  public Palette palette = null;
  public Bitmap bitmap = null;
  public FontMapping font_mapping = null;

  public byte text[];

  /**
   * Initialize an event to safe defaults.
   */
  public Event(Info ki) {
    this.ki = ki;
    id = -1;
    kr = null;
    ks = null;
    ks2 = null;
    motions = null;
    palette = null;
    bitmap = null;
    text = null;
    font_mapping = null;
    text_encoding = ki.text_encoding;
    text_directionality = ki.text_directionality;
    markup_type = ki.markup_type;
    language = null;
  }
}
