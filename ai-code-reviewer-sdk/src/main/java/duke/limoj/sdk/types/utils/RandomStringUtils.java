package duke.limoj.sdk.types.utils;

import java.util.Random;

/**
 * RandomStringUtils
 *
 * @Description:
 * @CreateTime: 2025-02-12
 */
public class RandomStringUtils {
    public static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
