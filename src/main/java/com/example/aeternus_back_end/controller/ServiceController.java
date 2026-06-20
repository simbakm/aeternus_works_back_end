package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.ServiceInfo;
import com.example.aeternus_back_end.repository.ServiceInfoRepository;
import com.example.aeternus_back_end.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceInfoRepository serviceInfoRepository;
    private final SupabaseStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<ServiceInfo>> getAllServices() {
        return ResponseEntity.ok(serviceInfoRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceInfo> getServiceById(@PathVariable String id) {
        return serviceInfoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceInfo> createService(@RequestBody ServiceInfo service) {
        if (service.getId() == null || service.getId().isBlank()) {
            service.setId(generateSlug(service.getTitle()));
        }
        if (service.getImageUrl() != null) {
            service.setImageUrl(fileStorageService.uploadBase64Image(service.getImageUrl()));
        }
        if (service.getProcess() != null) {
            for (int i = 0; i < service.getProcess().size(); i++) {
                var step = service.getProcess().get(i);
                step.setService(service);
                if (step.getSortOrder() == null) step.setSortOrder(i);
            }
        }
        if (service.getFaqs() != null) {
            for (int i = 0; i < service.getFaqs().size(); i++) {
                var faq = service.getFaqs().get(i);
                faq.setService(service);
                if (faq.getSortOrder() == null) faq.setSortOrder(i);
            }
        }
        return ResponseEntity.ok(serviceInfoRepository.save(service));
    }

    private String generateSlug(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Service title is required to generate an ID");
        }
        return title.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceInfo> updateService(@PathVariable String id, @RequestBody ServiceInfo details) {
        return serviceInfoRepository.findById(id)
                .map(service -> {
                    service.setTitle(details.getTitle());
                    service.setIcon(details.getIcon());
                    service.setDescription(details.getDescription());
                    if (details.getImageUrl() != null) {
                        service.setImageUrl(fileStorageService.uploadBase64Image(details.getImageUrl()));
                    }
                    return ResponseEntity.ok(serviceInfoRepository.save(service));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable String id) {
        return serviceInfoRepository.findById(id)
                .map(service -> {
                    serviceInfoRepository.delete(service);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
