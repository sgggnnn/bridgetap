# FFmpeg Windows runtime

This local preview includes the unmodified FFmpeg 7.1 Windows x64 executable
distributed in imageio-ffmpeg 0.6.0 (Gyan essentials build). It is run as a
separate process for H.264/AAC video export. FFmpeg is licensed under GPLv3 or
later for this build; the license is included alongside this notice.

Source: https://github.com/FFmpeg/FFmpeg/tree/n7.1
Source archive: https://ffmpeg.org/releases/ffmpeg-7.1.tar.xz
Build provider and configuration: https://www.gyan.dev/ffmpeg/builds/
Runtime distribution/build recipes: https://github.com/imageio/imageio-ffmpeg
The executable prints its complete build configuration with `ffmpeg.exe -buildconf`.

Pinned wheel: imageio_ffmpeg-0.6.0-py3-none-win_amd64.whl
SHA-256: 02fa47c83703c37df6bfe4896aab339013f62bf02c5ebf2dce6da56af04ffc0a

The small WASAPI helper is BridgeTap source code, available in native/Loopback.cs.
