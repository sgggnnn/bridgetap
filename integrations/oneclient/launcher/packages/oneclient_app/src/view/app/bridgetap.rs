use crate::components::{Button, ScrollArea};
use crate::hooks::{settled_or_loading, use_clusters};
use crate::theme::{colors, text_size};
use freya::prelude::*;
use oneclient_core::bridgetap::{self, versions};

#[derive(PartialEq)]
pub struct BridgeTap;
impl Component for BridgeTap {
    fn render(&self) -> impl IntoElement {
        let clusters = settled_or_loading(&use_clusters()).unwrap_or_default();
        let mut revision = use_state(|| 0u64);
        let _ = *revision.read();
        let mut message = use_state(String::new);
        let mut busy = use_state(|| false);
        let mut available = use_state(|| None::<versions::Release>);
        let mut selected = use_state(|| 0i64);
        let version = versions::selection();
        let auto = version.as_ref().is_ok_and(|s| s.auto_update);
        let current = version
            .as_ref()
            .map(|s| s.current().to_string())
            .unwrap_or_else(|e| e.to_string());
        let mut body = rect().vertical().spacing(16.).padding(40.)
            .child(label().text("BridgeTap").font_size(text_size(30.)).color(colors::fg_primary()))
            .child(label().text("Tous tes outils, une version de mod au choix et tes profils 1.8.9 Ornithe.").color(colors::fg_secondary()))
            .child(label().text(format!("Version sélectionnée : {current}")).font_size(text_size(19.)).color(colors::fg_primary()))
            .child(label().text("Les changements de version s'appliquent au prochain lancement de Minecraft. Tes replays, îles et réglages sont conservés.").color(colors::fg_secondary()))
            .child(rect().horizontal().spacing(10.)
                .child(Button::new().text("Chercher une mise à jour").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);message.set("Recherche sur GitHub…".into());
                    spawn(async move {
                        match versions::check_update().await {
                            Ok(Some(release)) => {message.set(format!("BridgeTap {} est disponible. Clique sur Installer la mise à jour.",release.version));available.set(Some(release));}
                            Ok(None) => {available.set(None);message.set("Aucune version stable plus récente. La version sélectionnée est conservée.".into());}
                            Err(e) => message.set(format!("Recherche impossible : {e}")),
                        }
                        busy.set(false);
                    });
                }))
                .child(Button::new().text("Choisir un .jar…").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);
                    spawn(async move {
                        if let Some(file) = rfd::AsyncFileDialog::new().set_title("Choisir BridgeTap pour OneClient / Ornithe").add_filter("Mod BridgeTap", &["jar"]).pick_file().await {
                            match versions::select_local(file.path()).await {
                                Ok(version) => {available.set(None);message.set(format!("BridgeTap {version} sélectionné. Redémarre Minecraft pour l'utiliser."));}
                                Err(e) => message.set(format!("JAR refusé : {e}")),
                            }
                        }
                        revision.set(*revision.peek()+1);busy.set(false);
                    });
                }))
                .child(Button::new().text("Version embarquée").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);spawn(async move {
                        message.set(match versions::restore_bundled().await {Ok(())=>"Version embarquée restaurée pour le prochain lancement.".into(),Err(e)=>e});
                        available.set(None);revision.set(*revision.peek()+1);busy.set(false);
                    });
                })))
            .child(Button::new().text(if auto {"Mises à jour automatiques : activées"} else {"Mises à jour automatiques : désactivées"}).disabled(*busy.read()).on_press(move |_| {
                busy.set(true);spawn(async move {
                    message.set(match versions::set_auto_update(!auto).await {Ok(())=>"Préférence enregistrée. Recherche automatique au lancement (au plus toutes les 6 heures), sans rétrograder un build plus récent.".into(),Err(e)=>e});
                    revision.set(*revision.peek()+1);busy.set(false);
                });
            }));
        if let Some(release) = available.read().clone() {
            body = body.child(
                Button::new()
                    .text(format!("Installer BridgeTap {}", release.version))
                    .disabled(*busy.read())
                    .on_press(move |_| {
                        let release = release.clone();
                        busy.set(true);
                        message.set("Téléchargement et vérification SHA-256…".into());
                        spawn(async move {
                            message.set(match versions::install_release(&release).await {
                                Ok(version) => {
                                    format!("BridgeTap {version} prêt pour le prochain lancement.")
                                }
                                Err(e) => format!("Mise à jour refusée : {e}"),
                            });
                            available.set(None);
                            revision.set(*revision.peek() + 1);
                            busy.set(false);
                        });
                    }),
            );
        }
        body = body
            .child(
                label()
                    .text(message.read().clone())
                    .color(colors::fg_primary()),
            )
            .child(
                label()
                    .text("Profils compatibles")
                    .font_size(text_size(22.))
                    .color(colors::fg_primary()),
            );
        let compatible: Vec<_> = clusters
            .iter()
            .filter(|c| bridgetap::compatible(c))
            .collect();
        for cluster in &compatible {
            let active = bridgetap::enabled(cluster);
            let copy = (**cluster).clone();
            let id = cluster.id;
            body = body.child(rect().horizontal().spacing(12.)
                .child(label().text(cluster.name.clone()).color(colors::fg_primary()))
                .child(Button::new().text(if active {"Désactiver BridgeTap"} else {"Activer BridgeTap"}).on_press(move |_| {
                    message.set(match bridgetap::set_enabled(&copy,!active) {Ok(())=>"Préférence enregistrée pour le prochain lancement du profil.".into(),Err(e)=>e.to_string()});
                    revision.set(*revision.peek()+1);
                }))
                .child(Button::new().text("Menus de ce profil").on_press(move |_| selected.set(id))));
        }
        let chosen = compatible
            .iter()
            .find(|c| c.id == *selected.read())
            .or_else(|| compatible.first());
        if let Some(cluster) = chosen {
            body = body.child(label().text(format!("Ouvrir dans Minecraft : {}",cluster.name)).font_size(text_size(22.)).color(colors::fg_primary()))
                .child(label().text("Rejoins un monde dans ce profil, puis choisis un menu. En jeu, retrouve les mêmes catégories dans Maj droite > BridgeTap. Les outils d'un replay commencent par un sélecteur d'enregistrement.").color(colors::fg_secondary()));
            let catalog: Vec<serde_json::Value> =
                serde_json::from_str(bridgetap::MENU_CATALOG).unwrap_or_default();
            let mut previous = String::new();
            for menu in catalog {
                let category = menu["category"].as_str().unwrap_or_default();
                if category != previous {
                    previous = category.to_string();
                    body = body.child(
                        label()
                            .text(previous.clone())
                            .font_size(text_size(18.))
                            .color(colors::fg_primary()),
                    );
                }
                let id = menu["id"].as_str().unwrap_or_default().to_string();
                let copy = (**cluster).clone();
                body = body.child(Button::new().text(menu["title"].as_str().unwrap_or("Ouvrir").to_string()).on_press(move |_| {
                    let copy = copy.clone();let id = id.clone();
                    spawn(async move { message.set(match bridgetap::open_menu(&copy,&id).await {Ok(())=>"Demande envoyée. Reviens dans Minecraft pour utiliser le menu.".into(),Err(e)=>e}); });
                }));
            }
        } else {
            body = body.child(label().text("Crée ou importe un profil Minecraft 1.8.9 Ornithe dans Versions. Garde le bundle OneConfig activé.").color(colors::fg_secondary()));
        }
        ScrollArea::new()
            .width(Size::fill())
            .height(Size::fill())
            .child(body)
    }
}
