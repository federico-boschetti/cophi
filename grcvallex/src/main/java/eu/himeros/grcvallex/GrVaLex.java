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
import java.util.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

/**
 *
 * @author federico
 */
public class GrVaLex {
    int p = 0;
    SAXBuilder builder = null;
    Document docIn = null;
    Document docOut = null;
    Element rootIn = null;
    XPathExpression<Element> xpath = null;
    XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
    BufferedWriter bw = null;
    int abstrLevel = 2;
    String struct = "";
    ArrayList<String> structs = new ArrayList(10000);
    ArrayList<String> args = new ArrayList(10);
    String[] argLabels = {"SBJ", "OBJ", "PNOM", "OCOMP"};
    String[] bridgeLabels = {"AuxP", "AuxC"};
    String[] groupingLabels = {"COORD", "APOS"};
    String[] relevantLabels = {"SBJ", "OBJ", "PNOM", "OCOMP", "AuxP", "AuxC", "COORD", "APOS", "PRED"};
    HashMap<String, String> count = new HashMap(10000);
    HashMap<String, String> argOrd = new HashMap(6);
    HashMap<String, Element> curArg = new HashMap(10);
    HashMap<String, Element> ExDs = new HashMap(10);
    HashMap<String, Element> sortedSentence = new HashMap(100);
    HashMap<String,String> ctsMap=null;
    HashMap<String,StringBuffer> locMap=new HashMap(10000);
    int prog = 0;
    boolean morphSubj = false;
    String buf = null;
    String sentenceId=null;
    String wordId=null;    

    /**
     * @param args the command line arguments
     */
    public GrVaLex(String inFileName, String outFileName, String ctsFileName) {
        try {
            argOrd.put("SBJ","1");
            argOrd.put("PNOM","2");
            argOrd.put("OBJ","3");
            argOrd.put("OCOMP","4");
            argOrd.put("AuxP","5");
            argOrd.put("AuxC","6");
            ctsMap=makeCTS(ctsFileName);
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "UTF-8"));
            builder = new SAXBuilder();
            docIn = builder.build(inFileName);
            rootIn = docIn.getRootElement();
            xpath=XPathFactory.instance().compile("/ns:aldt_treebank/ns:aldt_trees",Filters.element(),null,Namespace.getNamespace("ns","http://ufal.mff.cuni.cz/pdt/pml/"));
            List<Element> nodes=xpath.evaluate(rootIn);
            for (Element el : nodes) {
                parseTopDown(el);
            }
            fileCloseHook();
        } catch (JDOMException | IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public final void parseTopDown(Element el) {
        List<Element> children = el.getChildren();
        if (children != null) {
            for (Element child : children) {
                try{
                    parse(child);
                    parseTopDown(child);
                }catch(Exception ex){
                    ex.printStackTrace(System.err);
                    System.err.println("--BEG--");
                    System.err.println(xop.outputString(child));
                    System.err.println("--END--");
                }
            }
        }
    }

    public static void main(String[] args) {
        GrVaLex gvl = new GrVaLex(args[0], args[1], args[2]);
    }

    public void fileOpenedHook() {
    }

    public final void fileCloseHook() {
        try {
            Collections.sort(structs,AncientGreekCollator.newInstance());
            count = makeOptSubj(countUnique(structs));
            String[] keys = count.keySet().toArray(new String[0]);
            Collections.sort(Arrays.asList(keys),AncientGreekCollator.newInstance());
            keys=locMap.keySet().toArray(new String[0]);
            Collections.sort(Arrays.asList(keys),AncientGreekCollator.newInstance());
            for(String key:keys){
                String locVal=locMap.get(key).toString();
                int occurr=1;
                for(char c:locVal.toCharArray()){
                    if(c==' '){
                        occurr++;
                    }
                }
                bw.write(occurr+"\t"+key+"\t"+locVal);
                bw.newLine();
            }
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }

    public void parse(Element thisNode) {
        struct = "";
        args = new ArrayList(10);
        morphSubj = false;
        if((getAV(thisNode,"document_id"))!=null){
            sentenceId=getAV(thisNode,"id");
        }else if (checkAV(thisNode, "pos", "verb")) {
            wordId=sentenceId+"-"+getAV(thisNode,"id");
            if ((checkAV(thisNode, "person", "third_person") && checkAV(thisNode, "number", "singular")) || checkAV(thisNode, "person", null) || (checkAV(thisNode, "number", null))) {
                morphSubj = false;
            } else {
                morphSubj = true;
            }
            makeExDs(thisNode);
            ExDs = new HashMap(10);
            Element parent = thisNode.getParentElement();
            //List<Element> children;
            String lemma = getAV(thisNode, "lemma");
            String relation = getAV(thisNode, "relation");
            lemma = ((lemma != null) ? lemma.replaceAll("[0-9]$", "") : "");
            //String person = getAV(thisNode, "person");
            //String number = getAV(thisNode, "number");
            HashMap<String, String> persons = new HashMap(3);
            persons.put("first_person", "1");
            persons.put("second_person", "2");
            persons.put("third_person", "3");
            HashMap<String, String> numbers = new HashMap(3);
            numbers.put("singular", "s");
            numbers.put("plural", "p");
            numbers.put("dual", "d");
            struct = lemma + "[" + ((buf = getAV(thisNode, "voice")) != null && buf.length() > 2 ? buf.substring(0, 3) : "") + "] ";
            ArrayList<Element> childrenArrayList=new ArrayList();
            childrenArrayList.addAll(thisNode.getChildren());
            if ((relation != null && relation.matches(".*?_CO$")) && (checkAV(parent, "relation", "COORD"))) {
                childrenArrayList.addAll(parent.getChildren());
            }
            for (Element child : childrenArrayList) {
                prog = 0;
                curArg = new HashMap(10);
                String chRel = getAV(child, "relation");
                if (chRel != null && chRel.matches("^((SBJ)|(OBJ)|(PNOM)|(OCOMP))$")) {
                    addArg(makeArg(child));
                } else if (chRel != null && chRel.matches("^((AuxP)|(AuxC))$")) {
                    addArg(makeBridge(child));
                } else if (chRel != null && chRel.matches("^((COORD)|(APOS))$")) {
                    addArg(makeSet(child));
                }
            }
            if (morphSubj&&!args.contains("1SBJ ")) {
                args.add("1SBJ ");
            }
            if (args != null) {
                Collections.sort(args);
                for (String arg : args) {
                    struct += arg;
                }
            }
            struct = struct.replaceAll("( |\\|)[0-9]", "$1");
            StringBuffer locVal=locMap.get(struct);
            if(locVal==null){
                //!!!
                try{
                    locMap.put(struct,new StringBuffer(ctsMap.get(wordId)));
                }catch(Exception ex){
                    locMap.put(struct,new StringBuffer("Xyz.0.0"));
                }
            }else{
                try{
                    locMap.put(struct,locMap.get(struct).append(" ").append(ctsMap.get(wordId)));
                }catch(Exception ex){
                    locMap.put(struct,new StringBuffer("Xyz.0.0"));
                }
            }
            structs.add(struct);
        }
    }

    public boolean makeArg(Element thisNode, Element bridge) {
        prog++;
        String key = String.format("%02d", prog);
        if (bridge != null) {
            key = key + ">" + getAV(bridge, "relation") + ":" + (((buf=getAV(bridge, "lemma"))==null||buf.equals(""))?"":buf.substring(0,buf.length() - 1));
        }
        curArg.put(key,thisNode);
        return true;
    }

    public boolean makeArg(Element thisNode) {
        return makeArg(thisNode, null);
    }

    public boolean makeBridge(Element thisNode, Element bridge, Element coord) {
        prog++;
        Element thisBridge = thisNode;
        String key = String.format("%02d", prog);
        if (bridge != null) {
            key = key + ">" + getAV(bridge, "relation") + ":" + getAV(bridge, "lemma").substring(0, getAV(bridge, "lemma").length() - 1);
        }
        List<Element> children = thisNode.getChildren();
        for (Element child : children) {
            String chRel = getAV(child, "relation");
            if (chRel.matches("^((SBJ)|(OBJ)|(PNOM)|(OCOMP))((_AP)|(_CO))*$")) {
                if ((coord != null
                        && getAV(child, "relation").matches(".*?((_AP)|(_CO))+$"))
                        || (coord == null
                        && getAV(child, "relation").matches("^((SBJ)|(OBJ)|(PNOM)|(OCOMP))$"))) {
                    makeArg(child, thisBridge);
                }//else if(coord==null&&getAV(child,"relation").matches("^((SBJ)|(OBJ)|(PNOM)|(OCOMP))$")){
                //makeArg(child,thisBridge);
                //}

            } else if (chRel.matches("^((AuxP)|(AuxC))$")) {
                makeBridge(child, thisBridge, coord);
            } else if (chRel.matches("^((COORD)|(APOS))$")) {
                makeSet(child, thisBridge, coord);
            }
        }
        return true;
    }

    public boolean makeBridge(Element thisNode) {
        return makeBridge(thisNode, null, null);
    }

    public boolean makeSet(Element thisNode, Element bridge, Element coord) {
        Element thisCoord = thisNode;
        String key = String.format("%02d", prog);
        if (bridge != null) {
            key = key + ">" + getAV(bridge, "relation") + ":" + getAV(bridge, "lemma").substring(0, getAV(bridge, "lemma").length() - 1);
        }
        List<Element> children = thisNode.getChildren();
        for (Element child : children) {
            String chRel = getAV(child, "relation");
            if (chRel.matches("^((SBJ)|(OBJ)|(PNOM)|(OCOMP))((_AP)|(_CO))+$")) {
                makeArg(child, bridge);
            } else if (chRel.matches("^((AuxP)|(AuxC))$")) {
                makeBridge(child, bridge, thisCoord);
            } else if (chRel.matches("^((COORD)|(APOS))$")) {
                makeSet(child, bridge, thisCoord);
            }
        }
        return true;
    }

    public boolean makeSet(Element thisNode) {
        return makeSet(thisNode, null, null);
    }

    public void addArg(boolean status) {
        String str = "";
        HashMap<String, Integer> set = new HashMap(10);
        String[] keys = (String[]) curArg.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for (String key : keys) {
            Element curNode = curArg.get(key);
            String aux = "";
            if (key.contains(">")) {
                aux = key;
                aux = aux.replaceAll(".*?>(.*?)", "$1");
            }
            String formattedArg = formatArg(aux, curNode);
            if (formattedArg.matches(".*?SBJ.*?")) {
                formattedArg = "1SBJ ";
            }
            Integer val = ((val = set.get(formattedArg)) == null
                    ? set.put(formattedArg, 1)
                    : set.put(formattedArg, val++));
        }
        keys = (String[]) set.keySet().toArray(new String[0]);
        for (String key : keys) {
            if(key!=null){
                str += key + "|";
            }
        }
        str = str.replaceAll(" +?", "");
        if (abstrLevel == 0) {
            str = str.replaceAll("^(.*?\\|).*$", "$1");
        }
        str = str.replaceAll("\\|$", " ");
        args.add(str);
    }

    public String formatArg(String aux, Element curNode) {
        if(aux==null){aux="";}
        String relation = getAV(curNode, "relation");
        relation = relation.replaceAll("((_AP)|(_CO))+", "");
        String relAux = aux;
        relAux = relAux.replaceAll("(.*?):.*", "$1");
        String ordKey = ((relAux != null && !relAux.equals("")) ? relAux : relation);
        switch (abstrLevel) {
            case 0:
                return ((buf=argOrd.get(ordKey))==null?"":buf) + relation;
            case 1:
                if (aux != null && !aux.equals("")) {
                    aux = aux.replaceAll("(.*?):.*", "$1+");
                    aux = aux.replaceAll("AuxP", "prep");
                    aux = aux.replaceAll("AuxC", "conj");
                }
                return ((buf=argOrd.get(ordKey))==null?"":buf) + aux + relation;
            case 2:
                if (aux != null && !aux.equals("")) {
                    aux = aux.replaceAll(".*?:(.*)", "$1+");
                }
                if(relation.matches(".*?SBJ.*?")){
                    return "1SBJ ";
                }else{
                    return ((buf=argOrd.get(ordKey))==null?"":buf) + relation + "["+aux + morph(curNode) + "]#";
                }
            default:
                return null;
        }
    }

    public HashMap<String, String> countUnique(ArrayList<String> structs) {
        for (String key : structs) {
            String val = ((val = count.get(key)) == null
                    ? count.put(key, "1")
                    : count.put(key, "" + (Integer.valueOf(val) + 1)));
        }
        return count;
    }

    public HashMap<String, String> makeOptSubj(HashMap<String, String> count) {
        String pattern = ".*?SBJ.*?";
        String sbj="SBJ";
        String wSubj = "";
        String[] keys = count.keySet().toArray(new String[0]);
        String value = "0";
        String wVal = "0";
        for (String key : keys) {
            value = (((value = count.get(key)) == null)? "0" : value);
            if (!key.matches(pattern)) {
               
                wSubj = key;
                wSubj = wSubj.replaceAll("(^.*? )(.*?)", "$1" + sbj + " $2");
                if (count.get(wSubj) != null) {
                    wVal = count.get(wSubj);
                    count.put(key, "+" + (Integer.valueOf(value) + Integer.valueOf(wVal)));
                    count.put(wSubj, "-" + count.get(wSubj));
                } else {
                    count.put(key, "=" + value);
                }
            }
        }
        HashMap<String, String> count2 = new HashMap(10000);
        for (String key : keys) {
            value = ((value = count.get(key)) == null ? "0" : value);
            switch (value.substring(0, 1)) {
                case "-":
                    break;
                case "+":
                    key=key.replaceAll("^([^ ]*?) ","$1 SBJ? ");
                    count2.put(key,value.substring(1));
                case "=":
                    count2.put(key, value.substring(1));
                    break;
                default:
                    //key=key.replaceAll("SBJ","SBJ ");
                    count2.put(key.substring(0, key.length() - 1), value);
                    break;
            }
        }
        return count2;
    }

    public String morph(Element node) {
        String pos = (pos = getAV(node, "pos")) != null ? pos : "";
        String case_ = (case_ = getAV(node, "case")) != null ? case_ : "";
        String mood = (mood = getAV(node, "mood")) != null ? mood : "";
        if (!case_.equals("")) {
            return case_.substring(0, 3);
        } else if (!mood.equals("")) {
            return mood.substring(0, 3);
        } else if (pos.equals("adverb")) {
            return "adv";
        } else {
            return pos + "(?)";
        }
    }

    public synchronized void  makeExDs(Element thisNode) {
        Element node1 = null;
        String nodeId;
        String nodeRel;
        Element prevNode = null;
        String[] nodeStrs = null;
        List<Element> children = thisNode.getChildren();
        Element[] childrenArray=children.toArray(new Element[0]);
        for (Element child : childrenArray) {
            if (getAV(child, "relation") != null && getAV(child, "relation").matches(".*?_ExD[0-9]_.*?")) {
                nodeStrs = getAV(child, "relation").split("_ExD(?=[0-9])");
                for (String nodeStr : nodeStrs) {
                    String[] idRels = nodeStr.split("(?<=^[0-9])_");
                    if (idRels == null || idRels.length < 2) {
                        nodeRel = nodeStr;
                        node1 = child;
                    } else {
                        nodeId = 1000 + idRels[0];
                        nodeRel = idRels[1];
                        if (ExDs.get(nodeId) != null) {
                            node1 = ExDs.get(nodeId);
                            //????
                            prevNode.detach();
                            node1.addContent(prevNode);
                            //node1.detach();
                            //prevNode.addContent(node1);
                        } else {
                            //????
                            Element parent = prevNode.getParentElement();
                            prevNode.detach();
                            node1 = new Element("LM");
                            node1.addContent(prevNode);
                            parent.addContent(node1);
                            ExDs.put(nodeId, node1);
                        }
                    }
                    node1 = setAV(node1, "relation", nodeRel);
                    nodeId = "";
                    prevNode = node1;
                }
            }
            makeExDs(child);
        }
    }

    public void sortSentence(Element thisNode) {
        ArrayList<Element> children = (ArrayList) thisNode.getChildren();
        for (Element child : children) {
            sortedSentence.put(getAV(child, "id"), child);
            sortSentence(child);
        }
    }

    public void printSentence(Element thisNode, String color) {
        System.out.println("<font color=\"" + color + "\">");
        sortSentence(thisNode);
        String[] keys = (String[]) sortedSentence.keySet().toArray();
        Arrays.sort(keys);
        for (String word : keys) {
            Element wordNode = sortedSentence.get(word);
            System.out.print(getAV(wordNode, "form"));
            if (grep(getAV(wordNode, "relation"), "re", relevantLabels)) {
                System.out.print("<font size=\"-3\"><sub>" + getAV(wordNode, "relation") + "<sub>" + getAV(wordNode.getParentElement(), "id") + ">" + getAV(wordNode, "id") + "</sub></sub></font>");
            }
            System.out.print(" ");
        }
        System.out.print("</font><br/>\n");
    }

    public void printStruct(String struct) {
        System.out.print("<br/><font color=\"red\">" + struct + "</font><br/>\n");
    }

    public void printProlog() {
        System.out.print("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>\n");
    }

    public void printEpilog() {
        System.out.print("</body></html>\n");
    }

    /*
     * Utilities
     */
    private boolean checkAV(Element el, String attribute, String test) {
        if (el != null) {
            String value = el.getAttributeValue(attribute);
            if (value != null) {
                return value.equals(test);
            } else {
                return test == null;
            }
        } else {
            return test == null;
        }
    }

    private String getAV(Element el, String attribute) {
        return el.getAttributeValue(attribute);
    }

    private Element setAV(Element el, String attribute, String value) {
        return el.setAttribute(attribute, value);
    }

    private boolean grep(String test, String type, String[] strs) {
        for (String str : strs) {
            switch (type) {
                case "eq":
                    if (str.equals(test)) {
                        return true;
                    }
                    break;
                case "re":
                    if (str.matches(".*?"+test+".*?")) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }
    
    private HashMap<String, String> makeCTS(String ctsFileName){
        HashMap<String, String> ctsMap=new HashMap(200000);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ctsFileName), "UTF-8"))) {
                String line;
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
