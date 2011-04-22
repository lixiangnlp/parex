import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

class WeightedParaphrase {
	int[] ref;
	int[] par;
	double prob;
	int wc;

	public WeightedParaphrase(int[] ref, int[] par, double prob, int wc) {
		this.ref = ref;
		this.par = par;
		this.prob = prob;
		this.wc = wc;
	}

}

public class MergeParaphraseTables {

	// Similar to Util.groupParaphrases, adds word counts, reads multiple files
	public static void mergeAndWeightParaphrases(String groupFile, String[] args)
			throws IOException {

		// Output file
		File sortFile = new File(groupFile);

		// For dictionary
		final PhraseTable pt = new PhraseTable();

		ArrayList<WeightedParaphrase> paraphrases = new ArrayList<WeightedParaphrase>();

		for (int i = 1; i < args.length; i += 2) {
			System.out.println(args[i]);
			URL rawFile = (new File(args[i])).toURI().toURL();
			int wc = Integer.parseInt(args[i + 1]);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(rawFile.openStream())));
			String line;
			int j = 0;
			while ((line = in.readLine()) != null) {
				j++;
				if (j % 1000000 == 0)
					System.out.println(j);
				String[] entry = line.split("\\|\\|\\|");
				String ref = entry[0].trim();
				String par = entry[1].trim();
				double prob = Double.parseDouble(entry[2].trim());
				paraphrases.add(new WeightedParaphrase(pt.mapPhrase(ref), pt
						.mapPhrase(par), prob, wc));
			}
			in.close();
		}

		// Sort
		System.out.println("sorting");
		Collections.sort(paraphrases, new Comparator<WeightedParaphrase>() {
			public int compare(WeightedParaphrase p1, WeightedParaphrase p2) {
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
		for (WeightedParaphrase p : paraphrases)
			out.println(pt.unmapPhrase(p.ref) + " ||| " + pt.unmapPhrase(p.par)
					+ " ||| " + p.prob + " ||| " + p.wc);
		out.close();
	}

	public static void combineMergedParaphrases(String groupedFile,
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
		int curWC = 0;
		String line;
		while ((line = in.readLine()) != null) {
			String[] entry = line.split("\\|\\|\\|");
			String ref = entry[0].trim();
			String par = entry[1].trim();
			double prob = Double.parseDouble(entry[2].trim());
			int wc = Integer.parseInt(entry[3].trim());
			if (!ref.equals(curRef) || !par.equals(curPar)) {
				if (!curRef.equals(""))
					out.println(curRef + " ||| " + curPar + " ||| "
							+ (curProb / curWC));
				curRef = ref;
				curPar = par;
				curProb = 0;
				curWC = 0;
			}
			curProb += prob * wc;
			curWC += wc;
		}
		if (!curRef.equals(""))
			out
					.println(curRef + " ||| " + curPar + " ||| "
							+ (curProb / curWC));

		in.close();
		out.close();
	}

	public static void main(String[] args) throws IOException {

		if (args.length < 5) {
			System.out.println("Paraphrase table merger");
			System.out.println();
			System.out
					.println("Usage: java -XX:+UseCompressedOops -Xmx12G -cp parex-*.jar MergeParaphraseTables <outPrefix> <par1.gz> <wc1> <par2.gz> <wc2> [par3.gz wc3 ...]");
			System.out.println();
			System.out.println("parN.gz: paraphrase table");
			System.out.println("wcN:     word count of corpus");
			System.exit(0);
		}

		System.out.println("Step 1: grouping and weighting paraphrases");
		String groupFile = args[0] + ".mrg.grp.gz";
		mergeAndWeightParaphrases(groupFile, args);

		System.out.println("Step 2: merging paraphrases");
		String mergedFile = args[0] + ".mrg.par.gz";
		combineMergedParaphrases(groupFile, mergedFile);

	}
}
