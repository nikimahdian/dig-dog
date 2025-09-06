# Tower Defense JavaFX

A minimal but complete tower-defense game implemented in pure JavaFX, featuring the exact map layout from the specification with two enemy routes and strategic build positions.

## Features

- **Single Hardcoded Map**: 11×9 grid with two distinct enemy routes matching the specification
- **Defense Types**: Fast towers, Heavy towers, and Anti-Air systems (60% and 80% hit chance)
- **Enemy Types**: Soldiers, Tanks (that shoot back), and Planes (with bombing runs)
- **Map Gadgets**: Speed-bump tiles and explosive bombs on enemy paths
- **Resource System**: Auto-generating money with thread-safe updates
- **Wave System**: 6 waves with difficulty scaling and multiple enemy types
- **Victory/Defeat**: Based on enemy power leakage (≥10% = defeat)

## Controls

- **Left Click**: On green build slots to open build menu
- **Build Menu**: Select tower type or gadget to purchase
- **Difficulty**: Choose Easy/Medium/Hard in main menu

## Map Layout

The game features an exact replica of the specified map:

### Enemy Routes
- **Route A**: Left vertical lane (0,0) → (0,1) → ... → (0,8)
- **Route B**: Top horizontal → Right vertical → Middle horizontal
  - Top: (0,0) → (1,0) → ... → (10,0)
  - Right: (10,1) → (10,2) → ... → (10,6)  
  - Middle: (9,6) → (8,6) → (7,6) → (6,6)

### Build Positions
- **Left-upper cluster**: 2×2 block at (2,2), (3,2), (2,3), (3,3)
- **Mid-left**: Single slot at (2,5)
- **Right column**: Vertical stack at (9,2), (9,3), (9,4), (9,5)
- **Center-right**: Single slot at (6,5)

### Gadget Slots
- **Speed-bumps**: Route A at (0,3), Route B at (10,2)
- **Bomb**: Route B at (9,6)

## Running the Game

```bash
./gradlew run
```

Or on Windows:
```cmd
gradlew.bat run
```

## SVG Asset Extraction

The game uses a custom SVG extractor that:

1. **Parses** the Kenney vector sprite sheet using javax.xml
2. **Extracts** sprite shapes by analyzing spatial regions
3. **Renders** sprites using JavaFX SVGPath and Group nodes
4. **Caches** rendered Images in a SpriteStore for performance
5. **Fallbacks** to simple colored shapes if SVG parsing fails

### Sprite Extraction Process
- Loads `kenney_tower-defense-top-down/Vector/towerDefense_vector.svg`
- Parses XML DOM to find `<path>` elements with `d` attributes
- Groups related paths by spatial proximity (simulated bounding boxes)
- Creates JavaFX SVGPath nodes with proper fill colors
- Snapshots Groups to create cached Image objects

## Architecture

The codebase follows clean OOP principles with composition over inheritance:

```
game/
├── Main.java              # Entry point
├── App.java              # JavaFX application bootstrap
├── Config.java           # All tuneable constants
├── core/                 # Game systems
│   ├── GameLoop.java     # 60 FPS AnimationTimer
│   ├── ResourceManager.java # Thread-safe money generation
│   ├── WaveManager.java  # Enemy spawning and wave logic  
│   └── Collision.java    # AoE and range calculations
├── map/                  # Map representation
│   ├── Grid.java         # 11×9 tile coordinate system
│   ├── Tile.java         # Tile types enum
│   ├── Route.java        # Enemy path waypoints
│   ├── MapDefinition.java # Hardcoded map layout
│   └── MapRenderer.java  # Map and tile rendering
├── entity/               # Game objects
│   ├── Entity.java       # Base class with HP/position
│   ├── Enemy.java        # Route-following movement
│   ├── Tower.java        # Targeting and combat
│   ├── Projectile.java   # Visual projectiles
│   └── [Soldier, Tank, Plane, FastTower, HeavyTower, AA60, AA80]
├── ui/                   # User interface
│   ├── HudView.java      # Money, HP, wave info
│   ├── BuildMenu.java    # Radial build selection
│   └── Toast.java        # Notification messages
└── svg/                  # Asset loading
    ├── SvgAtlasExtractor.java # XML parsing + SVGPath rendering
    └── SpriteStore.java  # Image caching and retrieval
```

## Game Balance

All numeric values are configurable in `Config.java`:

### Towers
- **Fast Tower**: $50, 100 HP, 3-tile range, 4 shots/sec, 8/4/0 damage
- **Heavy Tower**: $80, 220 HP, 4-tile range, 1 shot/sec, 20/12/0 damage
- **Anti-Air 60%**: $90, global coverage, 60% hit chance vs planes
- **Anti-Air 80%**: $140, global coverage, 80% hit chance vs planes

### Enemies  
- **Soldier**: 40 HP, 1.0 tile/sec, Power=1
- **Tank**: 260 HP, 0.6 tile/sec, Power=2, shoots towers (3-tile range, 12 damage)
- **Plane**: 3.0 tile/sec, Power=3, bombs optimal targets mid-flight

### Economy
- **Starting Money**: $100
- **Income**: +$5 every 0.5 seconds
- **Speed-bump**: $25 (50% slow, 12-second lifetime)
- **Bomb**: $40 (50 damage, 1.5-tile radius, single-use)

## Technical Notes

- **No External Dependencies**: Pure JavaFX with built-in XML parsing
- **Thread Safety**: ResourceManager uses AtomicInteger + Platform.runLater
- **Deterministic**: All constants in Config.java for reproducible gameplay
- **60 FPS**: AnimationTimer-based game loop with delta time integration
- **Memory Efficient**: Entity cleanup, sprite caching, minimal object allocation

## Building

Requires Java 17+ and JavaFX. The Gradle build automatically handles JavaFX modules and creates a runnable distribution.