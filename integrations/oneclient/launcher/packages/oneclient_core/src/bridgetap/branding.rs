//! Rebrand PolyPlus presentation while preserving class names, APIs and saved paths.
use crate::LauncherResult;
use std::{collections::HashMap, path::Path};

const MARKER: &str = "META-INF/bridgetapclient-branding-v1";
const LOGO: &[u8] = include_bytes!("../../assets/bridgetap-wordmark.svg");

fn visible(bytes: &[u8]) -> Option<Vec<u8>> {
    let text = std::str::from_utf8(bytes).ok()?;
    if text.contains("://")
        || text.contains('/')
        || text.contains('\\')
        || text.contains("org.polyfrost")
        || text.contains(".exe")
        || text.contains(".json")
        || text.contains(".jar")
        || text.contains(".dll")
        || text.contains(".class")
    {
        return None;
    }
    let renamed = text
        .replace("OneClient", "BridgeTapClient")
        .replace("ONECLIENT", "BridgeTapClient")
        .replace("One Client", "BridgeTapClient");
    (renamed != text).then(|| renamed.into_bytes())
}

/// Only CONSTANT_String references are remapped to NEW UTF8 entries. Shared class,
/// method, field and descriptor constants remain byte-for-byte unchanged.
fn class_strings(bytes: &[u8]) -> Result<Vec<u8>, String> {
    if bytes.len() < 10 || &bytes[..4] != b"\xca\xfe\xba\xbe" {
        return Err("Invalid class header".into());
    }
    let count = u16::from_be_bytes([bytes[8], bytes[9]]) as usize;
    let mut entries = vec![Vec::new(); count];
    let mut strings = HashMap::new();
    let mut cursor = 10;
    let mut index = 1;
    while index < count {
        let start = cursor;
        let tag = *bytes.get(cursor).ok_or("Truncated constant pool")?;
        cursor += 1;
        let size = match tag {
            1 => {
                let len = bytes
                    .get(cursor..cursor + 2)
                    .ok_or("Truncated UTF8 length")?;
                let len = u16::from_be_bytes([len[0], len[1]]) as usize;
                let value = bytes
                    .get(cursor + 2..cursor + 2 + len)
                    .ok_or("Truncated UTF8 value")?;
                strings.insert(index, value);
                2 + len
            }
            3 | 4 | 9 | 10 | 11 | 12 | 17 | 18 => 4,
            5 | 6 => 8,
            7 | 8 | 16 | 19 | 20 => 2,
            15 => 3,
            _ => return Err(format!("Unsupported constant pool tag {tag}")),
        };
        cursor += size;
        entries[index] = bytes
            .get(start..cursor)
            .ok_or("Truncated constant")?
            .to_vec();
        index += if tag == 5 || tag == 6 { 2 } else { 1 };
    }
    let mut added = Vec::new();
    for entry in &mut entries {
        if entry.first() != Some(&8) {
            continue;
        }
        let reference = u16::from_be_bytes([entry[1], entry[2]]) as usize;
        if let Some(renamed) = strings.get(&reference).and_then(|value| visible(value)) {
            let new_index = u16::try_from(count + added.len()).map_err(|_| "Constant pool full")?;
            let length = u16::try_from(renamed.len()).map_err(|_| "String too long")?;
            let mut constant = vec![1];
            constant.extend(length.to_be_bytes());
            constant.extend(renamed);
            added.push(constant);
            entry[1..3].copy_from_slice(&new_index.to_be_bytes());
        }
    }
    if added.is_empty() {
        return Ok(bytes.to_vec());
    }
    let count = u16::try_from(count + added.len()).map_err(|_| "Constant pool full")?;
    let mut out = bytes[..8].to_vec();
    out.extend(count.to_be_bytes());
    for entry in entries.into_iter().skip(1).chain(added) {
        out.extend(entry);
    }
    out.extend_from_slice(&bytes[cursor..]);
    Ok(out)
}

pub async fn install(mods: &Path, backup: &Path) -> LauncherResult<()> {
    let mut directory = tokio::fs::read_dir(mods).await?;
    while let Some(entry) = directory.next_entry().await? {
        let path = entry.path();
        if path.extension().and_then(|s| s.to_str()) != Some("jar") {
            continue;
        }
        let manifest = oneclient_content::packages::read_jar_manifest(&path).await;
        if manifest.mod_id.as_deref() != Some("polyplus") {
            continue;
        }
        let mut zip = polyio::ZipEntryCursor::open(&path, |_| true).await?;
        let names: Vec<_> = zip.names().cloned().collect();
        if names.iter().any(|name| name == MARKER) {
            continue;
        }
        if names.iter().any(|name| {
            name.starts_with("META-INF/")
                && [".SF", ".RSA", ".DSA", ".EC"]
                    .iter()
                    .any(|ext| name.to_uppercase().ends_with(ext))
        }) {
            return Err(std::io::Error::other(
                "Cannot rebrand signed PolyPlus. Original file preserved.",
            )
            .into());
        }
        let temporary = mods.join(format!(".bridgetap-brand-{}.part", uuid::Uuid::new_v4()));
        let result: LauncherResult<()> = async {
            let file = tokio::fs::File::create(&temporary).await?;
            let mut writer = async_zip::tokio::write::ZipFileWriter::with_tokio(file);
            for name in names {
                let mut bytes = zip.read(&name).await?;
                if name.starts_with("org/polyfrost/polyplus/") && name.ends_with(".class") {
                    bytes = class_strings(&bytes).map_err(std::io::Error::other)?;
                } else if [
                    "assets/polyplus/brand/oneclient.svg",
                    "assets/polyplus/mainmenu/logo.svg",
                    "assets/polyplus/mainmenu/footer-logo.svg",
                ]
                .contains(&name.as_str())
                {
                    bytes = LOGO.to_vec();
                }
                let entry =
                    async_zip::ZipEntryBuilder::new(name.into(), async_zip::Compression::Deflate);
                writer
                    .write_entry_whole(entry, &bytes)
                    .await
                    .map_err(std::io::Error::other)?;
            }
            let entry =
                async_zip::ZipEntryBuilder::new(MARKER.into(), async_zip::Compression::Stored);
            writer
                .write_entry_whole(
                    entry,
                    b"BridgeTapClient presentation; upstream notices retained\n",
                )
                .await
                .map_err(std::io::Error::other)?;
            writer.close().await.map_err(std::io::Error::other)?;
            Ok(())
        }
        .await;
        drop(zip);
        if let Err(error) = result {
            let _ = tokio::fs::remove_file(&temporary).await;
            return Err(error);
        }
        tokio::fs::create_dir_all(backup).await?;
        let original = backup.join(format!(
            "{}-{}",
            uuid::Uuid::new_v4(),
            entry.file_name().to_string_lossy()
        ));
        if let Err(error) = tokio::fs::rename(&path, &original).await {
            let _ = tokio::fs::remove_file(&temporary).await;
            return Err(error.into());
        }
        if let Err(error) = tokio::fs::rename(&temporary, &path).await {
            tokio::fs::rename(&original, &path).await?;
            return Err(error.into());
        }
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    #[tokio::test]
    async fn profile_branding_preserves_original_and_is_idempotent() {
        let root = std::env::temp_dir().join(format!("client-branding-{}", uuid::Uuid::new_v4()));
        let mods = root.join("mods");
        let backup = root.join("backups");
        tokio::fs::create_dir_all(&mods).await.unwrap();
        let jar = mods.join("polyplus.jar");
        let file = tokio::fs::File::create(&jar).await.unwrap();
        let mut writer = async_zip::tokio::write::ZipFileWriter::with_tokio(file);
        for (name, bytes) in [
            (
                "fabric.mod.json",
                br#"{"schemaVersion":1,"id":"polyplus","version":"test","name":"PolyPlus"}"#
                    .as_slice(),
            ),
            ("LICENSE", b"Upstream license must remain intact".as_slice()),
            ("assets/polyplus/mainmenu/logo.svg", b"old logo".as_slice()),
        ] {
            let entry =
                async_zip::ZipEntryBuilder::new(name.into(), async_zip::Compression::Stored);
            writer.write_entry_whole(entry, bytes).await.unwrap();
        }
        writer.close().await.unwrap();
        let original = tokio::fs::read(&jar).await.unwrap();
        install(&mods, &backup).await.unwrap();
        let once = tokio::fs::read(&jar).await.unwrap();
        assert_ne!(original, once);
        let mut entries = polyio::ZipEntryCursor::open(&jar, |_| true).await.unwrap();
        assert_eq!(
            entries.read("LICENSE").await.unwrap(),
            b"Upstream license must remain intact"
        );
        assert_eq!(
            entries
                .read("assets/polyplus/mainmenu/logo.svg")
                .await
                .unwrap(),
            LOGO
        );
        assert!(entries.read(MARKER).await.is_ok());
        drop(entries);
        install(&mods, &backup).await.unwrap();
        assert_eq!(tokio::fs::read(&jar).await.unwrap(), once);
        let mut backups = tokio::fs::read_dir(&backup).await.unwrap();
        assert_eq!(
            tokio::fs::read(backups.next_entry().await.unwrap().unwrap().path())
                .await
                .unwrap(),
            original
        );
        assert!(backups.next_entry().await.unwrap().is_none());
        tokio::fs::remove_dir_all(root).await.unwrap();
    }
    #[test]
    fn only_presentation_is_renamed() {
        assert_eq!(
            visible(b"Welcome to OneClient").unwrap(),
            b"Welcome to BridgeTapClient"
        );
        for value in [
            "oneclient://launch/test",
            "https://oneclient.info/OneClient",
            "org/polyfrost/OneClient",
            "OneClient.json",
            "OneClient.exe",
        ] {
            assert!(visible(value.as_bytes()).is_none());
        }
    }
    #[test]
    fn shared_utf8_class_reference_is_preserved() {
        // #1 UTF8 OneClient, #2 Class #1, #3 String #1, #4 UTF8 URL, #5 String #4.
        let mut class =
            b"\xca\xfe\xba\xbe\0\0\0\x34\0\x06\x01\0\x09OneClient\x07\0\x01\x08\0\x01".to_vec();
        let url = b"oneclient://launch/profile";
        class.push(1);
        class.extend((url.len() as u16).to_be_bytes());
        class.extend(url);
        class.extend(b"\x08\0\x04\0\x21\0\x02\0\0\0\0\0\0\0\0\0\0");
        let branded = class_strings(&class).unwrap();
        assert_eq!(&branded[8..10], &7u16.to_be_bytes());
        assert_eq!(&branded[10..25], &class[10..25]); // original UTF8 + Class #1
        assert_eq!(&branded[25..28], b"\x08\0\x06");
        assert!(branded.windows(url.len()).any(|part| part == url));
        assert!(branded.windows(15).any(|part| part == b"BridgeTapClient"));
        assert_eq!(class_strings(&branded).unwrap(), branded);
        assert!(class_strings(b"invalid").is_err());
    }
}
