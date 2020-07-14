package com.so.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.so.jpa.entity.Image;;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>{

}