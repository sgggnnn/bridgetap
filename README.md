# BridgeTap

Un mod client **Minecraft 1.8.9** pour analyser ses bridges : coordonnées des sauts, historique des runs et replays avec caméra libre, fantôme et vue à la première personne.

[Télécharger la dernière version](https://github.com/sgggnnn/bridgetap/releases/latest) · [Toutes les versions](https://github.com/sgggnnn/bridgetap/releases) · [Changelog](CHANGELOG.md) · [Signaler un bug](https://github.com/sgggnnn/bridgetap/issues)

## Installation

1. Ouvre la [release 1.4.5](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.5).
2. Télécharge le fichier qui correspond à ton installation :
   - **Forge 1.8.9** : `BridgeTap-1.4.5-Forge-1.8.9.jar`.
   - **Ornithe / Legacy Fabric 1.8.9** : `BridgeTap-1.4.5.jar`, pour un environnement utilisant les mappings Ornithe.
3. Mets ce JAR dans le dossier `mods` de ton instance, en remplaçant l’ancienne version de BridgeTap.
4. Lance Minecraft avec **Java 8** et le chargeur choisi.

Garde une seule édition de BridgeTap par instance. L’édition Forge inclut son moteur Mixin : aucun module Mixin supplémentaire n’est nécessaire. La version Forge utilisée pour les tests est **11.15.1.2318**.

Les JAR se trouvent dans les **assets des releases GitHub**. Les archives « Source code » servent à récupérer les sources pour le développement.

## Fonctionnalités

- **HUD de bridge** : coordonnées des sauts, écarts, ticks, turn, arrivée et indicateur Gain / No Gain.
- **Affichage personnalisable** : position, taille, fond, ombre, couleurs par élément, sortie dans le chat et mode `LAST_TURN` pour ne garder que les coordonnées du dernier saut et du turn.
- **Bibliothèque de runs** : historique récent, sauvegardes, favoris, recherche, renommage et tri par date, temps ou nom.
- **Replays dans un monde local** : déplacements, sauts, rotations, sneak, animations, skin et placements de blocs enregistrés.
- **Analyse par tick** : touches maintenues, pressées et relâchées, clics, CPS, vélocité et coordonnées du fantôme.
- **Caméra libre et POV** : déplacement autour du bridge, hitbox blanche et suivi de la vue du joueur enregistré.
- **Partage** : export et import de fichiers `.btreplay` autonomes.
- **Mesures du bridge** : distance entre les bords des plateformes et différence de hauteur lorsque l’enregistrement contient les données nécessaires.

Le panneau d’informations du replay se masque avec **Replay info: ON/OFF**. Les coordonnées de bridge classiques restent indépendantes de ce réglage. Le libellé de l’indicateur peut être choisi dans **No gain label**, et sa visibilité dans **Gain type**.

## Utilisation

### Ouvrir et sauvegarder un replay

Appuie sur **B** pour ouvrir la galerie ; la touche est modifiable dans les contrôles Minecraft. Le bouton **Settings** ouvre les réglages du mod.

- Clique sur une carte pour consulter ses détails, puis sur **`>`** pour lancer le replay.
- **+ Save** conserve une run récente.
- Un clic droit sur une carte ajoute ou retire le favori. L’ajout aux favoris sauvegarde aussi une run récente.
- **Rename**, **Sort**, la recherche et les filtres permettent de retrouver tes enregistrements.

### Contrôles du replay

Sélectionne un outil dans la hotbar puis clique pour l’utiliser :

- **Slot 1 — bâton arrière** : reculer d’une seconde en lecture, ou d’un tick en pause.
- **Slot 2 — Pause / Play** : mettre en pause ou reprendre. À la fin, l’outil devient **Replay** pour recommencer.
- **Slot 3 — bâton avant** : avancer d’une seconde en lecture, ou d’un tick en pause.
- **Slot 4 — horloge** : choisir la vitesse `0.25x`, `0.5x`, `1x`, `2x` ou `4x`.
- **Slot 9 — barrière** : quitter le replay.

La molette parcourt les emplacements occupés. Utilise tes touches de déplacement Minecraft, **Saut** pour monter, **Sneak** pour descendre et **Sprint** pour voler plus vite.

Un **clic gauche sur le joueur enregistré** active sa POV. Appuie sur **Sneak** pour revenir à la caméra libre. **B** ou **Échap** permet aussi de revenir à la galerie.

### Envoyer un replay à un ami

1. Sélectionne une run et clique sur **Export**.
2. Récupère le fichier `.btreplay` dans `config/bridgetap/exports/` ; **Files** ouvre ce dossier.
3. Envoie ce fichier à ton ami.
4. Ton ami le place dans `config/bridgetap/imports/`, ouvre **Import**, clique sur **Refresh**, puis importe le fichier.

Le mod n’envoie rien automatiquement. La configuration se trouve dans `config/bridgetap.properties` et les sauvegardes dans `config/bridgetap/saved/`. Ces données peuvent être copiées entre tes instances Forge et Ornithe.

## Compatibilité et vérification

BridgeTap cible Minecraft **1.8.9**. Les anciennes sauvegardes restent lisibles avec les informations qu’elles contiennent : des métriques absentes ne sont pas recréées. La mesure précise des plateformes nécessite les marqueurs de placement des enregistrements au format 4 ; sinon elle peut afficher `N/A`.

La version 1.4.5 corrige l’HUD Forge et les blocs posés qui restaient invisibles pendant un replay. Depuis la 1.4.4, le tick d’arrivée manquant est aussi corrigé : une run de **3,85 s** atteint bien **77 ticks**.

La validation couvre dix programmes de régression, les 34 hooks natifs et un test de chargement réel Forge/LaunchWrapper. Ce dernier reproduit le problème des chunks synthétiques et vérifie les mises à jour de rendu. Ces tests ne remplacent pas une vérification visuelle dans Minecraft avec les autres mods de ton instance.

## Développement

Les instructions de compilation et de test sont dans [docs/BUILDING.md](docs/BUILDING.md). Pour les contributions, voir [CONTRIBUTING.md](CONTRIBUTING.md).

- `src/main/` : sources Java communes, mixins et ressources.
- `forge/` : point d’entrée Forge, démarrage Mixin et mappings.
- `scripts/` : compilation et lancement des tests.
- `tests/` : régressions Java et vérifications des hooks.
- `docs/` : documentation technique et notes des releases.
- `dist/` : JAR compilés localement, ignorés par Git.
- `build/` : caches, classes et données temporaires, ignorés par Git.

## Crédits

Projet maintenu sur [sgggnnn/bridgetap](https://github.com/sgggnnn/bridgetap). Merci à **pavelpuchika** pour son aide.

BridgeTap s’appuie sur Minecraft Forge, Ornithe, Fabric Loader, Mixin et leurs outils de mapping. Les licences des bibliothèques embarquées sont conservées dans les JAR Forge sous `META-INF/licenses/`.
