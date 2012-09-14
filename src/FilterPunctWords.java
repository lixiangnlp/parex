import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class FilterPunctWords {
	public static void main(String[] args) throws IOException {
		// Usage
		if (args.length < 2) {
			System.err.println("Throw out words with punctuation only");
			System.err.println();
			System.err
					.println("Usage: java -cp parex-*.jar FilterPunctWords <raw.gz> <raw-filtered.gz> [symbols]");
			System.exit(0);
		}

		// Args
		String inP = args[0];
		String outP = args[1];

		String symbols = ParaphraseExtractor.SYMBOLS;
		if (args.length > 2) {
			symbols = args[3];
		}

		HashSet<Character> punct = new HashSet<Character>();
		for (int i = 0; i < symbols.length(); i++) {
			punct.add(symbols.charAt(i));
		}

		// Filter
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream((new File(inP)).toURI().toURL()
						.openStream())));
		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(outP)));
		String line;
		int i = 0;
		while ((line = in.readLine()) != null) {
			i++;
			if (i % 1000000 == 0)
				System.err.println(i);
			String[] entry = line.split("\\|\\|\\|");
			// Check each word in each field
			boolean clean = true;
			for (int j = 0; j < 3; j++) {
				StringTokenizer tok = new StringTokenizer(entry[j]);
				while (tok.hasMoreTokens()) {
					String t = tok.nextToken();
					boolean punctWord = true;
					// Need at least one non-punct token in each word
					for (int k = 0; k < t.length(); k++) {
						if (!punct.contains(t.charAt(k))) {
							punctWord = false;
							break;
						}
					}
					if (punctWord) {
						clean = false;
						break;
					}
				}
				if (!clean) {
					break;
				}
			}
			if (clean) {
				out.println(line);
			}
		}
		in.close();
		out.close();

	}
}
