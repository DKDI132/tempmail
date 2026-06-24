package com.example.demo;
import com.example.demo.entity.Connections;
import com.example.demo.entity.TempInbox;
import com.example.demo.repo.TempInboxRep;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class Czysciciel {
    private TempInboxRep skrzynki;
    private Connections polaczenia;
    public Czysciciel(TempInboxRep skrzynki,Connections polaczenia){
        this.skrzynki=skrzynki;this.polaczenia=polaczenia;
    }
    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void czysc(){
        LocalDateTime czas = LocalDateTime.now().minusHours(24);
        List<TempInbox> do_usuniecia = skrzynki.findByStworzoneBefore(czas);
        for(int i = 0;i<do_usuniecia.size();i++){
            String mail = do_usuniecia.get(i).getEmail();
            SseEmitter polaczenie = polaczenia.get(mail);
            if(polaczenie!=null){
                try{
                    polaczenie.send(SseEmitter.event().name("expired").data("expired"));
                } catch (Exception e){

                }
                polaczenie.complete();
            }
        }
        skrzynki.deleteByStworzoneBefore(czas);
    }
}
