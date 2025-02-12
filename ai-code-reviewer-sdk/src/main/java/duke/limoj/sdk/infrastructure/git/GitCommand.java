package duke.limoj.sdk.infrastructure.git;

import duke.limoj.sdk.types.utils.RandomStringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GitCommand
 *
 * @Description:
 * @CreateTime: 2025-02-12
 */
public class GitCommand {

    private final Logger logger = LoggerFactory.getLogger(GitCommand.class);

    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public String getGithubReviewLogUri() {
        return githubReviewLogUri;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public String diff() throws IOException, InterruptedException {
        ProcessBuilder logProcessBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        logProcessBuilder.directory(new File("."));
        Process logProcess = logProcessBuilder.start();

        BufferedReader logReader = new BufferedReader(new InputStreamReader(logProcess.getInputStream()));
        String latestCommit = logReader.readLine();
        logReader.close();
        logProcess.waitFor();

        ProcessBuilder diffProcessBuilder = new ProcessBuilder("git", "diff", latestCommit + "^", latestCommit);
        diffProcessBuilder.directory(new File("."));
        Process diffProcess = diffProcessBuilder.start();
        BufferedReader diffReader = new BufferedReader(new InputStreamReader(diffProcess.getInputStream()));
        String line;
        StringBuilder diffCode = new StringBuilder();

        while ((line = diffReader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }
        diffReader.close();

        int exitCode = diffProcess.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Failed to get diff, exit code:" + exitCode);
        }

        return diffCode.toString();
    }

    public String commitAndPush(String reviewLog) throws Exception {
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri + ".git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + System.currentTimeMillis() + "-" + RandomStringUtils.generateRandomString(4) + ".md";
        File newFile = new File(dateFolder, fileName);
        try(FileWriter writer = new FileWriter(newFile)) {
            writer.write(reviewLog);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via Github Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

        logger.info("ai-code-review git commit and push done! {}", fileName);

        return githubReviewLogUri + "/blob/main/" + dateFolderName + "/" + fileName;
    }


}
