# Script de test pour l'intégration du Dashboard
# Ce script teste l'endpoint /api/dashboard/medecin

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   TEST INTEGRATION DASHBOARD - KENEYA MUSO        " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"

# Fonction pour afficher les résultats
function Show-Response {
    param (
        [string]$Title,
        [object]$Response,
        [int]$StatusCode
    )
    
    Write-Host "[$Title]" -ForegroundColor Yellow
    Write-Host "Status Code: $StatusCode" -ForegroundColor $(if ($StatusCode -eq 200) { "Green" } else { "Red" })
    
    if ($Response) {
        $Response | ConvertTo-Json -Depth 5 | Write-Host
    }
    Write-Host ""
}

# Test 1: Vérifier que le backend est actif
Write-Host "[1/4] Vérification du backend..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method Get -ErrorAction Stop
    Write-Host "✓ Backend est actif" -ForegroundColor Green
    Write-Host "  Status: $($health.status)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "✗ Backend non accessible" -ForegroundColor Red
    Write-Host "  Erreur: $_" -ForegroundColor Red
    Write-Host "  Veuillez démarrer le backend avec start-backend.bat" -ForegroundColor Yellow
    exit 1
}

# Test 2: Authentification et obtention du token
Write-Host "[2/4] Authentification..." -ForegroundColor Cyan

$loginPayload = @{
    telephone = "+22377777777"
    motDePasse = "medecin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post `
        -ContentType "application/json" `
        -Body $loginPayload `
        -ErrorAction Stop
    
    if ($loginResponse.success -and $loginResponse.data.token) {
        $token = $loginResponse.data.token
        Write-Host "✓ Authentification réussie" -ForegroundColor Green
        Write-Host "  Utilisateur: $($loginResponse.data.nom) $($loginResponse.data.prenom)" -ForegroundColor Gray
        Write-Host "  Rôle: $($loginResponse.data.role)" -ForegroundColor Gray
        Write-Host "  Token: $($token.Substring(0, 20))..." -ForegroundColor Gray
        Write-Host ""
    } else {
        Write-Host "✗ Authentification échouée" -ForegroundColor Red
        Write-Host "  Message: $($loginResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Erreur lors de l'authentification" -ForegroundColor Red
    Write-Host "  Erreur: $_" -ForegroundColor Red
    Write-Host "  Assurez-vous que le compte médecin existe (+22377777777 / medecin123)" -ForegroundColor Yellow
    exit 1
}

# Test 3: Appel de l'endpoint dashboard
Write-Host "[3/4] Récupération des statistiques du dashboard..." -ForegroundColor Cyan

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $dashboardResponse = Invoke-RestMethod -Uri "$baseUrl/api/dashboard/medecin" -Method Get `
        -Headers $headers `
        -ErrorAction Stop
    
    if ($dashboardResponse.success -and $dashboardResponse.data) {
        Write-Host "✓ Statistiques récupérées avec succès" -ForegroundColor Green
        Write-Host ""
        
        $stats = $dashboardResponse.data
        
        # Affichage formaté des statistiques
        Write-Host "  STATISTIQUES DU DASHBOARD:" -ForegroundColor White -BackgroundColor DarkBlue
        Write-Host "  ─────────────────────────────────────" -ForegroundColor Gray
        Write-Host "  📊 Total Patientes      : $($stats.totalPatientes)" -ForegroundColor Cyan
        Write-Host "  ⏳ Suivis En Cours      : $($stats.suivisEnCours)" -ForegroundColor Yellow
        Write-Host "  ✓  Suivis Terminés      : $($stats.suivisTermines)" -ForegroundColor Green
        Write-Host "  🔔 Rappels Actifs       : $($stats.rappelsActifs)" -ForegroundColor Magenta
        Write-Host "  ⚠️  Alertes Actives      : $($stats.alertesActives)" -ForegroundColor Red
        Write-Host "  ─────────────────────────────────────" -ForegroundColor Gray
        Write-Host ""
        
    } else {
        Write-Host "✗ Erreur lors de la récupération des statistiques" -ForegroundColor Red
        Write-Host "  Message: $($dashboardResponse.message)" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "✗ Erreur lors de l'appel API" -ForegroundColor Red
    Write-Host "  Erreur: $_" -ForegroundColor Red
    exit 1
}

# Test 4: Validation de la structure de réponse
Write-Host "[4/4] Validation de la structure de réponse..." -ForegroundColor Cyan

$validationErrors = @()

if ($null -eq $stats.totalPatientes) {
    $validationErrors += "- Champ 'totalPatientes' manquant"
}
if ($null -eq $stats.suivisTermines) {
    $validationErrors += "- Champ 'suivisTermines' manquant"
}
if ($null -eq $stats.suivisEnCours) {
    $validationErrors += "- Champ 'suivisEnCours' manquant"
}
if ($null -eq $stats.rappelsActifs) {
    $validationErrors += "- Champ 'rappelsActifs' manquant"
}
if ($null -eq $stats.alertesActives) {
    $validationErrors += "- Champ 'alertesActives' manquant"
}

if ($validationErrors.Count -eq 0) {
    Write-Host "✓ Structure de réponse valide" -ForegroundColor Green
    Write-Host "  Tous les champs requis sont présents" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Host "✗ Structure de réponse invalide" -ForegroundColor Red
    foreach ($error in $validationErrors) {
        Write-Host "  $error" -ForegroundColor Red
    }
    exit 1
}

# Résumé final
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   ✓ TOUS LES TESTS SONT PASSES                    " -ForegroundColor Green
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "L'intégration du dashboard est fonctionnelle!" -ForegroundColor Green
Write-Host ""
Write-Host "Prochaines étapes:" -ForegroundColor Yellow
Write-Host "  1. Démarrer l'application Flutter: flutter run" -ForegroundColor Gray
Write-Host "  2. Se connecter avec: +22377777777 / medecin123" -ForegroundColor Gray
Write-Host "  3. Vérifier l'affichage des statistiques sur le dashboard" -ForegroundColor Gray
Write-Host ""

# Affichage de la réponse JSON complète pour référence
Write-Host "Réponse JSON complète:" -ForegroundColor Cyan
$dashboardResponse | ConvertTo-Json -Depth 5 | Write-Host -ForegroundColor Gray


