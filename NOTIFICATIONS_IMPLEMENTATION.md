# 🔔 Implémentation du Système de Notifications

## 📋 Vue d'ensemble

Ce document décrit l'implémentation complète du système de notifications pour l'application KènèyaMuso. Les notifications permettent d'alerter les patientes et les médecins sur des événements importants liés au suivi médical.

---

## 🏗️ Architecture

### Backend (Spring Boot)

#### 1. **Entité Rappel**
- **Fichier:** `src/main/java/com/keneyamuso/model/entity/Rappel.java`
- Représente une notification dans la base de données
- Champs principaux :
  - `message` : Contenu de la notification
  - `type` : Type de rappel (CPN, CPON, VACCINATION, CONSEIL)
  - `statut` : Statut (ENVOYE, LU, CONFIRME)
  - `utilisateur` : Destinataire de la notification
  - `dateEnvoi` : Date d'envoi
  - `dateCreation` : Date de création (audit)

#### 2. **RappelService**
- **Fichier:** `src/main/java/com/keneyamuso/service/RappelService.java`
- **Responsabilités :**
  - Récupérer les rappels d'un utilisateur
  - Marquer un rappel comme lu
  - **Convertir les `Rappel` en format compatible avec le frontend**

#### 3. **NotificationController**
- **Fichier:** `src/main/java/com/keneyamuso/controller/NotificationController.java`
- **Endpoints REST :**

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/notifications/patiente/{id}` | GET | Liste des notifications d'une patiente |
| `/api/notifications/medecin/{id}` | GET | Liste des notifications d'un médecin |
| `/api/notifications/me` | GET | Notifications de l'utilisateur connecté |
| `/api/notifications/{id}/lue` | PUT | Marquer comme lue |
| `/api/notifications/{id}/traitee` | PUT | Marquer comme traitée |
| `/api/notifications/{id}` | DELETE | Supprimer une notification |
| `/api/notifications/statistiques` | GET | Statistiques des notifications |

#### 4. **RappelRepository**
- **Fichier:** `src/main/java/com/keneyamuso/repository/RappelRepository.java`
- Méthodes de requête :
  - `findByUtilisateurId(Long utilisateurId)`
  - `findByType(TypeRappel type)`
  - `findByStatut(StatutRappel statut)`
  - `findByUtilisateurIdAndStatut(Long utilisateurId, StatutRappel statut)`

---

### Frontend (Flutter)

#### 1. **Modèles de Notifications**
- **Fichier:** `lib/models/notification_models.dart`
- **NotificationItem** : Modèle principal
  - `id`, `titre`, `message`
  - `type` : demandeSuivi, alerteUrgence, rappelConsultation, messagePatient, resultatExamen
  - `statut` : nonLue, lue, traitee, archivee
  - `priorite` : faible, normale, elevee, urgente
  - `patienteId`, `patienteNom`, `medecinId`, `medecinNom`
  - `donneesSupplementaires` : Données additionnelles (Map<String, dynamic>)

#### 2. **NotificationService**
- **Fichier:** `lib/services/notification_service.dart`
- **Méthodes :**
  - `getNotificationsByPatiente(int patienteId)` : Récupérer les notifications d'une patiente
  - `getNotificationsByMedecin(int medecinId)` : Récupérer les notifications d'un médecin
  - `marquerCommeLue(int notificationId)` : Marquer comme lue
  - `marquerCommeTraitee(int notificationId)` : Marquer comme traitée
  - `supprimerNotification(int notificationId)` : Supprimer une notification

#### 3. **Pages Frontend**
- **PageNotificationsPro** (`lib/pages/common/page_notifications_pro.dart`)
  - Liste toutes les notifications d'un médecin ou d'une patiente
  - Filtres par type et recherche
  - Groupement par période (Aujourd'hui, Cette semaine)
  - Navigation vers les détails

- **PageDetailNotification** (`lib/pages/common/page_detail_notification.dart`)
  - Affichage détaillé d'une notification
  - Actions : Oui / Non (pour les demandes de suivi)

- **PageAlertes** (`lib/pages/gynecologue/page_alertes.dart`)
  - Affichage des soumissions de dossiers médicaux en attente
  - Navigation vers PageDetailAlerte

- **PageDetailAlerte** (`lib/pages/gynecologue/page_detail_alerte.dart`)
  - Détails d'une soumission de dossier
  - Actions : Approuver / Rejeter / **Voir le dossier**
  - Redirection vers PageDossierPatiente ou PageDossierPostnatal selon le type

---

## 🔄 Mapping Backend ↔️ Frontend

### Types de Rappels
| Backend (TypeRappel) | Frontend (NotificationType) |
|---------------------|---------------------------|
| CPN | RAPPEL_CONSULTATION |
| CPON | RAPPEL_CONSULTATION |
| VACCINATION | RAPPEL_VACCINATION |
| CONSEIL | CONSEIL |
| - | demandeSuivi |
| - | alerteUrgence |
| - | messagePatient |
| - | resultatExamen |

### Statuts
| Backend (StatutRappel) | Frontend (NotificationStatus) |
|----------------------|----------------------------|
| ENVOYE | NON_LUE |
| LU | LUE |
| CONFIRME | TRAITEE |

### Priorités
Les priorités sont déterminées automatiquement selon le type :
- **CPN/CPON** : ELEVEE
- **VACCINATION** : NORMALE
- **CONSEIL** : FAIBLE

---

## 🔔 Génération Automatique des Notifications

### 1. **Soumissions de Dossier Médical**
Lorsqu'une soumission est approuvée ou rejetée par un médecin, une notification est automatiquement envoyée à la patiente.

**Fichier:** `DossierMedicalSubmissionService.java`
```java
private void envoyerAlerteApprobation(DossierMedicalSubmission submission) {
    Rappel rappel = new Rappel();
    rappel.setUtilisateur(submission.getPatiente());
    rappel.setType(TypeRappel.CPN ou CPON);
    rappel.setMessage("Votre formulaire a été approuvé par votre médecin.");
    rappel.setStatut(StatutRappel.ENVOYE);
    rappel.setDateEnvoi(LocalDateTime.now());
    rappelRepository.save(rappel);
}
```

---

## 🎯 Cas d'Usage

### 1. **Patiente consulte ses notifications**
1. La patiente ouvre la page "Mes Notifications"
2. L'app appelle `GET /api/notifications/me`
3. Les rappels sont récupérés et mappés au format frontend
4. Affichage avec filtres et recherche

### 2. **Médecin approuve/rejette une soumission**
1. Le médecin approuve ou rejette une soumission dans PageDetailAlerte
2. Backend :
   - Met à jour le statut de la soumission
   - Crée automatiquement un Rappel pour la patiente
3. La patiente reçoit la notification lors de la prochaine consultation

### 3. **Médecin consulte les alertes de soumissions**
1. Le médecin ouvre PageAlertes
2. Affichage des soumissions en attente (via DossierMedicalSubmissionController)
3. Navigation vers PageDetailAlerte
4. Possibilité d'ouvrir directement le dossier de la patiente

---

## 📱 Interface Utilisateur

### Page Notifications Pro
- **Barre de recherche** en haut
- **Filtres par type** (Toutes, Demandes, Alertes, Messages, Rappels)
- **Groupement temporel** :
  - Aujourd'hui
  - Cette semaine
- **Indicateur visuel** pour les notifications non lues
- **Badge URGENT** pour les priorités élevées

### Page Detail Alerte
- **Informations de la soumission**
- **Actions** :
  - 🟢 Approuver (vert)
  - 🔴 Rejeter (gris)
  - 🔵 Voir le dossier (outline pink)
- **Navigation intelligente** vers le bon type de dossier (prénatal/postnatal)

---

## 🔧 Configuration

### Backend
Aucune configuration supplémentaire requise. Les notifications utilisent les mêmes entités et repositories existants.

### Frontend
Les endpoints sont déjà configurés dans :
- `lib/services/api_config.dart` : `static const String notifications = '/notifications';`
- `lib/services/notification_service.dart` : Utilise `ApiClient` pour les requêtes

---

## 🚀 Prochaines Évolutions

### Court terme
- [ ] Notifications push via Firebase Cloud Messaging
- [ ] Notifications en temps réel via WebSockets
- [ ] Système de badges pour les notifications non lues

### Moyen terme
- [ ] Notifications programmées (rappel avant consultation)
- [ ] Historique complet des notifications
- [ ] Préférences de notifications par utilisateur

### Long terme
- [ ] Notifications SMS (intégration Twilio/Orange)
- [ ] Notifications vocales pour patientes
- [ ] Intelligence artificielle pour prioriser les notifications

---

## 📝 Notes Techniques

### Sécurité
- Toutes les endpoints sont protégées par `@SecurityRequirement(name = "bearerAuth")`
- Authentification via Spring Security
- Validation des utilisateurs avant envoi de notifications

### Performance
- Utilisation de `FetchType.LAZY` pour les relations
- Pagination possible pour les grandes listes (à implémenter)
- Cache Redis pour les notifications fréquentes (future optimisation)

### Tests
- Tests unitaires pour RappelService
- Tests d'intégration pour NotificationController
- Tests frontend pour NotificationService

---

## ✅ Checklist de Déploiement

- [x] Création de RappelService
- [x] Création de NotificationController
- [x] Mise à jour de RappelRepository
- [x] Mapping des entités vers le format frontend
- [x] Intégration avec DossierMedicalSubmissionService
- [x] Pages frontend existantes et fonctionnelles
- [x] Service frontend configuré
- [ ] Tests backend
- [ ] Tests frontend
- [ ] Documentation Swagger
- [ ] Documentation utilisateur

---

**Dernière mise à jour :** 2025-01-02
**Auteur :** KènèyaMuso Team

