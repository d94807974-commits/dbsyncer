/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.biz.util;

import org.realtimeetl.biz.model.TokenInfo;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWTTool class
 * SupportGenerate、Validation,Refreshtoken
 *
 * @author Realtime ETL.
 * @version 1.0.0
 */
public abstract class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private static final String ALGORITHM = "HmacSHA256";
    private static final String TYP = "JWT";
    private static final String ALG = "HS256";

    // TokenValid until：2hours（ms）
    private static final long TOKEN_EXPIRE_TIME = 2 * 60 * 60 * 1000L;

    // TokenRefreshTimeWindow:1.5hours（ms），Here.TimeInner windowRefreshtoken
    private static final long TOKEN_REFRESH_TIME = (long) (1.5 * 60 * 60 * 1000);

    /**
     * GenerateJWT Token
     *
     * @param secret Key
     * @return JWT Token
     */
    public static String generateToken(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        long now = System.currentTimeMillis();
        long expireTime = now + TOKEN_EXPIRE_TIME;

        // Header
        Map<String, String> header = new HashMap<>();
        header.put("typ", TYP);
        header.put("alg", ALG);
        String headerJson = JsonUtil.objToJson(header);
        String headerBase64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

        // Payload
        TokenInfo payload = new TokenInfo();
        // Expiration time
        payload.setExp(expireTime);
        // IssuanceTime
        payload.setIat(now);
        String payloadJson = JsonUtil.objToJson(payload);
        String payloadBase64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        // Signature
        String data = headerBase64 + "." + payloadBase64;
        String signature = hmacSha256(data, secret);
        String signatureBase64 = base64UrlEncode(signature.getBytes(StandardCharsets.UTF_8));
        return data + "." + signatureBase64;
    }

    /**
     * 对任意载荷签名为 JWT。
     *
     * @param payload Load Object
     * @param secret  Key
     * @return JWT
     */
    public static String signPayload(Object payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        if (payload == null || StringUtil.isBlank(secret)) {
            throw new IllegalArgumentException("Payload/secret cannot be empty");
        }
        Map<String, String> header = new HashMap<>();
        header.put("typ", TYP);
        header.put("alg", ALG);
        String headerBase64 = base64UrlEncode(JsonUtil.objToJson(header).getBytes(StandardCharsets.UTF_8));
        String payloadBase64 = base64UrlEncode(JsonUtil.objToJson(payload).getBytes(StandardCharsets.UTF_8));
        String data = headerBase64 + "." + payloadBase64;
        String signatureBase64 = base64UrlEncode(hmacSha256(data, secret).getBytes(StandardCharsets.UTF_8));
        return data + "." + signatureBase64;
    }

    /**
     * Sign and parse the load (no check expired, processed by the caller).
     *
     * @param token  JWT
     * @param secret Key
     * @param type   LoadType
     * @param <T>    LoadType
     * @return Load,FailedBack null
     */
    public static <T> T verifyPayload(String token, String secret, Class<T> type)
            throws NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtil.isBlank(token) || StringUtil.isBlank(secret) || type == null) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            logger.error("Token format error, part number: {", parts.length);
            return null;
        }
        String data = parts[0] + "." + parts[1];
        String expectedSignature = base64UrlEncode(hmacSha256(data, secret).getBytes(StandardCharsets.UTF_8));
        if (!expectedSignature.equals(parts[2])) {
            logger.error("Token signature authentication failed");
            return null;
        }
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        return JsonUtil.jsonToObj(payloadJson, type);
    }

    /**
     * AuthenticationJWT Token
     *
     * @param token  JWT Token
     * @param secret Key
     * @return TokenInfo，AuthenticationFailedBacknull
     */
    public static TokenInfo verifyToken(String token, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtil.isBlank(token) || StringUtil.isBlank(secret)) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            logger.error("Token format error, part number: {", parts.length);
            return null;
        }

        // Authenticate signature
        String data = parts[0] + "." + parts[1];
        String expectedSignature = base64UrlEncode(hmacSha256(data, secret).getBytes(StandardCharsets.UTF_8));
        if (!expectedSignature.equals(parts[2])) {
            logger.error("Token signature authentication failed");
            return null;
        }

        // ParsingPayload
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        TokenInfo tokenInfo = JsonUtil.jsonToObj(payloadJson, TokenInfo.class);

        // AuthenticationExpiration time
        Long exp = tokenInfo.getExp();
        if (exp == null || exp < System.currentTimeMillis()) {
            logger.warn("Token expired, ext.", exp);
            return null;
        }

        return tokenInfo;
    }

    /**
     * RefreshToken
     * IftokenYes.RefreshTimewindow, canRefreshGenerateNewtoken
     *
     * @param token  OriginalToken
     * @param secret Key
     * @return NewToken，If not,RefreshWindowBacknull
     */
    public static String refreshToken(String token, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        TokenInfo tokenInfo = verifyToken(token, secret);
        if (tokenInfo == null) {
            return null;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - tokenInfo.getIat();

        // InspectionYesNoYes.RefreshTimeWindow
        if (elapsed > TOKEN_REFRESH_TIME) {
            logger.warn("Token is not in the refreshing time window.", elapsed);
            return null;
        }

        // GenerateNewtoken
        return generateToken(secret);
    }

    /**
     * HMAC-SHA256Signature
     */
    private static String hmacSha256(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Base64 URLSecurity Code
     */
    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * Base64 URLSecurity Decoding
     */
    private static byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }

}
