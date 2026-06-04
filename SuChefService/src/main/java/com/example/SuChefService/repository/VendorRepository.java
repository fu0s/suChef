package com.example.SuChefService.repository;

import com.example.SuChefService.entity.Vendor;
import com.example.SuChefService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, String> {
    List<Vendor> findByUser(User user);

    List<Vendor> findByUserAndCategory(User user, String category);
}
