package org.lsandoval.fileparser.service.parser;

import org.lsandoval.fileparser.service.model.job.ProcessingResult;
import org.lsandoval.fileparser.service.model.parser.ParseRequest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the shared {@link AbstractFileParser.BatchAccumulator} flushing
 * behaviour, which all four concrete parsers rely on.
 */
class BatchAccumulatorTest {

    /** Minimal concrete parser used only to reach the protected accumulator factory. */
    private static final class TestParser extends AbstractFileParser {
        TestParser() {
            super(null, null);
        }

        @Override
        public Set<String> supportedExtensions() {
            return Set.of("test");
        }

        @Override
        public <T> ProcessingResult parse(File file, ParseRequest request, Class<T> dtoClass,
                                          int batchSize, Consumer<List<T>> batchProcessor) {
            // not used in this test
            return ProcessingResult.builder().build();
        }
    }

    private final TestParser parser = new TestParser();

    @Test
    void flushesWhenBatchSizeReachedAndEmitsRemainderOnFlush() {
        List<List<Integer>> emitted = new ArrayList<>();
        // The accumulator clears and reuses its internal list, so snapshot each emission.
        Consumer<List<Integer>> collector = batch -> emitted.add(new ArrayList<>(batch));

        AbstractFileParser.BatchAccumulator<Integer> accumulator = parser.batchAccumulator(2, collector);

        accumulator.add(1);
        assertThat(emitted).isEmpty(); // below batch size, nothing emitted yet

        accumulator.add(2); // reaches batch size -> auto-flush
        assertThat(emitted).containsExactly(List.of(1, 2));

        accumulator.add(3); // starts a new, partial batch
        assertThat(emitted).hasSize(1);

        accumulator.flush(); // emits the remainder
        assertThat(emitted).containsExactly(List.of(1, 2), List.of(3));
    }

    @Test
    void flushOnEmptyAccumulatorEmitsNothing() {
        List<List<Integer>> emitted = new ArrayList<>();
        AbstractFileParser.BatchAccumulator<Integer> accumulator =
                parser.batchAccumulator(5, batch -> emitted.add(new ArrayList<>(batch)));

        accumulator.flush();

        assertThat(emitted).isEmpty();
    }
}
