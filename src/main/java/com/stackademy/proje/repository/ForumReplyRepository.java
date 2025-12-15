package com.stackademy.proje.repository;

import com.stackademy.proje.entity.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ForumReplyRepository extends JpaRepository<ForumReply, UUID> {

    // Bir soruya (postId) ait tüm cevapları getir
    List<ForumReply> findByPostId(UUID postId);

    // Silinmemiş cevapları getir
    List<ForumReply> findByPostIdAndIsDeletedFalse(UUID postId);

    // Bir soruya ait cevap sayısını getir (silinmemişler)
    int countByPostIdAndIsDeletedFalse(UUID postId);
}