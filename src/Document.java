import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class Document {

	private int docID;
	private String title;
	private String content;
	private String author;
	private Date pubDate;
	private Set<Integer> citations;
	private Map<String, Integer> termFreq;
	private Map<String, Double> tfidf;
	private double pRank;

	public Document()
	{

	}

	public Document(int docID) {
		this.docID = docID;
	}

	public int getID() {
		return docID;
	}

	public void setID(int docID) {
		this.docID = docID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAbstract() {
		if (content != null) {
			return content;
		}
		else {
			return "";
		}
	}

	public void setAbstract(String abs) {
		content = abs;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthor() {
		if (author != null) {
			return author;
		}
		else {
			return "";
		}
	}
	
	public Integer getFrequency(String term) {
		if (termFreq != null && termFreq.containsKey(term)) {
			return termFreq.get(term);
		}
		else {
			return 0;
		}
	}
	
	public void setFrequency(String term, Integer frequency) {
		if (termFreq == null) {
			termFreq = new HashMap<String,Integer>();
		}
		termFreq.put(term, frequency);
	}
	
	public Set<String> getTerms() {
		if (termFreq != null) {
		return termFreq.keySet();
		}
		else {
			return new HashSet<String>();
		}
	}
	
	public Double getWeight(String term) {
		if (tfidf != null && tfidf.containsKey(term)) {
			return tfidf.get(term);
		}
		else {
			return 0.0;
		}
	}
	
	public void setWeight(String term, Double weight) {
		if (tfidf == null) {
			tfidf = new HashMap<String,Double>();
		}
		tfidf.put(term, weight);
	}
	
	public void setPublicationDate(Date d) {
		pubDate = d;
	}
	
	public Date getPublicationDate() {
		return pubDate;
	}
	
	public void setCitationSet(Set<Integer> s) {
		citations = s;
	}
	
	public Set<Integer> getCitations() {
		return citations;
	}
	
	public void addCitation(int citation) {
		citations.add(citation);
	}
	
	public void setPageRank(double rank){
		pRank = rank;
	}
	
	public double getPageRank() {
		return pRank;
	}
}

class Frequency {
	
	private int frequency;
		
	public Frequency() {
			frequency = 0;
	}
		
	public Frequency(int frequency) {
			this.frequency = frequency;
	}
		
	public void updateFrequency() {
			frequency += 1;
	}
		
	public int getDocFreq() {
			return frequency;
	}
	@Override
	public String toString() {
			return Integer.toString(frequency);
	}

}

class StemDoc {

	private Document document;
	private boolean use_stem;

	public StemDoc() {

	}

	public StemDoc(boolean stemmerOn) {
		this.use_stem = stemmerOn;
	}

	public void setDocument(Document doc) {
		this.document = doc;
	}

	public Set<String> findTerms() {
		Set<String> term_list = new TreeSet<String>();
		String title = document.getTitle();
		String content = document.getAbstract();
		String author = document.getAuthor();
		
		
		if (title != null) {
			String[] titleWords = title.split("\\W+");
			for (String term : titleWords) {
				if (term.matches("[a-zA-Z0-9]+")) {
					if (use_stem == true) {
						Stemmer stemmer = new Stemmer();
						stemmer.add(term.toLowerCase().toCharArray(), term.length());
						stemmer.stem();
						String stemmedTerm = stemmer.toString();
						term_list.add(stemmedTerm);

					}
					else {
						term_list.add(term.toLowerCase());
					}
				}
			}
		}
		if (content != null) {
			String[] words = content.split("\\W+");
			for (String term: words) {
				if (term.matches("[a-zA-Z0-9]+")) {
					if (use_stem == true) {
						Stemmer stem = new Stemmer();
						stem.add(term.toLowerCase().toCharArray(), term.length());
						stem.stem();
						String stemmed_str = stem.toString();
						term_list.add(stemmed_str.toLowerCase());
					}
					else {
						term_list.add(term.toLowerCase());
					}
				}
			}
		}
		if (author != null && !author.isEmpty()) {
			String[] words = author.split("\\W+");
			for (String term: words) {
				if (term.matches("[a-zA-Z]+")) {
					if (use_stem) {
						Stemmer stem = new Stemmer();
						stem.add(term.toLowerCase().toCharArray(), term.length());
						stem.stem();
						String stemmed_term = stem.toString();
						term_list.add(stemmed_term.toLowerCase());
					}
					else {
						term_list.add(term.toLowerCase());
					}
				}
			}
		}
		return term_list;
	}

	public int countFrequencies(String term) {
		int frequency = 0;
		if (document.getTitle() != null) {
			String[] content_words = document.getTitle().split("\\W+");			
			for (int i = 0; i < content_words.length; i++) {
				if (use_stem) {
					Stemmer stem = new Stemmer();
					stem.add(content_words[i].toLowerCase().toCharArray(), content_words[i].length());
					stem.stem();
					String stemmed_str = stem.toString();
					if (stemmed_str.equalsIgnoreCase(term))
						frequency++;
				}
				else 
					if (content_words[i].equalsIgnoreCase(term))
						frequency++;
				
			}
		}
		if (document.getAbstract() != null) {
			String[] content_words = document.getAbstract().split("\\W+");
			for (int i = 0; i < content_words.length; i++) {
				if (use_stem) {
					Stemmer stem = new Stemmer();
					stem.add(content_words[i].toLowerCase().toCharArray(), content_words[i].length());
					stem.stem();
					String stemmed_str = stem.toString();
					if (stemmed_str.equalsIgnoreCase(term)) 
						frequency++;
				}
				else 
					if (content_words[i].equalsIgnoreCase(term)) 
						frequency++;
			}
		}
		if (document.getAuthor() != null) {
			String[] words = document.getAuthor().split("\\W+");
			for (int i = 0; i < words.length; i++) {
				if (use_stem) {
					Stemmer stemmer = new Stemmer();
					stemmer.add(words[i].toLowerCase().toCharArray(), words[i].length());
					stemmer.stem();
					String stemmedTerm = stemmer.toString();
					if (stemmedTerm.equalsIgnoreCase(term)) 
						frequency++;
				}
				else {
					if (words[i].equalsIgnoreCase(term)) 
						frequency++;
				}
			}
		}
		return frequency;
	}

	public Set<Integer> stemTerm(String term) {
		Set<Integer> positions = new TreeSet<Integer>();
		String string_doc = document.getTitle();
		if (document.getAbstract() != null)
			string_doc = string_doc + document.getAbstract();
		String[] words = string_doc.split("\\W+");
		for (int i = 0; i < words.length; i++) {		
			Stemmer stemmer = new Stemmer();
			stemmer.add(words[i].toLowerCase().toCharArray(), words[i].length());
			stemmer.stem();
			String stemmedTerm = stemmer.toString();
			if (stemmedTerm.equalsIgnoreCase(term)) {
				positions.add(i);
			}
		}
		return positions;
	}

}


class Post {
	
	int document_id;

	int document_freq;
	Set<Integer> positions;
	public Post()
	{
		document_id= 0;
		document_freq= 0;
		positions = null;
	}
	public Post(int docID,int docFreq,Set<Integer> positions)
	{
		this.document_id=docID;
		this.document_freq=docFreq;
		this.positions=positions;
	}

	public int getID() {
		return  document_id;
	}
	
	public int getFreq() {
		return document_freq;
	}
	@Override
	public String toString() {
        String result="";
            for (Integer s :positions ) {
            	result = result + s.toString()+" ";
            }
     return  Integer.toString(document_id) +  " " +Integer.toString(document_freq) + " "+ result ;
    }
}

class PageRankScore implements Comparable<PageRankScore> {
	
	private int document_id;
	private Double prScore;
	
	public PageRankScore(int docID, double score) {
		this.document_id = docID;
		this.prScore = score;
	}
	
	public int getID(){
		return document_id;
	}
	
	public Double getScore() {
		return prScore;
	}
	
	public int compareTo(PageRankScore arg0) {
		return prScore.compareTo(arg0.getScore());
	}
}


