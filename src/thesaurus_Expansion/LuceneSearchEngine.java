
package thesaurus_Expansion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

import lucene_Expansion_PsuedoRelevance.XMLQueryParser;

import org.apache.http.*;



/**
 * To create Apache Lucene index in a folder and add files into this index based
 * on the input of the user.
 */
public class LuceneSearchEngine {
    private static Analyzer simpleAnalyzer = new SimpleAnalyzer();
    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<File>();

    public static void main(String[] args) throws IOException {
        
	String indexLocation = null;
	String s = "/Users/manusaxena/Documents/InformationRetreival/index";


	LuceneSearchEngine indexer = null;
	try {
	    indexLocation = s;
	    indexer = new LuceneSearchEngine(s);
	} catch (Exception ex) {
	    System.out.println("Cannot create index..." + ex.getMessage());
	    System.exit(-1);
	}

	// ===================================================
	// read input from user until he enters q for quit
	// ===================================================	
	String desktopPath = System.getProperty ("user.home") + "/Documents/InformationRetreival/cacm/";
	Set<String>  filepaths = new LinkedHashSet<String>();
	File folder = new File(desktopPath);
	File[] listOfFiles = folder.listFiles();
	for (File file : listOfFiles)
	{
	    if (file.isFile())
	    {
	    	try 
		    {
				filepaths.add(desktopPath+file.getName());
				
		    } 
		    catch (Exception e) {
			System.out.println("Error indexing " + s + " : "
				+ e.getMessage());
		    }
	    }
	}
	
	for (String filepath : filepaths)
	{
		try 
	    {
			
			indexer.indexFileOrDirectory(filepath);
	    } 
	    catch (Exception e) {
		System.out.println("Error indexing " + s + " : "
			+ e.getMessage());
	    }
	}
	
	// ===================================================
	// after adding, we always have to call the
	// closeIndex, otherwise the index is not created
	// ===================================================
	indexer.closeIndex();

	// =========================================================
	// Now search
	// =========================================================
	IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
	IndexSearcher searcher = new IndexSearcher(reader);

    indexer.GetAllTermFrequencies(reader);
	s = "";
	int count=0;
	XMLQueryParser qp =new XMLQueryParser();
	Map<Integer, String> queries = new TreeMap<Integer, String>();
	queries = qp.getQueries();
	System.out.println("entry point 1 \n");
	for(Map.Entry<Integer, String> dl : queries.entrySet())
    { 
		count++;
		try
	    {
			s=dl.getValue();
			String[] words = s.split(" ");
			for(int i=0;i<words.length;i++)
			{
				words[i]=words[i].replaceAll("[^a-zA-Z]", "");
				HttpResponse<JsonNode> response = Unirest.post("https://twinword-word-associations-v1.p.mashape.com/associations/")
		    			.header("X-Mashape-Key", "JR9M7f62lfmsh9vQD87CR856X2NBp1z3oMajsn9NGFcIScVDLv")
		    			.header("Content-Type", "application/x-www-form-urlencoded")
		    			.header("Accept", "application/json")
		    			.field("entry", words[i])
		    			.asJson();
				try
				{
			    	if(response.getBody().getObject().getString("result_code").equalsIgnoreCase("462"))
			    	{
			    		
			    	}
			    	else
			    	{
			    		String temp=response.getBody().getObject().getString("associations");
			    		temp=temp.substring(0, 100);
				    	s=s.concat(temp);
			    		
			    	}
			    	
				}
				catch (Exception ex) {
		    	    // complain to user
		    	}
				
		    		
		    	
			}

	    	Query q = new QueryParser("contents", simpleAnalyzer).parse(QueryParser.escape(s));
            TopDocs td = searcher.search(q, 100);
            System.out.println("Total Number of hits : " + td.totalHits);
            ScoreDoc[] hits = td.scoreDocs;

            
            System.out.println("Found " + hits.length + " hits.");
    		
            try
            {
            	FileWriter writer = new FileWriter("/Users/manusaxena/Documents/InformationRetreival/Theasaurus results/"+"Q"+count+"-DOCUMENT-RANK.txt"); 
            	for (int i = 0; i < hits.length; ++i) {
        		    int docId = hits[i].doc;
        		    Document d = searcher.doc(docId);
        		    writer.write(count+"\t"+"Q0"+"\t"+ d.get("path")+"\t"+(i + 1)+"\t"+ hits[i].score+"\t"+"Manu"+"\n");
        		}
            	writer.close();
	      	}
            catch (IOException ex) {
		    	    // complain to user
		    	}
            
            Term termInstance = new Term("contents", s);
            long termFreq = reader.totalTermFreq(termInstance);
            long docCount = reader.docFreq(termInstance);
            System.out.println(s + " Term Frequency " + termFreq+ " - Document Frequency " + docCount);
            
            // global warming potential
            // green power renewable energy
            // solar energy california
            // light bulb bulbs alternative alternatives
            // /Users/manusaxena/Downloads/projectHW4/Index

	    } 
	    catch (Exception e) {
		System.out.println("Error searching " + s + " : "
			+ e.getMessage());
		break;}
    }
        
}


	/**
     * Get all the term frequencies
     * @param reader
     * @throws IOException 
     */
    public void GetAllTermFrequencies(IndexReader reader) throws IOException {
        Map<String,Long> termFrequencies = new TreeMap<String, Long>();
        Terms terms = SlowCompositeReaderWrapper.wrap(reader).terms("contents");
        TermsEnum termItr = terms.iterator();
        BytesRef bytesRef;
        while ((bytesRef = termItr.next()) != null) {
            if (termItr.seekExact(bytesRef)) {
                String term = bytesRef.utf8ToString(); 
                Term termInstance = new Term("contents", term);
		long termFreq = reader.totalTermFreq(termInstance);
                termFrequencies.put(term, termFreq);      
            }
        }
    }
    
    /**
     * Function to sortMap
     * @param unsortMap
     * @return 
     */
    public Map sortByValue(Map unsortMap) {	 
	List list = new LinkedList(unsortMap.entrySet());
 
	Collections.sort(list, new Comparator() {
		public int compare(Object o1, Object o2) {
			return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
		}
	});
 
	Map sortedMap = new LinkedHashMap();
	for (Iterator it = list.iterator(); it.hasNext();) {
		Map.Entry entry = (Map.Entry) it.next();
		sortedMap.put(entry.getKey(), entry.getValue());
	}
	return sortedMap;
}

        
    
    /**
     * Constructor
     * 
     * @param indexDir
     *            the name of the folder in which the index should be created
     * @throws java.io.IOException
     *             when exception creating index.
     */
    public LuceneSearchEngine(String indexDir) throws IOException {
	FSDirectory dir = FSDirectory.open(Paths.get(indexDir));
	IndexWriterConfig config = new IndexWriterConfig(simpleAnalyzer);
	writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * 
     * @param fileName
     *            the name of a text file or a folder we wish to add to the
     *            index
     * @throws java.io.IOException
     *             when exception
     */
    public void indexFileOrDirectory(String fileName) throws IOException {
	// ===================================================
	// gets the list of files in a folder (if user has submitted
	// the name of a folder) or gets a single file name (is user
	// has submitted only the file name)
	// ===================================================
	addFiles(new File(fileName));

	int originalNumDocs = writer.numDocs();
	for (File f : queue) {
	    FileReader fr = null;
	    try {
		Document doc = new Document();

		// ===================================================
		// add contents of file
		// ===================================================
		fr = new FileReader(f);
		doc.add(new TextField("contents", fr));
		doc.add(new StringField("path", f.getPath(), Field.Store.YES));
		doc.add(new StringField("filename", f.getName(),
			Field.Store.YES));

		writer.addDocument(doc);
		System.out.println("Added: " + f);
	    } catch (Exception e) {
		System.out.println("Could not add: " + f);
	    } finally {
		fr.close();
	    }
	}

	int newNumDocs = writer.numDocs();
	System.out.println("");
	System.out.println("************************");
	System.out
		.println((newNumDocs - originalNumDocs) + " documents added.");
	System.out.println("************************");

	queue.clear();
    }

    private void addFiles(File file) {

	if (!file.exists()) {
	    System.out.println(file + " does not exist.");
	}
	if (file.isDirectory()) {
	    for (File f : file.listFiles()) {
		addFiles(f);
	    }
	} else {
	    String filename = file.getName().toLowerCase();
	    // ===================================================
	    // Only index text files
	    // ===================================================
	    if (filename.endsWith(".htm") || filename.endsWith(".html")
		    || filename.endsWith(".xml") || filename.endsWith(".txt")) {
		queue.add(file);
	    } else {
		System.out.println("Skipped " + filename);
	    }
	}
    }

    
    /**
     * Close the index.
     * 
     * @throws java.io.IOException
     *             when exception closing
     */
    public void closeIndex() throws IOException {
	writer.close();
    }
}