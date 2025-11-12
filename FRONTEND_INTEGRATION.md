# 📱 Guide d'Intégration Frontend - KènèyaMuso

## 🎯 Objectif

Ce guide explique comment intégrer le backend KènèyaMuso dans une application mobile (React Native, Flutter, etc.) ou web (React, Vue, Angular).

---

## 🔐 1. Authentification

### Inscription d'une Patiente

```typescript
// API Service
async function inscrirePatiente(data: {
  nom: string;
  prenom: string;
  telephone: string;
  motDePasse: string;
  dateDeNaissance: string;  // Format: YYYY-MM-DD
  adresse?: string;
  professionnelSanteId?: number;
}) {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      ...data,
      role: 'PATIENTE',
      langue: 'fr'
    })
  });
  
  const result = await response.json();
  
  if (result.success) {
    // Sauvegarder le token
    localStorage.setItem('token', result.data.token);
    localStorage.setItem('user', JSON.stringify(result.data));
    return result.data;
  } else {
    throw new Error(result.message);
  }
}

// Utilisation dans un composant React
function InscriptionForm() {
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    telephone: '',
    motDePasse: '',
    dateDeNaissance: '',
    adresse: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const user = await inscrirePatiente(formData);
      // Rediriger vers le tableau de bord
      navigate('/dashboard');
    } catch (error) {
      alert(error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input 
        type="text" 
        placeholder="Nom"
        value={formData.nom}
        onChange={(e) => setFormData({...formData, nom: e.target.value})}
      />
      <input 
        type="text" 
        placeholder="Prénom"
        value={formData.prenom}
        onChange={(e) => setFormData({...formData, prenom: e.target.value})}
      />
      <input 
        type="tel" 
        placeholder="Téléphone (+223...)"
        value={formData.telephone}
        onChange={(e) => setFormData({...formData, telephone: e.target.value})}
      />
      <input 
        type="password" 
        placeholder="Mot de passe"
        value={formData.motDePasse}
        onChange={(e) => setFormData({...formData, motDePasse: e.target.value})}
      />
      <input 
        type="date" 
        value={formData.dateDeNaissance}
        onChange={(e) => setFormData({...formData, dateDeNaissance: e.target.value})}
      />
      <textarea 
        placeholder="Adresse"
        value={formData.adresse}
        onChange={(e) => setFormData({...formData, adresse: e.target.value})}
      />
      <button type="submit">S'inscrire</button>
    </form>
  );
}
```

### Connexion

```typescript
async function connexion(telephone: string, motDePasse: string) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ telephone, motDePasse })
  });
  
  const result = await response.json();
  
  if (result.success) {
    localStorage.setItem('token', result.data.token);
    localStorage.setItem('user', JSON.stringify(result.data));
    return result.data;
  } else {
    throw new Error(result.message);
  }
}
```

---

## 🤰 2. Gestion de la Grossesse

### Déclarer une grossesse

```typescript
async function declarerGrossesse(dateDernieresMenstruations: string) {
  const user = JSON.parse(localStorage.getItem('user'));
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/grossesses', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      dateDernieresMenstruations,
      patienteId: user.id
    })
  });
  
  return await response.json();
}

// Composant React
function DeclarationGrossesse() {
  const [lmp, setLmp] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const result = await declarerGrossesse(lmp);
      if (result.success) {
        alert(`Grossesse déclarée ! DPA : ${result.data.datePrevueAccouchement}`);
        // 4 CPN ont été automatiquement créées
        navigate('/mes-cpn');
      }
    } catch (error) {
      alert(error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Déclarer ma grossesse</h2>
      <label>
        Date de dernières menstruations (DDM) :
        <input 
          type="date" 
          value={lmp}
          onChange={(e) => setLmp(e.target.value)}
          max={new Date().toISOString().split('T')[0]}
          required
        />
      </label>
      <p className="info">
        💡 La date prévue d'accouchement (DPA) sera calculée automatiquement<br/>
        📅 Vos 4 consultations prénatales seront planifiées automatiquement
      </p>
      <button type="submit" disabled={loading}>
        {loading ? 'Création en cours...' : 'Déclarer ma grossesse'}
      </button>
    </form>
  );
}
```

### Voir mes CPN

```typescript
async function getMesCPN() {
  const user = JSON.parse(localStorage.getItem('user'));
  const token = localStorage.getItem('token');
  
  const response = await fetch(
    `http://localhost:8080/api/consultations-prenatales/patiente/${user.id}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  
  const result = await response.json();
  return result.data;
}

// Composant React
function MesCPN() {
  const [cpn, setCpn] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getMesCPN().then(data => {
      setCpn(data);
      setLoading(false);
    });
  }, []);

  if (loading) return <div>Chargement...</div>;

  const cpnAVenir = cpn.filter(c => c.statut === 'A_VENIR');
  const cpnRealisees = cpn.filter(c => c.statut === 'REALISEE');

  return (
    <div>
      <h2>Mes Consultations Prénatales</h2>
      
      <section>
        <h3>📅 À venir ({cpnAVenir.length})</h3>
        {cpnAVenir.map(consultation => (
          <div key={consultation.id} className="cpn-card">
            <div className="date">{formatDate(consultation.datePrevue)}</div>
            <div className="notes">{consultation.notes}</div>
            <button onClick={() => navigate(`/cpn/${consultation.id}`)}>
              Voir les détails
            </button>
          </div>
        ))}
      </section>

      <section>
        <h3>✅ Réalisées ({cpnRealisees.length})</h3>
        {cpnRealisees.map(consultation => (
          <div key={consultation.id} className="cpn-card done">
            <div className="date">
              {formatDate(consultation.dateRealisee)}
            </div>
            <div className="details">
              <p>Poids : {consultation.poids} kg</p>
              <p>Tension : {consultation.tensionArterielle}</p>
              <p>Hauteur utérine : {consultation.hauteurUterine} cm</p>
            </div>
            <div className="notes">{consultation.notes}</div>
          </div>
        ))}
      </section>
    </div>
  );
}
```

---

## 👶 3. Gestion des Enfants & Vaccinations

### Enregistrer un enfant

```typescript
async function enregistrerEnfant(data: {
  nom: string;
  prenom: string;
  dateDeNaissance: string;
  sexe: 'MASCULIN' | 'FEMININ';
}) {
  const user = JSON.parse(localStorage.getItem('user'));
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/enfants', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      ...data,
      patienteId: user.id
    })
  });
  
  return await response.json();
}

// Composant
function EnregistrementEnfant() {
  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    dateDeNaissance: '',
    sexe: 'MASCULIN'
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const result = await enregistrerEnfant(formData);
      if (result.success) {
        alert(`${formData.prenom} a été enregistré(e) avec succès !`);
        alert(`19 vaccinations ont été planifiées automatiquement 💉`);
        navigate(`/enfant/${result.data.id}/vaccinations`);
      }
    } catch (error) {
      alert(error.message);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Enregistrer mon enfant</h2>
      <input 
        type="text" 
        placeholder="Nom"
        value={formData.nom}
        onChange={(e) => setFormData({...formData, nom: e.target.value})}
        required
      />
      <input 
        type="text" 
        placeholder="Prénom"
        value={formData.prenom}
        onChange={(e) => setFormData({...formData, prenom: e.target.value})}
        required
      />
      <input 
        type="date" 
        value={formData.dateDeNaissance}
        onChange={(e) => setFormData({...formData, dateDeNaissance: e.target.value})}
        required
      />
      <select 
        value={formData.sexe}
        onChange={(e) => setFormData({...formData, sexe: e.target.value as any})}
      >
        <option value="MASCULIN">Garçon</option>
        <option value="FEMININ">Fille</option>
      </select>
      <button type="submit">Enregistrer</button>
    </form>
  );
}
```

### Calendrier vaccinal

```typescript
async function getCalendrierVaccinal(enfantId: number) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(
    `http://localhost:8080/api/vaccinations/enfant/${enfantId}`,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  
  const result = await response.json();
  return result.data;
}

// Composant
function CalendrierVaccinal({ enfantId }) {
  const [vaccinations, setVaccinations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getCalendrierVaccinal(enfantId).then(data => {
      setVaccinations(data);
      setLoading(false);
    });
  }, [enfantId]);

  if (loading) return <div>Chargement...</div>;

  const vaccinationsAFaire = vaccinations.filter(v => v.statut === 'A_FAIRE');
  const vaccinationsFaites = vaccinations.filter(v => v.statut === 'FAIT');
  
  const pourcentage = (vaccinationsFaites.length / vaccinations.length) * 100;

  return (
    <div>
      <h2>Calendrier Vaccinal</h2>
      
      <div className="progress">
        <div className="progress-bar" style={{ width: `${pourcentage}%` }}>
          {Math.round(pourcentage)}%
        </div>
      </div>
      <p>{vaccinationsFaites.length} / {vaccinations.length} vaccinations effectuées</p>

      <h3>📅 À venir ({vaccinationsAFaire.length})</h3>
      {vaccinationsAFaire.map(vaccination => (
        <VaccinationCard 
          key={vaccination.id} 
          vaccination={vaccination}
          onConfirm={() => confirmerVaccination(vaccination.id)}
        />
      ))}

      <h3>✅ Effectuées ({vaccinationsFaites.length})</h3>
      {vaccinationsFaites.map(vaccination => (
        <VaccinationCard 
          key={vaccination.id} 
          vaccination={vaccination}
          done
        />
      ))}
    </div>
  );
}

function VaccinationCard({ vaccination, done, onConfirm }) {
  const isUpcoming = !done && isWithinDays(vaccination.datePrevue, 7);
  
  return (
    <div className={`vaccination-card ${done ? 'done' : ''} ${isUpcoming ? 'upcoming' : ''}`}>
      <div className="vaccine-icon">💉</div>
      <div className="vaccine-info">
        <h4>{vaccination.nomVaccin}</h4>
        <p className="date">
          {done ? 'Fait le' : 'Prévu le'} : {formatDate(done ? vaccination.dateRealisee : vaccination.datePrevue)}
        </p>
        {vaccination.notes && <p className="notes">{vaccination.notes}</p>}
      </div>
      {!done && isUpcoming && (
        <button onClick={onConfirm} className="btn-primary">
          ✓ Confirmer
        </button>
      )}
    </div>
  );
}

async function confirmerVaccination(vaccinationId: number) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(
    `http://localhost:8080/api/vaccinations/${vaccinationId}`,
    {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        dateRealisee: new Date().toISOString().split('T')[0],
        notes: 'Vaccin administré et bien toléré'
      })
    }
  );
  
  return await response.json();
}
```

---

## 💬 4. Messagerie

### Envoyer un message au médecin

```typescript
async function envoyerMessage(conversationId: number, contenu: string) {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ conversationId, contenu })
  });
  
  return await response.json();
}

// Composant Chat
function Chat({ conversationId }) {
  const [messages, setMessages] = useState([]);
  const [nouveauMessage, setNouveauMessage] = useState('');

  useEffect(() => {
    // Charger les messages
    const loadMessages = async () => {
      const token = localStorage.getItem('token');
      const response = await fetch(
        `http://localhost:8080/api/messages/conversation/${conversationId}`,
        { headers: { 'Authorization': `Bearer ${token}` } }
      );
      const result = await response.json();
      setMessages(result.data);
    };
    
    loadMessages();
    
    // Polling toutes les 5 secondes
    const interval = setInterval(loadMessages, 5000);
    return () => clearInterval(interval);
  }, [conversationId]);

  const handleEnvoyer = async (e) => {
    e.preventDefault();
    if (!nouveauMessage.trim()) return;
    
    await envoyerMessage(conversationId, nouveauMessage);
    setNouveauMessage('');
    // Les messages seront rechargés au prochain polling
  };

  return (
    <div className="chat">
      <div className="messages">
        {messages.map(msg => (
          <div 
            key={msg.id} 
            className={`message ${msg.expediteur.id === user.id ? 'sent' : 'received'}`}
          >
            <div className="message-content">{msg.contenu}</div>
            <div className="message-time">{formatTime(msg.timestamp)}</div>
          </div>
        ))}
      </div>
      
      <form onSubmit={handleEnvoyer} className="message-input">
        <input 
          type="text" 
          value={nouveauMessage}
          onChange={(e) => setNouveauMessage(e.target.value)}
          placeholder="Votre message..."
        />
        <button type="submit">Envoyer</button>
      </form>
    </div>
  );
}
```

---

## 📚 5. Conseils Éducatifs

```typescript
async function getConseils() {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/conseils', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const result = await response.json();
  return result.data;
}

function Conseils() {
  const [conseils, setConseils] = useState([]);
  const [categorie, setCategorie] = useState('TOUS');

  useEffect(() => {
    getConseils().then(setConseils);
  }, []);

  const conseilsFiltres = categorie === 'TOUS' 
    ? conseils 
    : conseils.filter(c => c.categorie === categorie);

  return (
    <div>
      <h2>📚 Conseils Santé</h2>
      
      <div className="categories">
        <button onClick={() => setCategorie('TOUS')}>Tous</button>
        <button onClick={() => setCategorie('NUTRITION')}>Nutrition</button>
        <button onClick={() => setCategorie('HYGIENE')}>Hygiène</button>
        <button onClick={() => setCategorie('ALLAITEMENT')}>Allaitement</button>
        <button onClick={() => setCategorie('PREVENTION')}>Prévention</button>
      </div>

      <div className="conseils-list">
        {conseilsFiltres.map(conseil => (
          <div key={conseil.id} className="conseil-card">
            <h3>{conseil.titre}</h3>
            <p>{conseil.contenu}</p>
            {conseil.lienMedia && (
              <a href={conseil.lienMedia} target="_blank">
                📹 Voir la vidéo
              </a>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
```

---

## 🔔 6. Gestion des Rappels (Push Notifications)

### Avec Firebase Cloud Messaging (FCM)

```typescript
// firebase-config.ts
import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';

const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  projectId: "keneyamuso",
  messagingSenderId: "YOUR_SENDER_ID",
  appId: "YOUR_APP_ID"
};

const app = initializeApp(firebaseConfig);
const messaging = getMessaging(app);

export async function requestNotificationPermission() {
  const permission = await Notification.requestPermission();
  
  if (permission === 'granted') {
    const token = await getToken(messaging);
    // Envoyer le token au backend pour l'associer à l'utilisateur
    await saveDeviceToken(token);
    return token;
  }
}

async function saveDeviceToken(token: string) {
  const authToken = localStorage.getItem('token');
  
  await fetch('http://localhost:8080/api/users/device-token', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    },
    body: JSON.stringify({ deviceToken: token })
  });
}

// Écouter les notifications en temps réel
onMessage(messaging, (payload) => {
  console.log('Message reçu:', payload);
  
  new Notification(payload.notification.title, {
    body: payload.notification.body,
    icon: '/icon.png'
  });
});
```

---

## 🎨 7. Helpers & Utilitaires

```typescript
// utils/date.ts
export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('fr-FR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

export function formatTime(timestamp: string): string {
  const date = new Date(timestamp);
  return new Intl.DateTimeFormat('fr-FR', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

export function isWithinDays(dateString: string, days: number): boolean {
  const date = new Date(dateString);
  const today = new Date();
  const diffTime = date.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  return diffDays >= 0 && diffDays <= days;
}

export function calculateWeeksPregnant(lmp: string): number {
  const lmpDate = new Date(lmp);
  const today = new Date();
  const diffTime = today.getTime() - lmpDate.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));
  return Math.floor(diffDays / 7);
}

// utils/api.ts
export class ApiClient {
  private baseURL = 'http://localhost:8080/api';

  private async request(endpoint: string, options: RequestInit = {}) {
    const token = localStorage.getItem('token');
    
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers
    };

    const response = await fetch(`${this.baseURL}${endpoint}`, {
      ...options,
      headers
    });

    const result = await response.json();

    if (!result.success) {
      throw new Error(result.message);
    }

    return result.data;
  }

  async get(endpoint: string) {
    return this.request(endpoint);
  }

  async post(endpoint: string, data: any) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  async put(endpoint: string, data: any) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  async delete(endpoint: string) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

export const api = new ApiClient();
```

---

## ✅ Checklist d'Intégration

- [ ] Configuration de l'API (baseURL)
- [ ] Gestion de l'authentification (token JWT)
- [ ] Formulaire d'inscription patiente avec profil complet
- [ ] Déclaration de grossesse avec calcul automatique DPA
- [ ] Affichage du calendrier CPN
- [ ] Enregistrement d'enfant avec génération du calendrier vaccinal
- [ ] Confirmation de vaccination
- [ ] Messagerie avec le médecin
- [ ] Consultation des conseils éducatifs
- [ ] Notifications push (Firebase)
- [ ] Gestion des erreurs et états de chargement
- [ ] Tests sur appareils réels (Android/iOS)

---

**KènèyaMuso** - *Pour une maternité saine au Mali* 🇲🇱

