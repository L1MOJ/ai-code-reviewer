package duke.limoj.sdk;

import com.alibaba.fastjson2.JSON;
import duke.limoj.sdk.domain.service.impl.AiCodeReviewService;
import duke.limoj.sdk.infrastructure.git.GitCommand;
import duke.limoj.sdk.infrastructure.openai.IOpenAI;
import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import duke.limoj.sdk.infrastructure.openai.impl.ChatGLM;
import duke.limoj.sdk.infrastructure.wechat.Wechat;
import duke.limoj.sdk.infrastructure.wechat.dto.TemplateMessageDTO;
import duke.limoj.sdk.domain.model.Model;
import duke.limoj.sdk.types.utils.BearerTokenUtils;
import duke.limoj.sdk.types.utils.WechatAccessTokenUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
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

    private static final Logger logger = LoggerFactory.getLogger(AiCodeReviewer.class);

    // 配置配置
    private String weixin_appid = "wx5bd0a1e6fbbd27ac";
    private String weixin_secret = "9a0e801e6d5072c498039a78076353f5";
    private String weixin_touser = "oD31H7IsFqbXk3KmV5fPjVIuIkYI";
    private String weixin_template_id = "iufgExA5JEPZ-hQbIMEoYJZVac7M-XS9y1ZhEePuEnA";

    // ChatGLM 配置
    private String chatglm_apiHost = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private String chatglm_apiKeySecret = "d7dca246bc704a558c542d80f590d5df.UyTgWnDalMWu2oPJ";

    // Github 配置
    private String github_review_log_uri;
    private String github_token;

    // 工程配置 - 自动获取
    private String github_project;
    private String github_branch;
    private String github_author;


    public static void main(String[] args) throws Exception {
        System.out.println("Starting code review...");

        GitCommand gitCommand = new GitCommand(
                getEnv("GITHUB_REVIEW_LOG_URI"),
                getEnv("GITHUB_TOKEN"),
                getEnv("COMMIT_PROJECT"),
                getEnv("COMMIT_BRANCH"),
                getEnv("COMMIT_AUTHOR"),
                getEnv("COMMIT_MESSAGE")
        );

        Wechat wechat = new Wechat(
                getEnv("WECHAT_APPID"),
                getEnv("WECHAT_SECRET"),
                getEnv("WECHAT_TOUSER"),
                getEnv("WECHAT_TEMPLATE_ID")
        );

        IOpenAI openAI = new ChatGLM(getEnv("CHATGLM_APIHOST"), getEnv("CHATGLM_APIKEYSECRET"));

        AiCodeReviewService aiCodeReviewService = new AiCodeReviewService(gitCommand, openAI, wechat);
        aiCodeReviewService.exec();

        logger.info("ai-code-review done!");

    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (null == value || value.isEmpty()) {
            throw new RuntimeException("value is null");
        }
        return value;
    }


}
