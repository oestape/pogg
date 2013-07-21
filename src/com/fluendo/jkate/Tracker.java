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

import java.awt.*;
import com.fluendo.utils.*;

public final class Tracker {

  private Dimension window;
  private Dimension frame;

  public com.fluendo.jkate.Event ev = null;

  public boolean has[] = new boolean[64];
  public static final int has_region = 0;
  public static final int has_text_alignment_int = 1;
  public static final int has_text_alignment_ext = 2;

  public float region_x;
  public float region_y;
  public float region_w;
  public float region_h;

  public Tracker (com.fluendo.jkate.Event ev) {
    this.ev = ev;
  }

  /**
   * Update the tracker at the given time for the given image's dimensions.
   */
  public boolean update(double t, Dimension window, Dimension frame)
  {
    this.window = window;
    this.frame = frame;

    /* find current region and style, if any */
    Region kr = ev.kr;
    Style ks = ev.ks;
    if (ks == null && kr != null && kr.style >= 0) {
      ks = ev.ki.styles[kr.style];
    }

    /* start with nothing */
    for (int n=0; n<has.length; ++n) has[n] = false;

    /* define region */
    if (kr != null) {
      if (kr.metric == KateSpaceMetric.kate_metric_percentage) {
        region_x = kr.x * frame.width / 100.0f;
        region_y = kr.y * frame.height / 100.0f;
        region_w = kr.w * frame.width / 100.0f;
        region_h = kr.h * frame.height / 100.0f;
      }
      else if (kr.metric == KateSpaceMetric.kate_metric_millionths) {
        region_x = kr.x * frame.width / 1000000.0f;
        region_y = kr.y * frame.height / 1000000.0f;
        region_w = kr.w * frame.width / 1000000.0f;
        region_h = kr.h * frame.height / 1000000.0f;
      }
      else if (kr.metric == KateSpaceMetric.kate_metric_pixels) {
        region_x = kr.x;
        region_y = kr.y;
        region_w = kr.w;
        region_h = kr.h;
      }
      else {
        Debug.debug("Invalid metrics");
        return false;
      }
      has[has_region] = true;
    }

    return true;
  }

}
