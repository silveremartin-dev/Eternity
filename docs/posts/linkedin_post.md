# 🚀 Post LinkedIn : Showcase Projet Eternity II (Distributed Solver in Java 25)

---

### 🧩 Comment nous avons conçu un solveur distribué haute performance pour Eternity II en Java 25

Le puzzle **Eternity II** est l'un des casse-têtes combinatoires les plus difficiles au monde : une grille de $16 \times 16$ tuiles avec plus de $10^{600}$ combinaisons possibles ($256! \times 4^{256}$).

Pour relever ce défi d'ingénierie, j'ai développé une plateforme distribuée et hautement résiliente en **Java 25**, combinant calcul hétérogène (CPU & GPU), sérialisation zero-copy et communication temps réel.

Voici les piliers techniques de ce projet :

---

### ⚡ 1. Modélisation Bas-Niveau & Performance Extrême
- **Représentation Primitive 64-bit :** Chaque tuile et son orientation sont encodées dans un scalaire `long` (`PiecePrimitive`). Les rotations ($90^\circ, 180^\circ, 270^\circ$) et vérifications de contraintes s'effectuent par de simples opérations bitwise en temps constant $O(1)$ sans allocation mémoire (zéro pression sur le Garbage Collector).
- **Indexation spatiale & Élagage :** `NeighborIndex` et `GlobalPruner` filtrent les branches invalides en amont, réduisant l'espace de recherche exponentiel.

### 🧠 2. Moteurs Algorithmiques Hétérogènes
- **Backtracking Heuristique (MCV) :** Parcours optimisé avec verrouillage strict des pièces d'indices (`isFixed[]`).
- **Monte Carlo Tree Search (MCTS) :** Exploration arborescente UCT avec rollouts stochastiques sur tuiles disponibles.
- **Accélération Matérielle GPU :** Offloading des calculs de validation massivement parallèles via **TornadoVM**.
- **Raffinement Stochastique :** Algorithme de recuit avec mutations par rotation unitaire et permutations.

### 🌐 3. Architecture Distribuée & Réseau Temps Réel
- **gRPC & FlatBuffers :** Transfert binaire des plateaux sans surcoût de parsing grâce au zero-copy FlatBuffers sur flux HTTP/2.
- **WebSocket & Web Dashboard :** Télémétrie temps réel et visualisation graphique du cluster de calcul.
- **Redis & Distributed Queues :** Distribution asynchrone des charges de travail (Lettuce) et cache partagé de contraintes.

### 🛡️ 4. Sécurité, Robustesse & Observabilité
- **Anti-RCE :** Filtrage strict de la désérialisation réseau via `ObjectInputFilter`.
- **Authentification & Cryptographie :** Hachage des mots de passe en **BCrypt** et tokens signés en **JWT (HS256)**.
- **Persistance Atomique :** Écritures de données crash-safe (`StandardCopyOption.ATOMIC_MOVE`) et pool SQL managé par HikariCP + Flyway.
- **Observabilité :** Métriques Prometheus / Micrometer (`/metrics`, `/health`, `/ready`) et logs asynchrones Log4j2.
- **Qualité :** 85 tests unitaires et d'intégration validés à 100%.

---

💡 **Ce projet démontre comment concevoir un système critique complet : de l'optimisation bas-niveau au système distribué cloud-ready.**

Je suis actuellement à l'écoute de nouvelles opportunités en **Architecture Logicielle / Développement Backend Senior (Java / Systèmes Distribués / High Performance Computing)**.

👉 Retrouvez le projet complet et le code source sur GitHub : [Lien vers votre repo]

N'hésitez pas à me contacter par message privé ou en commentaire pour échanger sur le sujet ! 🚀

---

#Java #Java25 #DistributedSystems #HighPerformance #gRPC #FlatBuffers #Concurrency #OpenToWork #BackendDeveloper #SoftwareEngineering #GPUComputing #Algorithms
