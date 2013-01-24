/*
 * This file is part of CophiProofReader
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
package eu.himeros.cophi.proofreader;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.ManagedBean;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.model.DefaultStreamedContent;
import sun.awt.image.ToolkitImage;

/**
 *
 * @author federico[DOT]boschetti[DOT]73[AT]gmail[DOT]com
 */
@ManagedBean
@SessionScoped
public class HocrParser implements Serializable, ValueChangeListener  {

    //final String inFileName = "/home/federico/lab041/ath01_0445-out.html";
    //final String imgFileName="/home/federico/lab041/ath01_0445.png";
    final String inFileName="/opt/junk/hocr208.html";
    final String imgFileName="/opt/junk/img208.png";
    private BufferedImage pageImage;
    private Element root;
    private List<List<String>> lines;
    private List<DefaultStreamedContent> images;
    private InputText inputText;
    private List<List<List<String>>> optionsss;
    private List<List<String>> colorss;

    public List<List<String>> getColorss() {
        return colorss;
    }

    public void setColorss(List<List<String>> colorss) {
        this.colorss = colorss;
    }

    public List<List<List<String>>> getOptionsss() {
        return optionsss;
    }

    public void setOptionsss(List<List<List<String>>> optionsss) {
        this.optionsss = optionsss;
    }


    private List<List<String>> valuess;

    public List<List<String>> getValuess() {
        return valuess;
    }

    public void setValuess(List<List<String>> valuess) {
        this.valuess = valuess;
    }

    public InputText getInputText() {
        inputText.setValue("ciao");
        return inputText;
    }

    public void setInputText(InputText inputText) {
        this.inputText = inputText;
        inputText.setValue("ciao");
    }

    public HocrParser() throws Exception {
        init();
    }

    private void init() throws Exception {
        
        inputText=new InputText();
        lines = new ArrayList<List<String>>(1000);
        images=new ArrayList<DefaultStreamedContent>(1000);
        optionsss=new ArrayList<List<List<String>>>(1000);
        valuess=new ArrayList<List<String>>(1000);
        colorss=new ArrayList<List<String>>(1000);
        initImage();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(inFileName);
        root = doc.getRootElement();
        for (Element body : root.getChildren()) {
            for (Element div : body.getChildren()) {
                for (Element line : div.getChildren()) {
                    if(line.getAttribute("title")==null) continue;
                    images.add(createImage(line));
                    List<String> items=new ArrayList<String>(100);
                    List<String> values=new ArrayList<String>(100);
                    List<String> colors=new ArrayList<String>(100);
                    List<List<String>> optionss=new ArrayList<List<String>>(100);
                    for (Element word : line.getChildren()) {
                        for(Element span:word.getChildren()){
                            items.add(span.getText().replaceAll("'","’"));
                            values.add(span.getText().replaceAll("'","’"));
                            colors.add(processClass(span.getAttributeValue("class")));
                            List<String> options=parseOptions(span);
                            optionss.add(options);
                        }
                    }
                    lines.add(items);
                    valuess.add(values);
                    optionsss.add(optionss);
                    colorss.add(colors);
                }
            }
        }
    }

    public List<List<String>> getLines() {
        return lines;
    }

    public void setItems(List<List<String>> lines) {
        this.lines = lines;
    }

    @Override
    public void processValueChange(ValueChangeEvent event) throws AbortProcessingException {
        System.err.println(event.getNewValue().toString());
    }
    

    public void initImage(){
        pageImage=null;
        try{
            ImageIcon iic=new ImageIcon(imgFileName);
            pageImage=((ToolkitImage)iic.getImage()).getBufferedImage();
        }catch(Exception ex){ex.printStackTrace(System.err);}
    }

    public List<DefaultStreamedContent> getImages(){
        return images;
    }
    
    public DefaultStreamedContent getImage(){
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getRenderResponse()) {
            //Rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        }else{
            String idStr = context.getExternalContext().getRequestParameterMap().get("imageId");
            int id = Integer.parseInt(idStr);
            return images.get(id);
        }
    }
    
    public void setImage(DefaultStreamedContent image){
        //do nothing!
    }
    
    public void setImages(List<DefaultStreamedContent> images){
        this.images=images;
    }
    
    private DefaultStreamedContent createImage(Element line){        
        DefaultStreamedContent image=null;
        try{
            int[] coords=parseBbox(line);
            BufferedImage bimg=pageImage.getSubimage(coords[0],coords[1],coords[2]-coords[0],coords[3]-coords[1]);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bimg,"png", os);
            image = new DefaultStreamedContent(new ByteArrayInputStream(os.toByteArray()), "image/png"); 
        }catch(Exception ex){ex.printStackTrace(System.err);}
        return image;
    }
    
    private int[] parseBbox(Element line){
        int[] coords={0,0,0,0};
        try{
            String[] bboxChunks=line.getAttributeValue("title").split(" ");
            for(int i=1;i<6;i++){
                try{
                    coords[i-1]=Integer.parseInt(bboxChunks[i]);
                    System.err.println(coords[i-1]);
                }catch(NumberFormatException nfex){coords[i-1]=0;nfex.printStackTrace(System.err);}
            }
        }catch(Exception ex){ex.printStackTrace(System.err);}
        return coords;
    }
    
    private List<String> parseOptions(Element el){
        try{
            return Arrays.asList(el.getAttributeValue("title").split(" "));
        }catch(Exception ex){
            return new ArrayList<>(10);
        }
    }
    
    private String processClass(String clazz){
        String color="black";
        if("UCWORD".equals(clazz)){
            color="gray";
        }else if("SYLLABICSEQ".equals(clazz)){
            color="blue";
        }else if("BADMANY".equals(clazz)||"BADONE".equals(clazz)||"CHARSEQ".equals(clazz)){
            color="red";
        }
        return color;
    }
    
}
