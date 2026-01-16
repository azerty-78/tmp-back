# 🚀 Configuration SMTP en Production (VPS)

Ce guide explique comment configurer l'envoi d'emails en **production sur votre VPS**.

## 🚨 IMPORTANT : MailHog ne fonctionne PAS en production

**MailHog est UNIQUEMENT pour le développement local.**

❌ **En production, MailHog NE PEUT PAS être utilisé car** :
- Les emails ne sortent jamais de votre VPS
- Aucun utilisateur ne recevra d'emails
- MailHog n'est accessible que sur `localhost` (votre machine locale)

✅ **En production, vous DEVEZ utiliser un serveur SMTP externe** qui envoie vraiment les emails.

---

## 📊 Options de Serveurs SMTP pour Production

### Option 1 : LWS Email Pro (Recommandé si vous avez un domaine LWS)

**Avantages** :
- ✅ Professionnel (emails depuis votre domaine)
- ✅ Pas de limite
- ✅ Fiable

**Configuration** :
```env
# setup-api/.env sur votre VPS
MAIL_HOST=smtp.lws.fr
MAIL_PORT=587
MAIL_USERNAME=contact@votre-domaine.com
MAIL_PASSWORD=votre-mot-de-passe-email-pro
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

EMAIL_FROM_ADDRESS=noreply@votre-domaine.com
EMAIL_FROM_NAME=Nom de votre entreprise
```

---

### Option 2 : Gmail SMTP

**Avantages** :
- ✅ Gratuit
- ✅ Facile à configurer

**Inconvénients** :
- ❌ Limite de 500 emails/jour
- ❌ Pas professionnel (emails depuis @gmail.com)
- ❌ Pas recommandé pour production

**Configuration** :
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre-email@gmail.com
MAIL_PASSWORD=mot-de-passe-application-16-caracteres
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

**Quand l'utiliser** :
- Pour tester en production avant d'avoir un email pro
- Pour des petits projets avec peu d'utilisateurs (< 500 emails/jour)

---

### Option 3 : SendGrid (Gratuit jusqu'à 100 emails/jour)

**Avantages** :
- ✅ Gratuit pour commencer
- ✅ Professionnel
- ✅ API et SMTP disponibles

**Configuration** :
1. Créer un compte sur https://sendgrid.com
2. Générer une API Key
3. Configurer :

```env
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=votre-api-key-sendgrid
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

---

### Option 4 : Mailgun (Gratuit jusqu'à 5000 emails/mois)

**Avantages** :
- ✅ Gratuit pour 5000 emails/mois
- ✅ Professionnel
- ✅ Fiable

**Configuration** :
1. Créer un compte sur https://www.mailgun.com
2. Vérifier votre domaine
3. Récupérer les credentials SMTP

```env
MAIL_HOST=smtp.mailgun.org
MAIL_PORT=587
MAIL_USERNAME=postmaster@votre-domaine.com
MAIL_PASSWORD=votre-api-key-mailgun
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

---

### Option 5 : Amazon SES (Payant mais très économique)

**Avantages** :
- ✅ Très économique ($0.10 pour 1000 emails)
- ✅ Illimité (après vérification)
- ✅ Professionnel
- ✅ Très fiable

**Configuration** :
1. Créer un compte AWS
2. Activer Amazon SES
3. Vérifier votre domaine
4. Récupérer les credentials SMTP

```env
MAIL_HOST=email-smtp.region.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=votre-access-key
MAIL_PASSWORD=votre-secret-key
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

---

## 🔧 Configuration sur VPS

### Étape 1 : Modifier `setup-api/.env` sur votre VPS

```env
# Configuration SMTP Production
MAIL_HOST=smtp.lws.fr  # Ou smtp.gmail.com, smtp.sendgrid.net, etc.
MAIL_PORT=587
MAIL_USERNAME=votre-email@votre-domaine.com
MAIL_PASSWORD=votre-mot-de-passe
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
MAIL_CONNECTION_TIMEOUT=5000
MAIL_TIMEOUT=5000
MAIL_WRITE_TIMEOUT=5000

# Email de l'application
EMAIL_FROM_ADDRESS=noreply@votre-domaine.com
EMAIL_FROM_NAME=Nom de votre entreprise
EMAIL_FRONTEND_URL=https://votre-domaine.com
```

### Étape 2 : Redémarrer l'API

```bash
cd setup-api
docker-compose restart
```

### Étape 3 : Tester

Créer un compte de test et vérifier que l'email arrive bien.

---

## 📋 Checklist Production

- [ ] Serveur SMTP externe configuré (LWS, Gmail, SendGrid, etc.)
- [ ] `EMAIL_FROM_ADDRESS` configuré avec votre domaine
- [ ] Test d'envoi d'email réussi
- [ ] Vérifier que les emails arrivent bien (pas dans les spams)
- [ ] Configuration sauvegardée dans `setup-api/.env` (ne jamais commiter ce fichier)

---

## ❓ Questions Fréquentes

### Q: Puis-je installer un serveur SMTP sur mon VPS ?

**R:** Techniquement oui, mais **ce n'est PAS recommandé** :
- Configuration complexe (Postfix, etc.)
- Risque de blacklist (votre IP peut être blacklistée)
- Maintenance difficile
- Pas de garantie de livraison

**Recommandation** : Utilisez un service SMTP externe (LWS, SendGrid, etc.)

### Q: MailHog peut-il fonctionner sur mon VPS ?

**R:** ❌ **NON !** Même si vous installez MailHog sur votre VPS :
- Les emails ne sortiraient jamais de votre serveur
- Les utilisateurs ne recevraient rien
- MailHog est uniquement pour le développement local

### Q: Gmail suffit-il pour la production ?

**R:** ⚠️ **Dépend de votre volume** :
- ✅ OK si < 500 emails/jour
- ❌ Pas suffisant si > 500 emails/jour
- ⚠️ Pas professionnel (emails depuis @gmail.com)

**Recommandation** : Utilisez un service professionnel (LWS, SendGrid, Mailgun) pour la production.

### Q: Quel service choisir pour la production ?

**Recommandation** :
1. **LWS** : Si vous avez déjà un domaine chez LWS
2. **SendGrid** : Si vous voulez gratuit pour commencer (100 emails/jour)
3. **Mailgun** : Si vous voulez gratuit avec plus de volume (5000/mois)
4. **Amazon SES** : Si vous voulez un service très fiable et économique

---

## 🔒 Sécurité

**⚠️ IMPORTANT** :
- Ne jamais commiter les mots de passe SMTP
- Utiliser des variables d'environnement
- Ne pas mettre les credentials dans le code source
- Utiliser des mots de passe d'application (pour Gmail) ou des API keys (pour SendGrid/Mailgun)

---

**Voir aussi** :
- `QUEL-SMTP-UTILISER.md` : Guide complet MailHog vs Gmail vs Production
- `CONFIGURATION-GMAIL.md` : Guide Gmail SMTP
- `README.md` : Documentation principale
