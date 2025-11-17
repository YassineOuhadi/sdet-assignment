package com.example.deals.unit;

import com.example.deals.controller.DealController;
import com.example.deals.dto.DealResponse;
import com.example.deals.exception.DealPersistenceException;
import com.example.deals.parser.DealParser;
import com.example.deals.service.DealImportService;
import com.example.deals.result.RowResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DealControllerTest {

    private final DealParser parser = mock(DealParser.class);
    private final DealImportService service = mock(DealImportService.class);
    private final DealController controller = new DealController(parser, service);

    @Nested
    class ImportDealsTests {

        @Test
        void validFile_returnsResults() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "deals.csv",
                    "text/csv", "content".getBytes());

            when(parser.parse(any())).thenReturn(List.of());
            when(service.importRows(any())).thenReturn(List.of(RowResult.success("D1")));

            ResponseEntity<?> resp = controller.importDeals(file);

            assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
            Map<String, Object> body = (Map<String, Object>) resp.getBody();
            assertThat(body).containsKey("results");
        }

        @Test
        void nullFile_returnsBadRequest() {
            ResponseEntity<?> resp = controller.importDeals(null);
            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        }

        @Test
        void emptyFile_returnsBadRequest() {
            MockMultipartFile file = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);
            ResponseEntity<?> resp = controller.importDeals(file);
            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        }

        @Test
        void serviceThrowsException_returnsInternalServerError() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "deal.csv",
                    "text/csv",
                    "id,from,to,timestamp,amount\n1,USD,EUR,2025-01-01T00:00:00,100".getBytes()
            );

            when(service.importRows(any())).thenThrow(new DealPersistenceException("DB error"));

            ResponseEntity<?> resp = controller.importDeals(file);
            assertThat(resp.getStatusCodeValue()).isEqualTo(500);
        }

        @Test
        void nonCsvFile_returnsBadRequest() {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "notcsv.txt",
                    "text/plain",
                    "data".getBytes()
            );

            ResponseEntity<?> resp = controller.importDeals(file);

            assertThat(resp.getStatusCodeValue()).isEqualTo(400);
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertThat(body.get("error")).isEqualTo("Only CSV files are allowed");
        }
    }

    @Nested
    class HealthTests {

        @Test
        void health_returnsUp() {
            ResponseEntity<?> resp = controller.health();
            assertThat(resp.getStatusCodeValue()).isEqualTo(200);
            Map<?, ?> body = (Map<?, ?>) resp.getBody();
            assertThat(body.get("status")).isEqualTo("UP");
        }
    }

    @Nested
    class GetDealsTests {

        @Test
        void getAllDeals_emptyList_returns200() {
            when(service.getAllDealsDto()).thenReturn(List.of());
            ResponseEntity<List<DealResponse>> resp = controller.getAllDeals();
            assertThat(resp.getStatusCodeValue()).isEqualTo(200);
            assertThat(resp.getBody()).isEmpty();
        }

        @Test
        void getDealById_notFound_returns404() {
            when(service.getDealByIdDto("notexist")).thenReturn(Optional.empty());
            ResponseEntity<DealResponse> resp = controller.getDealById("notexist");
            assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        }
    }
}