//! Version selection is independent of the embedded fallback and survives every launch.
use oneclient_common::paths;
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use std::path::{Path, PathBuf};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const MAX_JAR: usize = 128 * 1024 * 1024;
pub const BUNDLED_VERSION: &str = include_str!("../../assets/bridgetap-version.txt");
static CHANGE: tokio::sync::Mutex<()> = tokio::sync::Mutex::const_new(());

#[derive(Default, Clone, Serialize, Deserialize)]
#[serde(default)]
pub struct Selection {
    pub auto_update: bool,
    pub version: Option<String>,
    pub sha256: Option<String>,
    pub last_check: u64,
}
#[derive(Clone)]
pub struct Release {
    pub version: String,
    pub url: String,
    pub sha256: String,
}
fn root() -> Result<PathBuf, String> {
    Ok(paths::config_dir()
        .map_err(|e| e.to_string())?
        .join("bridgetap/versions"))
}
pub fn selection() -> Result<Selection, String> {
    let path = root()?.join("selection.json");
    if !path.exists() {
        return Ok(Selection::default());
    }
    serde_json::from_slice(&std::fs::read(path).map_err(|e| e.to_string())?)
        .map_err(|e| e.to_string())
}
impl Selection {
    pub fn current(&self) -> &str {
        self.version.as_deref().unwrap_or(BUNDLED_VERSION.trim())
    }
}
async fn save(value: &Selection) -> Result<(), String> {
    let bytes = serde_json::to_vec_pretty(value).map_err(|e| e.to_string())?;
    polyio::write_atomic(root()?.join("selection.json"), bytes)
        .await
        .map_err(|e| e.to_string())
}
pub async fn set_auto_update(enabled: bool) -> Result<(), String> {
    let _guard = CHANGE.lock().await;
    let mut value = selection()?;
    value.auto_update = enabled;
    save(&value).await
}
pub async fn restore_bundled() -> Result<(), String> {
    let _guard = CHANGE.lock().await;
    let mut value = selection()?;
    value.version = None;
    value.sha256 = None;
    save(&value).await
}
fn hash(bytes: &[u8]) -> String {
    format!("{:x}", Sha256::digest(bytes))
}
fn safe_hash(value: &str) -> bool {
    value.len() == 64 && value.bytes().all(|b| b.is_ascii_hexdigit())
}

pub async fn validate(path: &Path) -> Result<String, String> {
    if std::fs::metadata(path).map_err(|e| e.to_string())?.len() > MAX_JAR as u64 {
        return Err("Ce JAR dépasse la limite de 128 Mo.".into());
    }
    let entries = polyio::read_zip_file_entries(path, |name| {
        name == "fabric.mod.json" || name == "com/bridger/momentum/oneclient/OneClientConfig.class"
    })
    .await
    .map_err(|e| e.to_string())?;
    let (_, bytes) = entries
        .iter()
        .find(|(n, _)| n == "fabric.mod.json")
        .ok_or("Choisis le JAR BridgeTap pour OneClient / Ornithe, pas l'édition Forge.")?;
    let json: serde_json::Value = serde_json::from_slice(bytes).map_err(|e| e.to_string())?;
    manifest_version(
        &json,
        entries
            .iter()
            .any(|(n, _)| n.ends_with("/OneClientConfig.class")),
    )
}
fn manifest_version(json: &serde_json::Value, adapter: bool) -> Result<String, String> {
    if json["id"] != "bridgetap" || json["depends"]["minecraft"] != "1.8.9" || !adapter {
        return Err(
            "Le fichier doit être BridgeTap 1.8.9 avec l'intégration OneConfig native.".into(),
        );
    }
    json["version"]
        .as_str()
        .filter(|v| !v.is_empty())
        .map(str::to_string)
        .ok_or("Version du mod absente.".into())
}

async fn select_locked(path: &Path, value: &mut Selection) -> Result<String, String> {
    validate(path).await?;
    let bytes = tokio::fs::read(path).await.map_err(|e| e.to_string())?;
    let digest = hash(&bytes);
    polyio::write_atomic(root()?.join(format!("{digest}.jar")), bytes)
        .await
        .map_err(|e| e.to_string())?;
    let version = validate(&root()?.join(format!("{digest}.jar"))).await?;
    value.version = Some(version.clone());
    value.sha256 = Some(digest);
    save(value).await?;
    Ok(version)
}
pub async fn select_local(path: &Path) -> Result<String, String> {
    let _guard = CHANGE.lock().await;
    select_locked(path, &mut selection()?).await
}
pub async fn payload() -> Result<Vec<u8>, String> {
    let value = selection()?;
    let Some(digest) = value.sha256 else {
        return Ok(super::PAYLOAD.to_vec());
    };
    if !safe_hash(&digest) {
        return Err(
            "Empreinte du mod invalide. Choisis un JAR ou restaure la version embarquée.".into(),
        );
    }
    let bytes = tokio::fs::read(root()?.join(format!("{digest}.jar")))
        .await
        .map_err(|e| e.to_string())?;
    if hash(&bytes) != digest.to_ascii_lowercase() {
        return Err("Le JAR sélectionné a changé : son empreinte ne correspond plus.".into());
    }
    Ok(bytes)
}
fn version_key(version: &str) -> (Vec<u64>, bool, Vec<u64>) {
    let (base, pre) = version
        .trim_start_matches('v')
        .split_once('-')
        .map_or((version.trim_start_matches('v'), None), |(a, b)| {
            (a, Some(b))
        });
    let numbers = |text: &str| {
        text.split(|c: char| !c.is_ascii_digit())
            .filter_map(|v| v.parse().ok())
            .collect::<Vec<u64>>()
    };
    (numbers(base), pre.is_none(), numbers(pre.unwrap_or("")))
}
pub fn newer(candidate: &str, current: &str) -> bool {
    version_key(candidate) > version_key(current)
}

async fn get(url: &str, cap: usize) -> Result<Vec<u8>, String> {
    let client = reqwest::Client::builder()
        .user_agent("bridgetapclient")
        .timeout(Duration::from_secs(45))
        .build()
        .map_err(|e| e.to_string())?;
    let mut response = client
        .get(url)
        .send()
        .await
        .map_err(|e| e.to_string())?
        .error_for_status()
        .map_err(|e| e.to_string())?;
    if response.content_length().is_some_and(|n| n > cap as u64) {
        return Err("Téléchargement trop volumineux.".into());
    }
    let mut data = Vec::new();
    while let Some(chunk) = response.chunk().await.map_err(|e| e.to_string())? {
        if data.len() + chunk.len() > cap {
            return Err("Téléchargement trop volumineux.".into());
        }
        data.extend_from_slice(&chunk);
    }
    Ok(data)
}
fn checksum(text: &str, file: &str) -> Option<String> {
    for line in text.lines() {
        let parts: Vec<_> = line.split_whitespace().collect();
        if parts.first().is_some_and(|s| safe_hash(s))
            && (parts.len() == 1
                || parts
                    .get(1)
                    .is_some_and(|s| s.trim_start_matches('*') == file))
        {
            return Some(parts[0].to_ascii_lowercase());
        }
    }
    None
}
pub async fn check_update() -> Result<Option<Release>, String> {
    let json: serde_json::Value = serde_json::from_slice(
        &get(
            "https://api.github.com/repos/sgggnnn/bridgetap/releases/latest",
            4 * 1024 * 1024,
        )
        .await?,
    )
    .map_err(|e| e.to_string())?;
    let version = json["tag_name"]
        .as_str()
        .ok_or("Version GitHub absente.")?
        .trim_start_matches('v')
        .to_string();
    if !newer(&version, selection()?.current()) {
        return Ok(None);
    }
    let assets = json["assets"]
        .as_array()
        .ok_or("Fichiers de release absents.")?;
    let asset = assets
        .iter()
        .find(|a| {
            a["name"]
                .as_str()
                .is_some_and(|n| n.ends_with("-OneClient.jar"))
        })
        .ok_or("Aucune édition OneClient dans cette release.")?;
    let name = asset["name"].as_str().ok_or("Nom du JAR absent.")?;
    let url = asset["browser_download_url"]
        .as_str()
        .ok_or("Adresse du JAR absente.")?
        .to_string();
    if !url.starts_with("https://github.com/sgggnnn/bridgetap/releases/download/") {
        return Err("Source de téléchargement inattendue.".into());
    }
    let mut digest = asset["digest"]
        .as_str()
        .and_then(|s| s.strip_prefix("sha256:"))
        .filter(|s| safe_hash(s))
        .map(str::to_string);
    if digest.is_none() {
        let checksum_asset = assets
            .iter()
            .find(|a| a["name"].as_str() == Some(&format!("{name}.sha256")))
            .or_else(|| {
                assets
                    .iter()
                    .find(|a| a["name"].as_str() == Some("SHA256SUMS.txt"))
            })
            .ok_or("La release ne fournit pas d'empreinte SHA-256 vérifiable.")?;
        let checksum_url = checksum_asset["browser_download_url"]
            .as_str()
            .ok_or("Adresse de l'empreinte absente.")?;
        if !checksum_url.starts_with("https://github.com/sgggnnn/bridgetap/releases/download/") {
            return Err("Source d'empreinte inattendue.".into());
        }
        digest = checksum(
            &String::from_utf8(get(checksum_url, 128 * 1024).await?).map_err(|e| e.to_string())?,
            name,
        );
    }
    Ok(Some(Release {
        version,
        url,
        sha256: digest.ok_or("Empreinte SHA-256 absente pour ce fichier.")?,
    }))
}
pub async fn install_release(release: &Release) -> Result<String, String> {
    if !safe_hash(&release.sha256)
        || !release
            .url
            .starts_with("https://github.com/sgggnnn/bridgetap/releases/download/")
    {
        return Err("Release invalide.".into());
    }
    let bytes = get(&release.url, MAX_JAR).await?;
    if hash(&bytes) != release.sha256.to_ascii_lowercase() {
        return Err("L'empreinte du téléchargement ne correspond pas à la release.".into());
    }
    let _guard = CHANGE.lock().await;
    let mut current = selection()?;
    if !newer(&release.version, current.current()) {
        return Ok(current.current().to_string());
    }
    let temporary = root()?.join(format!("download-{}.jar", uuid::Uuid::new_v4()));
    tokio::fs::create_dir_all(root()?)
        .await
        .map_err(|e| e.to_string())?;
    tokio::fs::write(&temporary, bytes)
        .await
        .map_err(|e| e.to_string())?;
    let result = async {
        if validate(&temporary).await? != release.version {
            return Err("La version du JAR ne correspond pas à la release.".into());
        }
        select_locked(&temporary, &mut current).await
    }
    .await;
    let _ = tokio::fs::remove_file(temporary).await;
    result
}
pub async fn maybe_auto_update() {
    let now = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap_or_default()
        .as_secs();
    let Ok(value) = selection() else {
        return;
    };
    if !value.auto_update || now.saturating_sub(value.last_check) < 6 * 60 * 60 {
        return;
    }
    let result = async {
        if let Some(release) = check_update().await? {
            install_release(&release).await?;
        }
        let _guard = CHANGE.lock().await;
        let mut value = selection()?;
        value.last_check = now;
        save(&value).await
    }
    .await;
    if let Err(error) = result {
        tracing::warn!(%error,"BridgeTap update unavailable; keeping selected version");
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn versions_do_not_downgrade_development_builds() {
        assert!(!newer("1.9.0", "1.10.0-dev.10"));
        assert!(newer("1.10.0", "1.10.0-dev.10"));
        assert!(newer("1.10.0-dev.11", "1.10.0-dev.9"));
        assert!(!newer("1.10.0-dev.9", "1.10.0-dev.11"));
    }
    #[test]
    fn checksums_must_name_the_selected_asset() {
        let hash = "a".repeat(64);
        assert_eq!(
            checksum(
                &format!("{hash}  BridgeTap-OneClient.jar"),
                "BridgeTap-OneClient.jar"
            ),
            Some(hash.clone())
        );
        assert_eq!(
            checksum(&format!("{hash}  other.jar"), "BridgeTap-OneClient.jar"),
            None
        );
        assert_eq!(
            checksum(
                "../../outside  BridgeTap-OneClient.jar",
                "BridgeTap-OneClient.jar"
            ),
            None
        );
    }
    #[test]
    fn only_native_bridgetap_189_is_accepted() {
        let valid =
            serde_json::json!({"id":"bridgetap","version":"test","depends":{"minecraft":"1.8.9"}});
        assert_eq!(manifest_version(&valid, true).unwrap(), "test");
        assert!(manifest_version(&valid, false).is_err());
        let mut other = valid.clone();
        other["id"] = "another-mod".into();
        assert!(manifest_version(&other, true).is_err());
        let mut modern = valid.clone();
        modern["depends"]["minecraft"] = "1.21.1".into();
        assert!(manifest_version(&modern, true).is_err());
    }
    #[tokio::test]
    async fn selected_jar_is_cached_verified_and_preserved_until_reset() {
        let temp =
            std::env::temp_dir().join(format!("bridgetap-version-test-{}", uuid::Uuid::new_v4()));
        std::fs::create_dir_all(&temp).unwrap();
        paths::set_launcher_dir(temp.clone());
        let origin = temp.join("download.jar");
        std::fs::write(&origin, super::super::PAYLOAD).unwrap();
        assert_eq!(select_local(&origin).await.unwrap(), BUNDLED_VERSION.trim());
        std::fs::write(&origin, b"original download moved or replaced").unwrap();
        assert_eq!(payload().await.unwrap(), super::super::PAYLOAD);
        set_auto_update(true).await.unwrap();
        let chosen = selection().unwrap();
        assert!(chosen.auto_update);
        assert!(chosen.sha256.is_some());
        std::fs::write(
            root()
                .unwrap()
                .join(format!("{}.jar", chosen.sha256.unwrap())),
            b"corrupted cache",
        )
        .unwrap();
        assert!(payload().await.is_err());
        restore_bundled().await.unwrap();
        assert!(selection().unwrap().version.is_none());
        assert_eq!(payload().await.unwrap(), super::super::PAYLOAD);
        std::fs::remove_dir_all(temp).unwrap();
    }
}
