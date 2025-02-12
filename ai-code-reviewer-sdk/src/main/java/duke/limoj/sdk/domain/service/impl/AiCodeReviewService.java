package duke.limoj.sdk.domain.service.impl;

import duke.limoj.sdk.domain.model.Model;
import duke.limoj.sdk.domain.service.AbstractAiCodeReviewService;
import duke.limoj.sdk.infrastructure.git.GitCommand;
import duke.limoj.sdk.infrastructure.openai.IOpenAI;
import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;
import duke.limoj.sdk.infrastructure.wechat.Wechat;
import duke.limoj.sdk.infrastructure.wechat.dto.TemplateMessageDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * AiCodeReviewService
 *
 * @Description:
 * @CreateTime: 2025-02-12
 */
public class AiCodeReviewService extends AbstractAiCodeReviewService {

    public AiCodeReviewService(GitCommand gitCommand, IOpenAI openAI, Wechat wechat) {
        super(gitCommand, openAI, wechat);
    }

    @Override
    protected void pushMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());

        wechat.sendTemplateMessage(logUrl, data);
    }

    @Override
    protected String getDiffCode() throws IOException, InterruptedException {
        return gitCommand.diff();
    }

    @Override
    protected String codeReview(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequestDTO = new ChatCompletionRequestDTO();
        chatCompletionRequestDTO.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequestDTO.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });

        ChatCompletionSyncResponseDTO completions = openAI.completions(chatCompletionRequestDTO);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();
    }

    @Override
    protected String recordCodeReview(String log) throws Exception {
         return gitCommand.commitAndPush(log);
    }

}
