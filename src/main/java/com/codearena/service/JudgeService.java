package com.codearena.service;

import com.codearena.entity.Language;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MVP execution engine: compiles/runs code directly on the host machine using
 * the interpreter/compiler already installed (python3, node, javac/java, g++).
 *
 * This is NOT sandboxed — it is meant for local development only. The
 * production version (Docker-isolated, resource-limited per submission)
 * is built in the dedicated "Code Execution" module.
 */
@Service
@Slf4j
public class JudgeService {

    public record ExecResult(boolean success, String stdout, String stderr, long timeMs, boolean timedOut) {}

    public ExecResult execute(Language language, String code, String stdin, int timeLimitMs) {
        Path workDir = null;
        try {
            workDir = Files.createTempDirectory("codearena-" + UUID.randomUUID());

            switch (language) {
                case PYTHON -> {
                    Path src = workDir.resolve("main.py");
                    Files.writeString(src, code, StandardCharsets.UTF_8);
                    return run(List.of("python3", src.toString()), workDir, stdin, timeLimitMs);
                }
                case JAVASCRIPT -> {
                    Path src = workDir.resolve("main.js");
                    Files.writeString(src, code, StandardCharsets.UTF_8);
                    return run(List.of("node", src.toString()), workDir, stdin, timeLimitMs);
                }
                case JAVA -> {
                    Path src = workDir.resolve("Main.java");
                    Files.writeString(src, code, StandardCharsets.UTF_8);
                    ExecResult compile = run(List.of("javac", src.toString()), workDir, "", 10000);
                    if (!compile.success()) {
                        return new ExecResult(false, "", compile.stderr(), compile.timeMs(), false);
                    }
                    return run(List.of("java", "-cp", workDir.toString(), "Main"), workDir, stdin, timeLimitMs);
                }
                case CPP -> {
                    Path src = workDir.resolve("main.cpp");
                    Path bin = workDir.resolve("main.out");
                    Files.writeString(src, code, StandardCharsets.UTF_8);
                    ExecResult compile = run(List.of("g++", "-O2", "-o", bin.toString(), src.toString()), workDir, "", 15000);
                    if (!compile.success()) {
                        return new ExecResult(false, "", compile.stderr(), compile.timeMs(), false);
                    }
                    return run(List.of(bin.toString()), workDir, stdin, timeLimitMs);
                }
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            }
        } catch (IOException e) {
            return new ExecResult(false, "", "Judge error: " + e.getMessage(), 0, false);
        } finally {
            if (workDir != null) {
                deleteRecursively(workDir.toFile());
            }
        }
    }

    private ExecResult run(List<String> command, Path workDir, String stdin, int timeLimitMs) {
        long start = System.currentTimeMillis();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(false);
            Process process = pb.start();

            try (OutputStream os = process.getOutputStream()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (IOException ignored) {
                // process may not read stdin at all — fine to ignore
            }

            boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
            long elapsed = System.currentTimeMillis() - start;

            if (!finished) {
                process.destroyForcibly();
                return new ExecResult(false, "", "Time limit exceeded", elapsed, true);
            }

            String stdout = readStream(process.getInputStream());
            String stderr = readStream(process.getErrorStream());
            boolean success = process.exitValue() == 0;

            return new ExecResult(success, stdout, stderr, elapsed, false);
        } catch (IOException | InterruptedException e) {
            return new ExecResult(false, "", "Execution error: " + e.getMessage(), System.currentTimeMillis() - start, false);
        }
    }

    private String readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        inputStream.transferTo(buffer);
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }

    private void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
