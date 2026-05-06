package com.bank.common.sensitive;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensitiveFieldProcessorTest {

    SensitiveFieldProcessor processor = new SensitiveFieldProcessor(
            new AesGcmTextEncryptor("MDEyMzQ1Njc4OWFiY2RlZg=="));

    @Test
    void ENCRYPT_정책_필드를_암호화한다() {
        Request request = new Request("홍길동", List.of(new Child("01012345678")));

        processor.processForStorage(request);

        assertThat(request.name).startsWith("ENC(").endsWith(")");
        assertThat(request.children.getFirst().phoneNumber).startsWith("ENC(").endsWith(")");
    }

    @Test
    void 암호화_후_복호화하면_원문이_복원된다() {
        Request request = new Request("홍길동", List.of(new Child("01012345678")));

        processor.processForStorage(request);
        processor.processForRestore(request);

        assertThat(request.name).isEqualTo("홍길동");
        assertThat(request.children.getFirst().phoneNumber).isEqualTo("01012345678");
    }

    @Test
    void 이미_암호화된_값은_다시_암호화하지_않는다() {
        Request request = new Request("ENC(value)", List.of());

        processor.processForStorage(request);

        assertThat(request.name).isEqualTo("ENC(value)");
    }

    @Test
    void null_필드는_그대로_유지된다() {
        Request request = new Request(null, List.of());

        processor.processForStorage(request);

        assertThat(request.name).isNull();
    }

    @Test
    void final_필드에_Sensitive를_선언하면_예외가_발생한다() {
        ImmutableRequest request = new ImmutableRequest("홍길동");

        assertThatThrownBy(() -> processor.processForStorage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("final field");
    }

    @Test
    void 미구현_StoragePolicy는_예외가_발생한다() {
        MaskRequest request = new MaskRequest("홍길동");

        assertThatThrownBy(() -> processor.processForStorage(request))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("MASK");
    }

    static class Request {
        @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
        String name;
        List<Child> children;

        Request(String name, List<Child> children) {
            this.name = name;
            this.children = children;
        }
    }

    static class Child {
        @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
        String phoneNumber;

        Child(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    static class ImmutableRequest {
        @Sensitive(storagePolicy = StoragePolicy.ENCRYPT)
        final String name;

        ImmutableRequest(String name) {
            this.name = name;
        }
    }

    static class MaskRequest {
        @Sensitive(storagePolicy = StoragePolicy.MASK)
        String name;

        MaskRequest(String name) {
            this.name = name;
        }
    }
}
