package com.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
    private String from;
    private String to;
    private String[] cc;
    private String[] bcc;
    private String subject;
    private String body;
    private String[] attachments;
}