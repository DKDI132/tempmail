package com.example.demo;
import com.example.demo.repo.TempInboxRep;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class Czysciciel {
    private TempInboxRep skrzynki;
    public Czysciciel(TempInboxRep skrzynki){
        this.skrzynki=skrzynki;
    }
    @Scheduled(cron = "0 0 * * * *")
    public void czysc(){
        LocalDateTime czas = LocalDateTime.now().minusHours(24);
        skrzynki.deleteByStworzoneBefore(czas);
    }
}
