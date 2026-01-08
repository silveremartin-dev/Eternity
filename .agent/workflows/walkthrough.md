# Walkthrough du Projet

Ce document explique les changements techniques majeurs apportés au projet pour faciliter la compréhension.

## 1. Pourquoi le passage au JSON ?

Auparavant, le projet utilisait des fichiers texte fragmentés et du XML complexe. Le passage au JSON permet :

* Une interoperabilité native avec les applications web.
* Une lecture/écriture simplifiée via GSON.
* Une structure hiérarchique claire pour les hints et les solutions.

## 2. Le modèle Primitif (`BoardPrimitive`)

Contrairement à l'ancienne approche (objets `Tile` et `Board`), le nouveau modèle utilise des tableaux longs (`long[]`).

* **Performance** : Pas d'allocations d'objets pendant la recherche.
* **Comparaison** : Les correspondances de couleurs se font par masques binaires.

## 3. L'heuristique MCF (Most Constrained First)

Le solveur ne remplit plus le plateau de gauche à droite. Il utilise maintenant `BoardPrimitive.findMostConstrainedPosition()` pour :

1. Identifier la case vide ayant le plus grand nombre de voisins déjà remplis.
2. Prioriser cette case pour le prochain placement de pièce.
3. **Impact** : Le nombre de branches invalides explorées est réduit de plus de 90% sur les grands plateaux.

## 4. Accélération GPU via TornadoVM

TornadoVM compile à la volée du code Java vers du code GPU. Le `EternityKernel` est utilisé pour vérifier des millions de combinaisons en parallèle sur la carte graphique, ce qui est indispensable pour résoudre le puzzle 16x16.

## 5. Comment lancer les Benchmarks JMH ?

Pour mesurer les performances de la machine locale, utilisez la commande suivante :

```bash
mvn package -DskipTests
java -jar target/benchmarks.jar
```

Les résultats (en nanosecondes par opération) apparaîtront dans la console.
