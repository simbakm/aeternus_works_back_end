package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.RenovationIdea;
import com.example.aeternus_back_end.repository.RenovationIdeaRepository;
import com.example.aeternus_back_end.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/renovations")
@RequiredArgsConstructor
public class RenovationController {

    private final RenovationIdeaRepository renovationRepository;
    private final SupabaseStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<List<RenovationIdea>> getAllRenovations() {
        return ResponseEntity.ok(renovationRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RenovationIdea> getRenovationById(@PathVariable String id) {
        return renovationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RenovationIdea> createRenovation(@RequestBody RenovationIdea renovation) {
        if (renovation.getImageUrl() != null) {
            renovation.setImageUrl(fileStorageService.uploadBase64Image(renovation.getImageUrl()));
        }
        if (renovation.getBeforeImageUrl() != null) {
            renovation.setBeforeImageUrl(fileStorageService.uploadBase64Image(renovation.getBeforeImageUrl()));
        }
        if (renovation.getAfterImageUrl() != null) {
            renovation.setAfterImageUrl(fileStorageService.uploadBase64Image(renovation.getAfterImageUrl()));
        }
        if (renovation.getTips() != null) {
            for (int i = 0; i < renovation.getTips().size(); i++) {
                var tip = renovation.getTips().get(i);
                tip.setRenovationIdea(renovation);
                if (tip.getSortOrder() == null) {
                    tip.setSortOrder(i);
                }
            }
        }
        return ResponseEntity.ok(renovationRepository.save(renovation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRenovation(@PathVariable String id) {
        return renovationRepository.findById(id)
                .map(renovation -> {
                    renovationRepository.delete(renovation);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
