import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        FileReader fileReader = new FileReader("lab1 - all.csv");
        CSVParser parser = CSVParser.parse(fileReader, CSVFormat.EXCEL.withDelimiter(',').withHeader());

        List<String> headerNames = parser.getHeaderNames();
        int border = headerNames.size() - 2;

        List<CSVRecord> records = new ArrayList<>();
        for (CSVRecord csvRecord : parser) {
            records.add(csvRecord);
        }

        List<Data> dataSet = RatioCalculator.buildDataset(headerNames, records, border);
        RatioCalculator ratioCalculator = new RatioCalculator(headerNames.subList(0, border), dataSet);
        Map<String, Float> result = ratioCalculator.calcGainRatio();

        FileWriter out = new FileWriter("result.csv");
        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT)) {
            result.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .forEach(e -> {
                        try {
                            printer.printRecord(e.getKey(), e.getValue());
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
        }
    }

}
