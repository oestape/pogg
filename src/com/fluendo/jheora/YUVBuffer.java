/* Jheora
 * Copyright (C) 2004 Fluendo S.L.
 *  
 * Written by: 2004 Wim Taymans <wim@fluendo.com>
 *   
 * Many thanks to 
 *   The Xiph.Org Foundation http://www.xiph.org/
 * Jheora was based on their Theora reference decoder.
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
package com.fluendo.jheora;

import java.awt.image.*;
//import java.util.Random;

public class YUVBuffer implements ImageProducer {

    public int y_width;
    public int y_height;
    public int y_stride;
    public int uv_width;
    public int uv_height;
    public int uv_stride;
    public short[] data;
    public int y_offset;
    public int u_offset;
    public int v_offset;
    private int[] pixels;
    private int pix_size;
    private boolean newPixels = true;
    private ColorModel colorModel = ColorModel.getRGBdefault();
    private ImageProducer filteredThis;
    private int crop_x;
    private int crop_y;
    private int crop_w;
    private int crop_h;

    public void addConsumer(ImageConsumer ic) {
    }

    public boolean isConsumer(ImageConsumer ic) {
        return false;
    }

    public void removeConsumer(ImageConsumer ic) {
    }

    public void requestTopDownLeftRightResend(ImageConsumer ic) {
    }

    public void startProduction(ImageConsumer ic) {
        ic.setColorModel(colorModel);
        ic.setHints(ImageConsumer.TOPDOWNLEFTRIGHT |
                ImageConsumer.COMPLETESCANLINES |
                ImageConsumer.SINGLEFRAME |
                ImageConsumer.SINGLEPASS);
        ic.setDimensions(y_width, y_height);
        prepareRGBData(0, 0, y_width, y_height);
        ic.setPixels(0, 0, y_width, y_height, colorModel, pixels, 0, y_width);
        ic.imageComplete(ImageConsumer.STATICIMAGEDONE);
    }

    private synchronized void prepareRGBData(int x, int y, int width, int height) {
        if (!newPixels) {
            return;
        }

        int size = width * height;

        try {
            if (size != pix_size) {
                pixels = new int[size];
                pix_size = size;
            }
            YUVtoRGB(x, y, width, height);
        } catch (Throwable t) {
            /* ignore */
        }
        newPixels = false;
    }

    public synchronized void newPixels() {
        newPixels = true;
    }

    // cropping code provided by Benjamin Schwartz
    public Object getObject(int x, int y, int width, int height) {
        if (x == 0 && y == 0 && width == y_width && height == y_height) {
            return this;
        } else {
            if (x != crop_x || y != crop_y || width != crop_w || height != crop_h) {
                crop_x = x;
                crop_y = y;
                crop_w = width;
                crop_h = height;
                CropImageFilter cropFilter = new CropImageFilter(crop_x, crop_y, crop_w, crop_h);
                filteredThis = new FilteredImageSource(this, cropFilter);
            }
            return filteredThis;
        }
    }


    /*
     * unused "classic" YUV conversion, kept for reference
     */
    private void YUVtoRGB_classic(int x, int y, int width, int height) {

        /*
         * this modified version of the original YUVtoRGB was
         * provided by Ilan and Yaniv Ben Hagai.
         *
         * additional thanks to Gumboot for helping with making this
         * code perform better.
         */

        // Set up starting values for YUV pointers
        int YPtr = y_offset + x + y * (y_stride);
        int YPtr2 = YPtr + y_stride;
        int UPtr = u_offset + x / 2 + (y / 2) * (uv_stride);
        int VPtr = v_offset + x / 2 + (y / 2) * (uv_stride);
        int RGBPtr = 0;
        int RGBPtr2 = width;
        int width2 = width / 2;
        int height2 = height / 2;

        // Set the line step for the Y and UV planes and YPtr2
        int YStep = y_stride * 2 - (width2) * 2;
        int UVStep = uv_stride - (width2);
        int RGBStep = width;

        for (int i = 0; i < height2; i++) {
            for (int j = 0; j < width2; j++) {
                int D, E, r, g, b, t1, t2, t3, t4;

                D = data[UPtr++] - 128;
                E = data[VPtr++] - 128;

                t1 = 298 * (data[YPtr] - 16);
                t2 = 409 * E + 128;
                t3 = (100 * D) + (208 * E) - 128;
                t4 = 516 * D + 128;

                r = (t1 + t2) >> 8;
                g = (t1 - t3) >> 8;
                b = (t1 + t4) >> 8;

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr + 1] - 16);

                // pack pixel
                pixels[RGBPtr] =
                        ((clamp255(r) << 16) + (clamp255(g) << 8) + clamp255(b)) | 0xff000000;

                r = (t1 + t2) >> 8;
                g = (t1 - t3) >> 8;
                b = (t1 + t4) >> 8;

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr2] - 16);

                // pack pixel
                pixels[RGBPtr + 1] =
                        ((clamp255(r) << 16) + (clamp255(g) << 8) + clamp255(b)) | 0xff000000;


                r = (t1 + t2) >> 8;
                g = (t1 - t3) >> 8;
                b = (t1 + t4) >> 8;

                // retrieve data for next pixel now, hide latency?
                t1 = 298 * (data[YPtr2 + 1] - 16);

                // pack pixel
                pixels[RGBPtr2] =
                        ((clamp255(r) << 16) + (clamp255(g) << 8) + clamp255(b)) | 0xff000000;


                r = (t1 + t2) >> 8;
                g = (t1 - t3) >> 8;
                b = (t1 + t4) >> 8;

                // pack pixel
                pixels[RGBPtr2 + 1] =
                        ((clamp255(r) << 16) + (clamp255(g) << 8) + clamp255(b)) | 0xff000000;

                YPtr += 2;
                YPtr2 += 2;
                RGBPtr += 2;
                RGBPtr2 += 2;
            }

            // Increment the various pointers
            YPtr += YStep;
            YPtr2 += YStep;
            UPtr += UVStep;
            VPtr += UVStep;
            RGBPtr += RGBStep;
            RGBPtr2 += RGBStep;
        }
    }

    private static final short clamp255(int val) {
        val -= 255;
        val = -(255 + ((val >> (31)) & val));
        return (short) -((val >> 31) & val);
    }

    /*
     * Fast table-lookup based conversion algorithm. The following comment
     * is directly lifted from Robin Watt's C implementation. It describes
     * RGB555, but it's still valid documentation on the idea.
     *
     * Thank you, Robin!
     *
     */

    /* The algorithm used here is based heavily on one created by Sophie Wilson
     * of Acorn/e-14/Broadcomm. Many thanks.
     *
     * Additional tweaks (in the fast fixup code) are from Paul Gardiner.
     *
     * The old implementation of YUV -> RGB did:
     *
     * R = CLAMP((Y-16)*1.164 +           1.596*V)
     * G = CLAMP((Y-16)*1.164 - 0.391*U - 0.813*V)
     * B = CLAMP((Y-16)*1.164 + 2.018*U          )
     *
     * We're going to bend that here as follows:
     *
     * R = CLAMP(y +           1.596*V)
     * G = CLAMP(y - 0.383*U - 0.813*V)
     * B = CLAMP(y + 1.976*U          )
     *
     * where y = 0               for       Y <=  16,
     *       y = (  Y-16)*1.164, for  16 < Y <= 239,
     *       y = (239-16)*1.164, for 239 < Y
     *
     * i.e. We clamp Y to the 16 to 239 range (which it is supposed to be in
     * anyway). We then pick the B_U factor so that B never exceeds 511. We then
     * shrink the G_U factor in line with that to avoid a colour shift as much as
     * possible.
     *
     * We're going to use tables to do it faster, but rather than doing it using
     * 5 tables as as the above suggests, we're going to do it using just 3.
     *
     * We do this by working in parallel within a 32 bit word, and using one
     * table each for Y U and V.
     *
     * Source Y values are    0 to 255, so    0.. 260 after scaling
     * Source U values are -128 to 127, so  -49.. 49(G), -253..251(B) after
     * Source V values are -128 to 127, so -204..203(R), -104..103(G) after
     *
     * So total summed values:
     * -223 <= R <= 481, -173 <= G <= 431, -253 <= B < 511
     *
     * We need to pack R G and B into a 32 bit word, and because of Bs range we
     * need 2 bits above the valid range of B to detect overflow, and another one
     * to detect the sense of the overflow. We therefore adopt the following
     * representation:
     *
     * osGGGGGgggggosBBBBBbbbosRRRRRrrr
     *
     * Each such word breaks down into 3 ranges.
     *
     * osGGGGGggggg   osBBBBBbbb   osRRRRRrrr
     *
     * Thus we have 8 bits for each B and R table entry, and 10 bits for G (good
     * as G is the most noticable one). The s bit for each represents the sign,
     * and o represents the overflow.
     *
     * For R and B we pack the table by taking the 11 bit representation of their
     * values, and toggling bit 10 in the U and V tables.
     *
     * For the green case we calculate 4*G (thus effectively using 10 bits for the
     * valid range) truncate to 12 bits. We toggle bit 11 in the Y table.
     */
    private final int FLAGS = 0x40080100;

    private void YUVtoRGB(int x, int y, int width, int height) {

        // Set up starting values for YUV pointers
        int YPtr = y_offset + x + y * (y_stride);
        int YPtr2 = YPtr + y_stride;
        int UPtr = u_offset + x / 2 + (y / 2) * (uv_stride);
        int VPtr = v_offset + x / 2 + (y / 2) * (uv_stride);
        int RGBPtr = 0;
        int RGBPtr2 = width;
        int width2 = width / 2;
        int height2 = height / 2;

        // Set the line step for the Y and UV planes and YPtr2
        int YStep = y_stride * 2 - (width2) * 2;
        int UVStep = uv_stride - (width2);
        int RGBStep = width;

        for (int i = height2; i > 0; i--) {
            for (int j = width2; j > 0; j--) {

                int y_val, tmp;
                int uv = (yuv2rgb_table[256 + data[UPtr++]] + yuv2rgb_table[512 + data[VPtr++]]);

                // first Y pixel
                y_val = uv + yuv2rgb_table[data[YPtr]];
                // fixup
                tmp = y_val & FLAGS;
                tmp -= tmp >> 8;
                y_val |= tmp;
                tmp = FLAGS & ~(y_val >> 1);
                y_val += tmp >> 8;
                // write ARGB
                pixels[RGBPtr] = 0xFF000000 | ((y_val << 5) & 0xFF0000) | ((y_val >> 14) & 0xFF00) | (y_val & 0xFF);

                // second Y pixel
                y_val = uv + yuv2rgb_table[data[YPtr + 1]];
                // fixup
                tmp = y_val & FLAGS;
                tmp -= tmp >> 8;
                y_val |= tmp;
                tmp = FLAGS & ~(y_val >> 1);
                y_val += tmp >> 8;
                // write ARGB
                pixels[RGBPtr + 1] = 0xFF000000 | ((y_val << 5) & 0xFF0000) | ((y_val >> 14) & 0xFF00) | (y_val & 0xFF);

                // third Y pixel
                y_val = uv + yuv2rgb_table[data[YPtr2]];
                // fixup
                tmp = y_val & FLAGS;
                tmp -= tmp >> 8;
                y_val |= tmp;
                tmp = FLAGS & ~(y_val >> 1);
                y_val += tmp >> 8;
                // write ARGB
                pixels[RGBPtr2] = 0xFF000000 | ((y_val << 5) & 0xFF0000) | ((y_val >> 14) & 0xFF00) | (y_val & 0xFF);

                // fourth Y pixel
                y_val = uv + yuv2rgb_table[data[YPtr2 + 1]];
                // fixup
                tmp = y_val & FLAGS;
                tmp -= tmp >> 8;
                y_val |= tmp;
                tmp = FLAGS & ~(y_val >> 1);
                y_val += tmp >> 8;
                // write ARGB
                pixels[RGBPtr2 + 1] = 0xFF000000 | ((y_val << 5) & 0xFF0000) | ((y_val >> 14) & 0xFF00) | (y_val & 0xFF);

                YPtr += 2;
                YPtr2 += 2;
                RGBPtr += 2;
                RGBPtr2 += 2;
            }

            // Increment the various pointers
            YPtr += YStep;
            YPtr2 += YStep;
            UPtr += UVStep;
            VPtr += UVStep;
            RGBPtr += RGBStep;
            RGBPtr2 += RGBStep;
        }
    }

    // lookup table for YUV conversion, lifted from Robin Watt's implementation
    private static int[] yuv2rgb_table = {
        /* y_table */
        0x7FFFFFED,
        0x7FFFFFEF,
        0x7FFFFFF0,
        0x7FFFFFF1,
        0x7FFFFFF2,
        0x7FFFFFF3,
        0x7FFFFFF4,
        0x7FFFFFF6,
        0x7FFFFFF7,
        0x7FFFFFF8,
        0x7FFFFFF9,
        0x7FFFFFFA,
        0x7FFFFFFB,
        0x7FFFFFFD,
        0x7FFFFFFE,
        0x7FFFFFFF,
        0x80000000,
        0x80400801,
        0x80A01002,
        0x80E01803,
        0x81202805,
        0x81803006,
        0x81C03807,
        0x82004008,
        0x82604809,
        0x82A0500A,
        0x82E0600C,
        0x8340680D,
        0x8380700E,
        0x83C0780F,
        0x84208010,
        0x84608811,
        0x84A09813,
        0x8500A014,
        0x8540A815,
        0x8580B016,
        0x85E0B817,
        0x8620C018,
        0x8660D01A,
        0x86C0D81B,
        0x8700E01C,
        0x8740E81D,
        0x87A0F01E,
        0x87E0F81F,
        0x88210821,
        0x88811022,
        0x88C11823,
        0x89012024,
        0x89412825,
        0x89A13026,
        0x89E14028,
        0x8A214829,
        0x8A81502A,
        0x8AC1582B,
        0x8B01602C,
        0x8B61682D,
        0x8BA1782F,
        0x8BE18030,
        0x8C418831,
        0x8C819032,
        0x8CC19833,
        0x8D21A034,
        0x8D61B036,
        0x8DA1B837,
        0x8E01C038,
        0x8E41C839,
        0x8E81D03A,
        0x8EE1D83B,
        0x8F21E83D,
        0x8F61F03E,
        0x8FC1F83F,
        0x90020040,
        0x90420841,
        0x90A21042,
        0x90E22044,
        0x91222845,
        0x91823046,
        0x91C23847,
        0x92024048,
        0x92624849,
        0x92A2504A,
        0x92E2604C,
        0x9342684D,
        0x9382704E,
        0x93C2784F,
        0x94228050,
        0x94628851,
        0x94A29853,
        0x9502A054,
        0x9542A855,
        0x9582B056,
        0x95E2B857,
        0x9622C058,
        0x9662D05A,
        0x96C2D85B,
        0x9702E05C,
        0x9742E85D,
        0x97A2F05E,
        0x97E2F85F,
        0x98230861,
        0x98831062,
        0x98C31863,
        0x99032064,
        0x99632865,
        0x99A33066,
        0x99E34068,
        0x9A434869,
        0x9A83506A,
        0x9AC3586B,
        0x9B23606C,
        0x9B63686D,
        0x9BA3786F,
        0x9BE38070,
        0x9C438871,
        0x9C839072,
        0x9CC39873,
        0x9D23A074,
        0x9D63B076,
        0x9DA3B877,
        0x9E03C078,
        0x9E43C879,
        0x9E83D07A,
        0x9EE3D87B,
        0x9F23E87D,
        0x9F63F07E,
        0x9FC3F87F,
        0xA0040080,
        0xA0440881,
        0xA0A41082,
        0xA0E42084,
        0xA1242885,
        0xA1843086,
        0xA1C43887,
        0xA2044088,
        0xA2644889,
        0xA2A4588B,
        0xA2E4608C,
        0xA344688D,
        0xA384708E,
        0xA3C4788F,
        0xA4248090,
        0xA4649092,
        0xA4A49893,
        0xA504A094,
        0xA544A895,
        0xA584B096,
        0xA5E4B897,
        0xA624C098,
        0xA664D09A,
        0xA6C4D89B,
        0xA704E09C,
        0xA744E89D,
        0xA7A4F09E,
        0xA7E4F89F,
        0xA82508A1,
        0xA88510A2,
        0xA8C518A3,
        0xA90520A4,
        0xA96528A5,
        0xA9A530A6,
        0xA9E540A8,
        0xAA4548A9,
        0xAA8550AA,
        0xAAC558AB,
        0xAB2560AC,
        0xAB6568AD,
        0xABA578AF,
        0xAC0580B0,
        0xAC4588B1,
        0xAC8590B2,
        0xACE598B3,
        0xAD25A0B4,
        0xAD65B0B6,
        0xADA5B8B7,
        0xAE05C0B8,
        0xAE45C8B9,
        0xAE85D0BA,
        0xAEE5D8BB,
        0xAF25E8BD,
        0xAF65F0BE,
        0xAFC5F8BF,
        0xB00600C0,
        0xB04608C1,
        0xB0A610C2,
        0xB0E620C4,
        0xB12628C5,
        0xB18630C6,
        0xB1C638C7,
        0xB20640C8,
        0xB26648C9,
        0xB2A658CB,
        0xB2E660CC,
        0xB34668CD,
        0xB38670CE,
        0xB3C678CF,
        0xB42680D0,
        0xB46690D2,
        0xB4A698D3,
        0xB506A0D4,
        0xB546A8D5,
        0xB586B0D6,
        0xB5E6B8D7,
        0xB626C8D9,
        0xB666D0DA,
        0xB6C6D8DB,
        0xB706E0DC,
        0xB746E8DD,
        0xB7A6F0DE,
        0xB7E6F8DF,
        0xB82708E1,
        0xB88710E2,
        0xB8C718E3,
        0xB90720E4,
        0xB96728E5,
        0xB9A730E6,
        0xB9E740E8,
        0xBA4748E9,
        0xBA8750EA,
        0xBAC758EB,
        0xBB2760EC,
        0xBB6768ED,
        0xBBA778EF,
        0xBC0780F0,
        0xBC4788F1,
        0xBC8790F2,
        0xBCE798F3,
        0xBD27A0F4,
        0xBD67B0F6,
        0xBDC7B8F7,
        0xBE07C0F8,
        0xBE47C8F9,
        0xBEA7D0FA,
        0xBEE7D8FB,
        0xBF27E8FD,
        0xBF87F0FE,
        0xBFC7F8FF,
        0xC0080100,
        0xC0480901,
        0xC0A81102,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        0xC0E82104,
        /* u_table */
        0x0C400103,
        0x0C200105,
        0x0C200107,
        0x0C000109,
        0x0BE0010B,
        0x0BC0010D,
        0x0BA0010F,
        0x0BA00111,
        0x0B800113,
        0x0B600115,
        0x0B400117,
        0x0B400119,
        0x0B20011B,
        0x0B00011D,
        0x0AE0011F,
        0x0AE00121,
        0x0AC00123,
        0x0AA00125,
        0x0A800127,
        0x0A600129,
        0x0A60012B,
        0x0A40012D,
        0x0A20012F,
        0x0A000131,
        0x0A000132,
        0x09E00134,
        0x09C00136,
        0x09A00138,
        0x09A0013A,
        0x0980013C,
        0x0960013E,
        0x09400140,
        0x09400142,
        0x09200144,
        0x09000146,
        0x08E00148,
        0x08C0014A,
        0x08C0014C,
        0x08A0014E,
        0x08800150,
        0x08600152,
        0x08600154,
        0x08400156,
        0x08200158,
        0x0800015A,
        0x0800015C,
        0x07E0015E,
        0x07C00160,
        0x07A00162,
        0x07A00164,
        0x07800166,
        0x07600168,
        0x0740016A,
        0x0720016C,
        0x0720016E,
        0x07000170,
        0x06E00172,
        0x06C00174,
        0x06C00176,
        0x06A00178,
        0x0680017A,
        0x0660017C,
        0x0660017E,
        0x06400180,
        0x06200182,
        0x06000184,
        0x05E00185,
        0x05E00187,
        0x05C00189,
        0x05A0018B,
        0x0580018D,
        0x0580018F,
        0x05600191,
        0x05400193,
        0x05200195,
        0x05200197,
        0x05000199,
        0x04E0019B,
        0x04C0019D,
        0x04C0019F,
        0x04A001A1,
        0x048001A3,
        0x046001A5,
        0x044001A7,
        0x044001A9,
        0x042001AB,
        0x040001AD,
        0x03E001AF,
        0x03E001B1,
        0x03C001B3,
        0x03A001B5,
        0x038001B7,
        0x038001B9,
        0x036001BB,
        0x034001BD,
        0x032001BF,
        0x032001C1,
        0x030001C3,
        0x02E001C5,
        0x02C001C7,
        0x02A001C9,
        0x02A001CB,
        0x028001CD,
        0x026001CF,
        0x024001D1,
        0x024001D3,
        0x022001D5,
        0x020001D7,
        0x01E001D8,
        0x01E001DA,
        0x01C001DC,
        0x01A001DE,
        0x018001E0,
        0x016001E2,
        0x016001E4,
        0x014001E6,
        0x012001E8,
        0x010001EA,
        0x010001EC,
        0x00E001EE,
        0x00C001F0,
        0x00A001F2,
        0x00A001F4,
        0x008001F6,
        0x006001F8,
        0x004001FA,
        0x004001FC,
        0x002001FE,
        0x00000200,
        0xFFE00202,
        0xFFC00204,
        0xFFC00206,
        0xFFA00208,
        0xFF80020A,
        0xFF60020C,
        0xFF60020E,
        0xFF400210,
        0xFF200212,
        0xFF000214,
        0xFF000216,
        0xFEE00218,
        0xFEC0021A,
        0xFEA0021C,
        0xFEA0021E,
        0xFE800220,
        0xFE600222,
        0xFE400224,
        0xFE200226,
        0xFE200228,
        0xFE000229,
        0xFDE0022B,
        0xFDC0022D,
        0xFDC0022F,
        0xFDA00231,
        0xFD800233,
        0xFD600235,
        0xFD600237,
        0xFD400239,
        0xFD20023B,
        0xFD00023D,
        0xFCE0023F,
        0xFCE00241,
        0xFCC00243,
        0xFCA00245,
        0xFC800247,
        0xFC800249,
        0xFC60024B,
        0xFC40024D,
        0xFC20024F,
        0xFC200251,
        0xFC000253,
        0xFBE00255,
        0xFBC00257,
        0xFBC00259,
        0xFBA0025B,
        0xFB80025D,
        0xFB60025F,
        0xFB400261,
        0xFB400263,
        0xFB200265,
        0xFB000267,
        0xFAE00269,
        0xFAE0026B,
        0xFAC0026D,
        0xFAA0026F,
        0xFA800271,
        0xFA800273,
        0xFA600275,
        0xFA400277,
        0xFA200279,
        0xFA20027B,
        0xFA00027C,
        0xF9E0027E,
        0xF9C00280,
        0xF9A00282,
        0xF9A00284,
        0xF9800286,
        0xF9600288,
        0xF940028A,
        0xF940028C,
        0xF920028E,
        0xF9000290,
        0xF8E00292,
        0xF8E00294,
        0xF8C00296,
        0xF8A00298,
        0xF880029A,
        0xF860029C,
        0xF860029E,
        0xF84002A0,
        0xF82002A2,
        0xF80002A4,
        0xF80002A6,
        0xF7E002A8,
        0xF7C002AA,
        0xF7A002AC,
        0xF7A002AE,
        0xF78002B0,
        0xF76002B2,
        0xF74002B4,
        0xF74002B6,
        0xF72002B8,
        0xF70002BA,
        0xF6E002BC,
        0xF6C002BE,
        0xF6C002C0,
        0xF6A002C2,
        0xF68002C4,
        0xF66002C6,
        0xF66002C8,
        0xF64002CA,
        0xF62002CC,
        0xF60002CE,
        0xF60002CF,
        0xF5E002D1,
        0xF5C002D3,
        0xF5A002D5,
        0xF5A002D7,
        0xF58002D9,
        0xF56002DB,
        0xF54002DD,
        0xF52002DF,
        0xF52002E1,
        0xF50002E3,
        0xF4E002E5,
        0xF4C002E7,
        0xF4C002E9,
        0xF4A002EB,
        0xF48002ED,
        0xF46002EF,
        0xF46002F1,
        0xF44002F3,
        0xF42002F5,
        0xF40002F7,
        0xF3E002F9,
        0xF3E002FB,
        /* v_table */
        0x1A09A000,
        0x19E9A800,
        0x19A9B800,
        0x1969C800,
        0x1949D000,
        0x1909E000,
        0x18C9E800,
        0x18A9F800,
        0x186A0000,
        0x182A1000,
        0x180A2000,
        0x17CA2800,
        0x17AA3800,
        0x176A4000,
        0x172A5000,
        0x170A6000,
        0x16CA6800,
        0x168A7800,
        0x166A8000,
        0x162A9000,
        0x160AA000,
        0x15CAA800,
        0x158AB800,
        0x156AC000,
        0x152AD000,
        0x14EAE000,
        0x14CAE800,
        0x148AF800,
        0x146B0000,
        0x142B1000,
        0x13EB2000,
        0x13CB2800,
        0x138B3800,
        0x134B4000,
        0x132B5000,
        0x12EB6000,
        0x12CB6800,
        0x128B7800,
        0x124B8000,
        0x122B9000,
        0x11EBA000,
        0x11ABA800,
        0x118BB800,
        0x114BC000,
        0x112BD000,
        0x10EBE000,
        0x10ABE800,
        0x108BF800,
        0x104C0000,
        0x100C1000,
        0x0FEC2000,
        0x0FAC2800,
        0x0F8C3800,
        0x0F4C4000,
        0x0F0C5000,
        0x0EEC5800,
        0x0EAC6800,
        0x0E6C7800,
        0x0E4C8000,
        0x0E0C9000,
        0x0DEC9800,
        0x0DACA800,
        0x0D6CB800,
        0x0D4CC000,
        0x0D0CD000,
        0x0CCCD800,
        0x0CACE800,
        0x0C6CF800,
        0x0C4D0000,
        0x0C0D1000,
        0x0BCD1800,
        0x0BAD2800,
        0x0B6D3800,
        0x0B2D4000,
        0x0B0D5000,
        0x0ACD5800,
        0x0AAD6800,
        0x0A6D7800,
        0x0A2D8000,
        0x0A0D9000,
        0x09CD9800,
        0x098DA800,
        0x096DB800,
        0x092DC000,
        0x090DD000,
        0x08CDD800,
        0x088DE800,
        0x086DF800,
        0x082E0000,
        0x07EE1000,
        0x07CE1800,
        0x078E2800,
        0x076E3800,
        0x072E4000,
        0x06EE5000,
        0x06CE5800,
        0x068E6800,
        0x064E7800,
        0x062E8000,
        0x05EE9000,
        0x05CE9800,
        0x058EA800,
        0x054EB800,
        0x052EC000,
        0x04EED000,
        0x04AED800,
        0x048EE800,
        0x044EF000,
        0x042F0000,
        0x03EF1000,
        0x03AF1800,
        0x038F2800,
        0x034F3000,
        0x030F4000,
        0x02EF5000,
        0x02AF5800,
        0x028F6800,
        0x024F7000,
        0x020F8000,
        0x01EF9000,
        0x01AF9800,
        0x016FA800,
        0x014FB000,
        0x010FC000,
        0x00EFD000,
        0x00AFD800,
        0x006FE800,
        0x004FF000,
        0x00100000,
        0xFFD01000,
        0xFFB01800,
        0xFF702800,
        0xFF303000,
        0xFF104000,
        0xFED05000,
        0xFEB05800,
        0xFE706800,
        0xFE307000,
        0xFE108000,
        0xFDD09000,
        0xFD909800,
        0xFD70A800,
        0xFD30B000,
        0xFD10C000,
        0xFCD0D000,
        0xFC90D800,
        0xFC70E800,
        0xFC30F000,
        0xFBF10000,
        0xFBD11000,
        0xFB911800,
        0xFB712800,
        0xFB313000,
        0xFAF14000,
        0xFAD14800,
        0xFA915800,
        0xFA516800,
        0xFA317000,
        0xF9F18000,
        0xF9D18800,
        0xF9919800,
        0xF951A800,
        0xF931B000,
        0xF8F1C000,
        0xF8B1C800,
        0xF891D800,
        0xF851E800,
        0xF831F000,
        0xF7F20000,
        0xF7B20800,
        0xF7921800,
        0xF7522800,
        0xF7123000,
        0xF6F24000,
        0xF6B24800,
        0xF6925800,
        0xF6526800,
        0xF6127000,
        0xF5F28000,
        0xF5B28800,
        0xF5729800,
        0xF552A800,
        0xF512B000,
        0xF4F2C000,
        0xF4B2C800,
        0xF472D800,
        0xF452E800,
        0xF412F000,
        0xF3D30000,
        0xF3B30800,
        0xF3731800,
        0xF3532800,
        0xF3133000,
        0xF2D34000,
        0xF2B34800,
        0xF2735800,
        0xF2336800,
        0xF2137000,
        0xF1D38000,
        0xF1B38800,
        0xF1739800,
        0xF133A800,
        0xF113B000,
        0xF0D3C000,
        0xF093C800,
        0xF073D800,
        0xF033E000,
        0xF013F000,
        0xEFD40000,
        0xEF940800,
        0xEF741800,
        0xEF342000,
        0xEEF43000,
        0xEED44000,
        0xEE944800,
        0xEE745800,
        0xEE346000,
        0xEDF47000,
        0xEDD48000,
        0xED948800,
        0xED549800,
        0xED34A000,
        0xECF4B000,
        0xECD4C000,
        0xEC94C800,
        0xEC54D800,
        0xEC34E000,
        0xEBF4F000,
        0xEBB50000,
        0xEB950800,
        0xEB551800,
        0xEB352000,
        0xEAF53000,
        0xEAB54000,
        0xEA954800,
        0xEA555800,
        0xEA156000,
        0xE9F57000,
        0xE9B58000,
        0xE9958800,
        0xE9559800,
        0xE915A000,
        0xE8F5B000,
        0xE8B5C000,
        0xE875C800,
        0xE855D800,
        0xE815E000,
        0xE7F5F000,
        0xE7B60000,
        0xE7760800,
        0xE7561800,
        0xE7162000,
        0xE6D63000,
        0xE6B64000,
        0xE6764800,
        0xE6365800};


    // some benchmarking stuff, uncomment if you need it
    /*public static void main(String[] args) {
        YUVBuffer yuvbuf = new YUVBuffer();

        // let's create a 512x512 picture with noise

        int x = 1280;
        int y = 720;

        int size = (x * y) + (x * y) / 2;
        short[] picdata = new short[size];

        Random r = new Random();
        for (int i = 0; i < picdata.length; ++i) {
            picdata[i] = (short) (r.nextInt(255) | 0xFF);
        }

        System.out.println("bench...");

        yuvbuf.data = picdata;
        yuvbuf.y_height = y;
        yuvbuf.y_width = x;
        yuvbuf.y_stride = x;
        yuvbuf.uv_height = y / 2;
        yuvbuf.uv_width = x / 2;
        yuvbuf.uv_stride = x / 2;
        yuvbuf.u_offset = x / 2;
        yuvbuf.v_offset = x + x / 2;

        int times = 5000;

        long start = System.currentTimeMillis();
        for (int i = 0; i < times; ++i) {
            yuvbuf.newPixels();
            yuvbuf.prepareRGBData(0, 0, x, y);
        }
        long end = System.currentTimeMillis();

        System.out.println("average conversion time per frame: " + ((double) (end - start)) / (times * 1f) + " ms.");

    }*/
}
