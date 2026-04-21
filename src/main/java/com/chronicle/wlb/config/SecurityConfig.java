package com.chronicle.wlb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 啟動特勤部隊
public class SecurityConfig {

    // 1. 設定密碼加密器 (將 BCrypt 變成全銀行共用的工具)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 規劃 API 通行權限
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF 防護 (因為我們是 REST API，且沒有使用傳統 Cookie 登入)
                .csrf(csrf -> csrf.disable())
                // 設定路由權限
                .authorizeHttpRequests(auth -> auth
                        // 🌟 白名單：允許所有人(permitAll)訪問註冊大門
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/ledger/**").permitAll() // 🌟 暫時開放記帳大門方便測試
                        // 其他所有的 API，都必須要有通行證(登入)才能訪問
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}