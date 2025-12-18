package com.stackademy.proje.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FFmpegService {

    private static final int TIMEOUT_MINUTES = 5;

    /**
     * Process video file with FFmpeg to fix metadata issues
     * 
     * @param inputFile  Original video file
     * @param outputFile Processed video file
     * @return true if processing successful, false otherwise
     */
    public boolean processVideo(File inputFile, File outputFile) {
        try {
            // FFmpeg command: re-encode with proper metadata
            List<String> command = new ArrayList<>();
            command.add("ffmpeg");
            command.add("-i");
            command.add(inputFile.getAbsolutePath());
            command.add("-c:v");
            command.add("libx264");
            command.add("-c:a");
            command.add("aac");
            command.add("-movflags");
            command.add("+faststart");
            command.add("-y"); // Overwrite output file
            command.add(outputFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            System.out.println("Starting FFmpeg processing...");
            System.out.println("Input: " + inputFile.getAbsolutePath());
            System.out.println("Output: " + outputFile.getAbsolutePath());

            Process process = processBuilder.start();

            // Read FFmpeg output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    // Print important lines
                    if (line.contains("Duration") || line.contains("frame=") || line.contains("error")) {
                        System.out.println("FFmpeg: " + line);
                    }
                }
            }

            // Wait for process to complete with timeout
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                System.err.println("FFmpeg timeout after " + TIMEOUT_MINUTES + " minutes");
                return false;
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                System.err.println("FFmpeg failed with exit code: " + exitCode);
                System.err.println("FFmpeg output:\n" + output);
                return false;
            }

            System.out.println("FFmpeg processing completed successfully");
            return true;

        } catch (Exception e) {
            System.err.println("FFmpeg processing error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if FFmpeg is available on the system
     * 
     * @return true if FFmpeg is available
     */
    public boolean isFfmpegAvailable() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
            Process process = processBuilder.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
