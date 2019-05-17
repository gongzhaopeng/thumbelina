package com.benben.wechat.mini;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

@Slf4j
public class BouncyCastleTest {

    static final private String KEY = "MOCKED_KEY";
    static final private String ALGORITHM = "AES/ECB/PKCS7Padding";

    @BeforeClass
    public static void startup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void aes256Encode() throws Exception {

        final var plainText = "God bless me!";

        final var cipher = Cipher.getInstance(ALGORITHM, "BC");
        final var encryptKey =
                DigestUtils.md5Digest(KEY.getBytes());
        log.info("Encrypt key: {}", DigestUtils.md5DigestAsHex(KEY.getBytes()));
        final var keySpec = new SecretKeySpec(encryptKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        final var cipherText = Base64Utils.encodeToString(
                cipher.doFinal(plainText.getBytes()));
        log.info("Base64 cipher text: {}", cipherText);

        Assert.assertNotNull(cipherText);
    }

    @Test
    public void aes256Decode() throws Exception {

        final var cipherText = "Yvn9ob+s/SLSIWTZfUxfPQ==";
        final var expectedPlainText = "God bless me!";

        final var decodedCipherText =
                Base64Utils.decodeFromString(cipherText);

        final var cipher = Cipher.getInstance(ALGORITHM, "BC");
        final var decryptKey =
                DigestUtils.md5Digest(KEY.getBytes());
        final var keySpec = new SecretKeySpec(decryptKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        final var actualPlainText = new String(
                cipher.doFinal(decodedCipherText));

        Assert.assertEquals(expectedPlainText, actualPlainText);
    }
}
