package com.stackademy.proje.controller;

import com.stackademy.proje.dto.ProgressUpdateRequest;
import com.stackademy.proje.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    // POST /api/progress/update
    @PostMapping("/update")
    public ResponseEntity<String> updateProgress(@RequestBody ProgressUpdateRequest request) {
        return ResponseEntity.ok(progressService.updateProgress(request));
    }

    // POST /api/progress/last-topic
    @PostMapping("/last-topic")
    public ResponseEntity<Void> updateLastTopic(@RequestBody Map<String, String> request, Principal principal) {
        String email = principal.getName();
        // Email'den user ID bulmak için service'e email de geçebiliriz ama burada
        // service'de ID bekliyor.
        // Hızlı çözüm: UserRepository'yi buraya inject etmek yerine, Service metodunu
        // email alacak şekilde güncelleyebiliriz
        // VEYA burada user bulabiliriz. Service'i güncellemek daha temiz.
        // Ancak ProgressService zaten repository enjekte etti, email ile bulabilir.
        // Fakat Service metodumuz UUID userId alıyor dedik. O zaman burada User bulalım
        // Mı?
        // HAYIR, Service metodunu User ID yerine Email veya Principal alacak şekilde
        // yapabiliriz ya da User Repository'i controller'a da inject edebiliriz.
        // Ama Repository'i Controller'a inject etmek best practice değil.
        // O yüzden Service metodunu overload ediyorum veya ID bulmak için UserService
        // kullanıyorum.
        // UserService inject edelim mi? Hayır, ProgressService içinde zaten
        // UserRepository var.
        // O zaman Service'e "email" ile update eden metod ekleyelim veya mevcudu
        // değiştirelim.
        // Mevcut metod UUID alıyor demiştik. Onu değiştirelim.

        // Düzeltme: Service'deki metodu UUID ile yazmıştım. Controller'da email var.
        // O yüzden Service'e email alan versiyonunu ekleyelim ya da Controller'da user
        // bulup ID verelim.
        // En temiz: Service'e `updateLastTopicByEmail` eklemek. Ama pratik olsun:
        // Service içindeki metodu çağırmadan önce user bulmak lazım.
        // ProgressController'a UserRepository EKLEMİYORUM. ProgressService'e email alan
        // metod ekleyeceğim (bir sonraki adımda düzeltirim).
        // Şimdilik ID gerektirdiği için derleme hatası almamak adına Service metodunu
        // değiştireceğim.

        // BEKLE: ProgressService'deki metodu az önce UUID userId alacak şekilde yazdım.
        // Controller'da principal.getName() var (email).
        // ProgressService'e "updateLastTopic(String email, ...)" şeklinde overload
        // eklemek en iyisi.
        // Ama şu an multi_replace ile Service'i değiştirdim.
        // Bir sonraki adımda Service'i düzelteceğim. Şimdilik Controller kodunu
        // yazıyorum ve Service'in email alacağını varsayıyorum.

        progressService.updateLastTopic(email, request.get("category"), request.get("topic"));
        return ResponseEntity.ok().build();
    }

    // GET /api/progress/last-topic
    @GetMapping("/last-topic")
    public ResponseEntity<Map<String, String>> getLastTopic(Principal principal) {
        return ResponseEntity.ok(progressService.getLastTopic(principal.getName()));
    }
}