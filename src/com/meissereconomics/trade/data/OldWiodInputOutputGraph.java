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
import com.meissereconomics.trade.util.Timer;

public class OldWiodInputOutputGraph extends InputOutputGraph {

	public static final String[] COUNTRY_LIST = new String[]{"AUS", "AUT", "BEL", "BGR", "BRA", "CAN", "CHN", "CYP", "CZE", "DEU", "DNK", "ESP", "EST", "FIN", "FRA", "GBR", "GRC", "HUN", "IDN", "IND", "IRL", "ITA", "JPN", "KOR", "LTU", "LUX", "LVA", "MEX", "MLT", "NLD", "POL", "PRT", "ROU", "RoW", "RUS", "SVK", "SVN", "SWE", "TUR", "TWN", "USA"};
	
	public static final int COUNTRIES = COUNTRY_LIST.length;
	public static final int SECTORS = 35;

	public OldWiodInputOutputGraph(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}

	public OldWiodInputOutputGraph(File csvFile) throws FileNotFoundException, IOException {
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

	public OldWiodInputOutputGraph(int year) throws FileNotFoundException, IOException {
		this(getFilename(year));
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
						assert Math.abs(c.getNode(sourceIndustry).getOutputsInclConsumption() - millions) < 0.1;
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
	
	private static String getFilename(int year) {
		if (year <= 1999) {
			return "data/wiot" + (year - 1900) + "_row_apr12.csv";
		} else if (year <= 2007) {
			return "data/wiot0" + (year - 2000) + "_row_apr12.csv";
		} else if (year <= 2009) {
			return "data/wiot0" + (year - 2000) + "_row_sep12.csv";
		} else {
			return "data/wiot" + (year - 2000) + "_row_sep12.csv";
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
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Timer timer = new Timer();
		InputOutputGraph graph = new OldWiodInputOutputGraph(2007);
		timer.time("Loaded graph");
		graph.deriveOrigins(EFlowBendingMode.DEFAULT, 0.0);
		timer.time("Derived origins, result " + graph.getGlobalImportReuse());
	}
	
	@Override
	public String toString(){
		return "WIOD 2013 " + super.toString();
	}

}
