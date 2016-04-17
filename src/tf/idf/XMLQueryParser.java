/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tf.idf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Norah
 */
public class XMLQueryParser {
    
    public String readFileStr(String fileName) {
        String retText = "";
        try {
            String s;
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));
            while ((s = br.readLine()) != null) {
                if(s.trim().length() > 1)
                    retText = retText + s.trim() + "\n";
            }
            br.close();
        } catch (Exception ex) {
            Logger.getLogger(XMLQueryParser.class.getName()).log(Level.SEVERE, null, ex);
        }
//        System.out.println("Done reading the file");
        return retText;
    }
    
    public Map<Integer,String> getQueries(){
        Map<Integer, String> queries = new TreeMap<Integer, String>();
        
      try {	
         String inputFile = "/Users/manusaxena/Documents/InformationRetreival/cacm.query";
         Pattern p = Pattern.compile("<DOC>[\\s\\S]*?<\\/DOC>");
         String input = readFileStr(inputFile);
         Matcher m = p.matcher(input);
         Pattern DocPattern  = Pattern.compile("<DOCNO>([\\s\\S])*?<\\/DOCNO>");
         while (m.find()) {
        	 
            String query = m.group(0);
            Matcher docno = DocPattern.matcher(query);
            String docNo = "";
            while(docno.find()) {
                docNo = docno.group();
                docNo = docNo.replaceAll("<DOCNO>", "");
                docNo = docNo.replaceAll("</DOCNO>", "");
               System.out.println("Doc No is " + docNo);
            }
            query = query.replaceAll("<DOCNO>([\\s\\S])*?<\\/DOCNO>", "");
            query = query.replaceAll("<DOC>", "");
            query = query.replaceAll("</DOC>", "");
            query = query.replaceAll("\n", " ");
            queries.put(Integer.parseInt(docNo.trim()), query.trim());
         }
      } catch (Exception e) {
          e.printStackTrace();
      }
      return queries;
   }
}
