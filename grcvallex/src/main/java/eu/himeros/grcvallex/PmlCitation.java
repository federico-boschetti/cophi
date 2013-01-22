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

import java.io.*;
import java.util.HashMap;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author federico_D0T_boschetti_D0T_73_AT_gmail_D0T_com
 */
public class PmlCitation {
    int p = 0;
    SAXBuilder builder = null;
    Document docIn = null;
    Document docOut = null;
    Element rootIn = null;
    XMLOutputter xop = null;
    HashMap<String, String> ctsMap=null;
    String id=null;
    String sentenceId=null;

    public PmlCitation(String inFileName, String outFileName, String ctsFileName){       
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8"))){
            ctsMap=makeCTS(ctsFileName);
            builder = new SAXBuilder();
            docIn = builder.build(inFileName);
            rootIn = docIn.getRootElement();
            parse(rootIn);
            Format f=Format.getPrettyFormat();
            f.setLineSeparator("\n");
            xop=new XMLOutputter(f);
            xop.output(docIn,bw);
        }catch(Exception ex){ex.printStackTrace(System.err);}
    }
    
    private void parse(Element el){
        if(el.getAttribute("document_id")!=null){
            sentenceId=el.getAttributeValue("id");
        }else if(el.getAttributeValue("id")!=null){
            id=sentenceId+"-"+el.getAttributeValue("id");
            String s;
            if((s=ctsMap.get(id))!=null){
                el.setAttribute("cts",s);
            }
        }
        for(Element child: (List<Element>)el.getChildren()){
                parse(child);
        }
    }
    
    public static void main(String[] args){
        System.err.println(args[0]);
        PmlCitation pc=new PmlCitation(args[0], args[1],args[2]);
    }
    
        private HashMap<String, String> makeCTS(String ctsFileName){
        HashMap<String, String> ctsMap=new HashMap(200000);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ctsFileName), "UTF-8"))) {
                String line=null;
                while((line=br.readLine())!=null){
                    String[] keyValue=line.split("\t");
                    ctsMap.put(keyValue[0], keyValue[1]);
                }
            }catch(IOException ex){
                ex.printStackTrace(System.err);
            }
        return ctsMap;
    }    
}
