package com.kule.VERI_MERKEZI;

import com.kule.LOKASYON.LOKASYONRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VERI_MERKEZIService {

    private final VERI_MERKEZIRepository repository;
    private final LOKASYONRepository lokasyonRepository;

    public VERI_MERKEZIService(VERI_MERKEZIRepository repository,
                               LOKASYONRepository lokasyonRepository) {
        this.repository = repository;
        this.lokasyonRepository = lokasyonRepository;
    }

    public List<VERI_MERKEZIDtos.VERI_MERKEZIResponse> findAll() {
        return repository.findAll().stream()
                .map(v -> new VERI_MERKEZIDtos.VERI_MERKEZIResponse(
                        v.getId(), v.getLokasyon_id(), v.getAdi(), v.getDurum()))
                .toList();
    }

    public VERI_MERKEZIDtos.VERI_MERKEZIResponse create(VERI_MERKEZIDtos.VERI_MERKEZIRequest request) {
        if (!lokasyonRepository.existsById(request.lokasyon_id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "lokasyon_id " + request.lokasyon_id() + " does not exist");
        }

        VERI_MERKEZI saved = repository.save(
                new VERI_MERKEZI(request.lokasyon_id(), request.adi(), request.durum()));

        return new VERI_MERKEZIDtos.VERI_MERKEZIResponse(
                saved.getId(), saved.getLokasyon_id(), saved.getAdi(), saved.getDurum());
    }
}