package com.meissereconomics.seminar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.meissereconomics.seminar.util.InstantiatingHashmap;

public class InputOutputGraph {

	public static final int COUNTRIES = 41;
	public static final int SECTORS = 35;

	private InstantiatingHashmap<String, Country> countries = new InstantiatingHashmap<String, Country>() {

		private int count = 0;
		
		@Override
		protected Country createValue(String name) {
			return new Country(name, count++, COUNTRIES);
		}
	};

	public InputOutputGraph(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}

	public InputOutputGraph(File csvFile) throws FileNotFoundException, IOException {
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
		for (Country c : countries.values()) {
			c.mergeConsumption();
		}
		for (Country c : countries.values()) {
			c.turnNegativeLinks();
		}
	}

	private boolean parse(Iterator<String> iter, ArrayList<String> industries, ArrayList<String> importers) {
		iter.next();
		String sourceIndustry = iter.next();
		String sourceCountry = iter.next();
		String code = iter.next();
		if (sourceCountry.equals("TOT")) {
			return false;
		} else {
			Country c = countries.obtain(sourceCountry);
			int index = 0;
			while (iter.hasNext()) {
				double millions = Double.parseDouble(iter.next().replace(",", ""));
				if (millions != 0) {
					String destIndustry = industries.get(index);
					if (destIndustry.equals("Total output")) {
						assert Math.abs(c.getNode(sourceIndustry).getOutputs() - millions) < 0.1;
					} else if (destIndustry.length() > 0) {
						String destCountry = importers.get(index);
						Country importer = countries.obtain(destCountry);
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

	public void collapseSmallestSectors(int i) {
		for (Country c : countries.values()) {
			c.collapseSmallestSectors(i);
		}
	}
	
	public void collapseRandomSectors(int seed, int i) {
		collapseRandomSectors(seed, i, null);
	}
	
	public void collapseRandomSectors(int seed, int i, BiConsumer<Node, Node> mergeListener) {
		for (Country c : countries.values()) {
			seed += 131313;
			c.collapseRandomSectors(seed, i);
		}
	}

	public void deriveOrigins(EFlowBendingMode mode, double consumptionPreference) {
		double difference = 1.0;
		while (difference >= 0.005) {
			difference = 0.0;
			for (Country c : countries.values()) {
				difference = Math.max(c.calculateComposition(mode, consumptionPreference), difference);
			}
		}
	}

	public double getGlobalImportReuse() {
		double totalReuse = 0.0;
		double totalExports = 0.0;
		for (Country c : countries.values()) {
			totalExports += c.getExports();
			totalReuse += c.getReusedImports();
		}
		return totalReuse / totalExports;
	}

	private void printImportExportStats() {
		System.out.println("Country\tImports\tExports\tImport Reuse\tConsumption");
		for (Country c : countries.values()) {
			double imports = c.getImports();
			double exports = c.getExports();
			double importReuse = c.getReusedImports();
			double size = c.getConsumption();
			System.out.println(c + "\t" + imports + "\t" + exports + "\t" + importReuse + "\t" + size);
		}
		// printReuse("Machinery, Nec");
		// printReuse("Coke, Refined Petroleum and Nuclear Fuel");
		// printReuse(Node.CONSUMPTION_TYPES[0]);
	}

	protected void printReuse(String industry) {
		System.out.println(industry);
		for (Country c : getCountries()) {
			System.out.println(c + "\t" + c.getOriginOf(industry));
		}
	}

	public Country getCountry(String string) {
		return countries.get(string);
	}

	public Collection<Country> getCountries() {
		ArrayList<Country> cs = new ArrayList<>(countries.values());
		Collections.sort(cs);
		return cs;
	}

}
