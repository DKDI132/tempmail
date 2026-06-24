package com.example.demo.entity;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Connections {
    private Map<String, SseEmitter> zbior = new ConcurrentHashMap<>();

    public void dodaj(String mail,SseEmitter polaczenie){
        zbior.put(mail,polaczenie);
    }
    public void usun(String mail, SseEmitter polaczenie){
        zbior.remove(mail, polaczenie);
    }
    public SseEmitter get(String mail){
        return zbior.get(mail);
    }

    @Scheduled(fixedRate = 20000)
    public void wyslijHeartbeat() {
        zbior.forEach((mail, polaczenie) -> {
            try {
                polaczenie.send(SseEmitter.event().comment("ping"));
            } catch (Exception e) {
                polaczenie.completeWithError(e);
            }
        });
    }
}
