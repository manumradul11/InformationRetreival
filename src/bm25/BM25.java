
package bm25;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import bm25.RankDoc;
import bm25.FrequencyDoc;

public class BM25 {

    public static void main(String[] args)
    {
        
        BM25 search = new BM25();
        String folder = "/Users/manusaxena/Downloads/projectHW4/corpus";
        Map<String, List<FrequencyDoc>> index = new HashMap<String, List<FrequencyDoc>>();
        Map<String, Integer> docLength = new HashMap<String, Integer>();
        
        
        // Building Index
        Scanner scanner = null;
        int docLen = 0;
        for(File file : new File(folder).listFiles()) {
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  
            }
            while (scanner.hasNextLine()) {
                String [] words = scanner.nextLine().trim().split(" ");
                System.out.println(file.getName());
                for(int i = 0 ; i < words.length; i++) 
                {
                    String word = words[i].trim();
                    if(word.length() <= 0)
                    {
                        continue;
                    }
                    if(index.containsKey(word)) 
                    {
                        List<FrequencyDoc> docFrequencies = index.get(word);
                        FrequencyDoc docFreq = new FrequencyDoc(file.getName(), 0);
                        if(index.get(word).contains(docFreq))
                        {
                            int idx = docFrequencies.indexOf(docFreq);
                            docFrequencies.get(idx).increment();
                            index.put(word, docFrequencies);
                        }
                        else 
                        {
                            docFreq.increment();
                            docFrequencies.add(docFreq);
                            index.put(word, docFrequencies);
                        }
                    }
                    else 
                    {
                        List<FrequencyDoc> docFrequencies = new ArrayList<FrequencyDoc>();
                        FrequencyDoc docFreq = new FrequencyDoc(file.getName(), 1);
                        docFrequencies.add(docFreq);
                        index.put(word, docFrequencies);
                    }
                }
                
                docLen += words.length;
            }
            docLength.put(file.getName(), docLen);
        }
        
        List<String> queries = new ArrayList<String>();
        queries.add("global warming potential");
        queries.add("green power renewable energy");
        queries.add("solar energy california");
        queries.add("light bulb bulbs alternative alternatives");
        
        int count = 0;
        for(String query : queries) {
            count++;
            List<RankDoc> rankedList = search.calculateRankScore(query, docLength, index);
            search.fileWriter("Query" + count +"-DOCUMENT-RANK"+ ".txt", count, 100, rankedList);
        }
    }
   
    
    public List<RankDoc> calculateRankScore(String query, Map<String, Integer> docLengths,Map<String, List<FrequencyDoc>> index) 
    {
       
        double k1 = 1.2;
        double k2 = 100;
        double avgDocLen = 0;
        double totalDocLen = 0;
        for(Map.Entry<String, Integer> dl : docLengths.entrySet())
        {            
            totalDocLen += dl.getValue();
        }
        avgDocLen = totalDocLen/docLengths.size();
        
        List<RankDoc> rankedList = new ArrayList<RankDoc>();
        double n;
        double bigN = (double) docLengths.size();
        double qf = 1;
        double score;
        
        String[] queryWords = query.split(" ");
        
       
        for(String qWord : queryWords)
        {
            List<FrequencyDoc> docFrequencies = index.get(qWord);
            if(docFrequencies == null) continue;
            n = docFrequencies.size();
            for(FrequencyDoc docf : docFrequencies) 
            {
                String docId = docf.getDocId();
                int freq = docf.getFrequency();
                int docLen = docLengths.get(docId);
                score = Math.log((1/ ((n + 0.5) / (bigN -n +0.5))))*(((k1+1)*freq)/(getK(avgDocLen, docLen)+freq)) * (((k2+ freq) * qf)/(k2 + qf));
                RankDoc rank = new RankDoc (docId, 0);
                if(hasDoc(rankedList, rank))
                {
                    int idx = rankedList.indexOf(rank);
                    rankedList.get(idx).setScore(score + rankedList.get(idx).getScore());
                }
                else
                {	
                    rankedList.add(new RankDoc(docId, score));
                }
            }           
        }
        
        Collections.sort(rankedList);
        return rankedList;
    }
    
    public boolean hasDoc(List<RankDoc> list, RankDoc obj)
    {
        for(RankDoc b : list)
        {
            if(b.getDocId() == obj.getDocId()) 
            {
                return true;
            }
        }
        return false;
    }
    
    
    public void fileWriter(String writerPath,int queryId, int limit, List<RankDoc> rankedList) 
    {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writerPath));
            for(int j = 0; j < limit; j ++) 
            {
            	int k=j+1;
                writer.write(k + "\t" + "Query-"+queryId+ "\t" + rankedList.get(j).getDocId() +  "\t" + rankedList.get(j).getScore() + "\t" +"\n");
            }

        }
        catch (IOException ex) 
        {
            ex.printStackTrace();
        } 
        finally
        {
            try 
            {
                writer.close();
            }
            catch (IOException ex) 
            {
               ex.printStackTrace();
            }
        }
    }
    
    public double getK(double avgDocLen, int docLen)
    {
        double k1 = 1.2;
        double b = 0.75;
        return (k1 * ((1-b) + b * (docLen/avgDocLen)));
    }
}
