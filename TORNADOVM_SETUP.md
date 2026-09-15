# TornadoVM Hardware GPU Acceleration Guide

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 1. Overview

The Eternity II solver includes native hardware acceleration through **TornadoVM** (`io.github.beehive-lab:tornado-api:4.0.0-jdk25`), enabling the engine to compile Java candidate validation code directly into OpenCL, CUDA, or SPIR-V kernels for execution on GPU hardware.

If no compatible GPU driver is available at runtime, the engine gracefully and silently defaults to the high-speed scalar CPU solver.

---

## 2. Hardware & Driver Prerequisites

### OpenCL Drivers
- **Intel GPUs (UHD / Iris / Arc):** Install [Intel OpenCL Graphics Driver](https://github.com/intel/compute-runtime/releases).
- **NVIDIA GPUs (RTX / GTX / Tesla):** Install [NVIDIA CUDA Toolkit](https://developer.nvidia.com/cuda-downloads).
- **AMD GPUs (Radeon / Instinct):** Install AMD ROCm or AMD OpenCL driver.

To check driver readiness on your machine:
```bash
# Verify OpenCL platforms with clinfo
clinfo
```

---

## 3. Installation & Setup

### Linux / WSL2 (Recommended for Production)

```bash
# 1. Install build tools and JDK 25
sudo apt update
sudo apt install build-essential cmake git openjdk-25-jdk

# 2. Clone and install TornadoVM
git clone https://github.com/beehive-lab/TornadoVM
cd TornadoVM
./bin/tornadovm-installer --jdk /usr/lib/jvm/java-25-openjdk --backend opencl

# 3. Source environment
source setvars.sh

# 4. Verify installation
tornado --devices
```

---

## 4. Running the GPU Solver

Once drivers are set up, run the project:

```bash
mvn clean package -DskipTests
java --enable-preview -cp target/eternity-1.0-SNAPSHOT.jar org.game.eternity2.client.ClientApp
```

In the Client UI:
1. Check the **Use GPU** option.
2. Click **Connect** to dispatch search batches to the GPU compute grid.

---

© 2026 Silvère Martin-Michiellot & Antigravity
