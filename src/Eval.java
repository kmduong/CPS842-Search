import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
public class Eval {

	public static void writeOutput(String text, FileWriter file) throws IOException {
		System.out.print(text);
		file.write(text);
	}
	
	public static void main(String[] args) throws Exception {
		Functions methods = new Functions();
		Scanner user = new Scanner(System.in);
		System.out.println("Stop words? y/n");
		String input = user.nextLine();

		while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
			System.out.println("Please enter y/n");
			input = user.nextLine();
		}
		if (input.equalsIgnoreCase("y")) {
			methods.use_Stop = true;
		}
		else if (input.equalsIgnoreCase("n")) {
			methods.use_Stop = false;
		}

		System.out.println("Stemming? y/n");
		input = user.nextLine();
		while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
			System.out.println("Please enter y/n");
			input = user.nextLine();
		}		
		if (input.equalsIgnoreCase("y")) {
			methods.use_Stem = true;
		}
		else if (input.equalsIgnoreCase("n")) {
			methods.use_Stem = false;
		}

		BufferedReader reader = new BufferedReader(new FileReader(new File(methods.qFile)));
		Set<Double> map‚“ = new HashSet<Double>();
		Set<Double> rPrecision‚“ = new HashSet<Double>();
		LinkedList<EvalQuery> queries = new LinkedList<EvalQuery>();
		EvalQuery query = null;
		String line = "";
		File evalFile = new File(methods.evalOutput);
		FileWriter writer = new FileWriter(evalFile);
		
		line = reader.readLine();
		while (line != null) {
			if ((line = line.trim()).isEmpty()) {
				continue;
			}
			if (line.startsWith(".I")) {
				if (query != null) {
					queries.add(query);
				}
				query = new EvalQuery();
				String[] split = line.split("\\s+");
				String id = split[1];
				query.setId(Integer.parseInt(id));
				line = reader.readLine();
			}
			else if (line.equals(".W")) {
				line = reader.readLine();
				while (line != null && !(line.equals(".N") || line.equals(".A") || line.startsWith(".I"))) {
					query.addQuery(line);
					line = reader.readLine();
				}
			}
			else if (line.equals(".N")) {
				line = reader.readLine();
				while (line != null && !(line.equals(".W") || line.equals(".A") || line.startsWith(".I"))) {
					query.addSource(line);
					line = reader.readLine();
				}
			}
			else {
				line = reader.readLine();
			}
			
		}
		
		
		reader.close();

		Map<Integer,Set<Integer>> relevant = methods.relevantQueries(methods.qrFile);

		long timeStart = System.currentTimeMillis();

		Invert invertedIndex = new Invert(methods.use_Stop,methods.use_Stem);		
		invertedIndex.invert();

		methods.stopwords = invertedIndex.getStopWords();
		methods.documents = invertedIndex.getDocuments();
		methods.docFreq = invertedIndex.getDocFreq();

		methods.setWeights(methods.documents, methods.docFreq);
		methods.setPageRank("E");

		long timeStop = System.currentTimeMillis() - timeStart;

		System.out.println("Time took to process: " + timeStop + "ms");
		
		System.out.print("w1: ");
		methods.w1 = Double.parseDouble(user.nextLine());
		System.out.print("w2: ");
		methods.w2 = Double.parseDouble(user.nextLine());
		while (methods.w1 + methods.w2 != 1) {
			System.out.println("Invalid Values, enter again: ");
			System.out.print("w1: ");
			methods.w1 = Double.parseDouble(user.nextLine());
			System.out.print("w2: ");
			methods.w2 = Double.parseDouble(user.nextLine());
		}

		

		
		
		long qtimeStart = System.currentTimeMillis();
		double averageRP = 0;
		double averageMAP = 0;
		
		for (int queryNo : relevant.keySet()) {
			Set<PageRankScore> prScores = new TreeSet<PageRankScore>(Collections.reverseOrder());
			String currentQuery = queries.get(queryNo-1).getQuery();
			for (int currentDoc : methods.documents.keySet()) {
				PageRankScore pageRankScore = new PageRankScore(currentDoc, methods.scoreE(methods.documents.get(currentDoc), methods.vector(currentQuery,methods.use_Stem), methods.w1, methods.w2));	
				if (pageRankScore.getScore() > 0) {
					prScores.add(pageRankScore);
				}
			}
			Set<Integer> relevantDocs = relevant.get(queryNo);
			int relevantTotal = relevantDocs.size();
			double j = 1;
			double relevantDocNum = 0;
			double mapValue = 0;
			if (!prScores.isEmpty()) {
				for (PageRankScore rs : prScores) {
					if (j > methods.topK) {
						break;
					}
					int docID = rs.getID();
					if (relevantDocs.contains(docID)) {
						if (j <= relevantTotal) {
							relevantDocNum++;
						}
						mapValue += relevantDocNum / j;
					}
					j += 1;
				}
				double rPrecision = relevantDocNum / relevantTotal;
				rPrecision‚“.add(rPrecision);
				averageRP += rPrecision;
				double totalMAP = mapValue / relevantTotal;
				map‚“.add(totalMAP);
				averageMAP += totalMAP;
				
				writeOutput("Query Number: " + queryNo + "\n", writer);
				writeOutput(" Relevant documents retrieved in search: " + relevantDocNum + "\n", writer);
				writeOutput(" Relevant documents in query: " + relevantTotal + "\n", writer);
				writeOutput(" MAP: " + totalMAP + "\n", writer);
				writeOutput(" R-Precision: " + rPrecision + "\n", writer);
				
			}
			else {
				System.out.println("No match for query " + queryNo + ": " + currentQuery);
			}
		}
		user.close();
		long queryEnd = System.currentTimeMillis() - qtimeStart;
		writeOutput("Average MAP Value over "+ queries.size() + " queries = " + (averageMAP/map‚“.size()) + "\n", writer);
		writeOutput("Average R-Precision over " + queries.size() + " queries = " + (averageRP/rPrecision‚“.size()) + "\n", writer);
		writeOutput("\nTime for MAP and R-precision: " + (queryEnd) + " ms\n", writer);
		writer.close();
	}
}

class EvalQuery {

	private int id;
	private String source;
	private String query;

	public EvalQuery() {
		this.query =
		this.source = "";
		this.id = -1;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void addQuery(String query) {
		this.query += (this.query.isEmpty() ? "" : " ") + query;
	}

	public void addSource(String source) {
		this.source += (this.source.isEmpty() ? "" : " ") + source;
	}

	@Override
	public String toString() {
		StringBuilder strbuf = new StringBuilder();
		strbuf.append("QueryID: ").append(id).
			append("\nQuery: ").append(query).
			append("\nSource: ").append(source);
		return strbuf.toString();
	}
}
