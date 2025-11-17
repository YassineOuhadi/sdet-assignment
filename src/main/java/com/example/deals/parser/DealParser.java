package com.example.deals.parser;

import com.example.deals.dto.DealRequest;
import com.example.deals.exception.CsvParseException;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DealParser {

    public record RowData(int rowNum, DealRequest request) {}

    private static final Logger log = LoggerFactory.getLogger(DealParser.class);

    public List<RowData> parse(Reader reader) {
        List<RowData> rows = new ArrayList<>();

        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            int rowNum = 1;
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                rowNum++;

                String dealId = (line.length == 0 || line[0].isBlank()) ? "UNKNOWN" : line[0].trim();
                String fromCurrency = line.length > 1 ? line[1].trim() : "";
                String toCurrency = line.length > 2 ? line[2].trim() : "";
                String timestamp = line.length > 3 ? line[3].trim() : "";
                String amountStr = line.length > 4 ? line[4].trim() : "";

                DealRequest req = new DealRequest();
                req.setDealId(dealId);
                req.setFromCurrency(fromCurrency);
                req.setToCurrency(toCurrency);
                req.setTimestamp(timestamp);
                req.setAmountStr(amountStr);

                rows.add(new RowData(rowNum, req));
            }

        } catch (Exception e) {
            throw new CsvParseException("Failed to read CSV: " + e.getMessage());
        }

        return rows;
    }
}