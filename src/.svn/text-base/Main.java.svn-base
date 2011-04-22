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
		System.out.println("Step 1: building common word lists");
		System.out.println("+ foreign");
		String fCommon = Util.findCommonWords(fCorpus, prefix + ".f", minRF);
		System.out.println("+ native");
		String nCommon = Util.findCommonWords(nCorpus, prefix + ".n", minRF);

		// Step 2: Extracting paraphrases
		System.out.println("Step 2: extracting paraphrases");
		String fRaw = prefix + ".f.raw.gz";
		String nRaw = prefix + ".n.raw.gz";
		System.out.println("+ foreign");
		ParaphraseExtractor.extractParaphrases(fTgtCorpus, pt, fCommon,
				nCommon, fRaw, ParaphraseExtractor.FOREIGN, minPhraseProb,
				symbols);
		System.out.println("+ native");
		ParaphraseExtractor.extractParaphrases(nTgtCorpus, pt, fCommon,
				nCommon, nRaw, ParaphraseExtractor.NATIVE, minPhraseProb,
				symbols);

		// Step 3: Group paraphrases
		System.out.println("Step 3: grouping paraphrases");
		String fGroup = prefix + ".f.grp.gz";
		String nGroup = prefix + ".n.grp.gz";
		System.out.println("+ foreign");
		Util.groupParaphrases(fRaw, fGroup);
		System.out.println("+ native");
		Util.groupParaphrases(nRaw, nGroup);

		// Step 4: Combine paraphrases
		System.out.println("Step 4: combining paraphrases");
		String fPar = prefix + ".f.par.gz";
		String nPar = prefix + ".n.par.gz";
		System.out.println("+ foreign");
		Util.combineParaphrases(fGroup, fPar);
		System.out.println("+ native");
		Util.combineParaphrases(nGroup, nPar);
	}

	public static void printUsage() {
		DecimalFormat df = new DecimalFormat("0.##########");
		System.out.println("Paraphrase Extractor");
		System.out.println();
		System.out
				.println("Usage: java -XX:+UseCompressedOops -Xmx12G -jar parex-*.jar <fCorpus> <nCorpus> <pt.gz> <fTgtCorpus> <nTgtCorpus> <outPrefix> [minTP] [minRF] [symbols]");
		System.out.println();
		System.out.println("Args:");
		System.out.println("<fCorpus> foreign corpus");
		System.out.println("<nCorpus> native corpus");
		System.out.println("<pt.gz> phrasetable built from corpora (gzipped)");
		System.out.println("<fTgtCorpus> foreign target corpus to paraphrase");
		System.out.println("<nTgtCorpus> native target corpus to paraphrase");
		System.out.println("<outPrefix> prefix for output files");
		System.out.println("[minTP] (default "
				+ df.format(ParaphraseExtractor.MIN_TRANS_PROB)
				+ ") minimum paraphrase translation probability");
		System.out.println("[minRF] (default " + df.format(Util.MIN_REL_FREQ)
				+ ") minimum word relative frequency for common word list");
		System.out.println("[symbols] string of symbols to filter");
		System.out.println();
		System.out
				.println("To merge paraphrase tables, use: java -cp parex-*.jar MergeParaphraseTables");
		System.out
				.println("To filter final tables, use: java -cp parex-*.jar Vacuum");
	}
}
