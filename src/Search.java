import java.text.DecimalFormat;
import java.util.*;


public class Search {

	private static StemDoc readDoc;
	public static void main(String[] args) {	
		Functions methods = new Functions();
		Scanner scanner = new Scanner(System.in);
		System.out.println("Stopwords? y/n");
		String input = scanner.nextLine();

		while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
			System.out.println("Please enter y/n");
			input = scanner.nextLine();
		}
		if (input.equalsIgnoreCase("y")) {
			methods.use_Stop = true;
		}
		else if (input.equalsIgnoreCase("n")) {
			methods.use_Stop = false;
		}

		System.out.println("Stemming? y/n");
		input = scanner.nextLine();
		while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
			System.out.println("Please enter y/n");
			input = scanner.nextLine();
		}		
		if (input.equalsIgnoreCase("y")) {
			methods.use_Stem = true;
		}
		else if (input.equalsIgnoreCase("n")) {
			methods.use_Stem = false;
		}


		long startTime = System.currentTimeMillis();

		Invert invertedIndex = new Invert(methods.use_Stop,methods.use_Stem);		
		invertedIndex.invert();

		methods.stopwords = invertedIndex.getStopWords();
		methods.documents = invertedIndex.getDocuments();
		readDoc = new StemDoc();
		methods.docFreq = invertedIndex.getDocFreq();

		methods.setWeights(methods.documents, methods.docFreq);
		methods.setPageRank("S");

		long totalTime = System.currentTimeMillis() - startTime;

		System.out.println("Total calculation time: " + totalTime + "ms"); 

		System.out.print("\n Please enter a query: ");
		for (input = scanner.nextLine(); !input.equalsIgnoreCase("ZZEND"); input = scanner.nextLine()) {

			System.out.print("w1: ");
			methods.w1 = Double.parseDouble(scanner.nextLine());
			System.out.print("w2: ");
			methods.w2 = Double.parseDouble(scanner.nextLine());
			while (methods.w1 + methods.w2 != 1) {
				System.out.println("Invalid Values, enter again: ");
				System.out.print("w1: ");
				methods.w1 = Double.parseDouble(scanner.nextLine());
				System.out.print("w2: ");
				methods.w2 = Double.parseDouble(scanner.nextLine());
			}
			Set<PageRankScore> relevanceScores = new TreeSet<PageRankScore>(Collections.reverseOrder());
			long queryStart = System.currentTimeMillis();
			for (Integer docID : methods.documents.keySet()) {
				PageRankScore rs = new PageRankScore(docID, methods.scoreS(methods.documents.get(docID),methods.vector(input,methods.use_Stem),methods.w1, methods.w2));	
				if (rs.getScore() > 0) {
					relevanceScores.add(rs);
				}
			}

			int j = 0;
			if (!relevanceScores.isEmpty()) {
				for (PageRankScore rs : relevanceScores) {
					if (j >= methods.topK) {
						break;
					}
					int docID = rs.getID();
					Document doc = methods.documents.get(docID);
					readDoc.setDocument(doc);
					DecimalFormat numberFormat = new DecimalFormat("#.0000");
					System.out.println("Rank: " +(j+1) + ": " + doc.getTitle().replaceAll("\n", " "));
					System.out.println(" Title: " + doc.getTitle().replaceAll("\n", " "));
					System.out.println(" Document ID: " + doc.getID());
					System.out.println(" Author: " + doc.getAuthor().replaceAll("\n",", "));
					System.out.println(" Relevance score:\t" + rs.getScore().doubleValue());
					System.out.println(" PageRank:\t\t" +  doc.getPageRank());
					j += 1;
				}
				if (j < methods.topK) {
					System.out.println("\n No relavent docs");
				}
			}
			else {
				System.out.println("No results for query");
			}
			long queryEnd = System.currentTimeMillis() - queryStart;
			System.out.println("\n Query Search Time  " + (queryEnd) + " ms");
			System.out.print("\nPlease enter a query: ");
		}
		scanner.close();
		System.out.println("ZZEND");
	}
}
