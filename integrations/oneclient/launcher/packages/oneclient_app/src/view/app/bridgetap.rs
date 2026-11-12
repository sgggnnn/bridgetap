use crate::components::{Button, ScrollArea};
use crate::hooks::{settled_or_loading, use_clusters};
use crate::theme::{colors, text_size};
use freya::prelude::*;

#[derive(PartialEq)]
pub struct BridgeTap;
impl Component for BridgeTap {
    fn render(&self) -> impl IntoElement {
        let clusters = settled_or_loading(&use_clusters()).unwrap_or_default();
        let mut revision = use_state(|| 0u64);
        let _ = *revision.read();
        let mut message = use_state(String::new);
        let mut body=rect().vertical().spacing(18.).padding(Gaps::new_all(40.))
            .child(label().text("BridgeTap · Local Edition").font_size(text_size(30.)).color(colors::fg_primary()))
            .child(label().text("Enable BridgeTap for your Minecraft 1.8.9 Fabric / Ornithe profiles. Changes apply on the next launch.").color(colors::fg_secondary()))
            .child(label().text("In game: open OneConfig > BridgeTap for coordinates, detection and tools. Use the OneConfig HUD editor to move all three overlays.").color(colors::fg_secondary()))
            .child(label().text("Replays · Island library & presets · Video clips · Block appearance · Overlay colors").color(colors::fg_secondary()))
            .child(label().text(message.read().clone()).color(colors::fg_primary()));
        let mut count = 0;
        for cluster in clusters
            .iter()
            .filter(|c| oneclient_core::bridgetap::compatible(c))
        {
            count += 1;
            let active = oneclient_core::bridgetap::enabled(cluster);
            let cluster = cluster.clone();
            body = body.child(
                rect()
                    .horizontal()
                    .spacing(20.)
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
                                match oneclient_core::bridgetap::set_enabled(&cluster, !active) {
                                    Ok(()) => message.set(
                                        "Updated. Restart the game to apply the change."
                                            .to_string(),
                                    ),
                                    Err(error) => message.set(error.to_string()),
                                }
                                let next = *revision.peek() + 1;
                                revision.set(next);
                            }),
                    ),
            );
        }
        if count == 0 {
            body = body.child(
                label()
                    .text("Create a Minecraft 1.8.9 Fabric / Ornithe profile from Versions first.")
                    .color(colors::fg_secondary()),
            );
        }
        ScrollArea::new()
            .width(Size::fill())
            .height(Size::fill())
            .child(body)
    }
}
