/*
 * ﻿Copyright (C) 2013 Atlas of Living Australia
 * All Rights Reserved.
 *
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 */

package au.org.ala.soils2sat

import java.awt.Color
import java.text.DecimalFormat
import java.util.regex.Pattern

/**
 * Created with IntelliJ IDEA.
 * User: baird
 * Date: 3/06/13
 * Time: 2:08 PM
 * To change this template use File | Settings | File Templates.
 */
class SoilColorUtils {

    private static Map<String, SoilColor> _colorMap = null

    private static Pattern MUNSELL_PATTERN = Pattern.compile("^((?:\\d\\d[.]\\d|\\d[.]\\d|\\d\\d|\\d)[A-Z]{1,2})(\\d[.]\\d|\\d)(\\d[.]\\d|\\d).*\$")

    public static synchronized Color parseMunsell(String code) {
        if (!_colorMap) {
            initMunsellColorMap()
        }

        def munsellColor = _colorMap[code]
        if (munsellColor) {
            return munsellColor.color
        }

        // That exact color code does not exist in the map - can be interpolate it?
        def munsellCode = parseMunsellCode(code)
        if (munsellCode) {
            return interpolateMunsellColor(munsellCode)?.color
        }

        return null
    }

    public static SoilColor interpolateMunsellColor(MunsellCode munsellCode) {

        def candidates= _colorMap.values().findAll {
            it.hue == munsellCode.hue && it.value == munsellCode.value
        }
        //
        if (candidates) {
            SoilColor min = null
            SoilColor max = null
            candidates.each {
                if (min == null) {
                    if (it.chroma < munsellCode.chroma) {
                        min = it
                    }
                } else {
                    if (it.chroma < munsellCode.chroma && it.chroma > min.chroma) {
                        min = it
                    }
                }

                if (max == null) {
                    if (it.chroma > munsellCode.chroma) {
                        max = it
                    }
                } else {
                    if (it.chroma > munsellCode.chroma && it.chroma < max.chroma) {
                        max = it
                    }
                }
            }
            if (min && max) {
                def ratio = (munsellCode.chroma - min.chroma) / (max.chroma - min.chroma)

                def newred = splitRatio(min.color.red, max.color.red, ratio)
                def newgreen = splitRatio(min.color.green, max.color.green, ratio)
                def newblue = splitRatio(min.color.blue, max.color.blue, ratio)

                Color c = new Color(newred, newgreen, newblue)

                return new SoilColor(chroma: munsellCode.chroma, hue: munsellCode.hue, value: munsellCode.value, color: c, description: "Interpolated")
            }

        }

        return null
    }

    private static int splitRatio(int n1, int n2, double ratio) {
        def small = Math.min(n1, n2)
        def large = Math.max(n1, n2)
        def delta = (large - small) * ratio
        return (int) Math.round((double) small + delta)
    }

    public static MunsellCode parseMunsellCode(String code) {
        if (!code) {
            return null
        }

        def m = MUNSELL_PATTERN.matcher(code?.toUpperCase())
        if (m.matches()) {
            return new MunsellCode(hue: m.group(1), value: Double.parseDouble(m.group(2)), chroma: Double.parseDouble(m.group(3)))
        }
        return null
    }

    private static void initMunsellColorMap() {
        def url = SoilColorUtils.class.getResource("/resources/munsell_data.txt")

        url?.withInputStream {
            def reader = new BufferedReader(new InputStreamReader(it))
            _colorMap = [:]
            String line
            boolean skipFirst = true
            while (line = reader.readLine()) {
                if (!skipFirst) {
                    def bits = line.split(",")
                    def hue = bits[1]
                    def value = Double.parseDouble(bits[2])
                    def chroma = Double.parseDouble(bits[3])
                    def r = Integer.parseInt(bits[16])
                    def g = Integer.parseInt(bits[17])
                    def b = Integer.parseInt(bits[18])
                    def key = "${hue}${value}${chroma}"
                    def color = new Color(r,g,b)
                    def entry = new SoilColor(hue: hue, value: value, chroma: chroma, color: color)
                    _colorMap[key] = entry
                } else {
                    skipFirst = false
                }

            }
        }
    }

    // sRGB XYZ - RGB [M]^-1
    private static MInv = [
        [3.2404542, -1.5371385, -0.4985314],
        [-0.9692660,  1.8760108,  0.0415560],
        [0.0556434, -0.2040259,  1.0572252]
    ]

    def static xyY2RGB(double x, double y, double Y) {

        double X,YY, Z, r,g,b
        (X,YY,Z) = xyY2XYZ(x, y, Y)
        (r,g,b) = XYZ2RGB(X, YY, Z)
        return [r, g, b]
    }

    def static xyY2XYZ(double x, double y, double Y) {
        if (y == 0) {
            return [0,0,0]
        }

        def X = (x * Y) / y
        def Z = ((1 - x - y) * Y) / y
        return [X, Y, Z]
    }

    def static XYZ2RGB(double X, double Y, double Z) {
        def r = (MInv[0][0] * X) + (MInv[0][1] * Y) + (MInv[0][2] * Z)
        def g = (MInv[1][0] * X) + (MInv[1][1] * Y) + (MInv[1][2] ** Z)
        def b = (MInv[2][0] * X) + (MInv[2][1] * Y) + (MInv[2][2] ** Z)
        def tuple = [r,g,b]

        // now to compand(???) to sRGB
        def RGB = []
        for (double v : tuple) {
            double V = v
            if (v <= 0.0031308) {
                V = v * 12.92
            } else {
                V = ((1.055 * v) ** (1/2.4)) - 0.055
            }
            RGB << V
        }

        return RGB
    }

}

class MunsellCode {
    String hue
    Double value
    Double chroma
}

class SoilColor {
    String hue
    Double value
    Double chroma
    String description
    Color color

    public String toString() {
        DecimalFormat dmf = new DecimalFormat("0.#")

        return "${hue}${dmf.format(value)}${dmf.format(chroma)} color:${color}"
    }
}
