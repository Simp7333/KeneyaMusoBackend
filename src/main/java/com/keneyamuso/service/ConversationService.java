package com.keneyamuso.service;

import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Conversation;
import com.keneyamuso.model.entity.Patiente;
import com.keneyamuso.model.entity.ProfessionnelSante;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.repository.ConversationRepository;
import com.keneyamuso.repository.PatienteRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Service de gestion des conversations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PatienteRepository patienteRepository;

    /**
     * Obtient ou crée une conversation entre une patiente et son médecin assigné
     * 
     * @param patienteId L'ID de la patiente
     * @return La conversation (existante ou nouvellement créée)
     */
    @Transactional
    public Conversation getOrCreateConversationWithMedecin(Long patienteId) {
        Patiente patiente = patienteRepository.findById(patienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Patiente", "id", patienteId));
        
        ProfessionnelSante medecin = patiente.getProfessionnelSanteAssigne();
        if (medecin == null) {
            throw new IllegalStateException("Cette patiente n'a pas de médecin assigné");
        }
        
        // Vérifier si une conversation existe déjà
        List<Conversation> existingConversations = conversationRepository
                .findConversationBetweenTwoUsers(patiente, medecin);
        
        if (!existingConversations.isEmpty()) {
            log.info("Conversation existante trouvée: {}", existingConversations.get(0).getId());
            return existingConversations.get(0);
        }
        
        // Créer une nouvelle conversation
        Conversation conversation = new Conversation();
        conversation.setTitre(String.format("Chat: %s %s ↔ Dr. %s %s",
                patiente.getPrenom(), patiente.getNom(),
                medecin.getPrenom(), medecin.getNom()));
        conversation.setParticipants(Arrays.asList(patiente, medecin));
        conversation.setActive(true);
        
        Conversation savedConversation = conversationRepository.save(conversation);
        log.info("Nouvelle conversation créée: {} entre patiente {} et médecin {}",
                savedConversation.getId(), patienteId, medecin.getId());
        
        return savedConversation;
    }
    
    /**
     * Obtient toutes les conversations d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<Conversation> getConversationsByUtilisateur(Long utilisateurId) {
        return conversationRepository.findByParticipantId(utilisateurId);
    }
    
    /**
     * Obtient une conversation par son ID
     */
    @Transactional(readOnly = true)
    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", id));
    }
}

