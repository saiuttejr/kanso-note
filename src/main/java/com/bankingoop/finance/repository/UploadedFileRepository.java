package com.bankingoop.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.UploadedFileEntity;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFileEntity, Long> {

    /** Finds all uploaded file records ordered by upload timestamp (newest first). */
    List<UploadedFileEntity> findAllByOrderByUploadedAtDesc();
}
