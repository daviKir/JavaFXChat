package ru.kirakosyan.client.service;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class ChatHistory implements AutoCloseable {

    private static final String FILENAME_PATTERN = "./history/history_%s.txt";

    private final String username;
    private PrintWriter printWriter;
    private File historyFile;
    private RandomAccessFile randomAccessFile;

    public ChatHistory(String username) {
        this.username = username;
    }

    public void init() {
        try {
            historyFile = createHistoryFile();
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(historyFile, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File createHistoryFile() throws IOException {
        String filePath = String.format(FILENAME_PATTERN, username);
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdir();
            file.createNewFile();
        }
        return file;
    }

    public void appendText(String text) {
        printWriter.print(text);
        printWriter.flush();
    }

    public String loadLastRows(int rows) {
        try {
            randomAccessFile = new RandomAccessFile(historyFile, "r");
            long pointer;
            int count = 0;

            for (pointer = randomAccessFile.length() - 1; pointer > 0 ; pointer--) {
                randomAccessFile.seek(pointer);

                if (randomAccessFile.read() == '\n') {
                    count++;
                }

                if (count == rows) {
                    break;
                }
            }

            if (pointer >= 0) {
                randomAccessFile.seek(pointer);
            }

            byte[] resultData = new byte[(int) (randomAccessFile.length() - randomAccessFile.getFilePointer())];
            randomAccessFile.read(resultData);
            return new String(resultData, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    @Override
    public void close() {
        if (printWriter != null) {
            printWriter.close();
        }
    }
}
