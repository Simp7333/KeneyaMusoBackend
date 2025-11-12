package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Conversation;
import com.keneyamuso.model.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Conversation
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    @Query("SELECT c FROM Conversation c JOIN c.participants p WHERE p.id = :utilisateurId")
    List<Conversation> findByParticipantId(@Param("utilisateurId") Long utilisateurId);
    
    List<Conversation> findByActive(Boolean active);
    
    /**
     * Trouve une conversation entre exactement deux utilisateurs spécifiques
     */
    @Query("""
        SELECT c FROM Conversation c 
        WHERE SIZE(c.participants) = 2
        AND :utilisateur1 MEMBER OF c.participants 
        AND :utilisateur2 MEMBER OF c.participants
    """)
    List<Conversation> findConversationBetweenTwoUsers(
        @Param("utilisateur1") Utilisateur utilisateur1,
        @Param("utilisateur2") Utilisateur utilisateur2
    );
}

