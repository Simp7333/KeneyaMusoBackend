package com.keneyamuso.controller;

import com.keneyamuso.dto.request.MessageRequest;
import com.keneyamuso.dto.response.ApiResponse;
import com.keneyamuso.dto.response.MessageDto;
import com.keneyamuso.exception.ResourceNotFoundException;
import com.keneyamuso.model.entity.Message;
import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.model.enums.MessageType;
import com.keneyamuso.repository.UtilisateurRepository;
import com.keneyamuso.service.MessageService;
import com.keneyamuso.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Controller pour la gestion des messages
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Messages", description = "APIs pour la messagerie avec les professionnels de santé")
public class MessageController {

    private final MessageService messageService;
    private final FileStorageService fileStorageService;
    private final UtilisateurRepository utilisateurRepository;


    @PostMapping
    @Operation(summary = "Envoyer un message texte", description = "Envoie un message texte dans une conversation")
    public ResponseEntity<ApiResponse<MessageDto>> envoyerMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        String telephone = authentication.getName();
        Message message = messageService.envoyerMessage(request, telephone);
        
        // Mapper vers DTO
        MessageDto messageDto = MessageDto.builder()
                .id(message.getId())
                .type(message.getType())
                .contenu(message.getContenu())
                .fileUrl(message.getFileUrl())
                .conversationId(message.getConversationId())
                .expediteurId(message.getExpediteurId())
                .expediteurNom(message.getExpediteurNom())
                .expediteurPrenom(message.getExpediteurPrenom())
                .expediteurTelephone(message.getExpediteurTelephone())
                .lu(message.getLu())
                .timestamp(message.getTimestamp())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message envoyé", messageDto));
    }
    
    @PostMapping("/upload/image")
    @Operation(summary = "Envoyer une image", description = "Envoie une image dans une conversation")
    public ResponseEntity<ApiResponse<Message>> envoyerImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") Long conversationId,
            Authentication authentication) {
        String telephone = authentication.getName();
        Message message = messageService.envoyerFichier(conversationId, file, MessageType.IMAGE, telephone);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image envoyée", message));
    }

    @PostMapping("/upload/audio")
    @Operation(summary = "Envoyer un message audio", description = "Envoie un fichier audio dans une conversation")
    public ResponseEntity<ApiResponse<Message>> envoyerAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") Long conversationId,
            Authentication authentication) {
        String telephone = authentication.getName();
        Message message = messageService.envoyerFichier(conversationId, file, MessageType.AUDIO, telephone);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message audio envoyé", message));
    }

    @PostMapping("/upload/document")
    @Operation(summary = "Envoyer un document", description = "Envoie un document dans une conversation")
    public ResponseEntity<ApiResponse<Message>> envoyerDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") Long conversationId,
            Authentication authentication) {
        String telephone = authentication.getName();
        Message message = messageService.envoyerFichier(conversationId, file, MessageType.DOCUMENT, telephone);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document envoyé", message));
    }
    
    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Path filePath = Paths.get("./uploads").resolve(fileName).normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                // Détecter le type MIME selon l'extension du fichier
                String contentType = "application/octet-stream";
                String contentDisposition = "inline"; // inline pour permettre la lecture directe
                
                String lowerFileName = fileName.toLowerCase();
                if (lowerFileName.endsWith(".m4a") || lowerFileName.endsWith(".mp4")) {
                    contentType = "audio/mp4";
                } else if (lowerFileName.endsWith(".mp3")) {
                    contentType = "audio/mpeg";
                } else if (lowerFileName.endsWith(".wav")) {
                    contentType = "audio/wav";
                } else if (lowerFileName.endsWith(".ogg")) {
                    contentType = "audio/ogg";
                } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowerFileName.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowerFileName.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowerFileName.endsWith(".pdf")) {
                    contentType = "application/pdf";
                    contentDisposition = "attachment"; // Les PDFs sont téléchargés
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition + "; filename=\"" + resource.getFilename() + "\"")
                        .header(HttpHeaders.ACCEPT_RANGES, "bytes") // Permet la lecture en streaming
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "Obtenir les messages d'une conversation", 
               description = "Récupère tous les messages d'une conversation")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessagesByConversation(@PathVariable Long conversationId) {
        List<Message> messages = messageService.getMessagesByConversation(conversationId);
        
        // Mapper vers DTOs
        List<MessageDto> messageDtos = messages.stream()
                .map(m -> MessageDto.builder()
                        .id(m.getId())
                        .type(m.getType())
                        .contenu(m.getContenu())
                        .fileUrl(m.getFileUrl())
                        .conversationId(m.getConversationId())
                        .expediteurId(m.getExpediteurId())
                        .expediteurNom(m.getExpediteurNom())
                        .expediteurPrenom(m.getExpediteurPrenom())
                        .expediteurTelephone(m.getExpediteurTelephone())
                        .lu(m.getLu())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Messages trouvés", messageDtos));
    }

    @PutMapping("/{id}/lire")
    @Operation(summary = "Marquer un message comme lu", description = "Marque un message comme lu")
    public ResponseEntity<ApiResponse<String>> marquerCommeLu(@PathVariable Long id) {
        messageService.marquerCommeLu(id);
        return ResponseEntity.ok(ApiResponse.success("Message marqué comme lu", null));
    }

    @GetMapping("/non-lus")
    @Operation(summary = "Obtenir les messages non lus", 
               description = "Récupère tous les messages non lus de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessagesNonLus(
            Authentication authentication) {
        String telephone = authentication.getName();
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "telephone", telephone));
        
        List<Message> messages = messageService.getMessagesNonLusByUtilisateurId(utilisateur.getId());
        
        // Mapper vers DTOs
        List<MessageDto> messageDtos = messages.stream()
                .map(m -> MessageDto.builder()
                        .id(m.getId())
                        .type(m.getType())
                        .contenu(m.getContenu())
                        .fileUrl(m.getFileUrl())
                        .conversationId(m.getConversationId())
                        .expediteurId(m.getExpediteurId())
                        .expediteurNom(m.getExpediteurNom())
                        .expediteurPrenom(m.getExpediteurPrenom())
                        .expediteurTelephone(m.getExpediteurTelephone())
                        .lu(m.getLu())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success("Messages non lus trouvés", messageDtos));
    }
}

