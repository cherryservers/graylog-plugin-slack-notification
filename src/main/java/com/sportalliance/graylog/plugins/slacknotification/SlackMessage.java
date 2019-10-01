package com.sportalliance.graylog.plugins.slacknotification;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class SlackMessage {

    private final String color;
    private final String iconEmoji;
    private final String iconUrl;
    private final String userName;
    private final String channel;
    private final boolean linkNames;
    private final String message;
    private String customMessage;
    private List<String> backlogItemMessages;

    public SlackMessage(
            String color,
            String iconEmoji,
            String iconUrl,
            String userName,
            String channel,
            boolean linkNames,
            String message,
            String customMessage,
            List<String> backlogItemMessages
    ) {
        this.color = color;
        this.iconEmoji = iconEmoji;
        this.iconUrl = iconUrl;
        this.userName = userName;
        this.channel = channel;
        this.linkNames = linkNames;
        this.message = message;
        this.customMessage = customMessage;
        this.backlogItemMessages = backlogItemMessages;
    }

    public String getJsonString() {
        // See https://api.slack.com/methods/chat.postMessage for valid parameters
        final Map<String, Object> params = new HashMap<String, Object>() {{
            put("channel", channel);
            put("text", message);
            put("link_names", linkNames);
        }};

        if (!isNullOrEmpty(userName)) {
            params.put("username", userName);
        }

        if (!isNullOrEmpty(iconUrl)) {
            params.put("icon_url", iconUrl);
        }

        if (!isNullOrEmpty(iconEmoji)) {
            params.put("icon_emoji", ensureEmojiSyntax(iconEmoji));
        }

        final List<Attachment> attachments = new ArrayList<>();
        if (!isNullOrEmpty(customMessage)) {
            final Attachment attachment = new Attachment(
                    color,
                    customMessage,
                    "Custom Message",
                    "Custom Message:",
                    null
            );
            attachments.add(attachment);
        }

        for (String backlogItemMessage : backlogItemMessages) {
            if(!isNullOrEmpty(backlogItemMessage)) {
                final Attachment attachment = new Attachment(
                        color,
                        backlogItemMessage,
                        "Backlog Item Message",
                        null,
                        null
                );
                attachments.add(attachment);
            }
        }

        if (!attachments.isEmpty()) {
            params.put("attachments", attachments);
        }

        try {
            return new ObjectMapper().writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not build payload JSON.", e);
        }
    }

    private String ensureEmojiSyntax(final String x) {
        String emoji = x.trim();

        if (!emoji.isEmpty() && !emoji.startsWith(":")) {
            emoji = ":" + emoji;
        }

        if (!emoji.isEmpty() && !emoji.endsWith(":")) {
            emoji = emoji + ":";
        }

        return emoji;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        @JsonProperty
        public String fallback;
        @JsonProperty
        public String text;
        @JsonProperty
        public String pretext;
        @JsonProperty
        public String color;
        @JsonProperty
        public List<AttachmentField> fields;

        @JsonCreator
        public Attachment(String color, String text, String fallback, String pretext, List<AttachmentField> fields) {
            this.fallback = fallback;
            this.text = text;
            this.pretext = pretext;
            this.color = color;
            this.fields = fields;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentField {
        @JsonProperty
        public String title;
        @JsonProperty
        public String value;
        @JsonProperty("short")
        public boolean isShort;

        @JsonCreator
        public AttachmentField(String title, String value, boolean isShort) {
            this.title = title;
            this.value = value;
            this.isShort = isShort;
        }
    }

}
