# MindForce Mobile — App Chef de Département (Android natif / Kotlin)

Application Android native (Kotlin + Jetpack Compose) pour le rôle **Chef de Département** du projet MindForce.

## ✅ Fonctionnalités implémentées

- **Authentification 2FA** : email/mot de passe → code reçu par email → vérification → session JWT
- **Matériel de mon département** : liste + détail, filtré automatiquement côté serveur via le nouvel endpoint `/api/materiels/mon-departement` (voir section Backend ci-dessous)
- **Signalement de panne avec photo obligatoire** : description + type (réparable / non réparable) + photo **prise avec l'appareil photo OU choisie dans la galerie** (les deux options sont proposées, une photo est obligatoire avant l'envoi)
- **Historique des pannes** d'un matériel donné
- **Mes signalements** : liste de toutes les pannes signalées par l'utilisateur connecté
- **Déconnexion** (bouton rouge, seule exception à la charte graphique)

## 🎨 Charte graphique respectée

| Usage | Couleur |
|---|---|
| Couleur principale (boutons, accents) | Orange `#F9AA00` |
| Fond | Blanc `#F8F5EE` |
| Texte | Noir `#010101` |
| Déconnexion / suppression (exception) | Rouge |

Voir `ui/theme/Color.kt` et `ui/theme/Theme.kt`.

## ⚙️ Configuration avant de lancer l'app

Ouvrir `app/src/main/java/com/mindforce/mindlog/data/remote/RetrofitClient.kt` et adapter `BASE_URL` :

- **Émulateur Android Studio** : `http://10.0.2.2:8080/` (déjà configuré par défaut, pointe vers le `localhost:8080` de votre machine)
- **Téléphone physique sur le même réseau Wi-Fi** : `http://<IP_LOCALE_DE_VOTRE_MACHINE>:8080/`
- **Backend déployé** : URL publique HTTPS

## 🛠️ Lancer le projet

> **Note sur le Gradle Wrapper** : le fichier binaire `gradle-wrapper.jar` n'est pas inclus dans cette archive (généré automatiquement, pas de sens à le livrer en source). À l'ouverture du projet, Android Studio proposera de le régénérer automatiquement ; sinon, lancez `gradle wrapper` une fois avec un Gradle installé localement.

1. Ouvrir le dossier `MindForceMobile` dans Android Studio (Koala ou plus récent)
2. Laisser Gradle synchroniser les dépendances (nécessite un accès internet la première fois)
3. Lancer le backend Spring Boot (voir le zip du backend fourni séparément)
4. Sélectionner un émulateur (API 26+) ou brancher un téléphone en mode debug USB
5. Run ▶️

## 📁 Architecture

```
data/
  model/        → DTOs Kotlin miroir des DTOs backend (Auth, Materiel, Panne)
  remote/       → Retrofit (ApiService), intercepteur JWT, gestion des erreurs
  local/        → SessionManager (DataStore) : token, identité, département
  repository/   → Logique d'appel API + mapping résultat (ApiResult.Success/Error)
ui/
  theme/        → Couleurs et thème Compose (charte MindForce)
  components/   → Boutons, bannières, barres de titre réutilisables
  navigation/    → Graphe de navigation Compose
  screens/
    login/      → Connexion (2 étapes : identifiants → code 2FA)
    home/       → Tableau de bord
    materiels/  → Liste + détail du matériel du département
    pannes/     → Signalement (avec photo) + historique + mes signalements
util/
  PhotoFileUtil → Gestion des fichiers photo (caméra via FileProvider, copie depuis la galerie)
```

## ⚠️ Important — modifications apportées au backend

Le cahier des charges initial ("matériels filtrés par département") ne correspondait pas exactement
à ce qui existait dans le code backend fourni. Voici ce qui a été corrigé (voir le zip backend mis à jour) :

1. **Nouvel endpoint `GET /api/materiels/mon-departement`** : renvoie uniquement le matériel affecté
   au département du Chef de Département connecté (résolu depuis le token, pas depuis un paramètre
   client — donc impossible pour un chef de consulter le matériel d'un autre département).
2. **`LoginResponse` enrichie** : après vérification du code 2FA, la réponse contient désormais
   `id` (identifiant de l'utilisateur, nécessaire pour signaler une panne), `departementId` et
   `departementNom` pour les Chefs de Département.
3. **`POST /api/pannes` passe en `multipart/form-data`** : une partie `data` (JSON) + une partie
   `photo` (fichier image, **obligatoire**). Le champ `justificatif` n'est plus saisi manuellement :
   il est calculé côté serveur à partir du fichier reçu et exposé publiquement via `/uploads/pannes/...`.
4. **Nouvel endpoint `GET /api/pannes/mes-signalements`** : pratique pour que l'app affiche les
   pannes signalées par l'utilisateur connecté sans connaître son ID à l'avance.

Ces changements sont rétrocompatibles avec le reste de l'application (aucun endpoint existant
utilisé par le front web n'a été supprimé), à l'exception du endpoint de signalement de panne qui
est passé de JSON pur à multipart — le front web devra donc être adapté s'il utilise cet endpoint
directement.
