package com.dannycode.AuthDTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String fullname;
    private String email;
    private Integer phone;
    private String password;

}
