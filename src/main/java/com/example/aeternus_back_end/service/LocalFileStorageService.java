package com.example.aeternus_back_end.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class LocalFileStorageService {

    private final SupabaseStorageService supabaseStorageService;

    @Autowired
    public LocalFileStorageService(SupabaseStorageService supabaseStorageService) {
        this.supabaseStorageService = supabaseStorageService;
    }

    public String saveBase64Image(String base64Image) {
        if (base64Image == null || !base64Image.startsWith("data:image/")) {
            return base64Image; // Return original if not base64 or already a URL
        }

        // Only use Supabase storage — no local fallback
        return supabaseStorageService.uploadBase64Image(base64Image);
    }
}
