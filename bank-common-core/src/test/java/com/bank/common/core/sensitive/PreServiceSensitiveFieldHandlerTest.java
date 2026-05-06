package com.bank.common.core.sensitive;

import com.bank.common.sensitive.Sensitive;
import com.bank.common.sensitive.SensitiveFieldProcessor;
import com.bank.common.sensitive.StoragePolicy;
import com.bank.common.sensitive.TextEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PreServiceSensitiveFieldHandlerTest {

    @Test
    void 컨트롤러_호출_전에_요청_body의_민감_필드를_암호화한다() throws Exception {
        TextEncryptor textEncryptor = new TextEncryptor() {
            @Override public String encrypt(String plainText) { return "ENC(" + plainText + ")"; }
            @Override public String decrypt(String cipherText) { return cipherText; }
        };
        PreServiceSensitiveFieldHandler handler = new PreServiceSensitiveFieldHandler(
                new SensitiveFieldProcessor(textEncryptor));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(handler)
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();

        mockMvc.perform(post("/test/pre-service")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "customerName": "홍길동"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", startsWith("ENC(")));
    }

    @RestController
    static class TestController {

        @PostMapping("/test/pre-service")
        TestRequest preService(@RequestBody TestRequest request) {
            return request;
        }
    }

    static class TestRequest {
        @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
        public String customerName;
    }
}
