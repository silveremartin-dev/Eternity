# Intel GPU OpenCL Configuration & Diagnostics

**Authors:** Silvère Martin-Michiellot, Antigravity (Google DeepMind)

---

## 1. Verifying OpenCL on Windows

### Option A: Using `clinfo` CLI
```powershell
# Install clinfo via Chocolatey
choco install opencl-intel-cpu-runtime

# Run diagnostics
clinfo
```

Expected output confirms:
- **Platform Name:** Intel(R) OpenCL HD Graphics
- **Device Name:** Intel(R) UHD / Iris / Arc Graphics
- **OpenCL Version:** 3.0 or higher

### Option B: Using GPU Caps Viewer
Download and launch [GPU Caps Viewer](https://www.geeks3d.com/gpucapsviewer/) to visually confirm the OpenCL device status and compute unit availability.

---

## 2. Testing OpenCL with TornadoVM

Within a Linux / WSL2 environment with TornadoVM installed:

```bash
cd TornadoVM/examples
tornado --printKernel uk.ac.manchester.tornado.benchmarks.BenchmarkRunner vectorAddition
```

---

## 3. Common Troubleshooting

### Error: "No OpenCL devices found"
**Solution:** Download and install the latest Intel Compute Runtime from [Intel Compute Runtime Releases](https://github.com/intel/compute-runtime/releases).

### Dedicated vs Integrated GPUs (iGPU)
For maximum throughput gains with TornadoVM, high-performance dedicated GPUs (NVIDIA RTX / Tesla or AMD Radeon / Instinct) are recommended. The built-in scalar CPU engine remains exceptionally fast (>21M candidates/sec) on modern multi-core processors.

---

© 2026 Silvère Martin-Michiellot & Antigravity
