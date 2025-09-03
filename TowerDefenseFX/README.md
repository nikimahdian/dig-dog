# Tower Defense FX

A comprehensive JavaFX-based tower defense game built with pure JavaFX (no FXML) using Java 21. Features complete gameplay systems including enemies, towers, special abilities, and strategic resource management.

## 🎮 Features

### Core Gameplay
- **Pure JavaFX**: Built without external graphics libraries or FXML
- **Serialized Maps**: Predefined maps and paths stored in JSON format  
- **Thread-Safe Economy**: Concurrent resource management with auto-income
- **Strategic Combat**: Tower targeting, projectiles, status effects

### Enemy Types
- **Soldiers**: Standard ground units following paths
- **Tanks**: Heavily armored units that can attack your defenses while moving
- **Aircraft**: Fast-moving units that trigger devastating row/column strikes

### Defense Systems  
- **Fast Towers**: High rate of fire, lower damage per shot
- **Power Towers**: High damage, slower rate of fire
- **AA Defenses**: Anti-aircraft systems with 60% and 80% hit chances
- **Speed Bumps**: Slow enemies temporarily (limited duration)
- **Bombs**: One-time explosive damage in radius

### Advanced Mechanics
- **Aircraft Strikes**: AI calculates optimal row/column strikes to maximize player damage
- **Tank Combat**: Tanks can target and destroy your defenses while moving
- **Status Effects**: Speed reduction, damage over time, visual feedback
- **Victory/Defeat Rules**: Lose if ≥10% of total enemy power reaches castle

## 🏗️ Architecture

### Clean Code Design
- **Model/View/Controller**: Clear separation of concerns
- **Event-Driven**: Decoupled systems using EventBus pattern
- **Thread-Safe**: Proper concurrency handling in economy system
- **Data-Driven**: All balance values configurable via JSON
- **Extensible**: Easy to add new enemy types, towers, abilities

### Project Structure
```
TowerDefenseFX/
├── build.gradle                    # Gradle + JavaFX + Jackson + JUnit
├── src/main/java/com/tdgame/
│   ├── App.java                    # JavaFX application entry point
│   ├── core/                       # Game loop, time, events
│   │   ├── Game.java               # Central game coordinator
│   │   ├── Time.java               # Delta time management
│   │   └── EventBus.java           # Event system for decoupling
│   ├── model/                      # Game entities and business logic
│   │   ├── grid/                   # Spatial grid, tiles, build slots
│   │   ├── actors/                 # Enemies, towers, projectiles  
│   │   ├── placeables/             # Speed bumps, bombs
│   │   └── systems/                # Core game systems
│   │       ├── WaveManager.java    # Enemy spawning
│   │       ├── CombatSystem.java   # Combat coordination
│   │       ├── EconomyManager.java # Thread-safe money management
│   │       ├── AircraftStrikeSystem.java # Strategic strike AI
│   │       └── Rules.java          # Victory/defeat logic
│   ├── view/                       # Rendering and UI
│   │   ├── GameCanvas.java         # Main game renderer
│   │   ├── HUD.java               # UI overlays and info
│   │   ├── MainMenu.java          # Menu system
│   │   └── SpriteLoader.java      # Asset management
│   ├── controller/                 # Input and user interaction
│   │   ├── InputController.java    # Mouse/keyboard handling
│   │   └── BuildMenuController.java # Construction logic
│   ├── config/                     # Configuration system
│   │   ├── GameConfig.java         # Central config loader
│   │   ├── Balance.java           # Game balance data classes
│   │   ├── LevelData.java         # Map structure data
│   │   └── WaveData.java          # Enemy wave definitions
│   └── util/                       # Utilities and helpers
├── src/main/resources/
│   ├── assets/kenney/              # Kenney tower defense sprites
│   ├── levels/level1.json          # Map layout and build slots
│   ├── waves/{easy,normal,hard}.json # Difficulty configurations
│   └── config/balance.json         # All game balance values
└── src/test/java/                  # Comprehensive unit tests
    ├── EconomyManagerTest.java     # Thread safety tests
    ├── RulesTest.java             # Victory/defeat logic tests
    └── GameConfigTest.java        # JSON loading tests
```

## 🚀 Getting Started

### Prerequisites
- **Java 21** (OpenJDK or Oracle JDK)
- **JavaFX 21** (included via Gradle)
- **Assets**: Kenney Tower Defense pack in `kenney_tower-defense-top-down/`

### Quick Start
```bash
# Clone or extract the project
cd TowerDefenseFX

# Run the game directly
./gradlew run
# OR on Windows:
gradlew.bat run

# Build the project
./gradlew build

# Run tests
./gradlew test
```

### Manual Setup (if needed)
```bash
# Copy assets to resources (if not already done)
cp "../kenney_tower-defense-top-down/PNG/Default size/towerDefense_tile0*.png" \
   "src/main/resources/assets/kenney/PNG/Default size/"

cp "../kenney_tower-defense-top-down/Tilesheet/towerDefense_tilesheet.png" \
   "src/main/resources/Tilesheet/"
```

## 🎯 Gameplay Guide

### Controls
- **Mouse Click**: Click green squares to build towers/AA defenses
- **Mouse Click**: Click orange circles for speed bumps  
- **Mouse Click**: Click red squares for bombs
- **ESC/Space**: Pause/resume game
- **1,2,3,4**: Quick build (when build menu is open)

### Strategy Tips
1. **Early Economy**: Let some easy enemies through to build up money
2. **Mixed Defense**: Combine fast towers (swarms) and power towers (tanks)
3. **AA Priority**: Aircraft strikes are devastating - build AA defenses
4. **Chokepoints**: Use speed bumps and bombs at path narrows
5. **Tank Counter**: Spread out defenses to minimize tank damage

### Victory Conditions
- ✅ **Win**: Defeat all enemy waves
- ❌ **Lose**: ≥10% of total enemy power reaches your castle

## 🔧 Configuration

### JSON Configuration Files

**Balance Configuration** (`config/balance.json`):
```json
{
  "money": { "start": 150, "incomePerSec": 3 },
  "towers": {
    "fast": { "cost": 60, "range": 3.5, "rpm": 90, "damage": 6, "hp": 60 },
    "power": { "cost": 110, "range": 4.0, "rpm": 35, "damage": 16, "hp": 120 }
  },
  "enemies": {
    "soldier": { "hp": 35, "speed": 2.2, "power": 1 },
    "tank": { "hp": 220, "speed": 1.2, "power": 4, "dpsVsDefenses": 10 }
  }
}
```

**Level Layout** (`levels/level1.json`):
```json
{
  "grid": { "cols": 20, "rows": 12, "tileSize": 48 },
  "buildSlots": [{"col":5,"row":3}, {"col":8,"row":6}],
  "paths": [{"name":"main", "waypoints": [[0,6], [19,8]]}]
}
```

**Wave Definitions** (`waves/easy.json`):
```json
{
  "waves": [
    {"delay": 0, "enemies": [{"type":"soldier","count":8,"gap":0.8}]},
    {"delay": 30, "enemies": [{"type":"tank","count":1}], "aircraftChance": 0.3}
  ]
}
```

## 🧪 Testing

Comprehensive test coverage includes:

- **Thread Safety**: Economy system concurrent access tests
- **Game Rules**: Victory/defeat condition validation  
- **Configuration**: JSON loading and data validation
- **Combat Logic**: Damage calculations and targeting
- **Wave Management**: Enemy spawning and progression

```bash
./gradlew test --info  # Run with detailed output
```

## 🎨 Technical Highlights

### Advanced Features
- **Smart AI**: Aircraft calculate optimal strike positions
- **Smooth Animation**: 60 FPS game loop with proper delta time
- **Visual Effects**: Health bars, range indicators, explosion effects
- **Resource Management**: Thread-safe economy with atomic operations
- **Event System**: Decoupled architecture using publish/subscribe

### Code Quality
- **SOLID Principles**: Single responsibility, dependency injection
- **Design Patterns**: Strategy, Observer, Factory patterns
- **Clean Architecture**: Clear layer separation and interfaces
- **Performance**: Efficient rendering and game loop optimization

## 📦 Dependencies

- **JavaFX 21**: Core UI framework
- **Jackson 2.15.2**: JSON serialization/deserialization  
- **JUnit 5**: Unit testing framework
- **Mockito 5**: Mocking for unit tests

## 🏆 Implementation Notes

This project demonstrates:
- **Production-quality architecture** with clean separation of concerns
- **Advanced game programming** concepts (game loops, delta time, entity systems)
- **Concurrent programming** with thread-safe resource management
- **Data-driven design** with external JSON configuration
- **Comprehensive testing** including concurrency and business logic tests

Built as a complete software engineering project showcasing both game development skills and enterprise-level code organization.

## 🎮 Game Features Summary

### ✅ Fully Implemented
- Complete tower defense gameplay loop
- Multiple enemy types with unique abilities  
- Strategic building and resource management
- Advanced AI systems (aircraft strikes)
- Thread-safe concurrent systems
- Comprehensive UI and visual effects
- Victory/defeat conditions with 10% leak rule
- Configurable difficulty levels
- Complete test coverage

### 🔮 Future Enhancements  
- A* pathfinding (clean seam provided)
- Additional enemy/tower types
- Multiple maps and campaigns
- Save/load functionality
- Sound effects and music
- Advanced visual effects