/*
 * This file is part of eu.himeros_grcvallex_jar_1.0-SNAPSHOT
 *
 * Copyright (C) 2013 federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.himeros.grcvallex;

/**
 *
 * @author federico_D0T_boschetti_D0T_73_AT_gmail_D0T_com
 */
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.RuleBasedCollator;

public class AncientGreekCollator {

    private static String s;
    private static RuleBasedCollator rbc;

    public AncientGreekCollator() {
        initialize();
    }

    private static void initialize() {
        s = "";
        //alpha (min)
        s += ",'\u0027'<\u03B1;\u1F00;\u1F01;\u1F04;\u1F02;\u1F06;\u1F05;\u1F03;\u1F07;\u1F71;\u1F70;\u1FB6";
        //alpha (min iotasub)
        s += ";\u1FB3;\u1F80;\u1F81;\u1F84;\u1F82;\u1F86;\u1F85;\u1F83;\u1F87;\u1FB4;\u1FB2;\u1FB7";
        //alpha (maiusc)
        s += ",\u0391;\u1F08;\u1F09;\u1F0C;\u1F0A;\u1F0E;\u1F0D;\u1F0B;\u1F0F";
        //alpha (maiusc iotasub)
        s += ";\u1FBC;\u1F88;\u1F89;\u1F8C;\u1F8A;\u1F8E;\u1F8D;\u1F8B;\u1F8F";
        //beta
        s += "<\u03B2,\u0392";
        //gamma
        s += "<\u03B3,\u0393";
        //delta
        s += "<\u03B4,\u0394";
        //epsilon (min)
        s += "<\u03B5;\u1F10;\u1F11;\u1F14;\u1F12;\u1F15;\u1F13;\u1F73;\u1F72";
        //epsilon (maiusc)
        s += ",\u0395;\u1F18;\u1F19;\u1F1C;\u1F1A;\u1F1D;\u1F1B";
        //zeta
        s += "<\u03B6,\u0396";
        //eta (min)
        s += "<\u03B7;\u1F20;\u1F21;\u1F24;\u1F22;\u1F26;\u1F25;\u1F23;\u1F27;\u1F75;\u1F74;\u1FC6";
        //eta (min iotasub)
        s += ";\u1FC3;\u1F90;\u1F91;\u1F94;\u1F92;\u1F96;\u1F95;\u1F93;\u1F97;\u1FC4;\u1FC2;\u1FC7";
        //eta (maiusc)
        s += ",\u0397;\u1F28;\u1F29;\u1F2C;\u1F2A;\u1F2E;\u1F2D;\u1F2B;\u1F2F";
        //eta (maiusc iotasub)
        s += ";\u1FCC;\u1F98;\u1F99;\u1F9C;\u1F9A;\u1F9E;\u1F9D;\u1F9B;\u1F9F";
        //theta
        s += "<\u03B8,\u0398";
        //iota (min)		
        s += "<\u03B9;\u1F30;\u1F31;\u1F34;\u1F32;\u1F36;\u1F35;\u1F33;\u1F37;\u1F77;\u1F76;\u1FD6;\u03CA;\u1FD3;\u1FD2";
        //iota (maiusc)
        s += ",\u0399;\u1F38;\u1F39;\u1F3C;\u1F3A;\u1F3E;\u1F3D;\u1F3B;\u1F3F";
        //kappa
        s += "<\u03BA,\u039A";
        //lambda
        s += "<\u03BB,\u039B";
        //mi
        s += "<\u03BC,\u039C";
        //ni
        s += "<\u03BD,\u039D";
        //xi
        s += "<\u03BE,\u039E";
        //omicron (min)
        s += "<\u03BF;\u1F40;\u1F41;\u1F44;\u1F42;\u1F45;\u1F43;\u1F79;\u1F78";
        //omicron (maiusc)
        s += ",\u039F;\u1F48;\u1F49;\u1F4C;\u1F4A;\u1F4D;\u1F4B";
        //pi
        s += "<\u03C0,\u03A0";
        //rho
        s += "<\u03C1=\u1FE4=\u1FE5,\u03A1";
        //sigma
        s += "<\u03C3=\u03C2,\u03A3";
        //tau
        s += "<\u03C4,\u03A4";
        //ypsilon (min)			
        s += "<\u03C5;\u1F50;\u1F51;\u1F54;\u1F52;\u1F56;\u1F55;\u1F53;\u1F57;\u1F7B;\u1F7A;\u1FE6;\u03CB;\u1FE3;\u1FE2";
        //ypsilon (maiusc)
        s += ",\u03A5;\u1F59;\u1F5D;\u1F5B;\u1F5F";
        //phi
        s += "<\u03C6,\u03A6";
        //chi
        s += "<\u03C7,\u03A7";
        //psi
        s += "<\u03C8,\u03A8";
        //omega (min)
        s += "<\u03C9;\u1F60;\u1F61;\u1F64;\u1F62;\u1F66;\u1F65;\u1F63;\u1F67;\u1F7D;\u1F7C;\u1FF6";
        //omega (min iotasub)
        s += ";\u1FF3;\u1FA0;\u1FA1;\u1FA4;\u1FA2;\u1FA6;\u1FA5;\u1FA3;\u1FA7;\u1FF4;\u1FF2;\u1FF7";
        //omega (maiusc)
        s += ",\u03A9;\u1F68;\u1F69;\u1F6C;\u1F6A;\u1F6E;\u1F6D;\u1F6F";
        //omega (maiusc iotasub)
        s += ";\u1FFC;\u1FA8;\u1FA9;\u1FAC;\u1FAA;\u1FAE;\u1FAD;\u1FAB;\u1FAF";
    }

    public static RuleBasedCollator newInstance() {
        try {
            initialize();
            return new RuleBasedCollator(s);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getAncientGreekCollationRule() {
        if (s == null) {
            initialize();
        }
        return s;
    }

    public static void main(String argv[]) {
        try {
            newInstance();
            String m = AncientGreekCollator.getAncientGreekCollationRule();
            System.out.println(m);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("stringGreek.txt"), "UTF-8"))) {
                bw.write(m, 0, m.length());
                bw.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }
}