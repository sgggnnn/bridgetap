# Changelog

Historique des versions disponibles dans les [releases GitHub](https://github.com/sgggnnn/bridgetap/releases). Chaque release conserve les JAR historiques, accompagnés de leurs sommes SHA-256. Les éditions Forge commencent à la version 1.4.4.

Certains commits regroupent plusieurs versions intermédiaires : leurs tags pointent vers le premier commit qui archive le binaire, pas vers un instantané source dédié. Cette distinction est indiquée dans les notes concernées.

## [1.4.5](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.5) — HUD Forge et blocs visibles dans les replays

### Corrigé

- Affichage du HUD des coordonnées sur Forge via RenderGameOverlayEvent.Post, car GuiIngameForge contourne la méthode vanilla utilisée auparavant.
- Mise à jour explicite du rendu des chunks du replay après l’apparition ou le retrait d’un bloc, y compris lors d’un retour arrière.
- Correction des blocs posés qui restaient invisibles dans les chunks synthétiques du replay.

### Vérifié

- Dix programmes de régression et les 34 hooks/accessors natifs.
- Chargement réel Forge/LaunchWrapper, abonnement de l’HUD et reproduction du défaut de notification des chunks synthétiques.
- Le test sans fenêtre ne remplace pas une vérification visuelle dans Minecraft.

[Notes complètes et provenance](docs/releases/v1.4.5.md).

## [1.4.4](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.4) — Première édition Forge et correction du tick final

### Ajouté

- Édition Forge 1.8.9, testée avec Forge 11.15.1.2318 et Java 8.
- Démarrage Forge/Mixin et dépendances privées inclus dans le JAR Forge. Les fonctionnalités existantes sont conservées.

### Corrigé

- Capture du tick d’arrivée qui manquait lorsque la run se terminait : une run de 3,85 s atteint 77 ticks.
- Compatibilité des anciens replays auxquels il manque exactement ce dernier tick : leur dernière position connue est maintenue jusqu’au temps sauvegardé, sans inventer de mouvement ou de télémétrie.

### À savoir pour cette archive

- Les problèmes d’affichage de l’HUD Forge et des blocs posés pendant un replay ont été corrigés dans la version 1.4.5.

[Notes complètes et provenance](docs/releases/v1.4.4.md).

## [1.4.3](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.3) — Options d’affichage et vitesse de lecture

### Ajouté

- Option Replay info pour afficher ou masquer l’ensemble du panneau d’informations du replay.
- Mode LAST_TURN pour afficher uniquement les coordonnées du dernier saut et du turn.
- Choix entre les libellés No Gain et No Gay pour l’indicateur, avec visibilité toujours contrôlée par Gain type.

### Modifié

- Retour du réglage de vitesse via une horloge au slot 4 : 0.25x, 0.5x, 1x, 2x et 4x.
- Suppression de Player XYZ hors replay tout en conservant les coordonnées classiques de bridge.

[Notes complètes et provenance](docs/releases/v1.4.3.md).

## [1.4.2](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.2) — Retour des coordonnées hors replay

### Corrigé

- Rétablissement du HUD classique des coordonnées de bridge en jeu normal.
- Retour de la ligne Player XYZ au-dessus de la hotbar, sans la télémétrie détaillée réservée au replay.
- Rétablissement de la sortie des coordonnées dans le chat en fin de tentative.

[Notes complètes et provenance](docs/releases/v1.4.2.md).

## [1.4.1](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.1) — Hotbar simplifiée et POV au clic

### Modifié

- Hotbar limitée à deux bâtons, Pause / Play et une barrière pour quitter.
- À la fin, Pause / Play devient Replay pour recommencer depuis le début.
- Un clic gauche sur le joueur active la POV ; Sneak permet de revenir à la caméra libre.
- Les nouvelles informations de télémétrie sont limitées au replay.

### Retiré

- Raccourci de replay dans le deuxième emplacement de la hotbar en jeu normal.
- Outils séparés de vitesse, de redémarrage et de changement de caméra dans cette version.

[Notes complètes et provenance](docs/releases/v1.4.1.md).

## [1.4.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.4.0) — Bibliothèque, partage et POV continue

### Ajouté

- Renommage persistant des replays, recherche par nom et tri par date, temps ou nom.
- Export et import de fichiers .btreplay autonomes pour partager les enregistrements.
- Mesure de la distance entre les bords des plateformes et de leur différence de hauteur lorsque les marqueurs enregistrés le permettent.
- POV continue suivant les déplacements et rotations du bridge enregistré.

### Conservé

- Télémétrie détaillée par tick, favoris et lecture des anciens formats de replay.

[Notes complètes et provenance](docs/releases/v1.4.0.md).

## [1.3.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.3.0) — Entrées, clics et vélocité par tick

### Ajouté

- Enregistrement des touches et boutons de souris maintenus, pressés et relâchés à chaque tick.
- Compteurs de clics gauche/droit/milieu, CPS sur une seconde et vélocité XYZ/horizontale.
- Panneau de télémétrie au-dessus de la hotbar.
- Raccourci de relecture du dernier bridge dans le deuxième emplacement après une run.

### Modifié

- Les bâtons avancent ou reculent d’une seconde pendant la lecture et d’un seul tick en pause.

[Notes complètes et provenance](docs/releases/v1.3.0.md).

## [1.2.2](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.2) — Directions de caméra et inspection du fantôme

### Corrigé

- Directions gauche/droite de la caméra alignées sur l’orientation Minecraft.
- Vol libre accéléré : 12 blocs/seconde, ou 36 avec Sprint.
- Possibilité d’entrer dans le personnage pour inspecter sa position, en masquant le modèle autour de la caméra tout en conservant sa hitbox.
- Synchronisation des rotations de la caméra et respect des touches Minecraft personnalisées.

### Modifié

- Informations du replay placées au-dessus de la hotbar.

[Notes complètes et provenance](docs/releases/v1.2.2.md).

## [1.2.1](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.1) — Caméra libre indépendante et retour à la galerie

### Corrigé

- Retour à l’interface de galerie précédente tout en conservant le moteur de replay natif.
- Caméra de replay indépendante du joueur réel, pilotée par les touches Minecraft pendant la lecture ou la pause.
- Séparation du contexte de rendu et du contrôleur du replay, avec restauration du client après le rendu.
- Réutilisation des ressources du renderer et nettoyage des buffers du monde de replay.

[Notes complètes et provenance](docs/releases/v1.2.1.md).

## [1.2.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.2.0) — Lecture native dans Minecraft

### Ajouté

- Lecture du replay dans un monde Minecraft local avec les textures et modèles des blocs.
- Fantôme utilisant le pseudo, le skin, les rotations, le sneak et les animations enregistrés ; hitbox blanche visible.
- Reconstruction des placements de blocs au fil du temps, avec retrait et restauration lors des retours arrière.
- Contrôles dans la hotbar pour la lecture, la pause, les déplacements temporels et la vitesse, avec messages dans l’Action Bar.
- Favoris dans la galerie.

### À savoir pour cette archive

- Les corrections de déplacement de la caméra sont arrivées dans les versions 1.2.1 et 1.2.2.

[Notes complètes et provenance](docs/releases/v1.2.0.md).

## [1.1.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.1.0) — Premiers replays et personnalisation des coordonnées

### Ajouté

- Enregistrement des déplacements et des blocs, sauvegarde du replay avec la run et première vue de replay 3D.
- Galerie avec recherche, cartes et détails des runs.
- Choix entre l’overlay des coordonnées et leur envoi dans le chat en fin de run.
- Choix des couleurs des différents éléments du HUD.

### Corrigé

- Libellé No Gain et application du réglage qui affiche ou masque le type de gain.

[Notes complètes et provenance](docs/releases/v1.1.0.md).

## [1.0.0](https://github.com/sgggnnn/bridgetap/releases/tag/v1.0.0) — Premier HUD et historique des bridges

### Ajouté

- Suivi des bridges et des sauts : coordonnées, écarts, ticks, temps et détection du turn.
- HUD configurable avec position, taille, fond, ombre, modes d’affichage et visibilité avec F3.
- Historique récent et sauvegarde des résumés de runs.
- Réglages des offsets des sauts et de l’arrivée, ainsi que de l’indicateur Gain / No Gain.

[Notes complètes et provenance](docs/releases/v1.0.0.md).
