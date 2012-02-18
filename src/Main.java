import java.text.DecimalFormat;

/**
 * Main class for paraphrase extraction
 * 
 */

public class Main {
	public static void main(String[] args) throws Exception {

		if (args.length < 6) {
			printUsage();
			System.exit(0);
		}

		// Corpus
		String fCorpus = args[0];
		String nCorpus = args[1];
		// Phrase table
		String pt = args[2];
		// Target corpus
		String fTgtCorpus = args[3];

		String nTgtCorpus = args[4];
		// Prefix
		String prefix = args[5];
		// Minimum translation prob
		double minPhraseProb = ParaphraseExtractor.MIN_TRANS_PROB;
		if (args.length > 6)
			minPhraseProb = Double.parseDouble(args[6]);
		// Minimum word relative frequency
		double minRF = Util.MIN_REL_FREQ;
		if (args.length > 7)
			minRF = Double.parseDouble(args[7]);
		// Symbol String
		String symbols = ParaphraseExtractor.SYMBOLS;
		if (args.length > 8)
			symbols = args[8];

		// Step 1: Find common words
		System.err.println("Step 1: building common word lists");
		System.err.println("+ foreign");
		String fCommon = Util.findCommonWords(fCorpus, prefix + ".f", minRF);
		System.err.println("+ native");
		String nCommon = Util.findCommonWords(nCorpus, prefix + ".n", minRF);

		// Step 2: Extracting paraphrases
		System.err.println("Step 2: extracting paraphrases");
		String fRaw = prefix + ".f.raw.gz";
		String nRaw = prefix + ".n.raw.gz";
		System.err.println("+ foreign");
		ParaphraseExtractor.extractParaphrases(fTgtCorpus, pt, fCommon,
				nCommon, fRaw, ParaphraseExtractor.FOREIGN, minPhraseProb,
				symbols);
		System.err.println("+ native");
		ParaphraseExtractor.extractParaphrases(nTgtCorpus, pt, fCommon,
				nCommon, nRaw, ParaphraseExtractor.NATIVE, minPhraseProb,
				symbols);

		// Step 3: Group paraphrases
		System.err.println("Step 3: grouping paraphrases");
		String fGroup = prefix + ".f.grp.gz";
		String nGroup = prefix + ".n.grp.gz";
		System.err.println("+ foreign");
		Util.groupParaphrases(fRaw, fGroup);
		System.err.println("+ native");
		Util.groupParaphrases(nRaw, nGroup);

		// Step 4: Combine paraphrases
		System.err.println("Step 4: combining paraphrases");
		String fPar = prefix + ".f.par.gz";
		String nPar = prefix + ".n.par.gz";
		System.err.println("+ foreign");
		Util.combineParaphrases(fGroup, fPar);
		System.err.println("+ native");
		Util.combineParaphrases(nGroup, nPar);
	}

	public static void printUsage() {
		DecimalFormat df = new DecimalFormat("0.##########");
		System.err.println("Paraphrase Extractor");
		System.err.println();
		System.err
				.println("Usage: java -XX:+UseCompressedOops -Xmx12G -jar parex-*.jar <fCorpus> <nCorpus> <pt.gz> <fTgtCorpus> <nTgtCorpus> <outPrefix> [minTP] [minRF] [symbols]");
		System.err.println();
		System.err.println("Args:");
		System.err.println("<fCorpus> foreign corpus");
		System.err.println("<nCorpus> native corpus");
		System.err.println("<pt.gz> phrasetable built from corpora (gzipped)");
		System.err.println("<fTgtCorpus> foreign target corpus to paraphrase");
		System.err.println("<nTgtCorpus> native target corpus to paraphrase");
		System.err.println("<outPrefix> prefix for output files");
		System.err.println("[minTP] (default "
				+ df.format(ParaphraseExtractor.MIN_TRANS_PROB)
				+ ") minimum paraphrase translation probability");
		System.err.println("[minRF] (default " + df.format(Util.MIN_REL_FREQ)
				+ ") minimum word relative frequency for common word list");
		System.err.println("[symbols] string of symbols to filter");
		System.err.println();
		System.err
				.println("To merge paraphrase tables, use: java -cp parex-*.jar MergeParaphraseTables");
		System.err
				.println("To filter final tables, use: java -cp parex-*.jar Vacuum");
	}
}
