package com.meissereconomics.seminar.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.meissereconomics.seminar.Country;
import com.meissereconomics.seminar.util.Formatter;

public class WiodInputOutputGraph extends InputOutputGraph {

	public static final String[] COUNTRY_LIST = new String[]{"AUS", "AUT", "BEL", "BGR", "BRA", "CAN", "CHN", "CYP", "CZE", "DEU", "DNK", "ESP", "EST", "FIN", "FRA", "GBR", "GRC", "HUN", "IDN", "IND", "IRL", "ITA", "JPN", "KOR", "LTU", "LUX", "LVA", "MEX", "MLT", "NLD", "POL", "PRT", "ROU", "RoW", "RUS", "SVK", "SVN", "SWE", "TUR", "TWN", "USA"};
	
	public static final int COUNTRIES = COUNTRY_LIST.length;
	public static final int SECTORS = 35;

	public WiodInputOutputGraph(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}

	public WiodInputOutputGraph(File csvFile) throws FileNotFoundException, IOException {
		super(COUNTRIES);
		try (CSVParser parser = new CSVParser(new FileReader(csvFile), CSVFormat.EXCEL.withDelimiter(';'))) {
			Iterator<CSVRecord> iter = parser.iterator();
			iter.next();
			iter.next();
			iter.next(); // header
			ArrayList<String> industries = toList(iter.next());
			ArrayList<String> countries = toList(iter.next());
			iter.next();

			Iterator<CSVRecord> records = parser.iterator();
			while (records.hasNext() && parse(records.next().iterator(), industries, countries))
				;
		}
		for (Country c : getCountries()) {
			c.mergeConsumption();
		}
		for (Country c : getCountries()) {
			c.turnNegativeLinks();
		}
	}

	public WiodInputOutputGraph(int year) throws FileNotFoundException, IOException {
		this(Formatter.getFilename(year));
	}

	private boolean parse(Iterator<String> iter, ArrayList<String> industries, ArrayList<String> importers) {
		iter.next();
		String sourceIndustry = iter.next();
		String sourceCountry = iter.next();
		String code = iter.next();
		if (sourceCountry.equals("TOT")) {
			return false;
		} else {
			Country c = getCountry(sourceCountry);
			int index = 0;
			while (iter.hasNext()) {
				double millions = Double.parseDouble(iter.next().replace(",", ""));
				if (millions != 0) {
					String destIndustry = industries.get(index);
					if (destIndustry.equals("Total output")) {
						assert Math.abs(c.getNode(sourceIndustry).getOutputs() - millions) < 0.1;
					} else if (destIndustry.length() > 0) {
						String destCountry = importers.get(index);
						Country importer = getCountry(destCountry);
						c.getNode(sourceIndustry).linkTo(importer.getNode(destIndustry), millions);
					}
				}
				index++;
			}
			return true;
		}
	}

	protected ArrayList<String> toList(CSVRecord csvRecord) {
		ArrayList<String> industries = new ArrayList<String>();
		Iterator<String> fields = csvRecord.iterator();
		for (int i = 0; i < 4; i++) {
			fields.next();
		}
		while (fields.hasNext()) {
			industries.add(fields.next().intern());
		}
		return industries;
	}

}
