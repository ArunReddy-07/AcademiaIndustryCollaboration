package com.academiaindustry.service;

import com.academiaindustry.dto.AuthResponse;
import com.academiaindustry.dto.LoginRequest;
import com.academiaindustry.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}