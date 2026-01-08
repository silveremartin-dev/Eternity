# Plan de Modernisation et d’Optimisation d’Eternity II

Ce plan détaille les étapes pour transformer le projet Eternity II en un solveur distribué de classe mondiale, utilisant l'accélération GPU et des heuristiques avancées.

## 1. Stabilité du Build et Benchmarking

* **Objectif** : Assurer une compilation robuste sur toutes les plateformes et mesurer les gains de performance.
* **Actions** :
  * Correction des plugins Maven pour Protobuf et gRPC (détection automatique de l'OS).
  * Intégration de JMH (Java Microbenchmark Harness).
  * Benchmarks des méthodes critiques de `PiecePrimitive` et `BoardPrimitive`.

## 2. Intelligence du Solveur (Heuristiques)

* **Objectif** : Réduire l'espace de recherche de manière exponentielle.
* **Actions** :
  * Implémentation de l'heuristique **Most Constrained First (MCF)**.
  * Intégration de MCF dans `AdvancedEternitySolver` et `BasicEternitySolver`.
  * Optimisation du tri des candidats basé sur la rareté des patterns.

## 3. Accélération Matérielle (TornadoVM)

* **Objectif** : Déporter les calculs massifs sur GPU.
* **Actions** :
  * Refactorisation de `EternityKernel` pour une compatibilité totale avec TornadoVM (suppression des allocations d'objets, utilisation de tableaux plats).
  * Mise à jour de la boucle de résolution pour prioriser l'accélération matérielle.

## 4. Infrastructure Distribuée et Monitoring

* **Objectif** : Gérer des milliers de clients et monitorer la progression globalement.
* **Actions** :
  * Refactorisation du `JobManager` pour utiliser **Redis** comme file d'attente persistante.
  * Extension du serveur WebSocket pour fournir des statistiques en temps réel.
  * Scaffolding d'une interface de monitoring.
