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
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级代码审查专家，精通软件架构、代码优化、安全漏洞检测、性能分析和代码质量评估。" +
                        "请你根据下面的 Git Diff 记录，对代码进行严格的代码审查，并提供详细的改进建议。\n\n" +

                        "请你分析并回答以下问题：\n" +
                        "1 **Bug 检测**：代码是否可能引入新的 bug？是否存在边界情况未考虑？\n" +
                        "2 **安全分析**：代码是否存在安全漏洞，如 SQL 注入、XSS 攻击、权限绕过？\n" +
                        "3 **代码优化**：代码是否可以优化？是否有冗余逻辑、重复代码、低效算法？\n" +
                        "4 **代码风格**：代码是否符合编码规范？变量命名是否清晰，缩进、格式是否一致？\n" +
                        "5 **可维护性**：代码是否符合 SOLID 设计原则？是否有过高的耦合度？\n" +
                        "6 **测试覆盖率**：是否需要额外的单元测试来保证代码正确性？\n\n" +

                        "请按照以下格式输出你的代码审查报告：\n" +
                        "```\n" +
                        "[**问题**]\n" +
                        "在第 XX 行发现可能的 Bug/安全漏洞/代码优化问题...\n" +
                        "\n" +
                        "[🚀 **优化建议**]\n" +
                        "1. xxx\n" +
                        "2. xxx\n" +
                        "\n" +
                        "[**修正后的代码示例**]\n" +
                        "```java\n" +
                        "// 这里是改进后的代码示例\n" +
                        "```\n" +
                        "```\n" +
                        "请确保你的代码分析 **清晰、详细、可执行**。下面是代码变更记录："));
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
