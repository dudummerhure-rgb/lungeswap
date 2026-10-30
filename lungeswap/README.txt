LungeSwap fuer Minecraft Java 1.21.11 (Fabric, nur Client)

JAR OHNE INSTALLATION BAUEN (GitHub)
1. Gehe auf github.com, melde dich an (kostenlos) und erstelle ein neues Repository "lungeswap" (privat ist ok).
2. Entpacke diese ZIP. Im Repository: "Add file" -> "Upload files" und ziehe den INHALT des Ordners
   "lungeswap" hinein (build.gradle, gradle.properties, settings.gradle, src, UND den versteckten Ordner .github).
   Wichtig: der Ordner .github muss mit hochgeladen werden.
3. Commit klicken. Danach Tab "Actions" -> "Build LungeSwap" laeuft automatisch (ca. 3-5 Minuten).
4. Klick auf den fertigen Lauf, unten bei "Artifacts" -> "lungeswap-jar" herunterladen, ZIP entpacken.
5. lungeswap-1.0.0.jar in .minecraft/mods legen (Fabric Loader + Fabric API fuer 1.21.11 noetig).

BENUTZUNG
- Optionen -> Steuerung -> "LungeSwap": Taste frei waehlen (auch Maus-Seitentaste).
- Antippen = ein Lunge-Swap. Halten = alle 5 Ticks wiederholt.
- Nimmt den Speer mit dem hoechsten Lunge-Level aus der Hotbar und wechselt danach auf den Slot mit dem
  geringsten Cooldown zurueck (Gleichstand: dein urspruenglicher Slot).

HINWEIS
- Pruefe die Regeln des Servers. Viele Server und Anti-Cheats werten Swap-Automatik als Cheat.
