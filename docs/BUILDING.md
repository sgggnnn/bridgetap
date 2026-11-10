# Compiler et tester BridgeTap

Exécute les commandes depuis la racine du dépôt. Les exemples PowerShell utilisent `;` comme séparateur de classpath. Sur un autre système, utilise `:` avec les scripts Python.

## Prérequis

- Un **JDK 9 ou supérieur** pour compiler avec `--release 8` ; le JDK 17 a été utilisé pour les vérifications.
- **Java 8** pour exécuter Minecraft 1.8.9 et vérifier la compatibilité des tests.
- Python 3 pour le portage Forge et les vérifications du bytecode.
- Les bibliothèques Minecraft indiquées ci-dessous. Minecraft et les dépendances de compilation ne sont pas versionnés dans ce dépôt.

Les JAR générés vont dans `dist/`. Les scripts refusent d’écraser un fichier existant ; supprime cette sortie ou fournis un autre nom avec `--output` / `-Output` avant de recompiler.

## Ornithe / Legacy Fabric 1.8.9

L’édition commune utilise les mappings intermédiaires **Ornithe**, pas les noms du client Minecraft original.

Le classpath doit contenir :

- le client Minecraft 1.8.9 remappé en intermediary Ornithe ;
- Fabric Loader, Mixin, LWJGL 2, Authlib et Guava.

Versions utilisées localement : Fabric Loader 0.20.1, sponge-mixin 0.17.5 + Mixin 0.8.7, LWJGL 2.9.4 + legacyfabric.15, Authlib 1.5.21 et Guava 17.0.

```powershell
$env:JAVA_HOME = 'C:/chemin/vers/jdk-17'
$env:BRIDGETAP_CLASSPATH = 'C:/libs/client-intermediary.jar;C:/libs/fabric-loader.jar;C:/libs/sponge-mixin.jar;C:/libs/lwjgl.jar;C:/libs/authlib.jar;C:/libs/guava.jar'
./scripts/build.ps1
```

Alternative Python, qui fonctionne aussi hors Windows :

```powershell
python scripts/build-source.py --java-home $env:JAVA_HOME --classpath $env:BRIDGETAP_CLASSPATH
```

Sortie : `dist/BridgeTap-1.4.5.jar`.

## Forge 1.8.9

```powershell
python scripts/build-forge.py --java-home 'C:/chemin/vers/jdk-17' --minecraft 'C:/libs/minecraft-1.8.9-client.jar' --classpath 'C:/libs/lwjgl.jar;C:/libs/authlib.jar;C:/libs/guava.jar'
```

`--minecraft` attend le **JAR original du client Minecraft 1.8.9**, fourni par ton launcher. Le script télécharge Forge 11.15.1.2318, le mapping MCP et les outils nécessaires dans `build/forge/`.

Les sources communes sont converties en noms SRG puis compilées avec le point d’entrée Forge. Les fichiers convertis se trouvent dans `build/forge/generated/`. Mixin et ses dépendances privées sont inclus dans le JAR ; Minecraft et Forge ne le sont pas.

Sortie : `dist/BridgeTap-1.4.5-Forge-1.8.9.jar`.

Les détails du portage sont dans [forge/README.md](../forge/README.md).

## Tests de régression

Le lanceur PowerShell crée un dossier de travail isolé sous `build/` pour ne pas modifier ta configuration Minecraft.

```powershell
./scripts/test.ps1 -JavaHome 'C:/chemin/vers/jdk-17' -RuntimeJavaHome 'C:/chemin/vers/java-8' -Jar dist/BridgeTap-1.4.5.jar
./scripts/test.ps1 -JavaHome 'C:/chemin/vers/jdk-17' -RuntimeJavaHome 'C:/chemin/vers/java-8' -Jar dist/BridgeTap-1.4.5-Forge-1.8.9.jar
```

Il compile et exécute dix programmes : configuration, historique, persistance des replays, affichage, timeline, caméra, télémétrie, géométrie, bibliothèque et contrôles.

### Hooks natifs

```powershell
python tests/check_native_hooks.py C:/libs/client-intermediary.jar dist/BridgeTap-1.4.5.jar
python tests/check_forge_hooks.py build/forge/minecraft-srg.jar dist/BridgeTap-1.4.5-Forge-1.8.9.jar
```

Pour Forge, lance d’abord `scripts/build-forge.py` afin de préparer les mappings et le client SRG. Ces scripts vérifient les injections et accessors, la restauration du contexte de rendu, l’invalidation des blocs et, sur Forge, l’abonnement de l’HUD à l’événement d’affichage.

### Chargement Forge sans fenêtre de jeu

```powershell
python tests/run_forge_smoke.py --java 'C:/chemin/vers/java-8/bin/java.exe' --javac 'C:/chemin/vers/jdk-17/bin/javac.exe' --classpath '<bibliothèques complètes du launcher Forge, séparées par ;>'
```

Ce classpath doit contenir Forge universal, LaunchWrapper, le client Minecraft original et ses bibliothèques normales. Le JAR du mod est copié dans un dossier de jeu isolé. Le test vérifie le chargement des 13 mixins et 34 méthodes injectées/accessors, les liaisons des classes du replay et les notifications de rendu des chunks synthétiques.

Il n’ouvre pas de contexte graphique : les textures, le rendu visuel et les interactions doivent aussi être essayés dans Minecraft.

## Publication

Les scripts de build ne publient rien. Pour une release, joins le JAR de chaque édition concernée et ses sommes SHA-256 aux assets GitHub, puis ajoute les changements au [changelog](../CHANGELOG.md). La branche principale conserve uniquement les sources, les scripts et la documentation.
