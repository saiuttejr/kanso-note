package com.bankingoop.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bankingoop.finance.entity.ProfileEntity;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {
}
