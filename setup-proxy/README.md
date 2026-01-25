# Setup Proxy (Traefik) - OPTIONNEL

## Quand utiliser ce setup ?

Ce setup est **OPTIONNEL** et n'est nécessaire que pour la **production** si vous avez besoin de :

- ✅ Gérer les domaines custom des tenants (ex: `app.cliententreprise.fr`)
- ✅ SSL/HTTPS automatique avec Let's Encrypt
- ✅ Wildcard SSL pour `*.kobecorporation.com`
- ✅ Load balancing (si plusieurs instances de l'API)

## Pour le développement local

**Vous n'avez PAS besoin de ce setup !**

En développement, utilisez simplement :
- Le header `X-Tenant-ID` pour spécifier le tenant
- L'URL `http://localhost:8090` pour l'API

## Architecture Multi-Tenant avec Traefik

```
Internet
    │
    ▼
┌─────────────────────────────────────────┐
│              TRAEFIK                    │
│  (Reverse Proxy + SSL + Load Balancer) │
└─────────────────────────────────────────┘
    │
    ├── kb-saas-acme.kobecorporation.com ──► API (tenant: acme)
    ├── kb-saas-demo.kobecorporation.com ──► API (tenant: demo)
    └── app.cliententreprise.fr ───────────► API (tenant: client)
```

## Démarrage

### 1. Prérequis
- Les autres setups doivent être démarrés d'abord (`setup-bd`, `setup-api`)
- Le réseau Docker `project-name-network` doit exister

### 2. Configuration
Modifier `.env` avec :
- `ACME_EMAIL` : Email pour Let's Encrypt
- `PLATFORM_DOMAIN` : Domaine principal

### 3. Lancement
```bash
cd setup-proxy
docker-compose up -d
```

### 4. Accès Dashboard Traefik
- URL : http://localhost:8080
- ⚠️ Désactiver en production !

## Configuration DNS

Pour que les domaines custom fonctionnent, vous devez configurer :

### Wildcard pour les sous-domaines par défaut
```
*.kobecorporation.com → IP_SERVEUR
```

### Domaines custom des clients
Demandez aux clients d'ajouter un enregistrement CNAME :
```
app.cliententreprise.fr → proxy.kobecorporation.com
```

## Fichiers

```
setup-proxy/
├── docker-compose.yaml   # Configuration Traefik
├── .env                  # Variables d'environnement
└── README.md             # Cette documentation
```
