import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Functions {

	final static String qFile = "query.text";
	final static String qrFile = "qrels.text";
	static final String evalOutput = "eval.txt";
	static final String prFile = "pageranks.txt";
	
 	static Map<Integer, Document> documents;
 	static Map<String,Frequency> docFreq;
 	static Set<String> stopwords  = new TreeSet<String>();;

	
	static boolean use_Stop = true;
 	boolean use_Stem = true;
 	
 	static final double commonVal = 0.85;
	static final double alp = (1 - commonVal);
	static int topK = 25;
 	static double w1 = 0;
 	static double w2 = 0;
	static double min = 0;
	static double max = 0;
	
	public static void setWeights(Map<Integer, Document> documents, Map<String,Frequency> docFreq) {
		for (int currentID : documents.keySet()) {
			Document document = documents.get(currentID);
			for (String term : document.getTerms()) {
				double termFreq = document.getFrequency(term);	
				int df = 0;
				
				termFreq = 1 + Math.log10(termFreq);
				Frequency doc_fr = docFreq.get(term);
				if (doc_fr != null) {
					df = doc_fr.getDocFreq();
				}
				double idf = Math.log10(documents.size() / df);
				if (Double.isNaN(idf)) {
					idf = 0.0;
				}
				document.setWeight(term, termFreq * idf);
			}
		}
	}
	
	public static Document vector(String input, boolean use_Stem) {
		Document relDoc = new Document();	
		relDoc.setAbstract(input);
		StemDoc queryStem = new StemDoc(use_Stem);
		queryStem.setDocument(relDoc);
		for (String term : queryStem.findTerms()) {
			if (!use_Stop || !stopwords.contains(term)) {
				int termFreq = queryStem.countFrequencies(term);
				double weight = Math.log10(termFreq)+ 1;
				relDoc.setFrequency(term, termFreq);
				relDoc.setWeight(term, weight);
			}
		}
		return relDoc;
	}
	
	public static double cossineSimilarity(Document doc, Document relDoc) {
		double result = 0;
		double docM = 0;
		double queryM = 0;
		Set<String> terms = new HashSet<String>();
		terms.addAll(doc.getTerms());
		terms.addAll(relDoc.getTerms());
		for(String term : terms) {			
			double docW = doc.getWeight(term);
			double queryW = relDoc.getWeight(term);
			result += (docW * queryW);
			docM += docW * docW;
			queryM += queryW * queryW;
		}
		docM = Math.sqrt(docM);
		queryM = Math.sqrt(queryM);
		double similarity = result / (docM * queryM);
		if (docM > 0 && queryM > 0) {
			return similarity;
		}
		else {
			return 0;
		}
	}
	static void pageRankS(double row[], double probab[][], int size){
		row[0] = 1.0;
		for (int i = 1; i <= 30; i++) {
			double sum = 0;
			double cp[] = new double[row.length];
			for (int ‚˜ = 0; ‚˜ < size; ‚˜++) {
				for (int ‚š = 0; ‚š < size; ‚š++) {
					sum += row[‚š]*probab[‚š][‚˜];
				}
				cp[‚˜] = sum;
				sum = 0;
			}
			row = cp;
		}
		double total = 0;
		min = row[0];
		max = row[0];
		File pageRank = new File(prFile);
		try {
			FileWriter writer = new FileWriter(pageRank);
			for (Integer docID : documents.keySet()) {
				if (row[docID-1] < min) {
					min = row[docID-1];
				}
				if (row[docID-1] > max) {
					max = row[docID-1];
				}
				total += row[docID-1];
				Document doc = documents.get(docID);
				doc.setPageRank(row[docID-1]);
				writer.write("\tDoc ID: " + docID + "\n");
				writer.write("PageRank Score: " + doc.getPageRank() + "\n");
		}
		writer.close();
		System.out.println("PageRank Total: " + total);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	static void pageRankE(double row[],double probab[][], int size){
		row[0] = 1.0;
		for (int i = 1; i <= 30; i++) {
			double sum = 0;
			double cp[] = new double[row.length];
			for (int x = 0; x < size; x++) {
				for (int z = 0; z < size; z++) {
					sum += row[z]*probab[z][x];
				}
				cp[x] = sum;
				sum = 0;
			}
			row = cp;
		}
		double total = 0;
		for (Integer docID : documents.keySet()) {
			Document doc = documents.get(docID);
			doc.setPageRank(row[docID-1]);
			total += row[docID-1];
		}
	
	}
	
	public static void setPageRank(String file) {
		Set<Integer> docList = documents.keySet();
		int size = docList.size();
		double prob[][] = new double[size][size];
		double row[] = new double[size];
		for (Integer currentID : docList) {
			row[currentID-1] = 0.0;
			double proba‚‚ = 0;
			Document doc = documents.get(currentID);
			Set<Integer> citeList = doc.getCitations();
			if (citeList != null || citeList.isEmpty()) {
				proba‚‚ = (1.0/citeList.size()) * (commonVal) + alp/size;
			}
			else {
				proba‚‚ = (commonVal)/size + alp/size;
			}
			for (int i = 0; i < size; i++) {
				if (citeList != null || citeList.isEmpty()) {
					if (citeList.contains(i+1)) {
						Document nextDoc = documents.get(i+1);
						if (doc.equals(nextDoc)) 
							prob[currentID-1][i] = proba‚‚;
						else {
							Date date = doc.getPublicationDate();
							Date nextDate = nextDoc.getPublicationDate();
							if (date != null && nextDate != null) {
								if (nextDoc.getPublicationDate().before(doc.getPublicationDate())) 
									prob[currentID-1][i] = proba‚‚;
								prob[currentID-1][i] = proba‚‚;
							}
							else {
								System.out.println("Date is Empty");
						}
					}
				}
				else {
					prob[currentID-1][i] = alp/size;
				}
			}
			else {
				prob[currentID-1][i] = proba‚‚;
			}
		}
	}
	if(file == "E") {
		pageRankE(row, prob, size);
	}
	if(file =="S"){
		pageRankS(row, prob, size);
		}
	}
	
	public static double scoreE(Document doc, Document query, double w1, double w2) {
		return w1 * cossineSimilarity(doc,query) + w2 * (doc.getPageRank() * 100);
	}
	public static double scoreS(Document doc, Document query, double w1, double w2) {
		double sim = w1 * cossineSimilarity(doc,query);
		double prScore = w2 * (doc.getPageRank());
		return sim + (prScore * 100);
	}
	
	public static Map<Integer, Set<Integer>> relevantQueries(String fileName) throws IOException,FileNotFoundException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
	
		Map<Integer, Set<Integer>> relevantList = new TreeMap<Integer, Set<Integer>>();
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if ((line = line.trim()).isEmpty()) {
				continue;
			}
			String[] split = line.split("\\s+");
			int query = Integer.parseInt(split[0]);
			int doc = Integer.parseInt(split[1]);
	
			Set<Integer> rDoc = relevantList.get(query);
			if (rDoc == null) {
				rDoc = new HashSet<Integer>();
			}
			rDoc.add(doc);
			relevantList.put(query, rDoc);
		}
		reader.close();
		return relevantList;
	}









































































}