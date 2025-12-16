package com.stackademy.proje.service;

import com.stackademy.proje.dto.CreatePostRequest;
import com.stackademy.proje.dto.CreateReplyRequest;
import com.stackademy.proje.dto.PostResponse;
import com.stackademy.proje.dto.ReplyResponse;
import com.stackademy.proje.entity.ForumPost;
import com.stackademy.proje.entity.ForumReply;
import com.stackademy.proje.entity.User;
import com.stackademy.proje.repository.ForumPostRepository;
import com.stackademy.proje.repository.ForumReplyRepository;
import com.stackademy.proje.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ForumService {

    private final ForumPostRepository postRepository;
    private final ForumReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final CloudStorageService cloudStorageService;

    public ForumService(ForumPostRepository postRepository, ForumReplyRepository replyRepository,
            UserRepository userRepository, CloudStorageService cloudStorageService) {
        this.postRepository = postRepository;
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
        this.cloudStorageService = cloudStorageService;
    }

    // ========== POST İŞLEMLERİ ==========

    // 1. Tüm postları getir (kategori filtresi opsiyonel)
    public List<PostResponse> getPosts(String category) {
        List<ForumPost> posts;

        if (category != null && !category.isEmpty()) {
            posts = postRepository.findByCategoryOrderByCreatedAtDesc(category);
        } else {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        List<PostResponse> responseList = new ArrayList<>();
        for (ForumPost post : posts) {
            responseList.add(convertToPostResponse(post));
        }
        return responseList;
    }

    // 2. Tek post detayı
    public PostResponse getPostById(UUID postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + postId));
        return convertToPostResponse(post);
    }

    // 3. Yeni post oluştur
    public PostResponse createPost(CreatePostRequest request) {
        // Susturma kontrolü
        checkUserTimeout(request.getUserId());

        ForumPost post = new ForumPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUserId(request.getUserId());
        post.setCategory(request.getCategory());
        post.setImageUrl(request.getImageUrl()); // Fotoğraf URL'i
        post.setAccessLevel(request.getAccessLevel() != null ? request.getAccessLevel() : "FREE"); // Default FREE
        post.setSolved(false);

        ForumPost savedPost = postRepository.save(post);
        return convertToPostResponse(savedPost);
    }

    // 4. Çözüldü işaretle (soru sahibi veya öğretmen)
    public PostResponse markAsSolved(UUID postId, UUID userId) {
        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + postId));

        // Yetki kontrolü: Öğretmen veya soru sahibi çözüldü işaretleyebilir
        boolean isTeacher = "TEACHER".equals(user.getRole());
        boolean isOwner = post.getUserId() != null && post.getUserId().equals(userId);

        if (!isTeacher && !isOwner) {
            throw new RuntimeException("Bu işlemi sadece soru sahibi veya öğretmenler yapabilir!");
        }

        post.setSolved(true);
        ForumPost savedPost = postRepository.save(post);
        return convertToPostResponse(savedPost);
    }

    // 4b. Çözüldü işaretini kaldır
    public PostResponse markAsUnsolved(UUID postId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + postId));

        // Yetki kontrolü: Öğretmen veya soru sahibi çözüldü işaretini kaldırabilir
        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isOwner = post.getUserId() != null && post.getUserId().equals(userId);

        if (!isTeacher && !isOwner) {
            throw new RuntimeException("Bu işlemi sadece soru sahibi veya öğretmenler yapabilir!");
        }

        post.setSolved(false);
        ForumPost savedPost = postRepository.save(post);
        return convertToPostResponse(savedPost);
    }

    // 5. Post sil (öğretmen veya soru sahibi)
    public void deletePost(UUID postId, UUID userId) {
        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + postId));

        // Yetki kontrolü: Öğretmen veya post sahibi silebilir
        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isOwner = post.getUserId() != null && post.getUserId().equals(userId);

        if (!isTeacher && !isOwner) {
            throw new RuntimeException("Bu işlemi sadece öğretmenler veya soru sahibi yapabilir!");
        }

        // Önce ilişkili resmi sil (varsa)
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            cloudStorageService.deleteFile(post.getImageUrl());
        }

        // İlişkili reply'ların resimlerini de sil
        List<ForumReply> replies = replyRepository.findByPostIdAndIsDeletedFalse(postId);
        for (ForumReply reply : replies) {
            if (reply.getImageUrl() != null && !reply.getImageUrl().isEmpty()) {
                cloudStorageService.deleteFile(reply.getImageUrl());
            }
        }

        postRepository.deleteById(postId);
    }

    // ========== REPLY İŞLEMLERİ ==========

    // 6. Bir postun cevaplarını getir
    public List<ReplyResponse> getRepliesByPostId(UUID postId) {
        List<ForumReply> replies = replyRepository.findByPostIdAndIsDeletedFalse(postId);

        List<ReplyResponse> responseList = new ArrayList<>();
        for (ForumReply reply : replies) {
            responseList.add(convertToReplyResponse(reply));
        }
        return responseList;
    }

    // 7. Cevap yaz
    public ReplyResponse createReply(CreateReplyRequest request) {
        // Susturma kontrolü
        checkUserTimeout(request.getUserId());

        // Post kontrolü - Eğer çözüldüyse yorum eklenemez
        ForumPost post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post bulunamadı: " + request.getPostId()));

        if (post.isSolved()) {
            throw new RuntimeException("Bu soru çözüldü, yeni yorum eklenemez!");
        }

        ForumReply reply = new ForumReply();
        reply.setContent(request.getContent());
        reply.setImageUrl(request.getImageUrl()); // Görsel URL'sini kaydet
        reply.setUserId(request.getUserId());
        reply.setPostId(request.getPostId());
        reply.setDeleted(false);

        ForumReply savedReply = replyRepository.save(reply);
        return convertToReplyResponse(savedReply);
    }

    // 8. Cevap sil (sadece öğretmen - soft delete)
    public void deleteReply(UUID replyId, UUID teacherId) {
        // Öğretmen kontrolü
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + teacherId));

        if (!"TEACHER".equalsIgnoreCase(teacher.getRole())) {
            throw new RuntimeException("Bu işlemi sadece öğretmenler yapabilir!");
        }

        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new RuntimeException("Cevap bulunamadı: " + replyId));

        reply.setDeleted(true);
        replyRepository.save(reply);
    }

    // ========== YARDIMCI METODLAR ==========

    // Susturma kontrolü
    private void checkUserTimeout(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));

        if (user.getTimeoutUntil() != null && user.getTimeoutUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Hesabınız " + user.getTimeoutUntil() + " tarihine kadar susturulmuş durumda.");
        }
    }

    // ForumPost -> PostResponse dönüştürme
    private PostResponse convertToPostResponse(ForumPost post) {
        String userName = "Bilinmiyor";

        if (post.getUserId() != null) {
            User user = userRepository.findById(post.getUserId()).orElse(null);
            if (user != null && user.getFullName() != null) {
                userName = user.getFullName();
            }
        }

        int commentCount = replyRepository.countByPostIdAndIsDeletedFalse(post.getId());

        return new PostResponse(
                post.getId().toString(),
                post.getUserId() != null ? post.getUserId().toString() : null,
                userName,
                post.getTitle(),
                post.getContent(),
                post.getImageUrl(),
                post.getCategory(),
                post.isSolved(),
                commentCount,
                post.getAccessLevel() != null ? post.getAccessLevel() : "FREE",
                post.getCreatedAt());
    }

    // ForumReply -> ReplyResponse dönüştürme
    private ReplyResponse convertToReplyResponse(ForumReply reply) {
        String userName = "Bilinmiyor";

        if (reply.getUserId() != null) {
            User user = userRepository.findById(reply.getUserId()).orElse(null);
            if (user != null && user.getFullName() != null) {
                userName = user.getFullName();
            }
        }

        return new ReplyResponse(
                reply.getId().toString(),
                reply.getPostId() != null ? reply.getPostId().toString() : null,
                reply.getUserId() != null ? reply.getUserId().toString() : null,
                userName,
                reply.getContent(),
                reply.getImageUrl(),
                reply.isDeleted(),
                reply.getCreatedAt());
    }

    // Eski metodlar (geriye uyumluluk için - DEPRECATED)
    @Deprecated
    public List<ForumPost> getAllPosts() {
        return postRepository.findAll();
    }
}