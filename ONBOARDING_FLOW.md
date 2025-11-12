# 🎯 Flux d'Onboarding - KènèyaMuso

## 📋 Vue d'ensemble

Workflow d'inscription en 2 étapes pour une meilleure expérience utilisateur :

```
┌─────────────────────┐
│  Page d'accueil     │
│  (Landing Page)     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Choix du profil    │  ← Étape 1
│  - Patiente 🤰      │
│  - Médecin 👨‍⚕️      │
│  - Administrateur 👤│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Inscription        │  ← Étape 2 (formulaire adapté)
│  (selon profil)     │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  Dashboard          │
└─────────────────────┘
```

---

## 🎨 Page 1 : Choix du Profil

### Design Moderne

```typescript
// pages/ChoixProfil.tsx
import React from 'react';
import { useNavigate } from 'react-router-dom';

interface ProfileCard {
  role: 'PATIENTE' | 'MEDECIN' | 'ADMINISTRATEUR';
  title: string;
  description: string;
  icon: string;
  color: string;
}

const profiles: ProfileCard[] = [
  {
    role: 'PATIENTE',
    title: 'Je suis une Patiente',
    description: 'Suivi de grossesse, consultations et vaccination',
    icon: '🤰',
    color: 'from-pink-500 to-rose-500'
  },
  {
    role: 'MEDECIN',
    title: 'Je suis Médecin',
    description: 'Suivi de mes patientes et consultations',
    icon: '👨‍⚕️',
    color: 'from-blue-500 to-cyan-500'
  },
  {
    role: 'ADMINISTRATEUR',
    title: 'Je suis Administrateur',
    description: 'Gestion de la plateforme',
    icon: '👤',
    color: 'from-purple-500 to-indigo-500'
  }
];

export function ChoixProfil() {
  const navigate = useNavigate();

  const handleSelectProfile = (role: string) => {
    // Rediriger vers la page d'inscription avec le rôle
    navigate('/inscription', { state: { role } });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 via-pink-500 to-red-500">
      <div className="container mx-auto px-4 py-16">
        
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold text-white mb-4">
            Bienvenue sur KènèyaMuso
          </h1>
          <p className="text-xl text-white/90">
            Pour une maternité saine au Mali 🇲🇱
          </p>
        </div>

        {/* Choix du profil */}
        <div className="text-center mb-8">
          <h2 className="text-3xl font-semibold text-white mb-2">
            Commençons !
          </h2>
          <p className="text-lg text-white/80">
            Choisissez votre profil pour continuer
          </p>
        </div>

        {/* Cards de profils */}
        <div className="grid md:grid-cols-3 gap-6 max-w-6xl mx-auto">
          {profiles.map((profile) => (
            <div
              key={profile.role}
              onClick={() => handleSelectProfile(profile.role)}
              className="bg-white rounded-2xl shadow-2xl p-8 cursor-pointer 
                         transform transition-all duration-300 hover:scale-105 
                         hover:shadow-3xl group"
            >
              {/* Icône avec gradient */}
              <div className={`w-20 h-20 mx-auto mb-6 rounded-full 
                              bg-gradient-to-br ${profile.color} 
                              flex items-center justify-center text-4xl
                              transform transition-transform group-hover:rotate-12`}>
                {profile.icon}
              </div>

              {/* Titre */}
              <h3 className="text-2xl font-bold text-gray-800 mb-3 text-center">
                {profile.title}
              </h3>

              {/* Description */}
              <p className="text-gray-600 text-center mb-6">
                {profile.description}
              </p>

              {/* Bouton */}
              <button
                className={`w-full py-3 rounded-xl text-white font-semibold
                           bg-gradient-to-r ${profile.color}
                           transform transition-all hover:shadow-lg`}
              >
                Continuer →
              </button>
            </div>
          ))}
        </div>

        {/* Déjà un compte */}
        <div className="text-center mt-12">
          <p className="text-white/90 text-lg">
            Vous avez déjà un compte ?{' '}
            <button
              onClick={() => navigate('/connexion')}
              className="font-bold underline hover:text-white"
            >
              Se connecter
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
```

---

## 📝 Page 2 : Inscription Adaptée

### Formulaire Dynamique selon le Profil

```typescript
// pages/Inscription.tsx
import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

export function Inscription() {
  const location = useLocation();
  const navigate = useNavigate();
  const role = location.state?.role || 'PATIENTE';

  const [formData, setFormData] = useState({
    nom: '',
    prenom: '',
    telephone: '',
    motDePasse: '',
    confirmMotDePasse: '',
    langue: 'fr',
    // Champs Patiente
    dateDeNaissance: '',
    adresse: '',
    professionnelSanteId: '',
    // Champs Médecin
    specialite: '',
    identifiantProfessionnel: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Configuration selon le rôle
  const roleConfig = {
    PATIENTE: {
      title: 'Inscription Patiente 🤰',
      color: 'from-pink-500 to-rose-500',
      fields: ['dateDeNaissance', 'adresse', 'professionnelSanteId']
    },
    MEDECIN: {
      title: 'Inscription Médecin 👨‍⚕️',
      color: 'from-blue-500 to-cyan-500',
      fields: ['specialite', 'identifiantProfessionnel']
    },
    ADMINISTRATEUR: {
      title: 'Inscription Administrateur 👤',
      color: 'from-purple-500 to-indigo-500',
      fields: []
    }
  };

  const config = roleConfig[role as keyof typeof roleConfig];

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validation
    if (formData.motDePasse !== formData.confirmMotDePasse) {
      setError('Les mots de passe ne correspondent pas');
      return;
    }

    if (formData.motDePasse.length < 6) {
      setError('Le mot de passe doit contenir au moins 6 caractères');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...formData,
          role: role
        })
      });

      const result = await response.json();

      if (result.success) {
        // Sauvegarder le token
        localStorage.setItem('token', result.data.token);
        localStorage.setItem('user', JSON.stringify(result.data));

        // Rediriger selon le rôle
        if (role === 'PATIENTE') {
          navigate('/dashboard-patiente');
        } else if (role === 'MEDECIN') {
          navigate('/dashboard-medecin');
        } else {
          navigate('/dashboard-admin');
        }
      } else {
        setError(result.message || 'Erreur lors de l\'inscription');
      }
    } catch (err) {
      setError('Erreur de connexion au serveur');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 py-12 px-4">
      <div className="max-w-2xl mx-auto">
        
        {/* Bouton retour */}
        <button
          onClick={() => navigate(-1)}
          className="mb-6 flex items-center text-gray-600 hover:text-gray-800"
        >
          ← Retour au choix du profil
        </button>

        {/* Card d'inscription */}
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          
          {/* Header avec gradient selon le rôle */}
          <div className={`bg-gradient-to-r ${config.color} p-6`}>
            <h1 className="text-3xl font-bold text-white text-center">
              {config.title}
            </h1>
            <p className="text-white/90 text-center mt-2">
              Remplissez vos informations pour créer votre compte
            </p>
          </div>

          {/* Formulaire */}
          <form onSubmit={handleSubmit} className="p-8">
            
            {/* Erreur */}
            {error && (
              <div className="mb-6 p-4 bg-red-100 border border-red-400 text-red-700 rounded-lg">
                ⚠️ {error}
              </div>
            )}

            {/* Champs communs */}
            <div className="grid md:grid-cols-2 gap-6 mb-6">
              <div>
                <label className="block text-gray-700 font-semibold mb-2">
                  Nom *
                </label>
                <input
                  type="text"
                  name="nom"
                  value={formData.nom}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                  placeholder="Traoré"
                />
              </div>

              <div>
                <label className="block text-gray-700 font-semibold mb-2">
                  Prénom *
                </label>
                <input
                  type="text"
                  name="prenom"
                  value={formData.prenom}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                  placeholder="Fatoumata"
                />
              </div>
            </div>

            <div className="mb-6">
              <label className="block text-gray-700 font-semibold mb-2">
                Téléphone *
              </label>
              <input
                type="tel"
                name="telephone"
                value={formData.telephone}
                onChange={handleChange}
                required
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                placeholder="+223 70 12 34 56"
              />
              <p className="text-sm text-gray-500 mt-1">
                Format : +223XXXXXXXX
              </p>
            </div>

            <div className="grid md:grid-cols-2 gap-6 mb-6">
              <div>
                <label className="block text-gray-700 font-semibold mb-2">
                  Mot de passe *
                </label>
                <input
                  type="password"
                  name="motDePasse"
                  value={formData.motDePasse}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="block text-gray-700 font-semibold mb-2">
                  Confirmer mot de passe *
                </label>
                <input
                  type="password"
                  name="confirmMotDePasse"
                  value={formData.confirmMotDePasse}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                  placeholder="••••••••"
                />
              </div>
            </div>

            {/* Champs spécifiques PATIENTE */}
            {role === 'PATIENTE' && (
              <>
                <div className="mb-6">
                  <label className="block text-gray-700 font-semibold mb-2">
                    Date de naissance *
                  </label>
                  <input
                    type="date"
                    name="dateDeNaissance"
                    value={formData.dateDeNaissance}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                  />
                </div>

                <div className="mb-6">
                  <label className="block text-gray-700 font-semibold mb-2">
                    Adresse
                  </label>
                  <input
                    type="text"
                    name="adresse"
                    value={formData.adresse}
                    onChange={handleChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-pink-500 focus:border-transparent"
                    placeholder="Quartier Hippodrome, Bamako"
                  />
                </div>
              </>
            )}

            {/* Champs spécifiques MEDECIN */}
            {role === 'MEDECIN' && (
              <>
                <div className="mb-6">
                  <label className="block text-gray-700 font-semibold mb-2">
                    Spécialité *
                  </label>
                  <select
                    name="specialite"
                    value={formData.specialite}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="">Sélectionnez une spécialité</option>
                    <option value="GYNECOLOGUE">Gynécologue</option>
                    <option value="PEDIATRE">Pédiatre</option>
                    <option value="GENERALISTE">Généraliste</option>
                  </select>
                </div>

                <div className="mb-6">
                  <label className="block text-gray-700 font-semibold mb-2">
                    Identifiant Professionnel *
                  </label>
                  <input
                    type="text"
                    name="identifiantProfessionnel"
                    value={formData.identifiantProfessionnel}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    placeholder="ML-GYN-12345"
                  />
                  <p className="text-sm text-gray-500 mt-1">
                    Votre numéro d'ordre ou identifiant professionnel
                  </p>
                </div>
              </>
            )}

            {/* Bouton de soumission */}
            <button
              type="submit"
              disabled={loading}
              className={`w-full py-4 rounded-xl text-white font-bold text-lg
                         bg-gradient-to-r ${config.color}
                         transform transition-all hover:scale-105 hover:shadow-lg
                         disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              {loading ? '⏳ Inscription en cours...' : '✨ Créer mon compte'}
            </button>

            {/* Lien connexion */}
            <p className="text-center text-gray-600 mt-6">
              Vous avez déjà un compte ?{' '}
              <button
                type="button"
                onClick={() => navigate('/connexion')}
                className="text-pink-600 font-semibold hover:underline"
              >
                Se connecter
              </button>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}
```

---

## 🗺️ Configuration des Routes

```typescript
// App.tsx ou routes/index.tsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ChoixProfil } from './pages/ChoixProfil';
import { Inscription } from './pages/Inscription';
import { Connexion } from './pages/Connexion';
import { DashboardPatiente } from './pages/DashboardPatiente';
import { DashboardMedecin } from './pages/DashboardMedecin';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Route par défaut : Choix du profil */}
        <Route path="/" element={<ChoixProfil />} />
        
        {/* Inscription avec profil sélectionné */}
        <Route path="/inscription" element={<Inscription />} />
        
        {/* Connexion */}
        <Route path="/connexion" element={<Connexion />} />
        
        {/* Dashboards selon le rôle */}
        <Route path="/dashboard-patiente" element={<PrivateRoute><DashboardPatiente /></PrivateRoute>} />
        <Route path="/dashboard-medecin" element={<PrivateRoute><DashboardMedecin /></PrivateRoute>} />
        <Route path="/dashboard-admin" element={<PrivateRoute><DashboardAdmin /></PrivateRoute>} />
        
        {/* Redirection */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </BrowserRouter>
  );
}

// Route privée (nécessite authentification)
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('token');
  return token ? <>{children}</> : <Navigate to="/connexion" />;
}
```

---

## 🎨 CSS (Tailwind) ou Styles

Si vous n'utilisez pas Tailwind, voici le CSS équivalent :

```css
/* styles/onboarding.css */
.profile-card {
  background: white;
  border-radius: 1rem;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.profile-card:hover {
  transform: scale(1.05);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.profile-icon {
  width: 5rem;
  height: 5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 3rem;
  margin: 0 auto 1.5rem;
}

.gradient-pink {
  background: linear-gradient(135deg, #ec4899 0%, #f43f5e 100%);
}

.gradient-blue {
  background: linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%);
}

.gradient-purple {
  background: linear-gradient(135deg, #a855f7 0%, #6366f1 100%);
}
```

---

## 📱 Version Mobile Responsive

```typescript
// Composant optimisé mobile
export function ChoixProfilMobile() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-600 to-pink-500 px-4 py-8">
      <div className="text-center mb-8">
        <h1 className="text-3xl md:text-5xl font-bold text-white mb-2">
          KènèyaMuso
        </h1>
        <p className="text-white/90">Pour une maternité saine 🇲🇱</p>
      </div>

      <div className="space-y-4">
        {profiles.map((profile) => (
          <button
            key={profile.role}
            onClick={() => handleSelect(profile.role)}
            className="w-full bg-white rounded-xl p-6 shadow-lg active:scale-95 transition-transform"
          >
            <div className="flex items-center gap-4">
              <div className={`w-16 h-16 rounded-full bg-gradient-to-br ${profile.color} flex items-center justify-center text-3xl`}>
                {profile.icon}
              </div>
              <div className="text-left flex-1">
                <h3 className="text-xl font-bold text-gray-800">
                  {profile.title}
                </h3>
                <p className="text-sm text-gray-600">
                  {profile.description}
                </p>
              </div>
              <span className="text-2xl">→</span>
            </div>
          </button>
        ))}
      </div>
    </div>
  );
}
```

---

## ✅ Checklist d'Implémentation

- [ ] Créer la page `ChoixProfil.tsx`
- [ ] Créer la page `Inscription.tsx` avec formulaire dynamique
- [ ] Configurer les routes React Router
- [ ] Tester le flux complet :
  - [ ] Choix Patiente → Inscription → Dashboard
  - [ ] Choix Médecin → Inscription → Dashboard
  - [ ] Choix Admin → Inscription → Dashboard
- [ ] Ajouter la validation des formulaires
- [ ] Gérer les erreurs d'inscription
- [ ] Design responsive mobile
- [ ] Tests sur différents navigateurs

---

**Voilà ! Un flux d'onboarding complet et moderne pour KènèyaMuso ! 🎉**

Le code est prêt à l'emploi avec React + TypeScript + Tailwind CSS !


