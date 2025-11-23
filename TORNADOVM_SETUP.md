# TornadoVM Setup - Guide

## Problème Actuel
Les dépendances TornadoVM ne sont pas disponibles via le repo Maven public spécifié. Actuellement, le code utilise un **CPU fallback** dans `EternityKernel.java`.

## 📱 Votre GPU Intel UHD Graphics

**Bonne nouvelle** : Intel UHD Graphics supporte OpenCL ! Votre GPU (pilote 27.20.100.9079) peut fonctionner avec TornadoVM.

### Installer OpenCL pour Intel GPU (Windows)

#### Pré-vérification : Votre GPU est-il compatible ?

```powershell
# Dans PowerShell, vérifier les GPUs détectés
Get-WmiObject Win32_VideoController | Select-Object Name, DriverVersion

# Vous devriez voir : Intel UHD Graphics
```

#### Option 1 : Intel OpenCL Runtime (Windows natif - RECOMMANDÉ)

```powershell
# 1. Télécharger Intel OpenCL Runtime
# URL: https://www.intel.com/content/www/us/en/developer/articles/tool/opencl-drivers.html
# Ou directement le package CPU Runtime
# URL: https://github.com/intel/compute-runtime/releases

# 2. Installer le runtime
# Double-cliquer sur le fichier .exe téléchargé

# 3. Vérifier l'installation OpenCL
# Télécharger GPU Caps Viewer ou clinfo
# URL GPU Caps Viewer: https://www.geeks3d.com/dl/getfile.php?id=394

# Ou utiliser clinfo (via chocolatey):
choco install opencl-intel-cpu-runtime
clinfo
```

#### Option 2 : Via WSL2 (Plus simple pour TornadoVM)

**Important**: TornadoVM est conçu pour **Linux/Mac uniquement**. Les scripts d'installation (`tornadovm-installer`, `source`) sont des scripts **bash**, incompatibles avec PowerShell Windows.

### Options pour Windows

#### Option A: WSL2 (Windows Subsystem for Linux) - Recommandé
```bash
# 1. Installer WSL2 (si pas déjà fait)
# Dans PowerShell Admin:
wsl --install

# 2. Redémarrer Windows

# 3. Dans WSL Ubuntu:
wsl

# 4. Installer les prérequis dans WSL
sudo apt update
sudo apt install build-essential cmake git openjdk-21-jdk

# 5. Cloner et installer TornadoVM dans WSL
git clone https://github.com/beehive-lab/TornadoVM
cd TornadoVM
./bin/tornadovm-installer --jdk /usr/lib/jvm/java-21-openjdk-amd64 --backend opencl

# 6. Sourcer l'environnement (dans WSL)
source setvars.sh

# 7. Tester
tornado --version
```

**Limitation**: Pas de support GPU dans WSL2 par défaut (sauf avec WSL2 GPU support pour CUDA).

#### Option B: Dual-boot Linux - Pour production

#### Option C: VM Linux avec GPU passthrough - Complexe

### Solution Simple: Gardez le CPU Fallback

**Recommandation forte**: Pour votre cas d'usage Windows, **gardez le CPU fallback actuel**. Il fonctionne parfaitement et évite toute la complexité TornadoVM sur Windows.

### Activation dans le projet (Linux uniquement)

**Si vous êtes sur Linux** ou avez réussi l'installation dans WSL2:

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
import uk.ac.manchester.tornado.api.annotations.Parallel;

public static void checkCandidates(...) {
    @Parallel
    for (int i = 0; i < candidates.length / 4; i++) {
        // ...
    }
}
```

3. **Compiler et exécuter avec TornadoVM**:
```bash
# Dans l'environnement TornadoVM (après source setvars.sh)
mvn clean package
tornado --printKernel -jar target/eternity-1.0-SNAPSHOT.jar
```

## ✅ Recommandation pour Windows: CPU Fallback

Le CPU fallback actuel est **totalement fonctionnel** et **suffisant** pour votre usage. 

**Pourquoi ne PAS installer TornadoVM sur Windows:**
1. ❌ Scripts incompatibles avec PowerShell
2. ❌ WSL2 complexe et sans vrai support GPU
3. ❌ Temps d'installation vs gain minimal
4. ✅ Le CPU moderne (multi-core) est déjà très performant pour ce problème
5. ✅ L'architecture est **prête** pour GPU si besoin futur

**Si vraiment besoin de GPU:** Utilisez une machine Linux ou un serveur cloud (AWS/GCP avec GPU).

## Benchmark (Optionnel)

```bash
# Tester les performances actuelles (CPU)
java -jar target/eternity-1.0-SNAPSHOT.jar --benchmark

# Comparer avec des metrics
# (À implémenter si nécessaire)
```

## Statut Actuel
- ✅ Architecture prête pour GPU
- ✅ CPU fallback fonctionnel
- ⏸️ TornadoVM en attente (dépendances non résolues)
- 📋 To-do: Benchmarker quand TornadoVM sera disponible
