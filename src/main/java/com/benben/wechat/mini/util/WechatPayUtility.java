package com.benben.wechat.mini.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.*;
import java.util.stream.IntStream;

public class WechatPayUtility {

    final static private DocumentBuilderFactory documentBuilderFactory
            = DocumentBuilderFactory.newInstance();

    final static private TransformerFactory transformerFactory
            = TransformerFactory.newInstance();

    final static private String FIELD_SIGN = "sign";

    static {
        if (Arrays.stream(Security.getProviders())
                .noneMatch(p -> p instanceof BouncyCastleProvider)) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    static public String sign(
            Map<String, Object> fieldsToSign,
            String apiKey,
            SignType signType) {

        final var splicedString =
                fieldsToSign.entrySet().stream()
                        .filter(entry -> {
                            final var value = entry.getValue();
                            if (value == null) {
                                return false;
                            }
                            return !(value instanceof String) ||
                                    StringUtils.hasText((String) value);
                        })
                        .sorted(Comparator.comparing(Map.Entry::getKey))
                        .map(entry -> entry.getKey() + '=' + entry.getValue().toString())
                        .reduce((s1, s2) -> s1 + "&" + s2).orElseThrow();

        final var stringToSign = splicedString + "&key=" + apiKey;

        String signature;
        if (signType == SignType.MD5) {
            signature = DigestUtils
                    .md5DigestAsHex(stringToSign.getBytes())
                    .toUpperCase();
        } else {
            throw new IllegalArgumentException();
        }

        return signature;
    }

    static public boolean checkSign(
            Map<String, Object> dataToSign,
            String apiKey,
            SignType signType) {

        final var providedSign = dataToSign.get(FIELD_SIGN);
        if (providedSign == null) {
            return false;
        }

        final var fieldsToSign = new HashMap<>(dataToSign);
        fieldsToSign.remove(FIELD_SIGN);
        final var actualSign = sign(fieldsToSign, apiKey, signType);

        return actualSign.equals(providedSign);
    }

    /**
     * @param cipherText
     * @param apiKey
     * @return
     * @throws RefundNotifyDecryptException
     */
    static public Map<String, String> decryptRefundNotify(
            String cipherText, String apiKey) {

        try {
            final var decodedCipherText =
                    Base64Utils.decodeFromString(cipherText);

            final var decryptKey =
                    DigestUtils.md5DigestAsHex(apiKey.getBytes())
                            .toLowerCase();

            final var cipher =
                    Cipher.getInstance("AES/ECB/PKCS7Padding",
                            "BC");
            final var keySpec =
                    new SecretKeySpec(decryptKey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            final var plainText =
                    new String(cipher.doFinal(decodedCipherText),
                            StandardCharsets.UTF_8);

            return parseXmlText(plainText);
        } catch (Exception e) {
            throw new RefundNotifyDecryptException(
                    String.format("Cipher text: %s", cipherText), e);
        }
    }

    static public String generateNonceStr() {

        return UUID.randomUUID().toString()
                .replaceAll("-", "");
    }

    static public JsapiParams generateJsapiParams(
            String appId, String prepayId, String apiKey) {

        final var timestamp = String.valueOf(
                System.currentTimeMillis() / 1000); // unit => second
        final var nonceStr = generateNonceStr();
        final var pack = "prepay_id=" + prepayId;
        final var signType = "MD5";

        final Map<String, Object> fieldsToSign = Map.of(
                "appId", appId,
                "timeStamp", timestamp,
                "nonceStr", nonceStr,
                "package", pack,
                "signType", signType);
        final var paySign = sign(fieldsToSign, apiKey, SignType.MD5);

        return new JsapiParams(timestamp, nonceStr, pack, signType, paySign);
    }

    static public Map<String, String> parseXmlText(String response) {

        final var documentBuilder = acquireDocumentBuilder();

        final Document document;
        try {
            document = documentBuilder.parse(
                    new InputSource(new StringReader(response)));
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }

        final var result = new HashMap<String, String>();

        final var nodeList = document.getFirstChild().getChildNodes();
        IntStream.range(0, nodeList.getLength()).forEach(i -> {
            final var currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                final var currentElement = (Element) currentNode;
                final var content = currentElement.getFirstChild()
                        .getTextContent();
                result.put(currentElement.getTagName(), content);
            }
        });

        return result;
    }

    static public String toXmlText(Map<String, Object> fields) {

        final var documentBuilder = acquireDocumentBuilder();
        final var document = documentBuilder.newDocument();

        final var rootElement = document.createElement("xml");
        document.appendChild(rootElement);

        fields.forEach((key, value) -> {
            final var element = document.createElement(key);
            element.appendChild(
                    document.createTextNode(value.toString()));
            rootElement.appendChild(element);

        });

        final var transformer = acquireTransformer();

        final var writer = new StringWriter();

        try {
            transformer.transform(
                    new DOMSource(document), new StreamResult(writer));
        } catch (TransformerException te) {
            throw new RuntimeException(te);
        }

        return writer.toString();
    }

    static private DocumentBuilder acquireDocumentBuilder() {

        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
        }
    }

    static private Transformer acquireTransformer() {

        try {
            final var transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            return transformer;
        } catch (TransformerConfigurationException tce) {
            throw new RuntimeException(tce);
        }
    }

    public enum SignType {
        MD5,
//        HMAC_SHA256
    }

    @Data
    @AllArgsConstructor
    static public class JsapiParams {

        private String timeStamp;
        private String nonceStr;
        @JsonProperty("package")
        private String pack;
        private String signType;
        private String paySign;
    }

    static public class RefundNotifyDecryptException
            extends RuntimeException {

        RefundNotifyDecryptException(
                String message, Throwable reason) {

            super(message, reason);
        }
    }
}
