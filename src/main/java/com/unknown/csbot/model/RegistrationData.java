package com.unknown.csbot.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RegistrationData {
    private String name;
    private String phone;
    private boolean agreedToTerms;
}
