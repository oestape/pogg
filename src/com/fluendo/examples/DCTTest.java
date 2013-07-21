/* Copyright (C) <2009> Maik Merten <maikmerten@googlemail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package com.fluendo.examples;

import com.fluendo.jheora.Constants;
import com.fluendo.jheora.iDCT;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DCTTest {
/*
    // create a lookup-table to translate into zig-zag
    private static int[] zigzag;

    static {
        zigzag = new int[64];
        for (int i = 0; i < 64; ++i) {
            int idx = Constants.dequant_index[i];
            zigzag[idx] = i;
        }
    }

    private static short[] zigzag(short[] input) {
        short[] result = new short[64];
        for (int i = 0; i < 64; ++i) {
            result[zigzag[i]] = input[i];
        }
        return result;
    }

    private static void fillArray(short[] array, String line) {
        String[] tokens = line.trim().split(" ");
        int i = tokens.length;
        int cnt = 0;

        while (--i >= 0 && cnt < 64) {
            int idx = (array.length - 1) - cnt;
            //System.out.println(idx + " " + tokens[i]);
            array[idx] = Short.parseShort(tokens[i]);
            cnt++;
        }
    }

    private static String pad(short s) {
        String result = s + "";

        while (result.length() < 4) {
            result = " " + result;
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("usage: DCTTest <dct-testvector.txt>");
            System.exit(1);
        }

        File input = new File(args[0]);
        BufferedReader br = new BufferedReader(new FileReader(input));

        short[] quantizers = new short[64];
        short[] quantized_coeffs = new short[64];
        short[] coeffs = new short[64];
        short[] result = new short[64];
        short[] myresult = new short[64];

        boolean b1 = false, b2 = false, b3 = false, b4 = false;

        iDCT dct = new iDCT();

        int cnt = 0;

        String line = br.readLine();
        while (line != null) {

            // empty lines indicate next block of test data
            if (line.trim().length() == 0) {
                b1 = b2 = b3 = b4 = false;
            }

            if (line.startsWith("QUANTIZER:")) {
                fillArray(quantizers, line);
                b1 = true;
            } else if (line.startsWith("QUANTIZED_COEFFS:")) {
                fillArray(quantized_coeffs, line);
                b2 = true;
            } else if (line.startsWith("DCT_COEFFICIENTS:")) {
                fillArray(coeffs, line);
                b3 = true;
            } else if (line.startsWith("DCT_RESULT:")) {
                fillArray(result, line);
                b4 = true;
            }

            // got a complete block of test data, so let's test
            if (b1 && b2 && b3 && b4) {

                // iDCT.java expects the coeffs to be zig-zagged!
                quantized_coeffs = zigzag(quantized_coeffs);
                quantizers = zigzag(quantizers);

                boolean just10 = true;
                for (int i = 10; i < 64; ++i) {
                    if (quantized_coeffs[i] != 0) {
                        just10 = false;
                        break;
                    }
                }

                if (just10) {
                    dct.IDct10(quantized_coeffs, quantizers, myresult);
                } else {
                    dct.IDctSlow(quantized_coeffs, quantizers, myresult);
                }

                boolean pass = true;
                for (int i = 0; i < 64; ++i) {
                    if (result[i] != myresult[i]) {
                        pass = false;
                        break;
                    }
                }

                System.out.println("test vector " + ++cnt + (pass ? " passed " : " didn't pass ") + " using " + (just10 ? " fast DCT " : " slow DCT."));

                if (!pass) {

                    System.out.print("reference: ");
                    for (int i = 0; i < result.length; ++i) {
                        System.out.print(" " + pad(result[i]));
                    }
                    System.out.println();

                    System.out.print("iDCT.java: ");
                    for (int i = 0; i < myresult.length; ++i) {
                        System.out.print(" " + pad(myresult[i]));
                    }
                    System.out.println();
                    System.out.println();
                }
            }

            line = br.readLine();
        }
    }*/
}
