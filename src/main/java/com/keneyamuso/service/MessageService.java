package com.keneyamuso.service;

import com.keneyamuso.dto.request.MessageRequest;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Conversation;
import com.keneyamuso.model.entity.Message;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.MessageType;
import com.keneyamuso.repository.ConversationRepository;
import com.keneyamuso.repository.MessageRepository;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * Service de gestion de la messagerie.
 * 
 * Ce service gère la communication sécurisée entre les patientes et les professionnels
 * de santé (médecins). Il permet :
 * 
 * - L'envoi de messages textuels dans des conversations
 * - La consultation de l'historique des échanges
 * - Le marquage des messages comme lus
 * - Le suivi des conversations actives
 * 
 * Chaque message est horodaté et lié à son expéditeur et à sa conversation.
 * Les conversations peuvent regrouper plusieurs participants (patiente + médecin(s)).
 * 
 * **Cas d'usage typiques :**
 * - Poser des questions sur la grossesse ou l'enfant
 * - Demander un conseil médical
 * - Recevoir des instructions du médecin
 * - Planifier un rendez-vous
 * 
 * @author KènèyaMuso Team
 * @version 1.0
 * @since 2025-10-16
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FileStorageService fileStorageService;

    /**
     * Envoie un nouveau message dans une conversation.
     * 
     * Cette méthode crée et enregistre un message dans une conversation existante.
     * Le message est automatiquement horodaté et marqué comme non lu.
     * L'expéditeur est identifié par son numéro de téléphone (extrait du token JWT).
     * 
     * @param request Les informations du message (contenu et ID de conversation)
     * @param telephoneExpediteur Le téléphone de l'expéditeur (authentifié via JWT)
     * @return Le message créé avec son identifiant et timestamp
     * @throws ResourceNotFoundException si la conversation n'existe pas
     * @throws ResourceNotFoundException si l'expéditeur n'existe pas
     */
    @Transactional
    public Message envoyerMessage(MessageRequest request, String telephoneExpediteur) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", request.getConversationId()));

        Utilisateur expediteur = utilisateurRepository.findByTelephone(telephoneExpediteur)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephoneExpediteur));

        Message message = new Message();
        message.setContenu(request.getContenu());
        message.setConversation(conversation);
        message.setExpediteur(expediteur);
        message.setLu(false);
        message.setType(MessageType.TEXTE);

        Message savedMessage = messageRepository.save(message);
        
        // Fetch the message with relationships to avoid lazy loading issues
        return messageRepository.findByIdWithRelations(savedMessage.getId());
    }

    @Transactional
    public Message envoyerFichier(Long conversationId, MultipartFile file, MessageType type, String telephoneExpediteur) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        Utilisateur expediteur = utilisateurRepository.findByTelephone(telephoneExpediteur)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephoneExpediteur));

        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/messages/download/")
                .path(fileName)
                .toUriString();

        Message message = new Message();
        message.setConversation(conversation);
        message.setExpediteur(expediteur);
        message.setLu(false);
        message.setType(type);
        message.setFileUrl(fileDownloadUri);
        // Le contenu peut être le nom du fichier original pour l'affichage
        message.setContenu(file.getOriginalFilename());

        Message savedMessage = messageRepository.save(message);
        
        // Fetch the message with relationships to avoid lazy loading issues
        return messageRepository.findByIdWithRelations(savedMessage.getId());
    }

    /**
     * Récupère tous les messages d'une conversation triés chronologiquement.
     * 
     * Cette méthode retourne l'historique complet d'une conversation,
     * avec les messages ordonnés du plus ancien au plus récent, permettant
     * de reconstituer le fil de discussion.
     * 
     * @param conversationId L'identifiant de la conversation
     * @return La liste des messages triés par timestamp (peut être vide)
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesByConversation(Long conversationId) {
        return messageRepository.findByConversationIdOrderByTimestamp(conversationId);
    }

    /**
     * Marque un message comme lu.
     * 
     * Cette méthode est appelée lorsqu'un utilisateur consulte un message.
     * Elle permet de suivre quels messages ont été vus et d'afficher les
     * notifications appropriées pour les messages non lus.
     * 
     * @param messageId L'identifiant du message à marquer comme lu
     * @throws ResourceNotFoundException si le message n'existe pas
     */
    @Transactional
    public void marquerCommeLu(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));
        message.setLu(true);
        messageRepository.save(message);
    }

    /**
     * Récupère tous les messages non lus d'un utilisateur.
     * 
     * Cette méthode retourne tous les messages reçus (non envoyés par l'utilisateur)
     * qui n'ont pas encore été marqués comme lus.
     * 
     * @param utilisateurId L'identifiant de l'utilisateur
     * @return La liste des messages non lus triés par date (plus récents en premier)
     */
    @Transactional(readOnly = true)
    public List<Message> getMessagesNonLusByUtilisateurId(Long utilisateurId) {
        return messageRepository.findUnreadMessagesByUtilisateurId(utilisateurId);
    }
}
