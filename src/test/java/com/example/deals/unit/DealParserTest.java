package com.example.deals.unit;

import com.example.deals.dto.DealRequest;
import com.example.deals.exception.CsvParseException;
import com.example.deals.parser.DealParser;
import com.example.deals.result.ImportResult;
import com.example.deals.result.ParseResult;
import com.example.deals.result.RowResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("DealParser Tests")
class DealParserTest {

    private final DealParser parser = new DealParser();

    @Nested
    @DisplayName("Parsing valid CSV")
    class ValidCsv {

        @Test
        @DisplayName("Parses rows with all fields")
        void parse_validCsv_returnsRows() {
            String csv = """
                    dealUniqueId,fromCurrency,toCurrency,timestamp,amount
                    D1,USD,EUR,2025-01-01T10:00:00Z,1000
                    D2,GBP,USD,2025-01-01T11:00:00Z,500
                    """;

            List<DealParser.RowData> rows = parser.parse(new StringReader(csv));

            assertThat(rows).hasSize(2);
            assertThat(rows.get(0).request().getDealId()).isEqualTo("D1");
            assertThat(rows.get(1).request().getFromCurrency()).isEqualTo("GBP");
        }

        @Test
        @DisplayName("Handles missing optional fields")
        void parse_missingFields_setsEmptyStrings() {
            String csv = """
                    dealUniqueId,fromCurrency,toCurrency,timestamp,amount
                    D1,,EUR,,1000
                    """;

            List<DealParser.RowData> rows = parser.parse(new StringReader(csv));

            DealRequest req = rows.get(0).request();
            assertThat(req.getFromCurrency()).isEmpty();
            assertThat(req.getTimestamp()).isEmpty();
        }

        @Test
        @DisplayName("Handles rows with missing columns")
        void parse_rowsWithMissingColumns_setsDefaults() {
            String csv = """
                    dealUniqueId,fromCurrency,toCurrency,timestamp,amount
                    D1
                    D2,USD
                    D3,USD,EUR
                    D4,USD,EUR,2025-01-01T10:00:00Z
                    """;

            List<DealParser.RowData> rows = parser.parse(new StringReader(csv));

            DealRequest r0 = rows.get(0).request();
            assertThat(r0.getDealId()).isEqualTo("D1");
            assertThat(r0.getFromCurrency()).isEmpty();
            assertThat(r0.getToCurrency()).isEmpty();
            assertThat(r0.getTimestamp()).isEmpty();
            assertThat(r0.getAmountStr()).isEmpty();

            DealRequest r1 = rows.get(1).request();
            assertThat(r1.getDealId()).isEqualTo("D2");
            assertThat(r1.getFromCurrency()).isEqualTo("USD");
            assertThat(r1.getToCurrency()).isEmpty();
            assertThat(r1.getTimestamp()).isEmpty();
            assertThat(r1.getAmountStr()).isEmpty();

            DealRequest r2 = rows.get(2).request();
            assertThat(r2.getDealId()).isEqualTo("D3");
            assertThat(r2.getFromCurrency()).isEqualTo("USD");
            assertThat(r2.getToCurrency()).isEqualTo("EUR");
            assertThat(r2.getTimestamp()).isEmpty();
            assertThat(r2.getAmountStr()).isEmpty();

            DealRequest r3 = rows.get(3).request();
            assertThat(r3.getDealId()).isEqualTo("D4");
            assertThat(r3.getFromCurrency()).isEqualTo("USD");
            assertThat(r3.getToCurrency()).isEqualTo("EUR");
            assertThat(r3.getTimestamp()).isEqualTo("2025-01-01T10:00:00Z");
            assertThat(r3.getAmountStr()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DealId edge cases")
    class DealIdEdgeCases {

        @Test
        @DisplayName("Empty row uses UNKNOWN for dealId")
        void parse_emptyRow_usesUnknownForDealId() {
            String csv = """
                    dealUniqueId,fromCurrency,toCurrency,timestamp,amount
                    """;
            List<DealParser.RowData> rows = parser.parse(new StringReader(csv));
            assertThat(rows).isEmpty(); // No data rows after header
        }

        @Test
        @DisplayName("Blank dealId uses UNKNOWN")
        void parse_blankDealId_usesUnknown() {
            String csv = """
                    dealUniqueId,fromCurrency,toCurrency,timestamp,amount
                    ,USD,EUR,2025-01-01T10:00:00Z,1000
                    """;
            List<DealParser.RowData> rows = parser.parse(new StringReader(csv));
            assertThat(rows.get(0).request().getDealId()).isEqualTo("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Throws CsvParseException on reader error")
        void parse_throwsCsvParseException_onReaderError() {
            Reader failingReader = new Reader() {
                @Override
                public int read(char[] cbuf, int off, int len) {
                    throw new RuntimeException("read error");
                }

                @Override
                public void close() {}
            };

            assertThatThrownBy(() -> parser.parse(failingReader))
                    .isInstanceOf(CsvParseException.class)
                    .hasMessageContaining("Failed to read CSV: read error");
        }
    }

    @Nested
    @DisplayName("Result Records")
    class ResultRecords {

        @Test
        @DisplayName("ParseResult record accessors and methods")
        void parseResult_record_methods() {
            DealRequest dr = new DealRequest();
            dr.setDealId("D1");

            RowResult rr = new RowResult("D1", "FAILURE", "");

            ParseResult result = new ParseResult(List.of(dr), List.of(rr));

            // Accessors
            assertThat(result.validRows()).contains(dr);
            assertThat(result.parsingErrors()).contains(rr);

            // toString, equals, hashCode
            String str = result.toString();
            assertThat(str).isNotNull();

            ParseResult result2 = new ParseResult(List.of(dr), List.of(rr));
            assertThat(result).isEqualTo(result2);
            assertThat(result.hashCode()).isEqualTo(result2.hashCode());
        }

        @Test
        @DisplayName("ImportResult record accessors and methods")
        void importResult_record_methods() {
            RowResult rr = new RowResult("D1", "FAILURE", "");

            ImportResult result = new ImportResult(List.of(rr));

            // Accessor
            assertThat(result.results()).contains(rr);

            // toString, equals, hashCode
            String str = result.toString();
            assertThat(str).isNotNull();

            ImportResult result2 = new ImportResult(List.of(rr));
            assertThat(result).isEqualTo(result2);
            assertThat(result.hashCode()).isEqualTo(result2.hashCode());
        }
    }
}