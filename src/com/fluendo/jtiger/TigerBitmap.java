/* JTiger
 * Copyright (C) 2008 ogg.k.ogg.k <ogg.k.ogg.k@googlemail.com>
 *
 * Parts of JTiger are based on code by Wim Taymans <wim@fluendo.com>
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

package com.fluendo.jtiger;

import java.awt.*;
import java.awt.image.*;
import com.fluendo.jkate.*;
import com.fluendo.utils.*;

public class TigerBitmap {
  private Image image;
  private Image scaled_image;

  /**
   * Create a new TigerBitmap from a Kate bitmap and optional palette.
   */
  public TigerBitmap(Component c, Bitmap kb, Palette kp)
  {
    if (kb == null) {
      image = null;
    }
    else if (kb.bpp == 0) {
      /* PNG */
      image = createPNGBitmap(c, kb, kp);
    }
    else {
      if (kp == null) {
        image = null;
      }
      else {
        image = createPalettedBitmap(c, kb, kp);
      }
    }
  }

  /**
   * Returns a scaled version of the image.
   */
  public Image getScaled(int width, int height)
  {
    if (scaled_image == null || width != scaled_image.getWidth(null) || height != scaled_image.getHeight(null)) {
      scaled_image = image.getScaledInstance(width, height, Image.SCALE_SMOOTH); // TODO: quality setting
    }
    return scaled_image;
  }

  /**
   * Create an image from bits representing a PNG image.
   */
  private Image createPNGBitmap(Component c, Bitmap kb, Palette kp)
  {
    Debug.warning("PNG bitmaps not supported yet");
    return null;
  }

  /**
   * Create a paletted image.
   */
  private Image createPalettedBitmap(Component c, Bitmap kb, Palette kp)
  {
    byte[] cmap = new byte[4*kp.colors.length];
    for (int n=0; n<kp.colors.length; ++n) {
      cmap[n*4+0] = kp.colors[n].r;
      cmap[n*4+1] = kp.colors[n].g;
      cmap[n*4+2] = kp.colors[n].b;
      cmap[n*4+3] = kp.colors[n].a;
    }

    IndexColorModel icm = new IndexColorModel(kb.bpp, kp.colors.length, cmap, 0, true);
    return c.createImage(new MemoryImageSource(kb.width, kb.height, icm, kb.pixels, 0, kb.width));
  }
}
