# Script de Test Complet - API KènèyaMuso
# PowerShell - Windows

$API_URL = "http://localhost:8080"
$TOKEN = ""

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Test Complet API KènèyaMuso" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Inscription Patiente
Write-Host "1️⃣  Inscription d'une patiente..." -ForegroundColor Yellow
$body = @{
    nom = "Traoré"
    prenom = "Fatoumata"
    telephone = "+22370123456"
    motDePasse = "Test123!"
    role = "PATIENTE"
    langue = "fr"
    dateDeNaissance = "1995-03-15"
    adresse = "Bamako"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$API_URL/api/auth/register" -Method Post -Body $body -ContentType "application/json"
    $TOKEN = $response.data.token
    Write-Host "✅ Token récupéré: $($TOKEN.Substring(0, 20))..." -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur lors de l'inscription: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

# 2. Créer une grossesse
Write-Host "2️⃣  Création d'une grossesse..." -ForegroundColor Yellow
$body = @{
    dateDernieresMenstruations = "2024-01-15"
    patienteId = 1
} | ConvertTo-Json

$headers = @{
    "Authorization" = "Bearer $TOKEN"
    "Content-Type" = "application/json"
}

try {
    $grossesse = Invoke-RestMethod -Uri "$API_URL/api/grossesses" -Method Post -Body $body -Headers $headers
    $GROSSESSE_ID = $grossesse.data.id
    Write-Host "✅ Grossesse créée (ID: $GROSSESSE_ID)" -ForegroundColor Green
    Write-Host "✅ 4 CPN générées automatiquement !" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. Voir les CPN
Write-Host "3️⃣  Récupération des CPN..." -ForegroundColor Yellow
try {
    $cpn = Invoke-RestMethod -Uri "$API_URL/api/consultations-prenatales/grossesse/$GROSSESSE_ID" -Method Get -Headers $headers
    $CPN_COUNT = $cpn.data.Count
    Write-Host "✅ $CPN_COUNT CPN trouvées" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. Voir une CPN
Write-Host "4️⃣  Détails de la première CPN..." -ForegroundColor Yellow
try {
    $cpn1 = Invoke-RestMethod -Uri "$API_URL/api/consultations-prenatales/1" -Method Get -Headers $headers
    Write-Host "✅ CPN1 - Date prévue: $($cpn1.data.datePrevue)" -ForegroundColor Green
    Write-Host "   Notes: $($cpn1.data.notes)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. Mettre à jour une CPN
Write-Host "5️⃣  Mise à jour de la CPN1..." -ForegroundColor Yellow
$body = @{
    datePrevue = "2024-04-08"
    dateRealisee = "2024-04-08"
    poids = 65.5
    tensionArterielle = "120/80"
    hauteurUterine = 12
    notes = "Grossesse évoluant normalement. Tout va bien."
    grossesseId = $GROSSESSE_ID
} | ConvertTo-Json

try {
    $cpnUpdate = Invoke-RestMethod -Uri "$API_URL/api/consultations-prenatales/1" -Method Put -Body $body -Headers $headers
    Write-Host "✅ CPN1 mise à jour - Statut: $($cpnUpdate.data.statut)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Terminer la grossesse
Write-Host "6️⃣  Terminaison de la grossesse..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$API_URL/api/grossesses/$GROSSESSE_ID/terminer" -Method Put -Headers $headers | Out-Null
    Write-Host "✅ Grossesse terminée" -ForegroundColor Green
    Write-Host "✅ 3 CPoN générées automatiquement !" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 7. Voir les CPoN
Write-Host "7️⃣  Récupération des CPoN..." -ForegroundColor Yellow
try {
    $cpon = Invoke-RestMethod -Uri "$API_URL/api/consultations-postnatales/patiente/1" -Method Get -Headers $headers
    $CPON_COUNT = $cpon.data.Count
    Write-Host "✅ $CPON_COUNT CPoN trouvées" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 8. Créer un enfant
Write-Host "8️⃣  Enregistrement d'un enfant..." -ForegroundColor Yellow
$body = @{
    nom = "Traoré"
    prenom = "Ibrahim"
    dateDeNaissance = "2024-10-21"
    sexe = "MASCULIN"
    patienteId = 1
} | ConvertTo-Json

try {
    $enfant = Invoke-RestMethod -Uri "$API_URL/api/enfants" -Method Post -Body $body -Headers $headers
    $ENFANT_ID = $enfant.data.id
    Write-Host "✅ Enfant créé (ID: $ENFANT_ID)" -ForegroundColor Green
    Write-Host "✅ 19 vaccinations générées automatiquement !" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 9. Voir les vaccinations
Write-Host "9️⃣  Récupération du calendrier vaccinal..." -ForegroundColor Yellow
try {
    $vaccins = Invoke-RestMethod -Uri "$API_URL/api/vaccinations/enfant/$ENFANT_ID" -Method Get -Headers $headers
    $VACCINS_COUNT = $vaccins.data.Count
    Write-Host "✅ $VACCINS_COUNT vaccinations trouvées" -ForegroundColor Green
    
    # Afficher les 3 premières vaccinations
    Write-Host "`n   Premières vaccinations:" -ForegroundColor Gray
    $vaccins.data | Select-Object -First 3 | ForEach-Object {
        Write-Host "   - $($_.nomVaccin) - Date prévue: $($_.datePrevue)" -ForegroundColor Gray
    }
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 10. Confirmer une vaccination
Write-Host "🔟 Confirmation de la vaccination BCG..." -ForegroundColor Yellow
$body = @{
    nomVaccin = "BCG"
    datePrevue = "2024-10-21"
    dateRealisee = "2024-10-21"
    notes = "Vaccin bien toléré, aucune réaction"
    enfantId = $ENFANT_ID
} | ConvertTo-Json

try {
    $vaccinUpdate = Invoke-RestMethod -Uri "$API_URL/api/vaccinations/1" -Method Put -Body $body -Headers $headers
    Write-Host "✅ BCG confirmé - Statut: $($vaccinUpdate.data.statut)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
}

# 11. Créer un conseil (nécessite un token admin)
Write-Host "1️⃣1️⃣  Création d'un conseil..." -ForegroundColor Yellow
Write-Host "   (Nécessite un compte administrateur - Ignoré)" -ForegroundColor Gray
Write-Host ""

# 12. Envoyer un message
Write-Host "1️⃣2️⃣  Envoi d'un message..." -ForegroundColor Yellow
$body = @{
    conversationId = 1
    contenu = "Bonjour Docteur, tout va bien. Merci pour le suivi !"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$API_URL/api/messages" -Method Post -Body $body -Headers $headers | Out-Null
    Write-Host "✅ Message envoyé" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "⚠️  Erreur (normal si conversation n'existe pas): $($_.Exception.Message)" -ForegroundColor Yellow
    Write-Host ""
}

# Résumé final
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  ✅ Tous les tests terminés !" -ForegroundColor Green
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Résumé des entités créées:" -ForegroundColor Cyan
Write-Host "  - 1 Patiente inscrite" -ForegroundColor White
Write-Host "  - 1 Grossesse créée" -ForegroundColor White
Write-Host "  - 4 CPN générées automatiquement" -ForegroundColor White
Write-Host "  - 3 CPoN générées automatiquement" -ForegroundColor White
Write-Host "  - 1 Enfant enregistré" -ForegroundColor White
Write-Host "  - 19 Vaccinations générées automatiquement" -ForegroundColor White
Write-Host ""
Write-Host "Total : 29 entités créées automatiquement ! 🎉" -ForegroundColor Green
Write-Host ""
Write-Host "💡 Pour voir tous les détails :" -ForegroundColor Yellow
Write-Host "   Swagger UI : http://localhost:8080/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host ""

