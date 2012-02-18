import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class Paraphrase {
	int[] ref;
	int[] par;
	double prob;

	public Paraphrase(int[] ref, int[] par, double prob) {
		this.ref = ref;
		this.par = par;
		this.prob = prob;
	}
}

public class Util {

	public static final double MIN_REL_FREQ = 0.001;

	public static String findCommonWords(String corpus, String prefix,
			double minRF) throws IOException {

		// Count words in corpus
		Hashtable<String, Integer> wc = new Hashtable<String, Integer>();
		int total = 0;
		BufferedReader in = new BufferedReader(new FileReader(corpus));
		String line;
		while ((line = in.readLine()) != null) {
			StringTokenizer tok = new StringTokenizer(line);
			String word = tok.nextToken();
			Integer i = wc.get(word);
			if (i == null)
				i = 0;
			wc.put(word, i + 1);
			total++;
		}
		in.close();

		// Write out common words
		String common = prefix + ".common";
		PrintWriter out = new PrintWriter(common);
		Enumeration<String> e = wc.keys();
		while (e.hasMoreElements()) {
			String word = e.nextElement();
			double rf = ((double) wc.get(word)) / total;
			if (rf > minRF)
				out.println(word);
		}
		out.close();

		// Return common word list location
		return common;

	}

	public static void groupParaphrases(String paraphraseFile,
			String groupedFile) throws IOException {

		URL rawFile = (new File(paraphraseFile)).toURI().toURL();
		File sortFile = new File(groupedFile);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(rawFile.openStream())));

		// For dictionary
		final PhraseTable pt = new PhraseTable();

		ArrayList<Paraphrase> paraphrases = new ArrayList<Paraphrase>();

		String line;
		int i = 0;
		while ((line = in.readLine()) != null) {
			i++;
			if (i % 1000000 == 0)
				System.err.println(i);
			String[] entry = line.split("\\|\\|\\|");
			String ref = entry[0].trim();
			String par = entry[1].trim();
			double prob = Double.parseDouble(entry[3].trim());
			paraphrases.add(new Paraphrase(pt.mapPhrase(ref),
					pt.mapPhrase(par), prob));
		}
		in.close();

		// Sort
		Collections.sort(paraphrases, new Comparator<Paraphrase>() {
			public int compare(Paraphrase p1, Paraphrase p2) {
				// First unmap and compare refs
				int diff = pt.unmapPhrase(p1.ref).compareTo(
						pt.unmapPhrase(p2.ref));
				// If not equal, return diff
				if (diff != 0)
					return diff;
				// Else compare paraphrases
				return pt.unmapPhrase(p1.par).compareTo(pt.unmapPhrase(p2.par));
			}
		});

		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(sortFile)));
		for (Paraphrase p : paraphrases)
			out.println(pt.unmapPhrase(p.ref) + " ||| " + pt.unmapPhrase(p.par)
					+ " ||| " + p.prob);
		out.close();
	}

	public static void combineParaphrases(String groupedFile,
			String finalParaphraseFile) throws IOException {

		URL sortFile = (new File(groupedFile)).toURI().toURL();
		File finalFile = new File(finalParaphraseFile);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(sortFile.openStream())));
		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(finalFile)));

		String curRef = "";
		String curPar = "";
		double curProb = 0;
		String line;
		while ((line = in.readLine()) != null) {
			String[] entry = line.split("\\|\\|\\|");
			String ref = entry[0].trim();
			String par = entry[1].trim();
			double prob = Double.parseDouble(entry[2].trim());
			if (!ref.equals(curRef) || !par.equals(curPar)) {
				if (!curRef.equals(""))
					out.println(curRef + " ||| " + curPar + " ||| " + curProb);
				curRef = ref;
				curPar = par;
				curProb = 0;
			}
			curProb += prob;
		}
		if (!curRef.equals(""))
			out.println(curRef + " ||| " + curPar + " ||| " + curProb);

		in.close();
		out.close();
	}
}