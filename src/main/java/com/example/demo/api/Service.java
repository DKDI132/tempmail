package com.example.demo.api;
import com.example.demo.repo.*;
import com.example.demo.entity.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/service")
public class Service {
    private MailsRep maile;
    private TempInboxRep skrzynka;
    private Connections polaczenia;


    public Service(MailsRep maile,TempInboxRep skrzynka,Connections polaczenia){
        this.maile=maile;
        this.skrzynka = skrzynka;
        this.polaczenia=polaczenia;
    }
    private String token_generator(){
        return UUID.randomUUID().toString();
    }
    private String mail_generator(){
        return UUID.randomUUID().toString().substring(0, 8) + "@tymczasowymail.pl";
    }
    @PostMapping("/generate")
    public Map<String,String> generuj_maila(){
        String mail = mail_generator();
        String token = token_generator();
        TempInbox nowa = new TempInbox(mail,token);
        skrzynka.save(nowa);
        return Map.of("status","ok","mail",mail,"token",token);
    }
    @GetMapping("/stream")
    public SseEmitter otworzStrumien(@RequestParam String token){


        SseEmitter polaczenie = new SseEmitter(1800000L);
        TempInbox klient = skrzynka.findByToken(token).orElseThrow(() -> new IllegalArgumentException("Taki token nie istnieje"));
        String email = klient.getEmail();
        List<ReceivedEmail> maile = klient.getEmails();
        Long liczba = skrzynka.count();
        try {
            polaczenie.send(SseEmitter.event().name("stats").data(liczba));
            polaczenie.send(SseEmitter.event().name("maile").data(maile));

        }catch (IOException e){
            polaczenie.completeWithError(e);
        }
        polaczenie.onCompletion(() -> polaczenia.usun(email, polaczenie));
        polaczenie.onTimeout(() -> polaczenia.usun(email, polaczenie));
        polaczenie.onError((ex) -> polaczenia.usun(email, polaczenie));
        polaczenia.dodaj(email,polaczenie);
        return polaczenie;


    }
}
