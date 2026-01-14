// Script d'initialisation MongoDB
// Ce script est exécuté automatiquement au premier démarrage de MongoDB
// Il crée la base de données et configure les utilisateurs si nécessaire

// Obtenir le nom de la base de données depuis la variable d'environnement
// ou utiliser 'project-name' par défaut
const dbName = process.env.MONGO_INITDB_DATABASE || 'project-name';

print('==========================================');
print('Initialisation de la base de données MongoDB');
print('==========================================');
print('Base de données: ' + dbName);

// Sélectionner la base de données (la crée si elle n'existe pas)
db = db.getSiblingDB(dbName);

// Créer une collection vide pour forcer la création de la base de données
// MongoDB crée la base de données uniquement lors de la première écriture
db.createCollection('_init');

// Créer un index sur cette collection pour s'assurer que la base est bien créée
db._init.createIndex({ "createdAt": 1 });

print('✅ Base de données "' + dbName + '" créée avec succès');
print('✅ Collection "_init" créée pour initialiser la base');

// Optionnel : Créer un utilisateur spécifique pour cette base de données
// Décommentez les lignes suivantes si vous voulez créer un utilisateur dédié
/*
db.createUser({
  user: 'app_user',
  pwd: 'app_password',
  roles: [
    {
      role: 'readWrite',
      db: dbName
    }
  ]
});
print('✅ Utilisateur "app_user" créé pour la base "' + dbName + '"');
*/

print('==========================================');
print('Initialisation terminée');
print('==========================================');
