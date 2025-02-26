package org.telebotv1.dto;

import lombok.Data;

import java.util.List;

@Data
public class GptResponseDto {

    private List<Choice> choices;

    public static class Message {
        private String content;
    }

    public static class Choice {
        private Message message;
    }
}
