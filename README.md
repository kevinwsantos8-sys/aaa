# Shadow Katana (Fabric 1.21.1)

Mod Fabric para Minecraft 1.21.1 (Java 21, Fabric API 0.102.0+1.21.1).

## Controles
- **R** sacar/guardar a katana (precisa estar na hotbar), com animacao do braco e sons
- **Z** teleporte sombrio (12 blocos, dano em area, recarga 3 s) / **X** invisibilidade 20 s + golpe sombrio (recarga 40 s). Exigem a katana na mao
- **K** menu de upgrades (gasta niveis de XP, nivel max 50). Os niveis ficam guardados na propria katana
- Receita: obsidiana + netherite + graveto (diagonal), ou `/give @s shadowkatana:shadow_katana`

## Build
Pelo GitHub: `.github/workflows/build.yml` -> aba Actions -> Artifacts -> `shadowkatana-mod`.
Local: `gradle build` (Gradle 8.14, JDK 21) -> `build/libs/shadowkatana-0.1.0.jar` (NAO use o `-sources.jar`).

## Ajustes
- Posicao/animacao da katana: `client/render/KatanaPose.java` e `KatanaAnim.java`
- Balanceamento: `katana/KatanaData.java` e topo de `katana/KatanaServer.java`
- Sons: `tools/generate_sounds.py`
