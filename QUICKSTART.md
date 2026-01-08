# Eternity II - Quick Start Guide

**Authors:** Gemini AI Assistant, Silvère

## 🚀 Get started in 3 minutes

### Prerequisites

- ✅ Java 21 installed
- ✅ Maven installed
- ✅ Docker installed (optional, for Redis)

### Option 1 : Mode Simple (Sans Redis)

```bash
# 1. Compiler
mvn clean package -DskipTests

# 2. Lancer le serveur (GUI)
java -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp

# 3. Le serveur démarre sur le port 12345
# Mode in-memory - pas besoin de Redis
```

### Option 2 : Mode Complet (Avec Redis)

```bash
# 1. Démarrer Redis
docker-compose up -d

# 2. Vérifier Redis
docker ps  # Redis doit être "Up"

# 3. Compiler et lancer
mvn clean package -DskipTests
java -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp

# 4. Le serveur utilise Redis automatiquement si REDIS_HOST est défini ou localhost
```

### Option 3 : Mode Kubernetes (Local)

```bash
# 1. Activer Kubernetes dans Docker Desktop
# Settings → Kubernetes → Enable Kubernetes

# 2. Builder l'image
docker build -t eternity-server:latest .

# 3. Déployer
kubectl apply -f k8s/redis.yaml
kubectl apply -f k8s/eternity.yaml
kubectl apply -f k8s/hpa.yaml

# 4. Vérifier
kubectl get pods
kubectl get svc

# 5. Accéder au service
kubectl port-forward svc/eternity-server 12345:12345
```

---

## 🧪 Vérifier que tout fonctionne

### Test 1 : Le serveur répond

```bash
# Le serveur doit afficher au démarrage :
# "Server started on port 12345 (Virtual Threads)"
```

### Test 2 : Redis (si activé)

```bash
# Se connecter à Redis
docker exec -it eternity-redis-1 redis-cli

# Tester
127.0.0.1:6379> PING
PONG
```

### Test 3 : Client

```bash
# Lancer un client pour se connecter au serveur
java -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
```

---

## 🔧 Configuration Rapide

### Variables d'environnement

```bash
# Modifier le port serveur (nécessite modif code ou config file)
# Par défaut : 12345

# Pointer vers un Redis distant
export REDIS_HOST=redis.example.com
export REDIS_PORT=6379

# Lancer
java -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.server.ServerApp
```

### Fichier de configuration (optionnel)

Créer `application.properties` (si supporté par la version actuelle) :

```properties
server.port=12345
redis.host=localhost
redis.port=6379
solver.threads=8
```

---

## 📊 Monitoring Basique

### Logs

```bash
# Logs du serveur (niveau INFO par défaut)
tail -f logs/server.log

# Changer le niveau de log dans log4j2.xml
```

### Métriques Docker

```bash
# Ressources utilisées
docker stats eternity-server

# Logs du container
docker logs -f eternity-server
```

### Métriques Kubernetes

```bash
# CPU/Mémoire des pods
kubectl top pods

# Statut HPA
kubectl get hpa

# Logs
kubectl logs -f deployment/eternity-server
```

---

## 🐛 Troubleshooting

### Problème : "Port already in use"

```bash
# Trouver le processus sur le port 8080
netstat -ano | findstr :8080

# Tuer le processus (Windows)
taskkill /PID <PID> /F

# Ou changer le port
java -Dserver.port=9000 -jar target/eternity-1.0-SNAPSHOT.jar
```

### Problème : "Cannot connect to Redis"

```bash
# Vérifier Redis
docker ps | grep redis

# Redémarrer Redis
docker-compose restart

# Vérifier les logs
docker logs eternity-redis-1
```

### Problème : "Build failed"

```bash
# Nettoyer Maven
mvn clean

# Forcer re-téléchargement des dépendances
mvn clean install -U

# Vérifier Java version
java -version  # Doit être 21
```

---

## 🎯 Prochaines Étapes

### Pour tester localement

1. ✅ Lancer le serveur (voir ci-dessus)
2. ✅ Ouvrir l'interface JavaFX (si disponible)
3. ✅ Lancer un solving job
4. ✅ Observer les logs

### Pour déployer en production

1. 📖 Lire `PROJECT_SUMMARY.md`
2. 🔧 Configurer les Secrets (Redis password, etc.)
3. ☁️ Choisir un provider cloud (AWS/GCP/Azure)
4. 🚀 Suivre le guide Kubernetes

### Pour activer le GPU (futur)

1. 📖 Lire `DEPLOYMENT.md`
2. 🖥️ Provisionner une VM avec GPU (NVIDIA)
3. 🔧 Installer CUDA + TornadoVM
4. ⚙️ Décommenter les dépendances dans `pom.xml`

---

## 📚 Documentation Complète

- **[README.md](README.md)** - Vue d'ensemble
- **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** - Résumé complet
- **[TASK.md](TASK.md)** - Liste des tâches
- **[REDIS_SETUP.md](REDIS_SETUP.md)** - Config Redis détaillée

---

**Bon développement ! 🎮**
