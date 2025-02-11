package duke.limoj.sdk;

import com.alibaba.fastjson2.JSON;
import duke.limoj.sdk.domain.model.ChatCompletionRequest;
import duke.limoj.sdk.domain.model.ChatCompletionSyncResponse;
import duke.limoj.sdk.domain.model.Message;
import duke.limoj.sdk.domain.model.Model;
import duke.limoj.sdk.types.utils.BearerTokenUtils;
import duke.limoj.sdk.types.utils.WechatAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * AiCodeReviewer
 *
 * @Description:
 * @CreateTime: 2025-02-03
 */

public class AiCodeReviewer {

    public static void main(String[] args) throws Exception {
        System.out.println("Starting code review...");

        String token = System.getenv("GITHUB_TOKEN");
        if (StringUtils.isEmptyOrNull(token)) {
            throw new RuntimeException("Token is null");
        }
        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        System.out.println("Exit with code:" + exitCode);

        System.out.println("Diff Code: " + diffCode.toString());

        // 2. AI code review
        String log = codeReview(diffCode.toString());
        System.out.println("Code Review: " + log);

        // 3. Log write
        String logUrl = writeLog(token, log);
        System.out.println("Log url: " + logUrl);

        // 4. Wechat message
        System.out.println("pushMessage: " + logUrl);
        pushMessage(logUrl);

    }

    private static void pushMessage(String logUrl) {
        String accessToken = WechatAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("project", "test-project");
        message.put("review", logUrl);
        message.setUrl(logUrl);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String codeReview(String diffCode) throws Exception {

        String apiKeySecret = "d7dca246bc704a558c542d80f590d5df.UyTgWnDalMWu2oPJ";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        System.out.println("评审结果：" + content.toString());

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();
    }


    private static String writeLog(String token, String log) throws Exception {

        Git git = Git.cloneRepository()
                .setURI("https://github.com/L1MOJ/ai-code-reviewer-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, ""))
                .call();

        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = generateRandomString(12) + ".md";
        File newFile = new File(dateFolder, fileName);
        try(FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
        }

        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via Github Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        return "https://github.com/L1MOJ/ai-code-reviewer-log/blob/main/" + dateFolderName + "/" + fileName;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

}
