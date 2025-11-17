package com.example.deals.mock;

import com.example.deals.integration.AbstractIntegrationTest;
import com.example.deals.repository.DealRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DealConcurrencyImportIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DealRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void concurrentImports_noDuplicates() throws InterruptedException {
        int threads = 5;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "deals.csv",
                "text/csv",
                ("dealId,fromCurrency,toCurrency,timestamp,amount\n" +
                        "D1,USD,EUR,2025-01-01T10:00:00Z,100\n" +
                        "D2,EUR,USD,2025-01-01T11:00:00Z,200").getBytes()
        );

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch latch = new CountDownLatch(threads);

        IntStream.range(0, threads).forEach(i -> executor.submit(() -> {
            try {
                mockMvc.perform(multipart("/api/v1/deals/import").file(file))
                        .andExpect(status().isOk());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        }));

        latch.await();
        executor.shutdown();

        Assertions.assertEquals(2, repository.count(), "DB should have only 2 unique deals");
    }
}
