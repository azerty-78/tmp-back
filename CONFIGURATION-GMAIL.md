# 📧 Configuration Gmail SMTP pour Tests

Ce guide vous explique comment configurer l'application pour envoyer des emails via Gmail SMTP, afin de tester avec votre vraie boîte Gmail.

## ✅ Sécurité du Code de Vérification

**Oui, le code est généré de manière aléatoire et sécurisée !**

Le code utilise `SecureRandom` de Java, qui est :
- ✅ **Cryptographiquement sécurisé** : Utilise un générateur de nombres aléatoires cryptographiquement fort
- ✅ **Imprévisible** : Impossible de deviner ou prédire le prochain code
- ✅ **Aléatoire** : Chaque code a une probabilité égale d'être généré (1 chance sur 1 000 000)
- ✅ **Non séquentiel** : Les codes ne suivent pas un ordre prévisible

**Format du code** : 6 chiffres (000000 à 999999)
**Exemples** : `123456`, `789012`, `456789`

## 🔧 Configuration Gmail SMTP

### Étape 1 : Activer l'authentification à deux facteurs sur Gmail

1. Allez sur https://myaccount.google.com/security
2. Activez la **"Validation en deux étapes"** si ce n'est pas déjà fait

### Étape 2 : Générer un mot de passe d'application

1. Allez sur https://myaccount.google.com/apppasswords
2. Sélectionnez "Application" : **Mail**
3. Sélectionnez "Appareil" : **Autre (nom personnalisé)**
4. Entrez un nom (ex: "KOBE API")
5. Cliquez sur **"Générer"**
6. **Copiez le mot de passe à 16 caractères** (ex: `abcd efgh ijkl mnop`)

⚠️ **Important** : Ce mot de passe est différent de votre mot de passe Gmail normal. Utilisez ce mot de passe d'application pour l'API.

### Étape 3 : Configurer l'application

#### Option A : Via le script d'initialisation

Lors de l'exécution de `init-project.ps1` ou `init-project.sh` :
- Choisissez "Configuration SMTP de production"
- Entrez les paramètres Gmail :
  - **Host SMTP** : `smtp.gmail.com`
  - **Port SMTP** : `587`
  - **Username** : `votre-email@gmail.com`
  - **Password** : Le mot de passe d'application généré (16 caractères, sans espaces)

#### Option B : Modifier directement `setup-api/.env`

```env
# Configuration SMTP Gmail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=abcdefghijklmnop
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_CONNECTION_TIMEOUT=5000
MAIL_TIMEOUT=5000
MAIL_WRITE_TIMEOUT=5000

# Email de l'application
EMAIL_FROM_ADDRESS=votre-email@gmail.com
EMAIL_FROM_NAME=KOBE Corporation
EMAIL_FRONTEND_URL=http://localhost:3000
```

### Étape 4 : Tester l'envoi d'emails

1. **Démarrer l'application** :
   ```bash
   ./gradlew bootRun
   # OU
   cd setup-api && docker-compose up -d
   ```

2. **Créer un compte de test** :
   ```bash
   curl -X POST http://localhost:8090/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "votre-email@gmail.com",
       "password": "Test123!",
       "firstName": "Test",
       "lastName": "User"
     }'
   ```

3. **Vérifier votre boîte Gmail** :
   - Ouvrez votre boîte Gmail
   - Vérifiez les spams si nécessaire
   - Vous devriez recevoir un email avec un code à 6 chiffres

4. **Vérifier l'email avec le code** :
   ```bash
   curl -X POST http://localhost:8090/api/auth/verify-email \
     -H "Content-Type: application/json" \
     -d '{
       "email": "votre-email@gmail.com",
       "code": "123456"
     }'
   ```
   Remplacez `123456` par le code réel reçu dans Gmail.

## ⚙️ Paramètres SMTP Gmail

| Paramètre | Valeur |
|-----------|--------|
| **Host** | `smtp.gmail.com` |
| **Port** | `587` (STARTTLS) ou `465` (SSL) |
| **Username** | Votre adresse Gmail complète |
| **Password** | Mot de passe d'application (16 caractères) |
| **Auth** | `true` |
| **STARTTLS** | `true` (pour le port 587) |

## 🔒 Sécurité

### Pourquoi utiliser un mot de passe d'application ?

- ✅ Plus sécurisé que votre mot de passe Gmail principal
- ✅ Peut être révoqué individuellement
- ✅ Ne donne pas accès à votre compte Gmail complet
- ✅ Spécifique à l'application

### Limites Gmail

- **Quota quotidien** : Gmail limite à environ 500 emails/jour pour les comptes gratuits
- **Pour la production** : Utilisez un service email professionnel (LWS, SendGrid, etc.)

## 🐛 Dépannage

### Erreur : "Username and Password not accepted"

**Solutions** :
1. Vérifiez que vous utilisez le **mot de passe d'application** (pas votre mot de passe Gmail)
2. Vérifiez que la validation en deux étapes est activée
3. Vérifiez que le mot de passe d'application n'a pas d'espaces

### Erreur : "Connection timeout"

**Solutions** :
1. Vérifiez votre connexion internet
2. Vérifiez que le port 587 n'est pas bloqué par un firewall
3. Essayez le port 465 avec SSL au lieu de STARTTLS

### L'email n'arrive pas

**Vérifications** :
1. Vérifiez le dossier **Spam** dans Gmail
2. Vérifiez les logs de l'application pour les erreurs SMTP
3. Vérifiez que le mot de passe d'application est correct
4. Attendez quelques secondes (Gmail peut avoir un délai)

## 📝 Alternative : Gmail avec OAuth2

Pour une sécurité encore plus élevée, vous pouvez utiliser OAuth2 au lieu d'un mot de passe d'application, mais cela nécessite une configuration plus complexe. Pour les tests, le mot de passe d'application est suffisant.

---

**Note** : Pour la production, utilisez un service email professionnel plutôt que Gmail.
