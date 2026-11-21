//! BridgeTap local edition. Payload is supplied by the reproducible integration build.
use crate::LauncherResult;
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
        if target.is_file() && std::fs::read(&target)? == PAYLOAD {
            std::fs::remove_file(target)?;
        }
        return Ok(());
    }
    std::fs::create_dir_all(&mods)?;
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
    if target.is_file() && std::fs::read(&target)? == PAYLOAD {
        return Ok(());
    }
    let temporary = mods.join(format!(".bridgetap-{}.part", uuid::Uuid::new_v4()));
    std::fs::write(&temporary, PAYLOAD)?;
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
