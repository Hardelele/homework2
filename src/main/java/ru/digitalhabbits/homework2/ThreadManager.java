package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class ThreadManager {

    private final Exchanger<String> exchanger = new Exchanger<>();
    private final LineProcessor<Integer> lineProcessor = new LineCounterProcessor();
    private final List<Future<Pair<String, Integer>>> lineProcessorFuturesList = new ArrayList<>();

    private final ExecutorService lineProcessorService;
    private final Thread writer;

    public ThreadManager(int size, String resultFileName) {
        this.lineProcessorService = Executors.newFixedThreadPool(size);
        this.writer = new Thread(new FileWriterRunnable(resultFileName, exchanger));
        this.writer.start();
    }

    private void bindNewLineProcessor(String string) {
        lineProcessorFuturesList.add(
                lineProcessorService.submit(() ->
                        lineProcessor.process(string))
        );
    }

    public void fillPoolOfSize(int chunkSize, Scanner scanner) {
        int counter = 0;
        while (counter < chunkSize && scanner.hasNextLine()) {
            this.bindNewLineProcessor(scanner.nextLine());
            counter++;
        }
    }

    public void joinPool() throws ExecutionException, InterruptedException {
        for (Future<Pair<String, Integer>> r : lineProcessorFuturesList) {
            Pair<String, Integer> p = r.get();
            exchanger.exchange(p.getKey() + " " + p.getValue());
        }
    }

    public void complete() {
        writer.interrupt();
        lineProcessorService.shutdown();
    }
}
