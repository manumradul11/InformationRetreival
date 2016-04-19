
package tf.idf;

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
import java.util.TreeMap;

import bm25.RankDoc;
import lucene_Expansion_PsuedoRelevance.XMLQueryParser;
import bm25.FrequencyDoc;

public class TFIDF {

    public static void main(String[] args)
    {
        
        TFIDF search = new TFIDF();
        String folder = "/Users/manusaxena/Documents/InformationRetreival/cacm";
        Map<String, List<FrequencyDoc>> index = new HashMap<String, List<FrequencyDoc>>();
        Map<String, Integer> docLength = new HashMap<String, Integer>();
        
        
        // Building Index
        Scanner scanner = null;
        int docLen = 0;
        for(File file : new File(folder).listFiles()) {
        	System.out.println(file.getName());
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();  
            }
            while (scanner.hasNextLine()) {
                String [] words = scanner.nextLine().trim().split(" ");
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
        
        XMLQueryParser qp =new XMLQueryParser();
    	Map<Integer, String> queries = new TreeMap<Integer, String>();
    	queries = qp.getQueries();
    	int count = 0;
    	for(Map.Entry<Integer, String> dl : queries.entrySet())
        { 
    		String query = dl.getValue();
    		count++;
    		List<RankDoc> rankedList = search.calculateRankScore(query, docLength, index);
    		search.fileWriter("/Users/manusaxena/Documents/InformationRetreival/Tfidf RESULTS /"+"Query" + count +"-DOCUMENT-RANK_TFIDF"+ ".txt", count, 100, rankedList);
		}
        
        
       
    }
   
    
    public List<RankDoc> calculateRankScore(String query, Map<String, Integer> docLengths,Map<String, List<FrequencyDoc>> index) 
    {  
    	String[] queryWords = query.split(" ");
    	double n;
    	double score;
    	List<RankDoc> rankedList = new ArrayList<RankDoc>();
    	
    	
        for(Map.Entry<String, Integer> dl : docLengths.entrySet())
        {            
            String docId= dl.getKey();
            double doc_length= dl.getValue();
            
            double Dj_Qj=0;
            double Dj=0;
            double Qj=0;
                        
            for(String qWord : queryWords)
            {
                List<FrequencyDoc> docFrequencies = index.get(qWord);
                if(docFrequencies == null) continue;
                n = docFrequencies.size();
             
                for(FrequencyDoc docf : docFrequencies) 
                {
                	if(docf.getDocId().equalsIgnoreCase(docId))
                	{
                		double freq = docf.getFrequency()/doc_length;
                        double idf=  (1 + Math.log(1000 /n));
                        double doc_tfidf= freq*idf;
                        //double query_tfidf = 1*idf;
                        //Dj_Qj+= doc_tfidf*query_tfidf;
                        Dj+= doc_tfidf;
                        //Qj+= query_tfidf*query_tfidf;
                	}
                }           
            }
            
            score=Dj;
            //score = Dj_Qj/Math.sqrt(Dj*Qj);
            if(java.lang.Double.isNaN(score))
            {
            	score=0;
            	 System.out.println(Dj_Qj+": "+Dj+": "+ Qj+"\n");
            }
            RankDoc rank = new RankDoc (docId, score);
            rankedList.add(rank);
            System.out.println(rank.getDocId()+":"+rank.getScore()+"\n");
            
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
            	String rest_path="/Users/manusaxena/Documents/InformationRetreival/cacm/";
            	writer.write(queryId+"\t"+"Q0"+"\t"+ rest_path+rankedList.get(j).getDocId()+"\t"+k+"\t"+ rankedList.get(j).getScore()+"\t"+"Manu"+"\n");
                
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
