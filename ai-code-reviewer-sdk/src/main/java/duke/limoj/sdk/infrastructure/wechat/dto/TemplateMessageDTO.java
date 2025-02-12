package duke.limoj.sdk.infrastructure.wechat.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Message
 *
 * @Description:
 * @CreateTime: 2025-02-10
 */

public class TemplateMessageDTO {

    private String touser = "oD31H7IsFqbXk3KmV5fPjVIuIkYI";
    private String template_id = "8DTWicAWKWM399ak-3vxy56oxBP4d2Q-g6bA7mmjYdI";
    private String url = "https://weixin.qq.com";
    private Map<String, Map<String, String>> data = new HashMap<>();

    public TemplateMessageDTO(String touser, String template_id) {
        this.touser = touser;
        this.template_id = template_id;
    }

    public void put(String key, String value) {
        data.put(key, new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    public static void put(Map<String, Map<String, String>> data, TemplateKey key, String value) {
        data.put(key.getCode(), new HashMap<String, String>() {
            private static final long serialVersionUID = 7092338402387318563L;

            {
                put("value", value);
            }
        });
    }

    public enum TemplateKey {
        REPO_NAME("repo_name", "project name"),
        BRANCH("branch_name", "branch name"),
        COMMIT_AUTHOR("commit_author", "commit author"),
        COMMIT_MESSAGE("commit_message", "commit message");

        private String code;
        private String info;

        TemplateKey(String code, String info) {
            this.code = code;
            this.info = info;
        }

        public String getCode() {
            return code;
        }

        public String getInfo() {
            return info;
        }
    }


    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser;
    }

    public String getTemplate_id() {
        return template_id;
    }

    public void setTemplate_id(String template_id) {
        this.template_id = template_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }

}
