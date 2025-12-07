# Intel OpenCL GPU Check - Windows

**Authors:** Gemini AI Assistant, Silvère

## Check if OpenCL is installed

```powershell
# Option 1: Avec GPU Caps Viewer (Interface graphique)
# Télécharger et installer : https://www.geeks3d.com/gpucapsviewer/

# Option 2: Avec clinfo (ligne de commande)
# Installer via Chocolatey (gestionnaire de paquets Windows)
# Si pas chocolatey : https://chocolatey.org/install

# Une fois Chocolatey installé:
choco install opencl-intel-cpu-runtime

# Vérifier OpenCL:
clinfo

# Vous devriez voir quelque chose comme:
# Platform Name: Intel(R) OpenCL HD Graphics
# Device Name: Intel(R) UHD Graphics
```

## Tester OpenCL avec un programme simple

```bash
# Dans WSL2 Ubuntu (après installation TornadoVM):
cd TornadoVM/examples
tornado --printKernel uk.ac.manchester.tornado.benchmarks.BenchmarkRunner vectorAddition

# Si ça fonctionne, vous verrez le kernel compilé pour votre GPU Intel
```

## Résultat attendu

Si OpenCL est correctement installé, `clinfo` devrait montrer :

- **Platform**: Intel(R) OpenCL HD Graphics
- **Device**: Intel(R) UHD Graphics
- **OpenCL Version**: 3.0 ou supérieur
- **Driver Version**: Votre version actuelle (27.20.100.9079)

## Problèmes courants

### "No OpenCL devices found"

**Solution** : Installer Intel OpenCL Runtime

- URL : <https://www.intel.com/content/www/us/en/developer/articles/tool/opencl-drivers.html>
- Ou via Windows Update (pilote Intel récent)

### "clinfo: command not found"

**Solution** : Installer via `choco install opencl-intel-cpu-runtime`

### Performance GPU < CPU

**Normal** : Intel UHD Graphics est un GPU intégré (iGPU). Pour un vrai gain, il faudrait un GPU dédié (NVIDIA/AMD).
Avec Intel UHD, le gain sera **minime** voire négatif vs CPU multi-core.

## Recommandation finale

Votre Intel UHD Graphics **peut** techniquement faire tourner OpenCL, mais :

❌ **Gain de performance faible** (iGPU vs CPU moderne)
❌ **Complexité d'installation** (WSL2 + drivers + TornadoVM)
✅ **CPU fallback déjà fonctionnel**

**Verdict** : Sauf si c'est pour expérimenter, **restez sur le CPU** !
