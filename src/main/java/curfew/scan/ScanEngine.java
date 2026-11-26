package curfew.scan;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * Classifica em paralelo, com um teto de tarefas ao mesmo tempo.
 */
public final class ScanEngine {

    private final int parallelism;

    public ScanEngine(int parallelism) {
        if (parallelism < 1) {
            throw new IllegalArgumentException("parallelism");
        }
        this.parallelism = parallelism;
    }

    public List<ClassifiedAccess> classifyAll(List<RawAccess> raw, ScanRules rules) {
        return classifyAll(raw, rules::classify);
    }

    public List<ClassifiedAccess> classifyAll(List<RawAccess> raw, Function<RawAccess, ClassifiedAccess> classify) {
        if (raw.isEmpty()) {
            return List.of();
        }
        Semaphore gate = new Semaphore(parallelism);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<ClassifiedAccess>> futures = raw.stream()
                    .map(item -> CompletableFuture.supplyAsync(() -> classifyOne(classify, gate, item), executor))
                    .toList();
            return futures.stream().map(CompletableFuture::join).toList();
        }
    }

    private static ClassifiedAccess classifyOne(
            Function<RawAccess, ClassifiedAccess> classify, Semaphore gate, RawAccess item) {
        gate.acquireUninterruptibly();
        try {
            return classify.apply(item);
        } finally {
            gate.release();
        }
    }
}
