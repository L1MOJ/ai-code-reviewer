package duke.limoj.sdk.infrastructure.wechat;

import com.alibaba.fastjson2.JSON;
import duke.limoj.sdk.infrastructure.git.GitCommand;
import duke.limoj.sdk.infrastructure.wechat.dto.TemplateMessageDTO;
import duke.limoj.sdk.types.utils.WechatAccessTokenUtils;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * Wechat
 *
 * @Description:
 * @CreateTime: 2025-02-12
 */
public class Wechat {

    private final Logger logger = LoggerFactory.getLogger(Wechat.class);

    private final String appid;
    private final String secret;
    private final String touser;
    private final String template_id;


    public Wechat(String appid, String secret, String touser, String template_id) {
        this.appid = appid;
        this.secret = secret;
        this.touser = touser;
        this.template_id = template_id;
    }

    public void sendTemplateMessage(String logUrl, Map<String, Map<String, String>> data) throws Exception {
        String accessToken = WechatAccessTokenUtils.getAccessToken(appid, secret);
        System.out.println(accessToken);

        TemplateMessageDTO templateMessageDTO = new TemplateMessageDTO(touser, template_id);
        templateMessageDTO.setUrl(logUrl);
        templateMessageDTO.setData(data);

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        OkHttpClient client = new OkHttpClient();
        String jsonBody = JSON.toJSONString(templateMessageDTO);
        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                System.out.println("Response: " + response.body().string());
            } else {
                System.out.println("Failed: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
