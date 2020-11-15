package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriterRunnable implements Runnable {
    private static final Logger logger = getLogger(FileWriterRunnable.class);
    private final String fileName;
    private final Exchanger<String> exchanger;

    public FileWriterRunnable(String fileName, Exchanger<String> exchanger) {
        this.fileName = fileName;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        try {
            FileWriter fileWriter = prepareAndGetNewFileWriter(fileName);
            this.executeWhile(fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        logger.info("Finish writer thread {}", currentThread().getName());
    }

    private FileWriter prepareAndGetNewFileWriter(String fileName) throws IOException {
        File resultFile = new File(fileName);
        if (!resultFile.exists()) {
            resultFile.createNewFile();
        }
        return new FileWriter(resultFile);
    }

    private void executeWhile(FileWriter fileWriter) throws IOException {
        while (!currentThread().isInterrupted()) {
            try {
                fileWriter.write(exchanger.exchange(null) + "\n");
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
