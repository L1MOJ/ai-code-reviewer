package duke.limoj.sdk.domain.service;

import duke.limoj.sdk.infrastructure.git.GitCommand;
import duke.limoj.sdk.infrastructure.openai.IOpenAI;
import duke.limoj.sdk.infrastructure.wechat.Wechat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AbstractAiCodeReviewService
 *
 * @Description:
 * @CreateTime: 2025-02-12
 */
public abstract class AbstractAiCodeReviewService implements IAiCodeReviewService {

    private final Logger logger = LoggerFactory.getLogger(AbstractAiCodeReviewService.class);

    protected final GitCommand gitCommand;
    protected final IOpenAI openAI;
    protected final Wechat wechat;

    public AbstractAiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, Wechat wechat) {
        this.gitCommand = gitCommand;
        this.openAI = openAI;
        this.wechat = wechat;
    }

    @Override
    public void exec() {
        try {
            // 1. 获取提交代码
            String diffCode = getDiffCode();
            // 2. code review
            String log = codeReview(diffCode);
            // 3. 记录结果， 返回日志地址 - git
            String logUrl = recordCodeReview(log);
            // 4. 发送消息通知： 日志地址，通知内容 - 微信
            pushMessage(logUrl);
        } catch (Exception e) {
            logger.error("ai-code-review error: " + e);
        }
    }

    protected abstract void pushMessage(String logUrl) throws Exception;

    protected abstract String getDiffCode() throws IOException, InterruptedException;

    protected abstract String codeReview(String diffCode) throws Exception;

    protected abstract String recordCodeReview(String log) throws Exception;
}
