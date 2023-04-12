package fi.riista.util;

import java.awt.*;

public class ColorConversionUtil {

    public static Color hslToRgb(final float h, final float s, final float l) {
        // http://biginteger.blogspot.com/2012/01/convert-rgb-to-hsl-and-vice-versa-in.html

        // HSL to RGB
        /*
            C = 1 - |2L - 1| * S
            X = C * (1 - |(H / 60) mod 2 - 1|)
            m = L - C / 2
                          | (C, X, 0), 0 <= H < 60
                          | (X, C, 0), 60 <= H < 120
           (R', G', B') = | (0, C, X), 120 <= H < 180
                          | (0, X, C), 180 <= H < 240
                          | (X, 0, C), 240 <= H < 300
                          | (C, 0, X), 300 <= H < 360
            (R, G, B) = ((R' + m) * 255, (G' + m) * 255, (B' + m) * 255)
         */

        final float c = (1 - Math.abs(2.f * l - 1.f)) * s;
        final float h_ = h / 60.f;
        float h_mod2 = h_;
        if (h_mod2 >= 4.f) {
            h_mod2 -= 4.f;
        } else if (h_mod2 >= 2.f) {
            h_mod2 -= 2.f;
        }

        final float x = c * (1 - Math.abs(h_mod2 - 1));
        final float r_;
        final float g_;
        final float b_;
        if (h_ < 1) {
            r_ = c;
            g_ = x;
            b_ = 0;
        }
        else if (h_ < 2) {
            r_ = x;
            g_ = c;
            b_ = 0;
        }
        else if (h_ < 3) {
            r_ = 0;
            g_ = c;
            b_ = x;
        }
        else if (h_ < 4) {
            r_ = 0;
            g_ = x;
            b_ = c;
        }
        else if (h_ < 5) {
            r_ = x;
            g_ = 0;
            b_ = c;
        }
        else {
            r_ = c;
            g_ = 0;
            b_ = x;
        }

        final float m = l - (0.5f * c);
        final int r = (int)((r_ + m) * (255.f) + 0.5f);
        final int g = (int)((g_ + m) * (255.f) + 0.5f);
        final int b = (int)((b_ + m) * (255.f) + 0.5f);

        return new Color(r, g, b);
    }

}
