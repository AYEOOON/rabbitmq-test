package com.RabbitMQ.RabbitMQTest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {
    private String courseId;
    private String studentId;
}
