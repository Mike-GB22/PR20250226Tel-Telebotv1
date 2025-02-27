package org.telebotv1.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class GptRequestDto {

    private String model;
    private List<Message> messages;

    @Builder.Default
    private double temperature = 1.0;
    @Builder.Default
    private int max_tokens = 200;
    @Builder.Default
    private int n = 1;

    @Builder
    @Getter
    public static class Message {
        private Role role;
        private String content;
    }

    public static enum Role {
        SYSTEM("system"), USER("user"), ASSISTANT("assistant");

        final String role;

        Role(String role) {
            this.role = role;
        }

        @JsonValue
        @Override
        public String toString() {
            return role;
        }
    }
}
