package duke.limoj.sdk.infrastructure.openai;

import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import duke.limoj.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAI {

    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;
}
