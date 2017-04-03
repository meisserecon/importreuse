package com.meissereconomics.trade.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.meissereconomics.trade.graph.Country;
import com.meissereconomics.trade.graph.EFlowBendingMode;

public class USGraph extends InputOutputGraph {

	private static final String COUNTRY = "USA";
	private static final String REST = Country.ROW;
	private static final boolean MERGE_MODE = false;

	public USGraph() throws FileNotFoundException, IOException {
		this(new File("data/USA-after-pro-2007-" + (15) + ".csv"), false);
	}

	public USGraph(boolean large) throws FileNotFoundException, IOException {
		this(new File("data/USA-after-pro-2007-" + (large ? 389 : 71) + ".csv"), large);
	}

	public USGraph(File csvFile, boolean large) throws FileNotFoundException, IOException {
		super(2007, 2);
		try (CSVParser parser = new CSVParser(new FileReader(csvFile), CSVFormat.EXCEL.withDelimiter(';'))) {
			Iterator<CSVRecord> iter = parser.iterator();
			iter.next(); // header
			iter.next();
			iter.next();
			iter.next();
			ArrayList<String> codes = null;
			if (!large) {
				codes = toList(iter.next());
			}
			ArrayList<String> industries = toList(iter.next());
			if (large) {
				codes = toList(iter.next());
			}

			Iterator<CSVRecord> records = parser.iterator();
			while (records.hasNext() && parse(records.next().iterator(), industries, codes, large))
				;
		}
		for (Country c : getCountries()) {
			c.turnNegativeLinks();
		}
	}

	private boolean parse(Iterator<String> iter, ArrayList<String> industries, ArrayList<String> codes, boolean large) {
		String code = iter.next();
		String sourceIndustry = iter.next();
		String source = getNodeName(code, sourceIndustry);
		if (sourceIndustry.toLowerCase().contains("world adjustment") || sourceIndustry.startsWith("Total ") || sourceIndustry.equals("Sum of Intermediate Selected")) {
			return false;
		} else {
			Country c = getCountry(COUNTRY);
			int index = 0;
			double total = 0.0;
			boolean totals = false;
			double imports = 0.0;
			double exports = 0.0;
			while (iter.hasNext()) {
				String next = iter.next().replace(",", "");
				double millions = next.isEmpty() || next.equals("...") ? 0 : Double.parseDouble(next);
				String destIndustry = industries.get(index);
				String dest = getNodeName(codes.get(index), destIndustry);
				if (destIndustry.equals("Total Intermediate") || destIndustry.equals("Sum of Intermediate Selected")) {
					assert millions == 0.0 || Math.abs(total - millions) / millions < 0.01 || Math.abs(total - millions) <= 3;
					totals = true;
				} else if (totals) {
					if (destIndustry.equals("Imports of goods and services")) {
						imports = millions;
						if (millions <= 0) {
							Country other = getCountry(REST);
							other.getNode("imports").linkOrAdd(c.getNode(source), -millions);
						} else {
							// strange cases, see http://www.bea.gov/papers/pdf/IOmanual_092906.pdf chapter 7
						}
					} else if (destIndustry.equals("Exports of goods and services")) {
						exports = millions;
						Country other = getCountry(REST);
						c.getNode(source).linkOrAdd(other.getConsumptionNode(), millions);
					} else if (destIndustry.equals("Total Final Uses (GDP)")) {
						double consumption = millions - imports - exports;
						if (consumption > 0) {
							c.getNode(source).linkOrAdd(c.getConsumptionNode(), consumption);
						} else {
							// "local value creation" such as reduced inventories
						}
					}
				} else {
					c.getNode(source).linkOrAdd(c.getNode(dest), millions);
					total += millions;
				}
				index++;
			}
			return true;
		}
	}

	private static final String[] FOUR_LETTER_CODES = new String[] { "336", "541", "S00" };
	private static final String[] RENAME_FROM = new String[] { "5310HS", "531ORE", "23", "55", "81", "22", "61", "42", "513", "514", "487OS", "521CI", "GFGD", "GFGN", "GFE", "GSLG", "GSLE", "Used", "Other" };
	private static final String[] RENAME_TO = new String[] { "HS", "ORE", "230", "550", "811", "221", "611", "420", "515", "518", "492", "522", "S005", "S006", "S001", "S007", "S002", "S004", "S009" };

	private String getNodeName(String code, String sourceIndustry) {
		if (MERGE_MODE) {
			code = code.replace("\u00A0", "").trim();
			for (int i = 0; i < RENAME_FROM.length; i++) {
				if (code.equals(RENAME_FROM[i])) {
					return RENAME_TO[i];
				}
			}
			for (String s : FOUR_LETTER_CODES) {
				if (code.startsWith(s)) {
					return code.substring(0, 4);
				}
			}
			return code.length() > 3 ? code.substring(0, 3) : code; // merge first three digits
		} else {
			return sourceIndustry;
		}
	}

	protected ArrayList<String> toList(CSVRecord csvRecord) {
		ArrayList<String> industries = new ArrayList<String>();
		Iterator<String> fields = csvRecord.iterator();
		fields.next();
		fields.next();
		while (fields.hasNext()) {
			industries.add(fields.next().intern());
		}
		return industries;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		USGraph graph = new USGraph(true);
		Country usa = graph.getCountry(COUNTRY);
		graph.deriveOrigins(EFlowBendingMode.DEFAULT, 0.6);
		System.out.println(usa.getImportReuse());
	}

}
