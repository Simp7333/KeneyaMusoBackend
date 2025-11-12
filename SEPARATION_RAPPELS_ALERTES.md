# 🔔 Séparation Rappels et Alertes

## 📊 Vue d'Ensemble

Le système distingue maintenant clairement :
- **🔔 Rappels** = Notifications automatiques (CPN, CPON, Vaccinations)
- **⚠️ Alertes** = Soumissions de dossiers médicaux en attente de validation

---

## 🏗️ Architecture

### Backend (Java)

#### **1. Modèle de Données**

```java
// DashboardStatsResponse.java
public class DashboardStatsResponse {
    private long totalPatientes;
    private long suivisTermines;
    private long suivisEnCours;
    private long rappelsActifs;    // ← Rappels CPN/CPON/Vaccination non lus
    private long alertesActives;   // ← Soumissions de dossiers EN_ATTENTE
}
```

#### **2. Service**

```java
// DashboardService.java
public DashboardStatsResponse getMedecinDashboardStats(String telephone) {
    // ...
    
    // Rappels = Notifications CPN/CPON/Vaccination non lues
    long rappelsActifs = rappelRepository.countByProfessionnelIdAndStatut(
        professionnelSante.getId(),
        StatutRappel.ENVOYE  // Statut = non lu
    );
    
    // Alertes = Soumissions de dossiers en attente
    long alertesActives = submissionRepository.countByProfessionnelSanteIdAndStatus(
        professionnelSante.getId(),
        SubmissionStatus.EN_ATTENTE  // Dossiers à valider
    );
    
    return DashboardStatsResponse.builder()
        .rappelsActifs(rappelsActifs)
        .alertesActives(alertesActives)
        .build();
}
```

#### **3. API**

**Endpoint** : `GET /api/dashboard/medecin`

**Réponse** :
```json
{
  "success": true,
  "data": {
    "totalPatientes": 45,
    "suivisEnCours": 12,
    "suivisTermines": 33,
    "rappelsActifs": 3,     // ← Rappels automatiques
    "alertesActives": 2     // ← Soumissions de dossiers
  }
}
```

---

### Frontend (Flutter)

#### **1. Modèle**

```dart
// dashboard_stats.dart
class DashboardStats {
  final int rappelsActifs;    // Rappels CPN/CPON/Vaccination
  final int alertesActives;   // Soumissions de dossiers
  
  factory DashboardStats.fromJson(Map<String, dynamic> json) {
    return DashboardStats(
      rappelsActifs: _asInt(json['rappelsActifs']),
      alertesActives: _asInt(json['alertesActives']),
    );
  }
}
```

#### **2. Dashboard**

```dart
// stats_grid.dart - Maintenant 5 cartes
GridView.count(
  crossAxisCount: 2,
  children: [
    StatCard(label: 'Patients suivies', ...),
    StatCard(label: 'Suivis terminés', ...),
    StatCard(label: 'Suivis en attente', ...),
    
    // Carte Rappels (vert)
    StatCard(
      value: stats.rappelsActifs,
      label: 'Rappels',
      icon: Icons.notifications_outlined,
      color: Colors.green,  // Vert pour info
      onTap: () => Navigator.pushNamed(..., AppRoutes.proNotifications),
    ),
    
    // Carte Alertes (rouge)
    StatCard(
      value: stats.alertesActives,
      label: 'Alertes urgentes',
      icon: Icons.warning_amber_outlined,
      color: Colors.red,  // Rouge pour urgence
      onTap: () => Navigator.pushNamed(..., AppRoutes.proAlertes),
    ),
  ],
)
```

---

## 📱 Affichage Visuel

### **Dashboard (Vue Médecin)**

```
╔════════════════════╦════════════════════╗
║  👥  45            ║  ✓  33            ║
║  Patients suivies  ║  Suivis terminés   ║
╠════════════════════╬════════════════════╣
║  ⏳  12            ║  🔔  3            ║
║  Suivis en attente ║  Rappels          ║  ← Vert (CPN/CPON/Vaccin)
╠════════════════════╩════════════════════╣
║  ⚠️   2                                  ║
║  Alertes urgentes                       ║  ← Rouge (Dossiers à valider)
╚═════════════════════════════════════════╝
```

---

## 🎯 Différenciation

| Critère | Rappels 🔔 | Alertes ⚠️ |
|---------|-----------|-----------|
| **Source** | Système automatique | Patientes (soumissions) |
| **Type** | CPN, CPON, Vaccination | Dossiers médicaux |
| **Fréquence** | Quotidien (8h du matin) | À la demande |
| **Couleur** | 🟢 Vert | 🔴 Rouge |
| **Urgence** | Information | Urgente |
| **Action** | Consulter notifications | Valider/Rejeter dossier |
| **Navigation** | Page Notifications | Page Alertes |
| **Endpoint** | `/api/notifications/me` | `/api/dossiers/submissions/medecin` |

---

## 🔄 Flux de Données

### **Rappels Automatiques**

```
1. Chaque jour à 8h00
   ↓
2. RappelService.envoyerRappelsQuotidiens()
   ↓
3. Créer rappels pour CPN/CPON/Vaccination demain/dans 2 jours
   ↓
4. Statut = ENVOYE (non lu)
   ↓
5. Comptés dans rappelsActifs
   ↓
6. Affichés dans carte "Rappels" (vert)
   ↓
7. Clic → Page Notifications
```

### **Alertes Soumissions**

```
1. Patiente soumet son dossier médical
   ↓
2. POST /api/dossiers/submissions
   ↓
3. Status = EN_ATTENTE
   ↓
4. Assigné au médecin de la patiente
   ↓
5. Compté dans alertesActives
   ↓
6. Affiché dans carte "Alertes urgentes" (rouge)
   ↓
7. Clic → Page Alertes
   ↓
8. Médecin valide ou rejette
   ↓
9. Status = APPROUVE ou REJETE
   ↓
10. Disparaît du compteur alertesActives
```

---

## 🧪 Tests

### **1. Tester les Rappels**

```bash
# Créer un rappel CPN pour demain
curl -X POST http://localhost:8080/api/notifications/envoyer-rappels-manuel \
  -H "Authorization: Bearer TOKEN_MEDECIN"
```

**Vérifier** :
- ✅ `rappelsActifs` augmente
- ✅ Carte "Rappels" affiche le nouveau total
- ✅ Clic → Navigation vers notifications
- ✅ Les 3 rappels s'affichent dans la liste

### **2. Tester les Alertes**

```bash
# Créer une soumission de dossier
curl -X POST http://localhost:8080/api/dossiers/submissions \
  -H "Authorization: Bearer TOKEN_PATIENTE" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "CPN",
    "data": {"poids": 65, "tension": "12/8"}
  }'
```

**Vérifier** :
- ✅ `alertesActives` augmente
- ✅ Carte "Alertes urgentes" affiche le nouveau total
- ✅ Clic → Navigation vers alertes
- ✅ La soumission s'affiche dans la liste

---

## 📊 Logs de Debug

Dans la console Flutter, vous verrez maintenant :

```
📊 Stats Dashboard:
   - Patientes: 45
   - Suivis en cours: 12
   - Suivis terminés: 33
   - 🔔 Rappels actifs (CPN/CPON/Vaccination): 3
   - ⚠️ Alertes actives (Soumissions dossiers): 2
```

---

## 🎨 Code Couleurs

| Élément | Couleur | Code | Signification |
|---------|---------|------|---------------|
| Patients | Bleu | `Colors.blue` | Information neutre |
| Suivis terminés | Vert | `Colors.green` | Positif |
| Suivis en attente | Ambre | `Colors.amber` | Attention |
| **Rappels** | **Vert** | **`Color(0xFF4CAF50)`** | **Information utile** |
| **Alertes** | **Rouge** | **`Colors.red`** | **Action requise** |

---

## ✅ Avantages de la Séparation

1. **✅ Clarté** : Le médecin sait immédiatement ce qui est informatif vs urgent
2. **✅ Priorisation** : Les alertes rouges attirent l'attention en premier
3. **✅ Navigation** : 2 pages distinctes pour 2 types de contenu différents
4. **✅ Filtrage** : Possibilité de gérer séparément les rappels et les validations
5. **✅ Scalabilité** : Facile d'ajouter d'autres types d'alertes plus tard

---

## 🔮 Évolutions Futures

- [ ] Badge animé si alertesActives > 0
- [ ] Son de notification pour nouvelles soumissions urgentes
- [ ] Filtres avancés dans page Rappels (CPN/CPON/Vaccination)
- [ ] Statistiques par type de rappel
- [ ] Historique des alertes traitées

---

**Développé par l'équipe KènèyaMuso** 🇲🇱

