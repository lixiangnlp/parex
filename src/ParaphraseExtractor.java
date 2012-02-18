import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ParaphraseExtractor {

	public static final int NATIVE = 0;
	public static final int FOREIGN = 1;

	public static final double MIN_TRANS_PROB = 0.001;
	public static final String SYMBOLS = "~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

	// Check if phrase is clean of symbols
	private static boolean isClean(String s, HashSet<Character> symbols) {
		for (int i = 0; i < s.length(); i++)
			if (symbols.contains(s.charAt(i)))
				return false;
		return true;
	}

	// Check if phrase contains at least one uncommon word
	private static boolean isUsable(int[] words, HashSet<Integer> commons) {
		for (int word : words)
			if (!commons.contains(word))
				return true;
		return false;
	}

	// Check if 2 word (int) arrays are equal
	private static boolean eqWords(int[] words1, int[] words2) {
		if (words1.length != words2.length)
			return false;
		for (int i = 0; i < words1.length; i++)
			if (words1[i] != words2[i])
				return false;
		return true;
	}

	public static void extractParaphrases(String targetCorpusFile,
			String phrasetableFile, String fCommonFile, String nCommonFile,
			String outFile, int direction, double minTransProb,
			String symbolString) throws IOException {

		// Table stores (pivot, reference, p(piv|ref))
		// used to trace paraphrases back to ref
		PhraseTable pt = new PhraseTable();

		Hashtable<Integer, Hashtable> corpus = new Hashtable<Integer, Hashtable>();
		HashSet<Character> symbols = new HashSet<Character>();
		for (int i = 0; i < symbolString.length(); i++)
			symbols.add(symbolString.charAt(i));

		// Load corpus
		System.err.println("Loading corpus");
		BufferedReader in = new BufferedReader(new FileReader(targetCorpusFile));
		String line;
		while ((line = in.readLine()) != null) {
			int[] words = pt.mapPhrase(line);
			// For each start index
			for (int i = 0; i < words.length; i++) {
				// Load the (n-i)-gram
				Hashtable<Integer, Hashtable> table = corpus;
				for (int j = i; j < words.length; j++) {
					if (!table.containsKey(words[j]))
						table.put(words[j], new Hashtable<Integer, Hashtable>());
					table = table.get(words[j]);
				}
			}
		}

		HashSet<Integer> fCommons = new HashSet<Integer>();
		HashSet<Integer> nCommons = new HashSet<Integer>();

		// Load common words
		System.err.println("Loading common words (foreign)");
		in = new BufferedReader(new FileReader(fCommonFile));
		while ((line = in.readLine()) != null) {
			fCommons.add(pt.mapWord(line));
		}
		System.err.println("Loading common words (native)");
		in = new BufferedReader(new FileReader(nCommonFile));
		while ((line = in.readLine()) != null) {
			nCommons.add(pt.mapWord(line));
		}

		URL ptFile = (new File(phrasetableFile)).toURI().toURL();
		in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				ptFile.openStream())));

		int lineCount = 0;
		int phraseCount = 0;
		System.err.println("Loading phrases");
		while ((line = in.readLine()) != null) {

			lineCount++;
			if (lineCount % 10000000 == 0)
				System.err.println(lineCount + " (" + phraseCount + ")");

			String[] part = line.split("\\|\\|\\|");

			// Foreign phrase 1
			String f1 = part[0].trim();
			// Native phrase 1
			String n1 = part[1].trim();
			// Probabilities
			StringTokenizer pTok = new StringTokenizer(part[2]);
			double pf1Gn1 = Double.parseDouble(pTok.nextToken());
			pTok.nextToken();
			double pn1Gf1 = Double.parseDouble(pTok.nextToken());

			// Original phrase and pivot
			String phrase1 = "";
			String pivot1 = "";
			double prob = 0;
			HashSet<Integer> p1commons = null;
			HashSet<Integer> piv1commons = null;
			if (direction == NATIVE) {
				phrase1 = n1;
				pivot1 = f1;
				prob = pf1Gn1;
				p1commons = nCommons;
				piv1commons = fCommons;
			} else {
				phrase1 = f1;
				pivot1 = n1;
				prob = pn1Gf1;
				p1commons = fCommons;
				piv1commons = nCommons;
			}

			// Skip if this will only make low scoring paraphrases
			if (prob < minTransProb)
				continue;

			// Vacuum phrases with symbols
			if (!isClean(phrase1, symbols) || !isClean(pivot1, symbols)) {
				continue;
			}

			int[] p1 = pt.mapPhrase(phrase1);
			int[] piv1 = pt.mapPhrase(pivot1);

			// Vacuum phrases with only common words
			if (!isUsable(p1, p1commons) || !isUsable(piv1, piv1commons)) {
				continue;
			}

			// Check if phrase1 (ref) in corpus
			boolean inCorpus = true;
			Hashtable<Integer, Hashtable> table = corpus;
			for (int word : p1) {
				if (!table.containsKey(word)) {
					inCorpus = false;
					break;
				}
				table = table.get(word);
			}
			// If not, skip entry
			if (!inCorpus)
				continue;

			// Otherwise store phrase entry
			pt.addPhrasePair(piv1, p1, prob);
			phraseCount++;
		}
		in.close();

		// For writing paraphrases
		PrintWriter nOut = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(new File(outFile))));

		// Second read, look for paraphrases
		in = new BufferedReader(new InputStreamReader(new GZIPInputStream(
				ptFile.openStream())));
		lineCount = 0;
		phraseCount = 0;
		System.err.println("Finding paraphrases");
		while ((line = in.readLine()) != null) {

			lineCount++;
			if (lineCount % 10000000 == 0)
				System.err.println(lineCount + " (" + phraseCount + ")");

			String[] part = line.split("\\|\\|\\|");

			// Foreign2
			String f2 = part[0].trim();
			// Native2
			String n2 = part[1].trim();
			// Probabilities
			StringTokenizer pTok = new StringTokenizer(part[2]);
			double pf2Gn2 = Double.parseDouble(pTok.nextToken());
			pTok.nextToken();
			double pn2Gf2 = Double.parseDouble(pTok.nextToken());

			// Paraphrase and pivot
			String phrase2 = "";
			String pivot2 = "";
			double prob = 0;
			HashSet<Integer> p2commons = null;
			HashSet<Integer> piv2commons = null;
			if (direction == NATIVE) {
				phrase2 = n2;
				pivot2 = f2;
				prob = pn2Gf2;
				p2commons = nCommons;
				piv2commons = fCommons;
			} else {
				phrase2 = f2;
				pivot2 = n2;
				prob = pf2Gn2;
				p2commons = fCommons;
				piv2commons = nCommons;
			}

			// Skip if this will only make low scoring paraphrases
			if (prob < minTransProb)
				continue;

			// Vacuum phrases with symbols
			if (!isClean(phrase2, symbols) || !isClean(pivot2, symbols))
				continue;

			int[] p2 = pt.mapPhrase(phrase2);
			int[] piv2 = pt.mapPhrase(pivot2);

			// Vacuum phrases with only common words
			if (!isUsable(p2, p2commons) || !isUsable(piv2, piv2commons)) {
				continue;
			}

			// For phrases p1 with piv1 == piv2
			for (PhraseTable.Phrase p1 : pt.getPhrases(piv2)) {
				// p1 != p2
				if (eqWords(p1.words, p2))
					continue;
				// p = p(phrase1|piv1) * p(phrase2|piv2)
				double parProb = p1.prob * prob;
				if (parProb < minTransProb)
					continue;
				// reference ||| paraphrase ||| pivot ||| prob
				nOut.println(p1.phrase + " ||| " + phrase2 + " ||| " + pivot2
						+ " ||| " + parProb);
				phraseCount++;
			}
		}
		in.close();
		nOut.close();
	}
}
