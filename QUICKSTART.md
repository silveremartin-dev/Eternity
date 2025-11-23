# Eternity II - Guide de Démarrage Rapide

## 🚀 Lancement en 3 minutes

### Prérequis
- ✅ Java 21 installé
- ✅ Maven installé
- ✅ Docker installé (optionnel pour Redis)

### Option 1 : Mode Simple (Sans Redis)

```bash
# 1. Compiler
mvn clean package

# 2. Lancer le serveur
java -jar target/eternity-1.0-SNAPSHOT.jar

# 3. Le serveur démarre sur le port 8080
# Mode in-memory - pas besoin de Redis
```

### Option 2 : Mode Complet (Avec Redis)

```bash
# 1. Démarrer Redis
docker-compose up -d

# 2. Vérifier Redis
docker ps  # Redis doit être "Up"

# 3. Compiler et lancer
mvn clean package
java -jar target/eternity-1.0-SNAPSHOT.jar

# 4. Le serveur utilise Redis automatiquement
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
kubectl port-forward svc/eternity-server 8080:8080
```

---

## 🧪 Vérifier que tout fonctionne

### Test 1 : Le serveur répond
```bash
# Le serveur doit afficher au démarrage :
# "EternityServer started on port 8080"
# "Using Virtual Threads: true"
```

### Test 2 : Redis (si activé)
```bash
# Se connecter à Redis
docker exec -it eternity-redis-1 redis-cli

# Tester
127.0.0.1:6379> PING
PONG

# Vérifier les clés (après quelques jobs)
127.0.0.1:6379> KEYS *
```

### Test 3 : gRPC (optionnel)
```bash
# Avec grpcurl installé :
grpcurl -plaintext localhost:50051 list

# Devrait afficher les services gRPC disponibles
```

---

## 🔧 Configuration Rapide

### Variables d'environnement

```bash
# Modifier le port serveur
export SERVER_PORT=9000

# Pointer vers un Redis distant
export REDIS_HOST=redis.example.com
export REDIS_PORT=6379

# Lancer avec config custom
java -jar target/eternity-1.0-SNAPSHOT.jar
```

### Fichier de configuration (optionnel)

Créer `application.properties` :
```properties
server.port=8080
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
1. 📖 Lire `TORNADOVM_SETUP.md`
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
