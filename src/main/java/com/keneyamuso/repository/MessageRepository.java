package com.keneyamuso.repository;

import com.keneyamuso.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Message
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId")
    List<Message> findByConversationId(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Message m WHERE m.expediteur.id = :expediteurId")
    List<Message> findByExpediteurId(@Param("expediteurId") Long expediteurId);
    
    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.conversation " +
           "LEFT JOIN FETCH m.expediteur " +
           "WHERE m.conversation.id = :conversationId " +
           "ORDER BY m.timestamp ASC")
    List<Message> findByConversationIdOrderByTimestamp(@Param("conversationId") Long conversationId);
    
    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.conversation " +
           "LEFT JOIN FETCH m.expediteur " +
           "WHERE m.id = :id")
    Message findByIdWithRelations(@Param("id") Long id);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id = :conversationId AND m.lu = false AND m.expediteur.id != :utilisateurId")
    Long countUnreadMessages(@Param("conversationId") Long conversationId, 
                             @Param("utilisateurId") Long utilisateurId);
}

