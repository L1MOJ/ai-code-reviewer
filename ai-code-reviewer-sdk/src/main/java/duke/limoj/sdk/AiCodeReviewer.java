package duke.limoj.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * AiCodeReviewer
 *
 * @Description:
 * @CreateTime: 2025-02-03
 */

public class AiCodeReviewer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Test1111");

        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exit with code:" + exitCode);

        System.out.println("Code Review: " + diffCode.toString());
    }
}
