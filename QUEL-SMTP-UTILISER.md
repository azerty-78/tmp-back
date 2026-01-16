# 📧 Quel Serveur SMTP Utiliser ?

Ce guide explique les différents serveurs SMTP disponibles et **quand utiliser chacun**.

## 🎯 Vue d'ensemble

Vous avez **3 options** pour envoyer des emails, selon votre contexte :

| Serveur SMTP | Usage | Avantages | Inconvénients |
|--------------|-------|-----------|---------------|
| **MailHog** | Développement/Test | Rapide, pas de config, interface web | Emails ne partent pas vraiment |
| **Gmail** | Tests avec vrais emails | Gratuit, facile à configurer | Limite 500 emails/jour, nécessite config |
| **LWS/Production** | Production | Professionnel, pas de limite | Nécessite achat domaine/email pro |

---

## 1. 🧪 MailHog (Serveur SMTP de Test)

### À quoi ça sert ?

**MailHog est votre serveur SMTP de développement**. Il capture tous les emails envoyés par l'application et les affiche dans une interface web, **sans vraiment les envoyer**.

### Quand l'utiliser ?

✅ **Développement local** : Quand vous codez et testez rapidement  
✅ **Tests automatisés** : Pour les tests unitaires/intégration  
✅ **Développement en équipe** : Chacun peut voir les emails sans configurer Gmail  
✅ **Pas de limite** : Envoyez autant d'emails que vous voulez  
✅ **Rapide** : Pas besoin de configurer Gmail ou un vrai serveur SMTP

### Avantages

- ✅ **Aucune configuration** : Fonctionne immédiatement
- ✅ **Interface web** : Voir tous les emails sur http://localhost:8025
- ✅ **Pas de limite** : Envoyez 1000 emails sans problème
- ✅ **Rapide** : Pas de délai réseau réel
- ✅ **Pas de spam** : Les emails ne partent pas vraiment

### Inconvénients

- ❌ **Les emails ne partent pas vraiment** : Ils restent dans MailHog
- ❌ **Pas de test réel** : Vous ne testez pas avec une vraie boîte mail
- ❌ **Pas pour la production** : Jamais utiliser MailHog en production

### Configuration

```env
# setup-api/.env
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
```

### Utilisation

```bash
# Démarrer MailHog
cd setup-smtp
docker-compose up -d

# Voir les emails
# Ouvrir http://localhost:8025
```

---

## 2. 📬 Gmail SMTP (Tests avec Vrais Emails)

### À quoi ça sert ?

**Gmail SMTP permet d'envoyer de vrais emails** via votre compte Gmail. Utile pour tester que les emails arrivent bien dans une vraie boîte mail.

### Quand l'utiliser ?

✅ **Tests avec vraie boîte mail** : Vérifier que les emails arrivent bien  
✅ **Démonstration client** : Montrer que le système fonctionne vraiment  
✅ **Tests avant production** : Valider le format des emails  
✅ **Développement personnel** : Tester avec votre propre Gmail

### Avantages

- ✅ **Vrais emails** : Les emails arrivent vraiment dans votre boîte Gmail
- ✅ **Gratuit** : Pas besoin d'acheter un domaine
- ✅ **Facile à configurer** : Juste un mot de passe d'application
- ✅ **Test réel** : Vous voyez exactement ce que recevront les utilisateurs

### Inconvénients

- ❌ **Limite de 500 emails/jour** : Gmail limite les comptes gratuits
- ❌ **Nécessite configuration** : Doit générer un mot de passe d'application
- ❌ **Pas professionnel** : L'email vient de @gmail.com, pas de votre domaine
- ❌ **Pas pour la production** : Utilisez un email professionnel en production

### Configuration

Voir **`CONFIGURATION-GMAIL.md`** pour les instructions détaillées.

```env
# setup-api/.env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=mot-de-passe-application-16-caracteres
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

---

## 3. 🏢 Serveur SMTP Production (LWS, etc.)

### À quoi ça sert ?

**Le serveur SMTP de production** est celui que vous utilisez avec votre domaine professionnel (ex: `noreply@votre-domaine.com`).

### Quand l'utiliser ?

✅ **Production** : Quand l'application est en ligne pour les vrais utilisateurs  
✅ **Client final** : Quand vous déployez pour un client avec son domaine  
✅ **Emails professionnels** : Pour envoyer depuis l'adresse de l'entreprise

### Avantages

- ✅ **Professionnel** : Emails depuis votre domaine (ex: noreply@votre-domaine.com)
- ✅ **Pas de limite** : Services professionnels n'ont généralement pas de limite
- ✅ **Fiable** : Services dédiés pour l'envoi d'emails
- ✅ **Branding** : Les emails viennent de votre entreprise

### Inconvénients

- ❌ **Nécessite achat** : Doit acheter un domaine et un service email
- ❌ **Configuration** : Doit configurer les paramètres SMTP du fournisseur
- ❌ **Coût** : Service payant (mais généralement raisonnable)

### Configuration (Exemple LWS)

```env
# setup-api/.env
MAIL_HOST=smtp.lws.fr
MAIL_PORT=587
MAIL_USERNAME=contact@votre-domaine.com
MAIL_PASSWORD=mot-de-passe-email-pro
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

EMAIL_FROM_ADDRESS=noreply@votre-domaine.com
EMAIL_FROM_NAME=Nom de votre entreprise
```

---

## 📊 Tableau Comparatif

| Critère | MailHog | Gmail | Production (LWS) |
|---------|---------|-------|------------------|
| **Configuration** | ⚡ Aucune | 🔧 Moyenne | 🔧 Moyenne |
| **Coût** | 💰 Gratuit | 💰 Gratuit | 💰 Payant |
| **Limite emails** | ♾️ Illimité | 📊 500/jour | ♾️ Illimité |
| **Emails réels** | ❌ Non | ✅ Oui | ✅ Oui |
| **Domaine pro** | ❌ Non | ❌ Non | ✅ Oui |
| **Pour dev** | ✅ Oui | ⚠️ Optionnel | ❌ Non |
| **Pour prod** | ❌ Non | ❌ Non | ✅ Oui |

---

## 🎯 Recommandations par Scénario

### Scénario 1 : Développement Local

**Utilisez MailHog** :
- Développement rapide
- Pas besoin de configurer Gmail
- Voir les emails instantanément dans l'interface web
- Pas de limite

```bash
cd setup-smtp
docker-compose up -d
# Configuration déjà prête dans setup-api/.env
```

### Scénario 2 : Test avec Vraie Boîte Mail

**Utilisez Gmail** :
- Vous voulez vérifier que les emails arrivent vraiment
- Vous voulez tester le format des emails
- Démonstration pour un client

**Configuration** : Voir `CONFIGURATION-GMAIL.md`

### Scénario 3 : Production

**Utilisez le serveur SMTP de production** (LWS, etc.) :
- Application en ligne pour les vrais utilisateurs
- Emails depuis le domaine professionnel
- Pas de limite

**Configuration** : Via le script `init-project.ps1` ou directement dans `setup-api/.env`

---

## 🔄 Changer de Serveur SMTP

### Passer de MailHog à Gmail

1. Modifier `setup-api/.env` :
   ```env
   MAIL_HOST=smtp.gmail.com
   MAIL_PORT=587
   MAIL_USERNAME=votre-email@gmail.com
   MAIL_PASSWORD=mot-de-passe-application
   MAIL_SMTP_AUTH=true
   MAIL_SMTP_STARTTLS=true
   ```

2. Arrêter MailHog (optionnel) :
   ```bash
   cd setup-smtp
   docker-compose down
   ```

3. Redémarrer l'API :
   ```bash
   cd setup-api
   docker-compose restart
   ```

### Passer de Gmail à Production

1. Modifier `setup-api/.env` avec les paramètres du serveur SMTP de production
2. Redémarrer l'API

---

## ❓ Questions Fréquentes

### Q: Puis-je utiliser MailHog ET Gmail en même temps ?

**R:** Non, vous ne pouvez utiliser qu'un seul serveur SMTP à la fois. Changez la configuration dans `setup-api/.env`.

### Q: MailHog est-il suffisant pour le développement ?

**R:** Oui ! MailHog est parfait pour le développement. Utilisez Gmail seulement si vous voulez vraiment tester avec une vraie boîte mail.

### Q: Dois-je configurer Gmail si j'utilise MailHog ?

**R:** Non ! MailHog fonctionne sans aucune configuration. Gmail est optionnel pour les tests avec de vrais emails.

### Q: Puis-je utiliser MailHog en production ?

**R:** ❌ **JAMAIS !** MailHog est uniquement pour le développement. Les emails ne partent pas vraiment, vos utilisateurs ne recevront rien.

### Q: Quelle est la différence entre MailHog et un vrai serveur SMTP ?

**R:** 
- **MailHog** : Capture les emails localement, ne les envoie pas vraiment
- **Vrai SMTP** : Envoie vraiment les emails aux destinataires

---

## 🚨 CLARIFICATION IMPORTANTE : Production vs Développement

### ⚠️ MailHog en Production : JAMAIS !

**MailHog ne peut PAS être utilisé en production** pour plusieurs raisons :

1. ❌ **Emails ne partent pas vraiment** : MailHog capture les emails localement, ils ne sortent jamais de votre machine
2. ❌ **Pas accessible depuis un VPS** : MailHog tourne sur `localhost`, il n'est accessible que sur votre machine locale
3. ❌ **Les utilisateurs ne recevront rien** : Si vous utilisez MailHog en production, aucun email ne sera vraiment envoyé
4. ❌ **Pas sécurisé** : MailHog n'a pas d'authentification

### ✅ En Production : Besoin d'un VRAI Serveur SMTP Externe

**Oui, pour envoyer de vrais emails en production, vous DEVEZ utiliser un serveur SMTP externe.**

Vous avez plusieurs options :

| Option | Coût | Limite | Professionnel |
|--------|------|--------|---------------|
| **LWS Email Pro** | 💰 Payant | ♾️ Illimité | ✅ Oui |
| **Gmail** | 💰 Gratuit | 📊 500/jour | ⚠️ Pas pro (@gmail.com) |
| **SendGrid** | 💰 Gratuit/Payant | 📊 100/jour (gratuit) | ✅ Oui |
| **Mailgun** | 💰 Gratuit/Payant | 📊 5000/mois (gratuit) | ✅ Oui |
| **Amazon SES** | 💰 Payant | ♾️ Illimité | ✅ Oui |
| **O2Switch** | 💰 Payant | ♾️ Illimité | ✅ Oui |

### 🔧 Configuration en Production sur VPS

Sur votre VPS en production, vous DEVEZ configurer un vrai serveur SMTP :

#### Option 1 : LWS (Recommandé si vous avez déjà un domaine LWS)

```env
# setup-api/.env sur votre VPS
MAIL_HOST=smtp.lws.fr
MAIL_PORT=587
MAIL_USERNAME=contact@votre-domaine.com
MAIL_PASSWORD=votre-mot-de-passe-email-pro
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

#### Option 2 : Gmail (Pour commencer, mais pas idéal)

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=mot-de-passe-application
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

⚠️ **Limite Gmail** : 500 emails/jour - pas suffisant pour une vraie production

#### Option 3 : SendGrid (Gratuit jusqu'à 100 emails/jour)

```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=votre-api-key-sendgrid
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

#### Option 4 : Mailgun (Gratuit jusqu'à 5000 emails/mois)

```env
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@votre-domaine.com
MAIL_PASSWORD=votre-api-key-mailgun
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

## 📝 Résumé

- **MailHog** = UNIQUEMENT développement local (jamais en production)
- **Gmail** = Tests avec vrais emails OU production basique (limite 500/jour)
- **LWS/Production** = Production réelle (professionnel, pas de limite)
- **SendGrid/Mailgun** = Alternatives gratuites pour production

### En Production sur VPS

**Vous DEVEZ utiliser un serveur SMTP externe** (LWS, Gmail, SendGrid, Mailgun, etc.)

**Vous NE POUVEZ PAS utiliser MailHog** car :
- Les emails ne sortiraient jamais de votre VPS
- Aucun utilisateur ne recevrait d'emails
- MailHog n'est accessible que localement

---

**Voir aussi** :
- `setup-smtp/README.md` : Guide MailHog
- `CONFIGURATION-GMAIL.md` : Guide Gmail SMTP
- `README.md` : Documentation principale
