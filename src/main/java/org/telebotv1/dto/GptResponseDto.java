package org.telebotv1.dto;

import lombok.Data;

import java.util.List;

@Data
public class GptResponseDto {

    private List<Choice> choices;

    @Data
    public static class Message {
        private String content;
    }

    @Data
    public static class Choice {
        private Message message;
    }

    public String getMessage(){
        return choices.stream().findFirst().map(choice -> choice.message.content).orElseGet(() -> "");
    }
}
