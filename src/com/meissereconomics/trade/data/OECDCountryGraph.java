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

public class OECDCountryGraph {
	
	private Country country;
	private Country world;

	public OECDCountryGraph(String filename) throws FileNotFoundException, IOException {
		this(new File(filename));
	}

	public OECDCountryGraph(File csvFile) throws FileNotFoundException, IOException {
		this.country = new Country("Belgium", 0, 2);
		this.world = new Country("World", 1, 2);
		try (CSVParser parser = new CSVParser(new FileReader(csvFile), CSVFormat.EXCEL.withDelimiter('\t'))) {
			Iterator<CSVRecord> iter = parser.iterator();
			iter.next();
			iter.next();
			iter.next();
			iter.next(); // header
			ArrayList<String> industries = toList(iter.next());
			iter.next();

			Iterator<CSVRecord> records = parser.iterator();
			while (records.hasNext() && parse(records.next().iterator(), industries))
				;
		}
		this.country.mergeConsumption();
		this.country.turnNegativeLinks();
	}

	private boolean parse(Iterator<String> iter, ArrayList<String> industries) {
 		String sourceIndustry = iter.next();
		iter.next();
		if (sourceIndustry.startsWith("TXS_INT_FNL")) {
			return false;
		} else {
			sourceIndustry = sourceIndustry.substring(4).intern(); // remove TTL_
			int index = 0;
			while (iter.hasNext()) {
				double millions = Double.parseDouble(iter.next().replace(",", ""));
				if (millions != 0) {
					String destIndustry = industries.get(index);
					if (destIndustry.startsWith("EXPO:")) {
						country.getNode(sourceIndustry).linkTo(world.getNode("Exports Dummy"), millions);
					} else if (destIndustry.startsWith("IMPO:")) {
						world.getNode("Imports Dummy").linkTo(country.getNode(sourceIndustry), millions);
					} else if (destIndustry.length() > 0) {
						country.getNode(sourceIndustry).linkTo(country.getNode(destIndustry), millions);
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
		for (int i = 0; i < 2; i++) {
			fields.next();
		}
		while (fields.hasNext()) {
			industries.add(fields.next().intern());
		}
		return industries;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		OECDCountryGraph g = new OECDCountryGraph("data/oecd-belgium-2003.csv");
		double diff = 1.0;
		double epsilon = 0.0001;
		while (epsilon >= 0.0001){
			diff = g.country.calculateComposition(EFlowBendingMode.DEFAULT, 0.0, diff * epsilon);
		}
		System.out.println(g.country.getConsumption());
		System.out.println(g.country.getExports());
		System.out.println(g.country.getCreatedValue());
		System.out.println(g.country.getImports());
		String s = "C34: Motor vehicles, trailers and semi-trailers";
		System.out.println(g.country.getNode(s).getReusedImports() / g.country.getNode(s).getExports());
	}

}
