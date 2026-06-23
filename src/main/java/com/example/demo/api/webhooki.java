package com.example.demo.api;
import com.example.demo.repo.*;
import com.example.demo.entity.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/webhooks")
public class webhooki {
    private MailsRep maile;
    private TempInboxRep skrzynki;
    private Connections polaczenia;
    public webhooki(MailsRep maile,TempInboxRep skrzynki,Connections polaczenia){
        this.maile=maile;
        this.skrzynki=skrzynki;
        this.polaczenia=polaczenia;
    }
    @PostMapping("/new")
    public Map<String,String> dodaj(@RequestBody Map<String,String> mail){
        String sender = mail.get("sender");
        String subject = mail.get("subject");
        String body = mail.get("body");
        String recipient = mail.get("recipient");
        TempInbox docelowa = skrzynki.findByEmail(recipient).orElseThrow(() -> new IllegalArgumentException("Podany mail nie istnieje"));
        ReceivedEmail zlozone = new ReceivedEmail(sender,subject,body,docelowa);
        maile.save(zlozone);
        SseEmitter polaczenie = polaczenia.get(recipient);
        if(polaczenie!=null){
            try{
            polaczenie.send(zlozone);
            }
            catch (IOException e){
                polaczenie.completeWithError(e);
            }}

        return Map.of("status","ok");
    }

}
