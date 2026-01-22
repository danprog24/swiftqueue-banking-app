package com.dannycode.AuthDTOs;

import com.dannycode.user.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    private String fullname;
    private String email;
    private String phone;
    private String password;
    private Role role;

}
