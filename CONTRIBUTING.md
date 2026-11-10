# Contribuer à BridgeTap

## Signaler un problème

Ouvre une [issue](https://github.com/sgggnnn/bridgetap/issues) avec :

- la version de BridgeTap et l’édition utilisée, Forge ou Ornithe ;
- la version du chargeur, de Java et les autres mods qui interviennent dans le rendu ou les contrôles ;
- les étapes pour reproduire le problème, le résultat attendu et ce qui se produit ;
- un extrait du log, une capture ou un `.btreplay` si cela aide à reproduire le problème.

## Proposer une modification

1. Crée une branche à partir de `main`.
2. Consulte [la documentation de compilation](docs/BUILDING.md).
3. Modifie les sources communes dans `src/main/` ; les fichiers sous `build/forge/generated/` sont générés et ne doivent pas être édités.
4. Compile les éditions concernées et lance les tests adaptés au changement.
5. Ouvre une pull request qui explique le problème, la correction et les vérifications effectuées.

Garde les sources compatibles Java 8. Les changements du replay doivent préserver les anciennes sauvegardes et restaurer le contexte du client après le rendu. Pour une modification visuelle, précise aussi ce qui a été vérifié dans Minecraft.

## Binaires et releases

Ne commite pas de `.jar`, de `.class`, de cache de compilation ou de données Minecraft locales. Les sorties de compilation vont dans `dist/` et les caches dans `build/`.

Les JAR publiés sont attachés aux releases GitHub avec un fichier `SHA256SUMS.txt`. Le [changelog](CHANGELOG.md) et les notes sous `docs/releases/` décrivent chaque version. Les versions historiques sont conservées sous leur forme d’origine.
