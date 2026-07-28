package com.atta.PolyPaste.services.auth;

import com.atta.PolyPaste.entitys.VkUserEntity;
import com.atta.PolyPaste.jwt.JwtProvider;
import com.atta.PolyPaste.mapper.VkUserMapper;
import com.atta.PolyPaste.repository.VkUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.objects.users.Fields;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VkAuthService {

    private final VkApiClient vk;
    private final VkUserRepository vkUserRepository;
    private final VkUserMapper vkUserMapper;
    private final JwtProvider jwtProvider;

    @Value("${vk.app-id}")
    private Integer appId;

    @Value("${vk.redirect-uri}")
    private String redirectUri;

    public String getAuthUrl(String codeVerifier) {
        String state = UUID.randomUUID().toString().replace("-", "");

        String codeChallenge = "";
        try {
            byte[] bytes = codeVerifier.getBytes("US-ASCII");
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes, 0, bytes.length);
            byte[] digest = messageDigest.digest();
            codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "https://id.vk.ru/authorize" +
                "?response_type=code" +
                "&client_id=" + appId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&state=" + state +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256" +
                "&scope=email";
    }

    @Transactional
    public VkUserEntity processVkAuth(String code, String codeVerifier, String deviceId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String tokenUrl = "https://id.vk.ru/oauth2/auth";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("client_id", String.valueOf(appId));
        map.add("code_verifier", codeVerifier);
        map.add("redirect_uri", redirectUri);
        map.add("device_id", deviceId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        System.out.println("VK Response: " + response.getBody());
        JsonNode root = mapper.readTree(response.getBody());
        String accessToken = root.path("access_token").asText();
        long vkUserId = root.path("user_id").asLong();

        UserActor actor = new UserActor(vkUserId, accessToken);
        var users = vk.users().get(actor)
                .fields(Fields.FIRST_NAME_NOM, Fields.LAST_NAME_NOM, Fields.PHOTO_50)
                .execute();
        if (users.isEmpty()) throw new RuntimeException("VK User not found");
        var vkUser = users.getFirst();

        VkUserEntity userEntity = vkUserRepository.findByVkId(vkUser.getId())
                .orElseGet(() -> vkUserRepository.save(vkUserMapper.toEntity(vkUser)));

        return userEntity;
    }

    public String createToken(Long id, String firstName, String lastName){
        return jwtProvider.generateToken(id, firstName, lastName);
    }
}