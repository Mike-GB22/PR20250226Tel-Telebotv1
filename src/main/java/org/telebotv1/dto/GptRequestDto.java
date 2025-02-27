package org.telebotv1.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GptRequestDto {

    private String model;
    private List<Message> messages;

//    private double temperature = 0.7;
//    private int max_tokens = 100;
//    private int n = 1;

    @Builder
    @Getter
    public static class Message {
        private String role;
        private String content;
    }

    public static enum Role {
        system, user, assistant;
    }
}
