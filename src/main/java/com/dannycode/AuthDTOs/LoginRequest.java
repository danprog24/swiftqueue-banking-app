package com.dannycode.AuthDTOs;

import lombok.*;

@Getter
@Setter
public class LoginRequest {

    private String email;
    private String password;

}
