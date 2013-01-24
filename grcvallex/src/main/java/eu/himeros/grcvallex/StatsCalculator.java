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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author federico_D0T_boschetti_D0T_73_AT_gmail_D0T_com
 */
public class StatsCalculator {

    int p = 0;
    SAXBuilder builder = null;
    Document docIn = null;
    Document docOut = null;
    Element rootIn = null;
    XMLOutputter xop = null;
    HashMap<String, String> ctsMap = null;
    String id = null;
    String sentenceId = null;
    BufferedWriter bw1 = null;
    BufferedWriter bw2 = null;

    public StatsCalculator(String inFileName, String statFileName1, String statFileName2) {
        try {
            bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statFileName1), "UTF-8"));
            bw2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(statFileName2), "UTF-8"));
            builder = new SAXBuilder();
            docIn = builder.build(inFileName);
            rootIn = docIn.getRootElement();
            parse(rootIn);
            //Format f = Format.getPrettyFormat();
            //f.setLineSeparator("\n");
            //xop = new XMLOutputter(f);
            //xop.output(docIn, bw1);
        } catch (IOException | JDOMException ex) {
            ex.printStackTrace(System.err);
        } finally {
            try {
                bw1.close();
                bw2.close();
            } catch (Exception ex1) {
                ex1.printStackTrace(System.err);
            }
        }

    }

    private void parse(Element el) throws IOException {
        if (el.getAttributeValue("document_id") != null) {
            bw1.write(el.getAttributeValue("id")+"\t");
            sortSentence(el);
        }
        for (Element child : (List<Element>) el.getChildren()) {
            parse(child);
        }
    }

    public void sortSentence(Element thisNode) throws IOException{
        Iterator<Content> descendantIter = thisNode.getDescendants();
        HashMap<Integer, String> sentence = new HashMap(100);
        while (descendantIter.hasNext()) {
            Content item = descendantIter.next();
            if (item instanceof Element) {
                String idStr = ((Element) item).getAttributeValue("id");
                String pos=((Element)item).getAttributeValue("pos");
                if(pos==null||!pos.equals("punctuation")){
                    sentence.put(Integer.valueOf(idStr), ((Element) item).getAttributeValue("form"));
                }
            }
        }
        Integer[] keys = sentence.keySet().toArray(new Integer[0]);
        Arrays.sort(keys);
        bw1.write(""+keys.length);
        bw1.newLine();
        for (Integer i : keys) {
            //System.out.println("" + i + ":" + sentence.get(i));
            String word=sentence.get(i);
            bw2.write(word+"\t"+word.length());
            bw2.newLine();
        }

    }

    public static void main(String[] args) {
        StatsCalculator sc = new StatsCalculator(args[0], args[1], args[2]);

    }
}
