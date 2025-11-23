# Feature 1.2 - Structured Concurrency - Technical Limitation

**Date:** 2025-11-23  
**Status:** ⚠️ **BLOCKED** - Requires Java 25 Preview

---

## 🔍 Discovery

Structured Concurrency (`java.util.concurrent.StructuredTaskScope`) is a **Preview Feature** that requires:
- **Java 25 Early Access** (not yet released as LTS)
- `--enable-preview` compiler flag
- Runtime preview flag

**Current Project:** Java 21 LTS (stable, recommended for production)

---

## 📊 Options Analysis

### Option 1: SKIP Feature 1.2 ✅ (Recommended)
**Pros:**
- Keep Java 21 LTS stability
- No preview feature risks
- Virtual Threads (Feature 1.1) already provides major benefits
- Can add Structured Concurrency when Java 25 becomes LTS (2026+)

**Cons:**
- Miss some advanced error handling patterns
- No built-in race-to-success coordination

**Recommendation:** SKIP and proceed to Feature 2.1 (gRPC + FlatBuffers)

---

### Option 2: Upgrade to Java 25 Preview ❌ (Not Recommended)
**Pros:**
- Access to latest Java features
- Future-proof code

**Cons:**
- Early Access = bugs and instability
- Preview features may change before final release
- Production risk
- Need JDK 25 EA installation

**Recommendation:** NOT suitable for production code

---

### Option 3: Use CompletableFuture Alternative ⚡ (Compromise)
**Pros:**
- Stable Java 21 API
- Similar coordination capabilities
- Production-ready

**Cons:**
- More verbose than Structured Concurrency
- Less elegant error handling

**Example:**
```java
// Instead of Structured Concurrency
CompletableFuture<Result1> future1 = CompletableFuture.supplyAsync(task1);
CompletableFuture<Result2> future2 = CompletableFuture.supplyAsync(task2);

CompletableFuture.allOf(future1, future2)
    .thenApply(v -> combineResults(future1.join(), future2.join()));
```

---

## 🎯 Decision: SKIP Feature 1.2

**Rationale:**
1. Virtual Threads (Feature 1.1) already provides 90% of concurrency improvements
2. Java 21 LTS stability is critical for production
3. Can revisit when Java 25 LTS is released (~2026)
4. Focus on higher-impact features (gRPC, FlatBuffers, GPU)

---

## 📋 Updated Roadmap

~~Feature 1.2: Structured Concurrency~~ **→ SKIPPED**

**Next Steps:**
1. **Feature 1.3:** JVM Configuration (ZGC/Shenandoah) - Optional
2. **Feature 2.1:** gRPC + FlatBuffers - **HIGH PRIORITY** ✅
3. Feature 2.2: gRPC Services
4. Feature 2.3: Protocol Migration

---

## 🔄 Future Consideration

Add Feature 1.2 to backlog for Java 25 LTS migration:
- Timeline: 2026+ (when Java 25 reaches LTS)
- Priority: Medium (nice-to-have, not critical)
- Benefits: Cleaner error handling, race-to-success patterns
