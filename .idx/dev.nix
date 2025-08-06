{ pkgs, ... }: {
  # Entorno de desarrollo para uHabits
  channel = "stable-23.11"; # O la versión que prefieras

  packages = [
    pkgs.unzip
    pkgs.wget
  ];

  # Configuración del JDK y Android SDK
  languages.java.enable = true;
  languages.java.jdk.package = pkgs.openjdk17;

  # Habilitar el SDK de Android
  android.sdk.enable = true;
  android.sdk.platforms = ["34"]; # API level 34
  android.sdk.buildTools = ["34.0.0"];
  android.sdk.platformTools.enable = true;
  android.sdk.cmdLineTools.enable = true;
  android.sdk.emulator.enable = true;

  # Variables de entorno
  env.JAVA_HOME = "${pkgs.openjdk17}/";
  env.ANDROID_HOME = "$HOME/.android/sdk";
  env.PATH = [
    "$ANDROID_HOME/cmdline-tools/latest/bin",
    "$ANDROID_HOME/platform-tools",
    "$ANDROID_HOME/emulator",
    "$JAVA_HOME/bin"
  ];
}
