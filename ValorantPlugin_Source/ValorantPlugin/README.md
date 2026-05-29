# ValorantMC Plugin — Aternos Setup Guide

## 📦 Requirements
- **Spigot / Paper 1.20.x** (set this in Aternos under Software)
- **Java 17** (Aternos handles this automatically)

---

## 🔨 Building the Plugin (on your PC)

### Prerequisites
- Java 17 JDK installed
- Maven installed (or use IntelliJ IDEA which bundles it)

### Steps
```bash
# 1. Go into the plugin folder
cd ValorantPlugin

# 2. Build with Maven
mvn clean package

# 3. Your JAR is in:
#    target/ValorantMC-1.0.0.jar
```

Alternatively, open the folder in **IntelliJ IDEA**, import as Maven project, then run `package`.

---

## 🚀 Installing on Aternos

1. Log into https://aternos.org
2. Go to your server → **Software** → set to **Spigot 1.20.4**
3. Go to **Files** → `plugins/` folder
4. Upload `ValorantMC-1.0.0.jar`
5. **Start your server**

---

## 🎮 How to Play

### Setup (all players before starting)
```
/vagent Jett       ← pick your agent (each player picks their own)
```

### Starting a game (OP only)
```
/vstart            ← auto-splits players into Attack/Defense teams
```

### During Buy Phase (30 seconds)
```
/vbuy Phantom      ← buy a weapon with your credits
/vbuy Vandal
/vbuy Sheriff
/vbuy Operator
/vbuy Spectre
/vbuy Bucky
/vbuy Shorty
```

### During a Round
- **Right-click** with your gun item → **Shoot**
- **Sneak + Right-click** → **Reload**
- `/vability q` → use **Q ability**
- `/vability e` → use **E ability**

### Admin
```
/vstop             ← force-stop the game
/vgun Classic      ← give yourself any gun (testing)
```

---

## 🦸 5 Agents

| Agent   | Passive                        | Q Ability              | E Ability           |
|---------|-------------------------------|------------------------|---------------------|
| Jett    | Speed boost                   | Updraft (jump up)      | Tailwind (dash)     |
| Sage    | Damage resistance             | Slow Orb               | Healing Orb         |
| Phoenix | Heals on kills                | Curveball (blind)      | Blaze (fire wall)   |
| Reyna   | Soul Orbs on kills            | Devour (heal w/ orb)   | Dismiss (invis)     |
| Breach  | None                          | Flashpoint (blind thru wall) | Fault Line (slow+confuse) |

---

## 🔫 Guns & Prices

| Gun      | Price | Damage | Notes              |
|----------|-------|--------|--------------------|
| Classic  | Free  | 45     | Starting pistol    |
| Shorty   | 200¢  | 70     | Close range        |
| Sheriff  | 800¢  | 145    | High damage pistol |
| Bucky    | 850¢  | 60     | Shotgun            |
| Spectre  | 1600¢ | 26     | SMG, fast fire     |
| Phantom  | 2900¢ | 35     | Rifle              |
| Vandal   | 2900¢ | 40     | Rifle, harder hit  |
| Operator | 4700¢ | 255    | Sniper, 1-shot     |

---

## 💰 Economy
- **Kill bonus**: +200¢
- **Win round**: +3000¢
- **Lose round**: +1900¢
- **Starting credits**: 800¢

---

## ⚙️ Permissions
- `valorantmc.start` — start game (default: OP)
- `valorantmc.stop`  — stop game (default: OP)

---

## 🗒️ Notes
- Headshots deal **1.5× damage**
- Sides swap after round 12
- First team to **13 round wins** wins the match
- Guns are mapped to Minecraft items; do NOT drop them (blocked)
- `/vstart` requires at least 2 online players
