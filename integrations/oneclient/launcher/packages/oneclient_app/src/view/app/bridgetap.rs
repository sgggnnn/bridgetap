use crate::components::{Button, ScrollArea, TextInput};
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
        let search = use_state(String::new);
        let version = versions::selection();
        let auto = version.as_ref().is_ok_and(|s| s.auto_update);
        let current = version
            .as_ref()
            .map(|s| s.current().to_string())
            .unwrap_or_else(|e| e.to_string());
        let mut body = rect().vertical().spacing(16.).padding(40.)
            .child(label().text("BridgeTap").font_size(text_size(30.)).color(colors::fg_primary()))
            .child(label().text("All your tools, your preferred mod version and your 1.8.9 Ornithe profiles.").color(colors::fg_secondary()))
            .child(label().text(format!("Selected version: {current}")).font_size(text_size(19.)).color(colors::fg_primary()))
            .child(label().text("The selected version applies to all BridgeTap profiles the next time Minecraft starts. Your replays, islands and settings are preserved.").color(colors::fg_secondary()))
            .child(rect().horizontal().spacing(10.)
                .child(Button::new().text("Check for updates").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);message.set("Checking GitHub…".into());
                    spawn(async move {
                        match versions::check_update().await {
                            Ok(Some(release)) => {message.set(format!("BridgeTap {} is available. Click Install update.",release.version));available.set(Some(release));}
                            Ok(None) => {available.set(None);message.set("No newer stable version. Your selected version is kept.".into());}
                            Err(e) => message.set(format!("Unable to check for updates: {e}")),
                        }
                        busy.set(false);
                    });
                }))
                .child(Button::new().text("Choose a .jar…").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);
                    spawn(async move {
                        if let Some(file) = rfd::AsyncFileDialog::new().set_title("Choose BridgeTap for bridgetapclient / Ornithe").add_filter("BridgeTap mod", &["jar"]).pick_file().await {
                            match versions::select_local(file.path()).await {
                                Ok(version) => {available.set(None);message.set(format!("BridgeTap {version} selected and pinned (automatic updates disabled). Restart Minecraft to use it."));}
                                Err(e) => message.set(format!("JAR rejected: {e}")),
                            }
                        }
                        let next = *revision.peek()+1; revision.set(next);busy.set(false);
                    });
                }))
                .child(Button::new().text("Bundled version").disabled(*busy.read()).on_press(move |_| {
                    busy.set(true);spawn(async move {
                        message.set(match versions::restore_bundled().await {Ok(())=>"Bundled version restored. Automatic updates disabled.".into(),Err(e)=>e});
                        available.set(None);let next = *revision.peek()+1; revision.set(next);busy.set(false);
                    });
                })))
            .child(Button::new().text(if auto {"Automatic updates: enabled"} else {"Automatic updates: disabled"}).disabled(*busy.read()).on_press(move |_| {
                busy.set(true);spawn(async move {
                    message.set(match versions::set_auto_update(!auto).await {Ok(())=>"Preference saved. Updates are checked on launch, at most every 6 hours. Newer builds are never downgraded.".into(),Err(e)=>e});
                    let next = *revision.peek()+1; revision.set(next);busy.set(false);
                });
            }));
        if let Some(release) = available.read().clone() {
            body = body.child(
                Button::new()
                    .text(format!("Install BridgeTap {}", release.version))
                    .disabled(*busy.read())
                    .on_press(move |_| {
                        let release = release.clone();
                        busy.set(true);
                        message.set("Downloading and verifying SHA-256…".into());
                        spawn(async move {
                            message.set(match versions::install_release(&release).await {
                                Ok(version) => {
                                    format!("BridgeTap {version} is ready for the next launch.")
                                }
                                Err(e) => format!("Update rejected: {e}"),
                            });
                            available.set(None);
                            let next = *revision.peek() + 1;
                            revision.set(next);
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
                    .text("Compatible profiles")
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
            body = body.child(
                rect()
                    .horizontal()
                    .spacing(12.)
                    .child(
                        label()
                            .text(cluster.name.clone())
                            .color(colors::fg_primary()),
                    )
                    .child(
                        Button::new()
                            .text(if active {
                                "Disable BridgeTap"
                            } else {
                                "Enable BridgeTap"
                            })
                            .on_press(move |_| {
                                message.set(match bridgetap::set_enabled(&copy, !active) {
                                    Ok(()) => {
                                        "Preference saved for the next profile launch.".into()
                                    }
                                    Err(e) => e.to_string(),
                                });
                                let next = *revision.peek() + 1;
                                revision.set(next);
                            }),
                    )
                    .child(
                        Button::new()
                            .text("Menus for this profile")
                            .on_press(move |_| selected.set(id)),
                    ),
            );
        }
        let chosen = compatible
            .iter()
            .find(|c| c.id == *selected.read())
            .or_else(|| compatible.first());
        if let Some(cluster) = chosen {
            body = body.child(label().text(format!("Open in Minecraft: {}",cluster.name)).font_size(text_size(22.)).color(colors::fg_primary()))
                .child(label().text("Join a world in this profile, then choose a menu. In game, find the same categories under Right Shift > BridgeTap. Replay tools start with a recording picker.").color(colors::fg_secondary()));
            let catalog: Vec<serde_json::Value> =
                serde_json::from_str(bridgetap::MENU_CATALOG).unwrap_or_default();
            body = body.child(
                TextInput::new(search).placeholder("Search menus: replay, sky, island, HUD…"),
            );
            let needle = search_key(search.read().trim());
            let mut previous = String::new();
            let mut matches = 0;
            for menu in catalog {
                let hay = search_key(&format!(
                    "{} {} {}",
                    menu["title"].as_str().unwrap_or_default(),
                    menu["category"].as_str().unwrap_or_default(),
                    menu["id"].as_str().unwrap_or_default()
                ));
                if !needle.split_whitespace().all(|word| hay.contains(word)) {
                    continue;
                }
                matches += 1;
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
                body = body.child(
                    Button::new()
                        .text(menu["title"].as_str().unwrap_or("Open").to_string())
                        .on_press(move |_| {
                            let copy = copy.clone();
                            let id = id.clone();
                            spawn(async move {
                                message.set(match bridgetap::open_menu(&copy, &id).await {
                                    Ok(()) => {
                                        "Request sent. Return to Minecraft to use the menu.".into()
                                    }
                                    Err(e) => e,
                                });
                            });
                        }),
                );
            }
            if matches == 0 {
                body = body.child(
                    label()
                        .text("No matching menus. Try another search.")
                        .color(colors::fg_secondary()),
                );
            }
        } else {
            body = body.child(label().text("Create or import a Minecraft 1.8.9 Ornithe profile in Versions. Keep the OneConfig bundle enabled.").color(colors::fg_secondary()));
        }
        ScrollArea::new()
            .width(Size::fill())
            .height(Size::fill())
            .child(body)
    }
}

fn search_key(text: &str) -> String {
    text.to_lowercase()
        .chars()
        .map(|c| match c {
            'é' | 'è' | 'ê' | 'ë' => 'e',
            'à' | 'â' | 'ä' => 'a',
            'î' | 'ï' => 'i',
            'ô' | 'ö' => 'o',
            'ù' | 'û' | 'ü' => 'u',
            'ç' => 'c',
            _ => c,
        })
        .collect()
}
