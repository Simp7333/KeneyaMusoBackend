# 🔔 Système de Rappels Automatiques

## 📋 Vue d'Ensemble

Le système de rappels automatiques génère et envoie des notifications aux patientes pour :
- ✅ **Consultations Prénatales (CPN)** - 1 jour avant
- ✅ **Consultations Postnatales (CPON)** - 1 jour avant  
- ✅ **Vaccinations** - 2 jours avant

## 🏗️ Architecture

### 1. **Service Principal** : `RappelService.java`

```
RappelService
├── Génération automatique de rappels
│   ├── creerRappelCPN(consultation)
│   ├── creerRappelCPON(consultation)
│   └── creerRappelVaccination(vaccination)
│
├── Envoi quotidien automatique
│   └── envoyerRappelsQuotidiens() @Scheduled(cron = "0 0 8 * * *")
│
└── Mapping pour le frontend
    └── rappelToNotificationMap(rappel)
```

### 2. **Scheduler Quotidien**

**Exécution** : Tous les jours à **8h00 du matin**

**Processus** :
1. Récupère toutes les CPN prévues demain (statut `A_VENIR`)
2. Récupère toutes les CPON prévues demain (statut `A_VENIR`)
3. Récupère toutes les vaccinations prévues dans 2 jours (statut `A_FAIRE`)
4. Crée un rappel pour chaque consultation/vaccination si pas déjà créé
5. Log le nombre de rappels envoyés

**Logs produits** :
```
=== DÉBUT ENVOI RAPPELS QUOTIDIENS ===
Création rappel CPN pour patiente 15 - Date: 2025-11-12
Création rappel CPON pour patiente 23 - Date: 2025-11-12
Création rappel Vaccination pour patiente 18 - Enfant: Amina - Date: 2025-11-13
Rappels envoyés - CPN: 3, CPON: 5, Vaccinations: 7
=== FIN ENVOI RAPPELS QUOTIDIENS ===
```

## 📊 Contenu des Rappels

### CPN (Consultation Prénatale)
```
Titre: "Rappel Consultation Prénatale"
Message: "Rappel : Vous avez une consultation prénatale prévue demain, 
          le 12/11/2025. N'oubliez pas votre carnet de suivi."
Priorité: ELEVEE
Type: RAPPEL_CONSULTATION
```

### CPON (Consultation Postnatale)
```
Titre: "Rappel Consultation Postnatale"
Message: "Rappel : Consultation postnatale J+3 prévue demain, 
          le 12/11/2025. Prenez soin de vous et de votre bébé."
Priorité: ELEVEE
Type: RAPPEL_CONSULTATION
```

### Vaccination
```
Titre: "Rappel Vaccination"
Message: "Rappel : Vaccination de Amina (BCG) prévue le 13/11/2025. 
          Pensez à apporter le carnet de santé de votre enfant."
Priorité: NORMALE
Type: RAPPEL_VACCINATION
```

## 🔧 Configuration

### Activer le Scheduling
Le scheduling est activé dans `KeneyaMusoApplication.java` :

```java
@SpringBootApplication
@EnableScheduling  // ← Active les tâches planifiées
public class KeneyaMusoApplication {
    // ...
}
```

### Modifier l'Heure d'Envoi
Dans `RappelService.java`, ligne 297 :

```java
@Scheduled(cron = "0 0 8 * * *") // Format: sec min hour day month dayOfWeek
```

**Exemples** :
- `"0 0 8 * * *"` → Tous les jours à 8h
- `"0 0 9 * * *"` → Tous les jours à 9h
- `"0 30 7 * * *"` → Tous les jours à 7h30
- `"0 0 8 * * MON-FRI"` → Du lundi au vendredi à 8h

### Modifier le Délai d'Avance
Dans `RappelService.java` :

```java
// CPN : 1 jour avant
LocalDateTime dateEnvoi = datePrevue.minusDays(1).atTime(9, 0);

// Vaccination : 2 jours avant
LocalDateTime dateEnvoi = datePrevue.minusDays(2).atTime(9, 0);
```

## 🧪 Tests

### 1. Test Manuel via API

**Endpoint** : `POST /api/notifications/envoyer-rappels-manuel`

**Authorization** : Bearer Token requis

**Exemple cURL** :
```bash
curl -X POST http://localhost:8080/api/notifications/envoyer-rappels-manuel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Réponse** :
```json
{
  "success": true,
  "message": "Rappels envoyés manuellement",
  "data": {
    "cpnDemain": 3,
    "cponDemain": 5,
    "vaccinationsDans2Jours": 7
  },
  "timestamp": "2025-11-10T14:30:00"
}
```

### 2. Vérifier les Logs

Recherchez dans les logs Spring Boot :
```
=== DÉBUT ENVOI RAPPELS QUOTIDIENS ===
```

### 3. Créer des Données de Test

Pour tester, créez des consultations avec des dates futures :

```sql
-- CPN pour demain
INSERT INTO consultations_prenatales (date_prevue, statut, grossesse_id) 
VALUES (DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'A_VENIR', 1);

-- CPON pour demain
INSERT INTO consultations_postnatales (date_prevue, statut, type, patiente_id) 
VALUES (DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'A_VENIR', 'JOUR_3', 15);

-- Vaccination dans 2 jours
INSERT INTO vaccinations (date_prevue, statut, nom_vaccin, enfant_id) 
VALUES (DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'A_FAIRE', 'BCG', 5);
```

## 📱 Intégration Flutter

### Service Notification
Le Flutter appelle déjà :
```dart
GET /api/notifications/patiente/{patienteId}
GET /api/notifications/me
PUT /api/notifications/{notificationId}/lue
```

### Widget Notification
Dans `page_notifications.dart`, les notifications s'affichent automatiquement avec :
- Badge rouge pour les notifications non lues
- Couleurs selon le type (CPN, CPON, Vaccination)
- Tri par date (Aujourd'hui / Cette semaine)

## 🔐 Sécurité

✅ Tous les endpoints sont protégés par JWT  
✅ Une patiente ne voit QUE ses propres notifications  
✅ Les rappels ne sont créés qu'une seule fois (vérification `existsBy...`)  
✅ Les erreurs sont loggées mais n'arrêtent pas le scheduler

## 📈 Statistiques

**Endpoint** : `GET /api/notifications/statistiques`

**Réponse** :
```json
{
  "success": true,
  "data": {
    "total": 45,
    "nonLues": 12,
    "lues": 33
  }
}
```

## 🐛 Dépannage

### Problème : Les rappels ne sont pas envoyés

**Solutions** :
1. Vérifier que `@EnableScheduling` est présent
2. Vérifier les logs pour les erreurs
3. Tester manuellement avec `POST /api/notifications/envoyer-rappels-manuel`
4. Vérifier qu'il existe des consultations avec la bonne date et le bon statut

### Problème : Rappels en double

**Cause** : Les méthodes `existsBy...` vérifient déjà l'existence

**Vérification** :
```sql
SELECT COUNT(*), consultation_prenatale_id 
FROM rappels 
WHERE type = 'CPN'
GROUP BY consultation_prenatale_id
HAVING COUNT(*) > 1;
```

### Problème : Mauvaise heure d'envoi

**Solution** : Vérifier le timezone du serveur

```java
// Dans RappelService.java, ajouter :
ZoneId zoneId = ZoneId.of("Africa/Bamako");
LocalDateTime dateEnvoi = datePrevue.minusDays(1)
    .atTime(9, 0)
    .atZone(zoneId)
    .toLocalDateTime();
```

## 📝 TODO Futur

- [ ] Ajouter des rappels SMS via API (ex: Twilio)
- [ ] Permettre aux patientes de configurer l'heure des rappels
- [ ] Ajouter des rappels pour les rendez-vous médicaux spéciaux
- [ ] Dashboard admin pour voir les stats d'envoi
- [ ] Notification push mobile (Firebase)

## 🎯 Résumé

| Fonctionnalité | Statut | Détails |
|----------------|--------|---------|
| Rappels CPN | ✅ | 1 jour avant, automatique |
| Rappels CPON | ✅ | 1 jour avant, automatique |
| Rappels Vaccination | ✅ | 2 jours avant, automatique |
| Scheduler quotidien | ✅ | 8h00 chaque jour |
| Test manuel | ✅ | POST /envoyer-rappels-manuel |
| Détection doublons | ✅ | existsBy... |
| Logs complets | ✅ | Début/Fin/Compteurs |
| Intégration Flutter | ✅ | Page notifications |

---

**Développé par l'équipe KènèyaMuso** 🇲🇱

