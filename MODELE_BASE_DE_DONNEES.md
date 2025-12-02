# Modèle de Base de Données - Keneya Muso

## Vue d'ensemble

Ce document décrit toutes les tables de la base de données, leurs attributs, relations et cardinalités pour créer le diagramme de classe UML.

## Héritage des Entités

L'application utilise une stratégie d'héritage **JOINED** :
- **Utilisateur** (classe de base)
  - **Patiente** (table: `patientes`)
  - **ProfessionnelSante** (table: `professionnels_sante`)

---

## 1. UTILISATEUR (Table: `utilisateurs`)

**Type:** Classe de base abstraite avec héritage JOINED

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| nom | String | NOT NULL | Nom de l'utilisateur |
| prenom | String | NOT NULL | Prénom de l'utilisateur |
| telephone | String | NOT NULL, UNIQUE | Téléphone (format: +?[0-9]{8,15}) |
| motDePasse | String | NOT NULL | Mot de passe hashé |
| role | RoleUtilisateur (Enum) | NOT NULL | Rôle (PATIENTE, MEDECIN, etc.) |
| langue | String | NOT NULL, DEFAULT 'fr' | Langue préférée |
| actif | Boolean | NOT NULL, DEFAULT true | Statut actif/inactif |
| photoProfil | String(500) | NULLABLE | URL de la photo de profil |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
- **Héritage:** Classe mère pour Patiente et ProfessionnelSante (stratégie JOINED)

---

## 2. PATIENTE (Table: `patientes`)

**Type:** Entité fille (hérite de Utilisateur)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, FK vers `utilisateurs.id` | Identifiant (hérité) |
| dateDeNaissance | LocalDate | NOT NULL | Date de naissance |
| adresse | String(500) | NULLABLE | Adresse complète |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| dossierMedical | DossierMedical | **1:1** | OneToOne (mappedBy) | - |
| professionnelSanteAssigne | ProfessionnelSante | **N:1** | ManyToOne | `professionnel_sante_id` |
| grossesses | Grossesse | **1:N** | OneToMany (mappedBy) | - |
| enfants | Enfant | **1:N** | OneToMany (mappedBy) | - |
| conversations | Conversation | **N:N** | ManyToMany (mappedBy) | Table: `conversation_participants` |

---

## 3. PROFESSIONNEL_SANTE (Table: `professionnels_sante`)

**Type:** Entité fille (hérite de Utilisateur)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, FK vers `utilisateurs.id` | Identifiant (hérité) |
| specialite | Specialite (Enum) | NOT NULL | Spécialité médicale |
| identifiantProfessionnel | String | NOT NULL, UNIQUE | Numéro d'identification professionnel |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patientes | Patiente | **1:N** | OneToMany (mappedBy) | - |
| conversations | Conversation | **N:N** | ManyToMany (mappedBy) | Table: `conversation_participants` |
| conseils | Conseil | **1:N** | OneToMany (mappedBy) | - |
| ordonnances | Ordonnance | **1:N** | OneToMany (mappedBy) | - |
| dossierMedicalSubmissions | DossierMedicalSubmission | **1:N** | OneToMany (mappedBy) | - |

---

## 4. GROSSESSE (Table: `grossesses`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| dateDebut | LocalDate | NOT NULL | Date de début de grossesse |
| datePrevueAccouchement | LocalDate | NOT NULL | Date prévue d'accouchement (DPA) |
| statut | StatutGrossesse (Enum) | NOT NULL, DEFAULT 'EN_COURS' | Statut de la grossesse |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **N:1** | ManyToOne | `patiente_id` |
| consultationsPrenatales | ConsultationPrenatale | **1:N** | OneToMany (mappedBy) | - |

---

## 5. ENFANT (Table: `enfants`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| nom | String | NOT NULL | Nom de l'enfant |
| prenom | String | NOT NULL | Prénom de l'enfant |
| dateDeNaissance | LocalDate | NOT NULL | Date de naissance |
| sexe | Sexe (Enum) | NOT NULL, VARCHAR(20) | Sexe (GARCON, FILLE) |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **N:1** | ManyToOne | `patiente_id` |
| vaccinations | Vaccination | **1:N** | OneToMany (mappedBy) | - |
| consultationsPostnatales | ConsultationPostnatale | **1:N** | OneToMany (mappedBy) | - |

---

## 6. DOSSIER_MEDICAL (Table: `dossiers_medicaux`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| antecedentsMedicaux | TEXT | NULLABLE | Antécédents médicaux |
| allergies | TEXT | NULLABLE | Liste des allergies |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **1:1** | OneToOne (FK unique) | `patiente_id` (UNIQUE) |
| formulairesCPN | FormulaireCPN | **1:N** | OneToMany (mappedBy) | - |
| formulairesCPON | FormulaireCPON | **1:N** | OneToMany (mappedBy) | - |

---

## 7. FORMULAIRE_CPN (Table: `formulaires_cpn`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| taille | Double | NULLABLE | Taille en mètres |
| poids | Double | NULLABLE | Poids en kg |
| dernierControle | LocalDate | NULLABLE | Date du dernier contrôle |
| dateDernieresRegles | LocalDate | NULLABLE | Date des dernières règles |
| nombreMoisGrossesse | Integer | NULLABLE | Nombre de mois de grossesse |
| groupeSanguin | GroupeSanguin (Enum) | NULLABLE | Groupe sanguin |
| complications | Boolean | DEFAULT false | Présence de complications |
| complicationsDetails | TEXT | NULLABLE | Détails des complications |
| mouvementsBebeReguliers | Boolean | DEFAULT false | Mouvements du bébé réguliers |
| symptomes | List<String> | NULLABLE | Liste des symptômes (ElementCollection) |
| symptomesAutre | TEXT | NULLABLE | Autres symptômes |
| prendMedicamentsOuVitamines | Boolean | DEFAULT false | Prise de médicaments/vitamines |
| medicamentsOuVitaminesDetails | TEXT | NULLABLE | Détails des médicaments |
| aEuMaladies | Boolean | DEFAULT false | Présence de maladies |
| maladiesDetails | TEXT | NULLABLE | Détails des maladies |
| dateSoumission | LocalDateTime | NOT NULL | Date de soumission |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| dossierMedical | DossierMedical | **N:1** | ManyToOne | `dossier_medical_id` |

### Tables associées
- **formulaire_cpn_symptomes** (ElementCollection)
  - `formulaire_cpn_id` (FK)
  - `symptome` (String)

---

## 8. FORMULAIRE_CPON (Table: `formulaires_cpon`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| accouchementType | TypeAccouchement (Enum) | NULLABLE | Type d'accouchement |
| nombreEnfants | String | NULLABLE | Nombre d'enfants (1er, 2e, 3e, Plus) |
| sentiment | List<String> | NULLABLE | Sentiments (ElementCollection) |
| saignements | Boolean | DEFAULT false | Présence de saignements |
| consultation | String | NULLABLE | Statut consultation (Non, Oui, CPON1, etc.) |
| sexeBebe | Sexe (Enum) | NULLABLE | Sexe du bébé |
| alimentation | AlimentationBebe (Enum) | NULLABLE | Type d'alimentation |
| dateSoumission | LocalDateTime | NOT NULL | Date de soumission |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| dossierMedical | DossierMedical | **N:1** | ManyToOne | `dossier_medical_id` |

### Tables associées
- **formulaire_cpon_sentiments** (ElementCollection)
  - `formulaire_cpon_id` (FK)
  - `sentiment` (String)

---

## 9. CONSULTATION_PRENATALE (Table: `consultations_prenatales`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| datePrevue | LocalDate | NOT NULL | Date prévue de la consultation |
| dateRealisee | LocalDate | NULLABLE | Date de réalisation |
| notes | String(1000) | NULLABLE | Notes de consultation |
| poids | Double | NULLABLE | Poids en kg |
| tensionArterielle | String | NULLABLE | Tension artérielle |
| hauteurUterine | Double | NULLABLE | Hauteur utérine en cm |
| statut | StatutConsultation (Enum) | NOT NULL, DEFAULT 'A_VENIR' | Statut de la consultation |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| grossesse | Grossesse | **N:1** | ManyToOne | `grossesse_id` |
| suiviConsultation | SuiviConsultation | **1:1** | OneToOne (mappedBy) | - |
| ordonnance | Ordonnance | **1:1** | OneToOne (mappedBy) | - |
| rappels | Rappel | **1:N** | OneToMany (mappedBy) | - |

---

## 10. CONSULTATION_POSTNATALE (Table: `consultations_postnatales`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| type | String | NOT NULL | Type de consultation (J+3, J+7, 6e semaine) |
| datePrevue | LocalDate | NOT NULL | Date prévue de la consultation |
| dateRealisee | LocalDate | NULLABLE | Date de réalisation |
| notesMere | String(1000) | NULLABLE | Notes sur la mère |
| notesNouveauNe | String(1000) | NULLABLE | Notes sur le nouveau-né |
| statut | StatutConsultation (Enum) | NOT NULL, DEFAULT 'A_VENIR' | Statut de la consultation |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **N:1** | ManyToOne | `patiente_id` |
| enfant | Enfant | **N:1** | ManyToOne | `enfant_id` |
| rappels | Rappel | **1:N** | OneToMany (mappedBy) | - |
| suiviConsultation | SuiviConsultation | **1:1** | OneToOne (mappedBy) | - |
| ordonnance | Ordonnance | **1:1** | OneToOne (mappedBy) | - |

---

## 11. DOSSIER_MEDICAL_SUBMISSION (Table: `dossier_medical_submissions`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| type | SubmissionType (Enum) | NOT NULL | Type de soumission (CPN, CPON) |
| status | SubmissionStatus (Enum) | NOT NULL, DEFAULT 'EN_ATTENTE' | Statut (EN_ATTENTE, APPROUVEE, REJETEE) |
| payload | LONGTEXT | NOT NULL | Données JSON du formulaire |
| remarqueMedecin | TEXT | NULLABLE | Remarque du médecin |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **N:1** | ManyToOne | `patiente_id` |
| professionnelSante | ProfessionnelSante | **N:1** | ManyToOne | `professionnel_id` |

---

## 12. VACCINATION (Table: `vaccinations`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| nomVaccin | String | NOT NULL | Nom du vaccin |
| datePrevue | LocalDate | NOT NULL | Date prévue de vaccination |
| dateRealisee | LocalDate | NULLABLE | Date de réalisation |
| statut | StatutVaccination (Enum) | NOT NULL, DEFAULT 'A_FAIRE' | Statut (A_FAIRE, FAITE, REPORTEE) |
| notes | String(500) | NULLABLE | Notes |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| enfant | Enfant | **N:1** | ManyToOne | `enfant_id` |
| rappels | Rappel | **1:N** | OneToMany (mappedBy) | - |

---

## 13. CONVERSATION (Table: `conversations`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| titre | String | NOT NULL | Titre de la conversation |
| active | Boolean | NOT NULL, DEFAULT true | Statut actif/inactif |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| participants | Utilisateur | **N:N** | ManyToMany | Table: `conversation_participants` |
| messages | Message | **1:N** | OneToMany (mappedBy) | - |

### Tables associées
- **conversation_participants** (ManyToMany)
  - `conversation_id` (FK)
  - `utilisateur_id` (FK)

---

## 14. MESSAGE (Table: `messages`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| type | MessageType (Enum) | NOT NULL, DEFAULT 'TEXTE' | Type (TEXTE, AUDIO, IMAGE, DOCUMENT) |
| contenu | String(2000) | NULLABLE | Contenu textuel du message |
| fileUrl | String | NULLABLE | URL du fichier (audio, image, document) |
| lu | Boolean | NOT NULL, DEFAULT false | Statut lu/non lu |
| timestamp | LocalDateTime | NOT NULL | Date d'envoi (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| conversation | Conversation | **N:1** | ManyToOne | `conversation_id` |
| expediteur | Utilisateur | **N:1** | ManyToOne | `expediteur_id` |

---

## 15. RAPPEL (Table: `rappels`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| message | String(500) | NOT NULL | Message du rappel |
| dateEnvoi | LocalDateTime | NOT NULL | Date d'envoi prévue |
| type | TypeRappel (Enum) | NOT NULL | Type de rappel |
| statut | StatutRappel (Enum) | NOT NULL, DEFAULT 'ENVOYE' | Statut du rappel |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| utilisateur | Utilisateur | **N:1** | ManyToOne | `utilisateur_id` |
| consultationPrenatale | ConsultationPrenatale | **N:1** | ManyToOne | `consultation_prenatale_id` (NULLABLE) |
| consultationPostnatale | ConsultationPostnatale | **N:1** | ManyToOne | `consultation_postnatale_id` (NULLABLE) |
| vaccination | Vaccination | **N:1** | ManyToOne | `vaccination_id` (NULLABLE) |

**Note:** Les relations avec ConsultationPrenatale, ConsultationPostnatale et Vaccination sont optionnelles (NULLABLE). Un rappel est lié à UNE seule de ces entités selon son type.

---

## 16. CONSEIL (Table: `conseils`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| titre | String | NOT NULL | Titre du conseil |
| contenu | String(5000) | NULLABLE | Contenu textuel |
| lienMedia | String(500) | NULLABLE | URL vers vidéo ou audio |
| categorie | CategorieConseil (Enum) | NOT NULL | Catégorie du conseil |
| cible | String | NOT NULL | Public cible (Femme enceinte, Jeune mère, etc.) |
| actif | Boolean | NOT NULL, DEFAULT true | Statut actif/inactif |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| createur | ProfessionnelSante | **N:1** | ManyToOne | `professionnel_id` |

---

## 17. ORDONNANCE (Table: `ordonnances`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| observations | TEXT | NULLABLE | Observations du médecin |
| medicaments | List<Medicament> | NULLABLE | Liste des médicaments (ElementCollection) |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| patiente | Patiente | **N:1** | ManyToOne | `patiente_id` |
| medecin | ProfessionnelSante | **N:1** | ManyToOne | `medecin_id` |
| consultationPrenatale | ConsultationPrenatale | **1:1** | OneToOne (FK unique) | `consultation_prenatale_id` (UNIQUE, NULLABLE) |
| consultationPostnatale | ConsultationPostnatale | **1:1** | OneToOne (FK unique) | `consultation_postnatale_id` (UNIQUE, NULLABLE) |

**Note:** Une ordonnance est liée à UNE consultation prénatale OU UNE consultation postnatale (pas les deux).

### Tables associées
- **ordonnance_medicaments** (ElementCollection)
  - `ordonnance_id` (FK)
  - `nom` (String)
  - `posologie` (String)
  - `duree` (String)
  - `observation` (String)

---

## 18. SUIVI_CONSULTATION (Table: `suivis_consultation`)

### Attributs
| Nom | Type | Contraintes | Description |
|-----|------|-------------|-------------|
| id | Long | PK, AUTO_INCREMENT | Identifiant unique |
| ageGrossesse | String | NULLABLE | Âge de la grossesse |
| poids | Double | NULLABLE | Poids |
| tensionArterielle | String | NULLABLE | Tension artérielle |
| hauteurUterine | Double | NULLABLE | Hauteur utérine |
| mouvementsFoetaux | String | NULLABLE | Mouvements fœtaux |
| bruitsDuCoeur | String | NULLABLE | Bruits du cœur |
| oedeme | String | NULLABLE | Œdème |
| albumine | String | NULLABLE | Albumine |
| etatCol | String | NULLABLE | État du col |
| toucherVaginal | String | NULLABLE | Toucher vaginal |
| observations | TEXT | NULLABLE | Observations générales |
| dateProchainRendezVous | LocalDate | NULLABLE | Date du prochain rendez-vous |
| soinsCuratifs | TEXT | NULLABLE | Soins curatifs |
| etatConjonctives | EtatConjonctives (Enum) | NULLABLE | État des conjonctives |
| gainPoidsDepuisDebutGrossesse | Double | NULLABLE | Gain de poids depuis le début |
| examenObstetrical | TEXT | NULLABLE | Examen obstétrical |
| inspectionPalpation | TEXT | NULLABLE | Inspection/palpation |
| presentation | String | NULLABLE | Présentation |
| etatBassinAtteintePromontoire | String | NULLABLE | État bassin/atteinte promontoire |
| recommandationsAccouchement | TEXT | NULLABLE | Recommandations pour l'accouchement |
| dateCreation | LocalDateTime | NOT NULL | Date de création (audit) |
| dateModification | LocalDateTime | NULLABLE | Date de dernière modification (audit) |

### Relations
| Relation | Entité cible | Cardinalité | Type | Colonne FK |
|----------|--------------|-------------|------|------------|
| consultationPrenatale | ConsultationPrenatale | **1:1** | OneToOne (FK unique) | `consultation_prenatale_id` (UNIQUE, NULLABLE) |
| consultationPostnatale | ConsultationPostnatale | **1:1** | OneToOne (FK unique) | `consultation_postnatale_id` (UNIQUE, NULLABLE) |

**Note:** Un suivi est lié à UNE consultation prénatale OU UNE consultation postnatale (pas les deux).

---

## 19. MEDICAMENT (Embeddable - Pas de table propre)

**Type:** Classe Embeddable (utilisée dans Ordonnance)

### Attributs
| Nom | Type | Description |
|-----|------|-------------|
| nom | String | Nom du médicament |
| posologie | String | Posologie |
| duree | String | Durée de traitement |
| observation | String | Observations |

**Stockage:** Dans la table `ordonnance_medicaments` (ElementCollection)

---

## Résumé des Relations et Cardinalités

### Relations One-to-One (1:1)
1. **Patiente ↔ DossierMedical** (1:1)
2. **ConsultationPrenatale ↔ SuiviConsultation** (1:1)
3. **ConsultationPostnatale ↔ SuiviConsultation** (1:1)
4. **ConsultationPrenatale ↔ Ordonnance** (1:1)
5. **ConsultationPostnatale ↔ Ordonnance** (1:1)

### Relations One-to-Many (1:N)
1. **Utilisateur → Patiente** (héritage)
2. **Utilisateur → ProfessionnelSante** (héritage)
3. **Patiente → Grossesse** (1:N)
4. **Patiente → Enfant** (1:N)
5. **Grossesse → ConsultationPrenatale** (1:N)
6. **Enfant → Vaccination** (1:N)
7. **Enfant → ConsultationPostnatale** (1:N)
8. **DossierMedical → FormulaireCPN** (1:N)
9. **DossierMedical → FormulaireCPON** (1:N)
10. **ConsultationPrenatale → Rappel** (1:N)
11. **ConsultationPostnatale → Rappel** (1:N)
12. **Vaccination → Rappel** (1:N)
13. **ProfessionnelSante → Patiente** (1:N - assignation)
14. **ProfessionnelSante → Conseil** (1:N)
15. **ProfessionnelSante → Ordonnance** (1:N)
16. **Conversation → Message** (1:N)
17. **Utilisateur → Rappel** (1:N)
18. **Utilisateur → Message** (1:N via expediteur)

### Relations Many-to-One (N:1)
- Toutes les relations One-to-Many inversées (côté Many)

### Relations Many-to-Many (N:N)
1. **Utilisateur ↔ Conversation** (N:N via table `conversation_participants`)
   - Une conversation peut avoir plusieurs participants (patientes, médecins)
   - Un utilisateur peut participer à plusieurs conversations

---

## Tables de Jointure (Many-to-Many)

### conversation_participants
| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| conversation_id | Long | PK, FK | ID de la conversation |
| utilisateur_id | Long | PK, FK | ID de l'utilisateur participant |

---

## Tables ElementCollection (Collections intégrées)

### formulaire_cpn_symptomes
| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| formulaire_cpn_id | Long | PK, FK | ID du formulaire CPN |
| symptome | String | NULLABLE | Symptôme |

### formulaire_cpon_sentiments
| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| formulaire_cpon_id | Long | PK, FK | ID du formulaire CPON |
| sentiment | String | NULLABLE | Sentiment |

### ordonnance_medicaments
| Colonne | Type | Contraintes | Description |
|---------|------|-------------|-------------|
| ordonnance_id | Long | PK, FK | ID de l'ordonnance |
| nom | String | NULLABLE | Nom du médicament |
| posologie | String | NULLABLE | Posologie |
| duree | String | NULLABLE | Durée |
| observation | String | NULLABLE | Observation |

---

## Enums Utilisés

| Enum | Valeurs possibles | Usage |
|------|-------------------|-------|
| RoleUtilisateur | PATIENTE, MEDECIN, ADMIN | Rôle de l'utilisateur |
| Specialite | GYNECOLOGUE, SAGE_FEMME, etc. | Spécialité du professionnel |
| StatutGrossesse | EN_COURS, TERMINEE, ANNULEE | Statut de la grossesse |
| StatutConsultation | A_VENIR, REALISEE, ANNULEE | Statut de la consultation |
| StatutVaccination | A_FAIRE, FAITE, REPORTEE | Statut de la vaccination |
| StatutRappel | ENVOYE, LU, ARCHIVE | Statut du rappel |
| SubmissionStatus | EN_ATTENTE, APPROUVEE, REJETEE | Statut de soumission |
| SubmissionType | CPN, CPON | Type de soumission |
| TypeRappel | RAPPEL_CONSULTATION, RAPPEL_VACCINATION, CONSEIL, etc. | Type de rappel |
| MessageType | TEXTE, AUDIO, IMAGE, DOCUMENT | Type de message |
| GroupeSanguin | A_PLUS, A_MOINS, B_PLUS, etc. | Groupe sanguin |
| Sexe | GARCON, FILLE | Sexe |
| TypeAccouchement | NORMAL, CESARIENNE | Type d'accouchement |
| AlimentationBebe | ALLAITEMENT_MATERNEL, BIberon, MIXTE | Alimentation |
| CategorieConseil | ALIMENTATION, SANTE, HYGIENE, etc. | Catégorie de conseil |
| EtatConjonctives | NORMAL, ANEMIQUE, ICTERIQUE | État des conjonctives |

---

## Diagramme UML - Notations

Pour créer le diagramme de classe UML, utilisez les notations suivantes :

### Cardinalités
- **1:1** : `1` → `1`
- **1:N** : `1` → `*`
- **N:1** : `*` → `1`
- **N:N** : `*` → `*`

### Types de relations
- **Héritage** : Flèche triangulaire pointant vers la classe parent
- **OneToOne** : Ligne avec `1` aux deux extrémités
- **OneToMany** : Ligne avec `1` d'un côté et `*` de l'autre
- **ManyToMany** : Ligne avec `*` aux deux extrémités

### Contraintes importantes
- **FK UNIQUE** : Clé étrangère unique (relation 1:1)
- **NULLABLE** : Peut être null
- **NOT NULL** : Obligatoire
- **CASCADE** : Suppression en cascade
- **OrphanRemoval** : Suppression automatique des orphelins

---

## Notes importantes

1. **Stratégie d'héritage JOINED** : Les tables `patientes` et `professionnels_sante` partagent la table `utilisateurs` via une clé étrangère sur `id`.

2. **Ordonnance et SuiviConsultation** : Ces entités peuvent être liées à UNE consultation prénatale OU UNE consultation postnatale, mais pas les deux simultanément (FK nullable et unique).

3. **Rappel** : Peut être lié à une consultation prénatale, postnatale ou vaccination, mais UNE seule à la fois (toutes les FK sont nullable).

4. **Conversation** : Utilise une table de jointure pour la relation Many-to-Many avec Utilisateur (permettant aux patientes et médecins de participer).

5. **ElementCollection** : Les listes de strings ou embeddables sont stockées dans des tables séparées avec une clé étrangère vers l'entité parente.

---

**Date de création du document :** 2025-01-02
**Version :** 1.0

