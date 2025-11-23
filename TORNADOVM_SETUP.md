# TornadoVM Setup - Guide

## Problème Actuel
Les dépendances TornadoVM ne sont pas disponibles via le repo Maven public spécifié. Actuellement, le code utilise un **CPU fallback** dans `EternityKernel.java`.

## Solution 1: Installation Système (Recommandé pour développement)

### Prérequis
- **SDK requis**: CUDA (NVIDIA) OU OpenCL (AMD/Intel/NVIDIA)
- **JDK**: Java 21 (déjà installé)
- **Git**: Pour cloner le repo

### Étapes d'installation

```bash
# 1. Cloner TornadoVM
git clone https://github.com/beehive-lab/TornadoVM
cd TornadoVM

# 2. Installer avec le backend approprié
# Pour OpenCL (compatible avec plus de GPUs):
./bin/tornadovm-installer --jdk /path/to/jdk-21 --backend opencl

# Pour CUDA (NVIDIA uniquement):
./bin/tornadovm-installer --jdk /path/to/jdk-21 --backend ptx

# 3. Sourcer les variables d'environnement
source setvars.sh

# 4. Vérifier l'installation
tornado --version
tornado --devices
```

### Activation dans le projet

Une fois TornadoVM installé localement:

1. **Décommenter dans pom.xml** (lignes 131-141):
```xml
<dependency>
    <groupId>uk.ac.manchester.tornado</groupId>
    <artifactId>tornado-api</artifactId>
    <version>0.15</version>
</dependency>
```

2. **Ajouter annotation dans EternityKernel.java**:
```java
@Parallel
for (int i = 0; i < candidates.length / 4; i++) {
    // ...
}
```

3. **Run avec TornadoVM**:
```bash
tornado --jvm="-Dtornado.load.api.implementation=uk.ac.manchester.tornado.runtime.TornadoVMBackendType" \
        --printKernel \
        -jar target/eternity-1.0-SNAPSHOT.jar
```

## Solution 2: Skip TornadoVM (Mode actuel)

Le CPU fallback fonctionne très bien. **Recommandation**: Gardez cette approche jusqu'à ce que:
- TornadoVM soit disponible via Maven Central
- Vous ayez un GPU dédié pour les tests
- Les performances CPU deviennent un vrai bottleneck

## Benchmark CPU vs GPU (À faire si activé)

```bash
# Test CPU
java -jar target/eternity-1.0-SNAPSHOT.jar --benchmark

# Test GPU (avec TornadoVM)
tornado -jar target/eternity-1.0-SNAPSHOT.jar --benchmark
```

## Statut Actuel
- ✅ Architecture prête pour GPU
- ✅ CPU fallback fonctionnel
- ⏸️ TornadoVM en attente (dépendances non résolues)
- 📋 To-do: Benchmarker quand TornadoVM sera disponible
