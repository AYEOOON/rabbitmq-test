package com.RabbitMQ.RabbitMQTest.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationRequest {
    private String studentName;
    private String courseName;
}
