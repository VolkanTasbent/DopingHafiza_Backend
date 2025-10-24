package com.example.backend.service;

import com.example.backend.model.Ders;
import com.example.backend.repository.DersRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DersService {
    private final DersRepository repo;
    public DersService(DersRepository repo) { this.repo = repo; }

    public List<Ders> getAll() {
        return repo.findAll();
    }

    public Ders add(String ad) {
        Ders d = new Ders(ad);
        return repo.save(d);
    }
}
