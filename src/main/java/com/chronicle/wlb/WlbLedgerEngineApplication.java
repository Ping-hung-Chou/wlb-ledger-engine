package com.chronicle.wlb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 🌟 啟動時間魔法！這行字告訴 Spring 總部：「請指派一個專屬的時鐘巡邏員！」
public class WlbLedgerEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(WlbLedgerEngineApplication.class, args);
    }
}