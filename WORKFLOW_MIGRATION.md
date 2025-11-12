# 🔄 Guide de Migration - Nouveau Workflow

## 📋 Vue d'ensemble

Ce guide explique comment migrer d'une version antérieure de l'API vers le nouveau workflow automatisé.

---

## ⚠️ Changements Breaking

### 1. RegisterRequest

**Avant** :
```json
{
  "nom": "Traoré",
  "prenom": "Fatoumata",
  "telephone": "+22370123456",
  "motDePasse": "password",
  "role": "PATIENTE",
  "langue": "fr"
}
```

**Maintenant** :
```json
{
  "nom": "Traoré",
  "prenom": "Fatoumata",
  "telephone": "+22370123456",
  "motDePasse": "password",
  "role": "PATIENTE",
  "langue": "fr",
  // Nouveaux champs pour PATIENTE
  "dateDeNaissance": "1995-03-15",
  "adresse": "Quartier Hippodrome, Bamako",
  "professionnelSanteId": 5  // Optionnel
}
```

**Pour un MEDECIN** :
```json
{
  "nom": "Diarra",
  "prenom": "Moussa",
  "telephone": "+22376543210",
  "motDePasse": "password",
  "role": "MEDECIN",
  "langue": "fr",
  // Nouveaux champs pour MEDECIN
  "specialite": "GYNECOLOGUE",
  "identifiantProfessionnel": "ML-GYN-12345"
}
```

### 2. GrossesseRequest

**Avant** :
```json
{
  "dateDebut": "2024-01-15",
  "datePrevueAccouchement": "2024-10-21",
  "patienteId": 12
}
```

**Maintenant** :
```json
{
  "dateDernieresMenstruations": "2024-01-15",  // LMP uniquement
  "patienteId": 12
  // La DPA est calculée automatiquement : LMP + 280 jours
}
```

---

## 🔄 Script de Migration des Données

### Étape 1 : Migration des Utilisateurs existants

Si vous avez des utilisateurs `Utilisateur` de base qui doivent devenir des `Patiente` ou `ProfessionnelSante` :

```sql
-- Migration des patientes
-- Créer les entrées dans la table patientes
INSERT INTO patientes (id, nom, prenom, telephone, mot_de_passe, role, langue, actif, date_de_naissance, adresse)
SELECT 
    id,
    nom,
    prenom,
    telephone,
    mot_de_passe,
    role,
    langue,
    actif,
    '1990-01-01',  -- Date par défaut, à mettre à jour manuellement
    NULL           -- Adresse à compléter
FROM utilisateurs
WHERE role = 'PATIENTE';

-- Migration des médecins
INSERT INTO professionnels_sante (id, nom, prenom, telephone, mot_de_passe, role, langue, actif, specialite, identifiant_professionnel)
SELECT 
    id,
    nom,
    prenom,
    telephone,
    mot_de_passe,
    role,
    langue,
    actif,
    'GENERALISTE',  -- Spécialité par défaut
    CONCAT('ML-MED-', id)  -- Identifiant généré
FROM utilisateurs
WHERE role = 'MEDECIN';

-- Supprimer les anciennes entrées
DELETE FROM utilisateurs WHERE role IN ('PATIENTE', 'MEDECIN');
```

### Étape 2 : Migration des Grossesses existantes

Si vous avez des grossesses créées avec l'ancien système :

```sql
-- Pas de modification nécessaire pour les grossesses existantes
-- Mais pour les nouvelles, utilisez le nouveau format avec LMP
```

### Étape 3 : Générer les CPN manquantes

Pour les grossesses existantes qui n'ont pas de CPN :

```java
// Script Java à exécuter une fois
@Service
public class MigrationService {
    
    @Autowired
    private GrossesseRepository grossesseRepository;
    
    @Autowired
    private ConsultationPrenataleRepository consultationPrenataleRepository;
    
    @Transactional
    public void genererCPNManquantes() {
        List<Grossesse> grossesses = grossesseRepository.findAll();
        
        for (Grossesse grossesse : grossesses) {
            // Vérifier si cette grossesse a déjà des CPN
            List<ConsultationPrenatale> cpnExistantes = 
                consultationPrenataleRepository.findByGrossesseId(grossesse.getId());
            
            if (cpnExistantes.isEmpty() && grossesse.getStatut() == StatutGrossesse.EN_COURS) {
                // Générer les 4 CPN
                LocalDate lmp = grossesse.getDateDebut();
                
                creerCPN(grossesse, lmp.plusWeeks(12), "CPN1 - Premier trimestre");
                creerCPN(grossesse, lmp.plusWeeks(24), "CPN2 - Deuxième trimestre");
                creerCPN(grossesse, lmp.plusWeeks(32), "CPN3 - Troisième trimestre");
                creerCPN(grossesse, lmp.plusWeeks(36), "CPN4 - Préparation à l'accouchement");
            }
        }
    }
    
    private void creerCPN(Grossesse grossesse, LocalDate datePrevue, String notes) {
        ConsultationPrenatale cpn = new ConsultationPrenatale();
        cpn.setGrossesse(grossesse);
        cpn.setDatePrevue(datePrevue);
        cpn.setStatut(StatutConsultation.A_VENIR);
        cpn.setNotes(notes);
        consultationPrenataleRepository.save(cpn);
    }
}
```

### Étape 4 : Générer les calendriers vaccinaux manquants

Pour les enfants existants sans calendrier vaccinal :

```java
@Service
public class VaccinationMigrationService {
    
    @Autowired
    private EnfantRepository enfantRepository;
    
    @Autowired
    private VaccinationRepository vaccinationRepository;
    
    @Transactional
    public void genererCalendriersVaccinauxManquants() {
        List<Enfant> enfants = enfantRepository.findAll();
        
        for (Enfant enfant : enfants) {
            // Vérifier si cet enfant a déjà des vaccinations
            List<Vaccination> vaccinationsExistantes = 
                vaccinationRepository.findByEnfantId(enfant.getId());
            
            if (vaccinationsExistantes.isEmpty()) {
                // Générer le calendrier complet
                genererCalendrierVaccinal(enfant);
            }
        }
    }
    
    private void genererCalendrierVaccinal(Enfant enfant) {
        LocalDate dateNaissance = enfant.getDateDeNaissance();
        
        // À la naissance
        creerVaccin(enfant, dateNaissance, "BCG");
        creerVaccin(enfant, dateNaissance, "Polio 0 (VPO)");
        
        // À 6 semaines
        LocalDate sixSemaines = dateNaissance.plusWeeks(6);
        creerVaccin(enfant, sixSemaines, "Pentavalent 1 (DTC-HepB-Hib)");
        creerVaccin(enfant, sixSemaines, "Polio 1 (VPO)");
        creerVaccin(enfant, sixSemaines, "Pneumocoque 1 (PCV13)");
        creerVaccin(enfant, sixSemaines, "Rotavirus 1");
        
        // À 10 semaines
        LocalDate dixSemaines = dateNaissance.plusWeeks(10);
        creerVaccin(enfant, dixSemaines, "Pentavalent 2 (DTC-HepB-Hib)");
        creerVaccin(enfant, dixSemaines, "Polio 2 (VPO)");
        creerVaccin(enfant, dixSemaines, "Pneumocoque 2 (PCV13)");
        creerVaccin(enfant, dixSemaines, "Rotavirus 2");
        
        // À 14 semaines
        LocalDate quatorzeSemaines = dateNaissance.plusWeeks(14);
        creerVaccin(enfant, quatorzeSemaines, "Pentavalent 3 (DTC-HepB-Hib)");
        creerVaccin(enfant, quatorzeSemaines, "Polio 3 (VPO)");
        creerVaccin(enfant, quatorzeSemaines, "Pneumocoque 3 (PCV13)");
        
        // À 9 mois
        LocalDate neufMois = dateNaissance.plusMonths(9);
        creerVaccin(enfant, neufMois, "Rougeole-Rubéole (RR)");
        creerVaccin(enfant, neufMois, "Fièvre jaune");
        creerVaccin(enfant, neufMois, "Méningite A");
        
        // À 15 mois
        LocalDate quinzeMois = dateNaissance.plusMonths(15);
        creerVaccin(enfant, quinzeMois, "Rougeole-Rubéole 2 (rappel)");
    }
    
    private void creerVaccin(Enfant enfant, LocalDate datePrevue, String nomVaccin) {
        // Vérifier si la date est déjà passée
        StatutVaccination statut = datePrevue.isBefore(LocalDate.now()) 
            ? StatutVaccination.A_FAIRE  // À régulariser
            : StatutVaccination.A_FAIRE;
            
        Vaccination vaccination = new Vaccination();
        vaccination.setEnfant(enfant);
        vaccination.setNomVaccin(nomVaccin);
        vaccination.setDatePrevue(datePrevue);
        vaccination.setStatut(statut);
        vaccinationRepository.save(vaccination);
    }
}
```

---

## 🧪 Tests de Migration

### Test 1 : Inscription d'une nouvelle patiente

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Touré",
    "prenom": "Aissata",
    "telephone": "+22370999888",
    "motDePasse": "TestPass123!",
    "role": "PATIENTE",
    "langue": "fr",
    "dateDeNaissance": "1998-05-20",
    "adresse": "Badalabougou, Bamako"
  }'
```

**Vérification** :
- ✅ Table `patientes` contient la nouvelle entrée
- ✅ `date_de_naissance` et `adresse` sont renseignés
- ✅ Token JWT retourné

### Test 2 : Création d'une grossesse avec génération automatique des CPN

```bash
curl -X POST http://localhost:8080/api/grossesses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dateDernieresMenstruations": "2024-01-15",
    "patienteId": 1
  }'
```

**Vérification** :
```sql
-- Vérifier la grossesse
SELECT * FROM grossesses WHERE patiente_id = 1;
-- Résultat attendu : DPA = 2024-10-21 (LMP + 280 jours)

-- Vérifier les CPN générées
SELECT * FROM consultations_prenatales WHERE grossesse_id = <ID_GROSSESSE>;
-- Résultat attendu : 4 CPN créées automatiquement
```

### Test 3 : Création d'un enfant avec calendrier vaccinal

```bash
curl -X POST http://localhost:8080/api/enfants \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Touré",
    "prenom": "Moussa",
    "dateDeNaissance": "2024-10-21",
    "sexe": "MASCULIN",
    "patienteId": 1
  }'
```

**Vérification** :
```sql
-- Vérifier l'enfant
SELECT * FROM enfants WHERE patiente_id = 1;

-- Vérifier les vaccinations générées
SELECT nom_vaccin, date_prevue, statut 
FROM vaccinations 
WHERE enfant_id = <ID_ENFANT>
ORDER BY date_prevue;
-- Résultat attendu : 19 vaccinations créées
```

---

## 📊 Checklist de Migration

- [ ] Sauvegarde complète de la base de données
- [ ] Migration des utilisateurs vers Patiente/ProfessionnelSante
- [ ] Vérification des profils créés
- [ ] Génération des CPN manquantes pour grossesses existantes
- [ ] Génération des calendriers vaccinaux pour enfants existants
- [ ] Tests des nouveaux endpoints avec les nouveaux formats
- [ ] Mise à jour de la documentation frontend
- [ ] Formation des utilisateurs sur le nouveau workflow
- [ ] Monitoring des erreurs post-migration

---

## 🆘 Rollback en cas de problème

Si la migration pose problème, restaurez la sauvegarde :

```bash
# Restaurer la base de données
mysql -u root -p keneyamuso_db < backup_before_migration.sql

# Revenir à la version précédente du code
git checkout <previous_commit_hash>

# Redémarrer l'application
mvn spring-boot:run
```

---

## 📞 Support

En cas de problème pendant la migration :
1. Consultez les logs : `tail -f logs/spring-boot-application.log`
2. Vérifiez les erreurs de validation des DTOs
3. Contactez l'équipe de développement

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

