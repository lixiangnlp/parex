import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Vacuum {

	public static void main(String[] args) throws IOException {

		// Usage
		if (args.length < 2) {
			System.err.println("Vacuum paraphrase table");
			System.err.println();
			System.err
					.println("Usage: java -cp parex-*.jar Vacuum <minProb> <phrasetable.gz> <new-phrasetable.gz>");
			System.exit(0);
		}

		// Args
		double minProb = Double.parseDouble(args[0]);
		String inPT = args[1];
		String outPT = args[2];

		// Vacuum
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new GZIPInputStream((new File(inPT)).toURI().toURL()
						.openStream())));
		PrintWriter out = new PrintWriter(new GZIPOutputStream(
				new FileOutputStream(outPT)));
		String line;
		int i = 0;
		while ((line = in.readLine()) != null) {
			i++;
			if (i % 1000000 == 0)
				System.err.println(i);
			String[] entry = line.split("\\|\\|\\|");
			double prob = Double.parseDouble(entry[2].trim());
			if (prob >= minProb)
				out.println(line);
		}
		in.close();
		out.close();
	}
}
