
import java.text.ParseException;
import java.util.*;
import java.io.*;
import java.text.SimpleDateFormat;

public class Invert {

	private final String cFile = "cacm.all";
	private final String sFile = "stopwords.txt";

	private Set<String> dictionary;
	private Set<Integer> position;
	private Map<Integer, Document> documents;
	private Map<String, Frequency> docFreq;
	private Set<String> stopWords = new TreeSet<String>();
	private ReadCacm fileParser;
	private StemDoc documentParser;
	private boolean stopword_enable;
	private static TreeMap<String,ArrayList<Post>> postingsList= new  TreeMap<String,ArrayList<Post>>();
	
	
	public Invert (boolean stopwords, boolean stemmerOn)  {
		dictionary = new TreeSet<String>();
		docFreq = new TreeMap<String, Frequency>();
		fileParser = new ReadCacm();
		documentParser = new StemDoc(stemmerOn);
		
		try {
			documents = fileParser.extractDocuments(cFile);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		this.stopword_enable = stopwords;
		if (stopwords) {
			stopWords = createStopWords(sFile);
		}
	}

	public Set<String> getDictionary() {
		return dictionary;
	}

	public Map<Integer,Document> getDocuments() {
		return documents;
	}

	public Map<String, Frequency> getDocFreq() {
		return docFreq;
	}

	public Set<String> createStopWords(String file) {
		Set<String> words = new TreeSet<String>();
		try {
			File sFile = new File(file);
			BufferedReader reader = new BufferedReader(new FileReader(sFile));			
			for (String word = reader.readLine(); word != null; word = reader.readLine()) {
				words.add(word);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return words;
	}
	
	public static void writeDictionary(Map<String, Frequency> unsorted, PrintWriter file) {
		Map<String, Frequency> mapWord = new TreeMap<String, Frequency>(unsorted);
		Set<?> setWord = mapWord.entrySet();
		Iterator<?> wordIterator = setWord.iterator();
		while (wordIterator.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) wordIterator.next();
			file.print(mapEntry.getKey()+" ");
			file.println(mapEntry.getValue());
		}
	}
	
	public static void writePost(TreeMap<String, ArrayList<Post>> unsorted, Map<String, Frequency> unsortedFreq, PrintWriter file) {

		for (Map.Entry<String, ArrayList<Post>> post : unsorted.entrySet()) {
			file.print(post.getKey() + " ");
			ArrayList<Post> placeHolder = new ArrayList<Post>();
			placeHolder = post.getValue();
			for(Post postings : placeHolder) {
				file.print(postings.toString());
			}
			file.println("");
		}
		
	}

	public Set<String> getStopWords() {
		return stopWords;
	}

	public void invert() {
		for (Integer docID : documents.keySet()) {
			Document document = documents.get(docID);
			documentParser.setDocument(document);
			for (String term : documentParser.findTerms()) {
				if (!stopword_enable || !stopWords.contains(term)) {	
					dictionary.add(term);
					int termFreq = documentParser.countFrequencies(term);
					document.setFrequency(term, termFreq);
					Frequency documentFreq = docFreq.get(term);
					if (documentFreq == null) {
						documentFreq = new Frequency();
						if (termFreq > 0) {
							documentFreq.updateFrequency();
						}
						docFreq.put(term, documentFreq);
					}
					else if (termFreq > 0) {
						documentFreq.updateFrequency();
					}
				}
			}
		}
		
		
		for (Map.Entry<Integer, Document> entry : documents.entrySet()) {
			Document document = entry.getValue();
			documentParser.setDocument(document);
			
			Set<String> list = document.getTerms();
			for (String term : list) {
				position = documentParser.stemTerm(term);
				ArrayList<Post> postList = new ArrayList<Post>();
				if(postingsList.containsKey(term)) {
					postList = postingsList.get(term);	
				}
				else {
					postList = new ArrayList<Post>();	
				}
				postList.add(new Post(document.getID(),document.getFrequency(term), position));
				Collections.sort(postList, new SortbyFreq()); 
				//System.out.println(term +  "  " + docFreq.get(term));
				postingsList.put(term, postList);
			}
		}
		
		try {
			PrintWriter writer = new PrintWriter("postings.txt");
			PrintWriter writer2 = new PrintWriter("dictionary.txt");
			writeDictionary(docFreq, writer2);
			writePost(postingsList, docFreq, writer);
			writer.close();
			writer2.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("Terms : \t" + dictionary.size());
		System.out.println("Docs : \t" + documents.size());
		
		
	}
}

class SortbyFreq implements Comparator<Post> 
{ 
    // Used for sorting in ascending order of 
    public int compare(Post a, Post b) 
    { 
        return a.getFreq() - b.getFreq(); 
    } 
} 

class ReadCacm {



	Boolean isFive = true;
	public ReadCacm() {}
	public int splitID(String line) {
		String[] split = line.split("\\s+");
		String id = split[1];
		return Integer.parseInt(id);
	}
	
	public int parseCitation(String line) {
		String[] split = line.split("\\s+");
		if (split[1].equals("5")) {
			isFive = true;
		}
		else {
			isFive = false;
		}
		return Integer.parseInt(split[0]);
	}
	
	public Map<Integer, Document> extractDocuments(String fileName) throws ParseException {
		Map<Integer, Document> documents = new TreeMap<Integer, Document>();
		try {
			int documentID = 0;
			File file = new File(fileName);		
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
	
			while (line != null) {
				if (line.startsWith(".I")) {
					documentID = splitID(line);
					Document doc = new Document(documentID);
					documents.put(documentID, doc);
					line = reader.readLine();
				}
				else if (line.equals(".T")) {
					line = reader.readLine();
					StringBuilder strBuilder = new StringBuilder();
					while (line != null && !(line.startsWith(".I") || line.equals(".T") || line.equals(".W") ||line.equals(".B") || line.equals(".A") || line.equals(".N") || line.equals(".K") || line.equals(".C") || line.equals(".X"))) {
						strBuilder.append(line + "\n");
						line = reader.readLine();
					}
					Document doc = documents.get(documentID);
					doc.setTitle(strBuilder.toString().trim());
				}	
				else if (line.equals(".W")) {
					line = reader.readLine();
					StringBuilder strbuilder2 = new StringBuilder();
					while (line != null && !(line.startsWith(".I") || line.equals(".T") || line.equals(".W") || line.equals(".B") || line.equals(".A") || line.equals(".N") || line.equals(".K") || line.equals(".C") || line.equals(".X") )) {
						strbuilder2.append(line + "\n");
						line = reader.readLine();
					}
					Document doc = documents.get(documentID);
					doc.setAbstract(strbuilder2.toString());
				}	
				else if (line.startsWith(".B")) {
					line = reader.readLine();
					StringBuilder builder = new StringBuilder();
					while (line != null && !(line.startsWith(".I") || line.equals(".T") || line.equals(".W") || line.equals(".B") || line.equals(".A") || line.equals(".N") || line.equals(".K") || line.equals(".C") || line.equals(".X") )) {
						builder.append(line + "\n");
						line = reader.readLine();
					}
					Document doc = documents.get(documentID);
					String dateString = builder.toString().substring(5).trim();
					dateString = dateString.replaceAll(" ", "");
					dateString = dateString.replaceAll(",", "");
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("MMMMy");
						Date date = dateFormat.parse(dateString);
						doc.setPublicationDate(date);
					}
					catch (ParseException e) {
						SimpleDateFormat dateFormat = new SimpleDateFormat("y");
						Date date = dateFormat.parse(dateString);
						doc.setPublicationDate(date);
					}
				}
				else if (line.equals(".A")) {
					line = reader.readLine();
					StringBuilder builder = new StringBuilder();
					while (line != null && !(line.startsWith(".I") || line.equals(".T") || line.equals(".W") || line.equals(".B") || line.equals(".A") || line.equals(".N") || line.equals(".K") || line.equals(".C") || line.equals(".X") )) {
						builder.append(line + "\n");
						line = reader.readLine();
					}
					Document doc = documents.get(documentID);
					doc.setAuthor(builder.toString().trim());
				}	
				
				else if (line.startsWith(".X")) {
					Document doc = documents.get(documentID);
					Set<Integer> citations = new TreeSet<Integer>();
					line = reader.readLine();					
					while (line != null && !(line.startsWith(".I") || line.equals(".T") || line.equals(".W") || line.equals(".B") || line.equals(".A") || line.equals(".N") || line.equals(".K") || line.equals(".C") || line.equals(".K") || line.equals(".X")  )) {
						int citation = parseCitation(line);
						if (isFive) {
							citations.add(citation);
						}
						line = reader.readLine();
					}
					doc.setCitationSet(citations);
				}
				
				else {
					line = reader.readLine();
				}
			}
			reader.close();
		} catch (IOException e ) {
			System.out.println( fileName + " cannot be found in the directory. Please add the files so that we dont fail.");
		} 

		return documents;
	}

	
}


