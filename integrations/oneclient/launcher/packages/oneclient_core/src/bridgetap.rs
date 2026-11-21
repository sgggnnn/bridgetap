//! BridgeTap local edition. Payload is supplied by the reproducible integration build.
use crate::LauncherResult;
pub mod versions;
mod branding;
pub const MENU_CATALOG: &str = include_str!("../assets/bridgetap-menus.json");
use oneclient_cluster::Cluster;
use oneclient_common::{domain::GameLoader, paths};
use std::path::Path;
const PAYLOAD: &[u8] = include_bytes!("../assets/BridgeTap-OneClient.jar");
const FILE: &str = "BridgeTap-OneClient.jar";
pub fn compatible(cluster: &Cluster) -> bool {
    cluster.mc_version == "1.8.9" && cluster.mc_loader == GameLoader::Ornithe
}
fn marker(cluster: &Cluster) -> LauncherResult<std::path::PathBuf> {
    Ok(paths::cluster_dir(&cluster.folder_name)?.join(".oneclient/bridgetap-enabled"))
}
pub fn enabled(cluster: &Cluster) -> bool {
    compatible(cluster) && marker(cluster).is_ok_and(|p| enabled_at(&p))
}
fn enabled_at(marker: &Path) -> bool {
    !marker.with_file_name("bridgetap-disabled").exists()
}
pub fn set_enabled(cluster: &Cluster, enabled: bool) -> LauncherResult<()> {
    if enabled && !compatible(cluster) {
        return Err(std::io::Error::other("BridgeTap requires Minecraft 1.8.9 Ornithe").into());
    }
    set_enabled_at(&marker(cluster)?, enabled)?;
    Ok(())
}
fn set_enabled_at(path: &Path, enabled: bool) -> std::io::Result<()> {
    let disabled = path.with_file_name("bridgetap-disabled");
    if enabled {
        std::fs::create_dir_all(path.parent().unwrap())?;
        std::fs::write(path, b"BridgeTap local edition\n")?;
        if disabled.exists() {
            std::fs::remove_file(disabled)?;
        }
    } else {
        std::fs::create_dir_all(disabled.parent().unwrap())?;
        std::fs::write(disabled, b"BridgeTap disabled by user\n")?;
        if path.exists() {
            std::fs::remove_file(path)?;
        }
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn bundled_default_and_persistent_opt_out() {
        let directory = std::env::temp_dir().join(format!("bridgetap-{}", uuid::Uuid::new_v4()));
        let marker = directory.join(".oneclient/bridgetap-enabled");
        assert!(enabled_at(&marker));
        set_enabled_at(&marker, false).unwrap();
        assert!(!enabled_at(&marker));
        set_enabled_at(&marker, false).unwrap();
        assert!(!enabled_at(&marker));
        set_enabled_at(&marker, true).unwrap();
        assert!(enabled_at(&marker));
        set_enabled_at(&marker, true).unwrap();
        assert!(enabled_at(&marker));
        std::fs::remove_dir_all(directory).unwrap();
    }
}
/// Called after normal content materialization and after the launch directory lock.
pub async fn install(cluster: &Cluster, cwd: &Path, mods_in_cluster: bool) -> LauncherResult<()> {
    let root = if mods_in_cluster {
        paths::cluster_dir(&cluster.folder_name)?
    } else {
        cwd.to_path_buf()
    };
    let mods = root.join("mods");
    let target = mods.join(FILE);
    let active = compatible(cluster) && enabled(cluster);
    if !active {
        if target.is_file()
            && oneclient_content::packages::read_jar_manifest(&target)
                .await
                .mod_id
                .as_deref()
                == Some("bridgetap")
        {
            let backup = root.join(".oneclient/bridgetap-backups");
            std::fs::create_dir_all(&backup)?;
            std::fs::rename(
                &target,
                backup.join(format!("{}-{FILE}", uuid::Uuid::new_v4())),
            )?;
        }
        return Ok(());
    }
    versions::maybe_auto_update().await;
    let payload = versions::payload().await.map_err(std::io::Error::other)?;
    std::fs::create_dir_all(&mods)?;
    branding::install(&mods, &root.join(".oneclient/bridgetap-branding-backups")).await?;
    let mut dependency = false;
    let mut check = tokio::fs::read_dir(&mods).await?;
    while let Some(entry) = check.next_entry().await? {
        let path = entry.path();
        if path.extension().and_then(|v| v.to_str()) != Some("jar") {
            continue;
        }
        if oneclient_content::packages::read_jar_manifest(&path)
            .await
            .mod_id
            .as_deref()
            == Some("oneconfig")
        {
            dependency = true;
            break;
        }
    }
    if !dependency {
        return Err(std::io::Error::other(
            "Enable the OneConfig bundle for this profile before launching BridgeTap",
        )
        .into());
    }
    let mut entries = tokio::fs::read_dir(&mods).await?;
    while let Some(entry) = entries.next_entry().await? {
        let path = entry.path();
        if !path.is_file()
            || path == target
            || path.extension().and_then(|x| x.to_str()) != Some("jar")
        {
            continue;
        }
        let manifest = oneclient_content::packages::read_jar_manifest(&path).await;
        if manifest.mod_id.as_deref() == Some("bridgetap") {
            let backup = root.join(".oneclient/bridgetap-backups");
            std::fs::create_dir_all(&backup)?;
            let destination = backup.join(format!(
                "{}-{}",
                uuid::Uuid::new_v4(),
                entry.file_name().to_string_lossy()
            ));
            std::fs::rename(&path, destination)?;
        }
    }
    if target.is_file() && std::fs::read(&target)? == payload {
        return Ok(());
    }
    let temporary = mods.join(format!(".bridgetap-{}.part", uuid::Uuid::new_v4()));
    std::fs::write(&temporary, &payload)?;
    if target.exists() {
        let backup = root.join(".oneclient/bridgetap-backups");
        std::fs::create_dir_all(&backup)?;
        std::fs::rename(
            &target,
            backup.join(format!("{}-{FILE}", uuid::Uuid::new_v4())),
        )?;
    }
    std::fs::rename(temporary, target)?;
    Ok(())
}

/// A small allowlisted message is consumed by the mod on its client thread.
pub async fn open_menu(cluster: &Cluster, id: &str) -> Result<(), String> {
    let catalog: serde_json::Value =
        serde_json::from_str(MENU_CATALOG).map_err(|e| e.to_string())?;
    if !catalog
        .as_array()
        .is_some_and(|rows| rows.iter().any(|row| row["id"].as_str() == Some(id)))
    {
        return Err("Unknown BridgeTap menu.".into());
    }
    if !compatible(cluster) || !enabled(cluster) {
        return Err("Enable BridgeTap on a 1.8.9 Ornithe profile.".into());
    }
    let root = cluster
        .game_dir()
        .map_err(|e| e.to_string())?
        .join("config/bridgetap");
    let now = std::time::SystemTime::now()
        .duration_since(std::time::UNIX_EPOCH)
        .unwrap_or_default()
        .as_millis() as u64;
    let ready = std::fs::read_to_string(root.join("client-menu.ready"))
        .ok()
        .and_then(|text| text.trim().parse::<u64>().ok());
    if !ready.is_some_and(|time| now.saturating_sub(time) < 5000 && time <= now + 5000) {
        return Err("Launch this profile with the new BridgeTap version and join a world, then try again.".into());
    }
    let data = format!(
        "menu={id}\ncreated={now}\nrequest={}\n",
        uuid::Uuid::new_v4()
    );
    polyio::write_atomic(root.join("client-menu.request"), data)
        .await
        .map_err(|e| e.to_string())
}
