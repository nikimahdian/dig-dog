import os
from PIL import Image

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

# Looking at the new sample more carefully
tiles = {
    # Ground
    'grass': 'towerDefense_tile024.png',
    'grass_light': 'towerDefense_tile091.png',  # Light green for placement spots
    'sand': 'towerDefense_tile093.png',  # Sandy/tan color for tower base area
    
    # Path tiles - brown dirt path
    'path_h': 'towerDefense_tile050.png',
    'path_v': 'towerDefense_tile001.png',
    'path_tl': 'towerDefense_tile002.png',
    'path_tr': 'towerDefense_tile003.png',
    'path_bl': 'towerDefense_tile004.png',
    'path_br': 'towerDefense_tile005.png',
    
    # Obstacles
    'bush': 'towerDefense_tile133.png',
    'rock': 'towerDefense_tile136.png',
    'crater': 'towerDefense_tile019.png',
    
    # Tower
    'tower_base': 'towerDefense_tile180.png',
    'tower_red': 'towerDefense_tile206.png',
    
    # Enemy
    'enemy': 'towerDefense_tile245.png',
    
    # UI/Markers
    'crosshair': 'towerDefense_tile015.png',
}

# Analyzing the sample image more carefully:
# It's approximately 12 tiles wide and 9 tiles tall
# The path forms a loop around the edge
# There's a central tower on a light/sand colored base
# Green placement spots are scattered around
# Enemies (red tanks) are on the path

grid = [
    # Row 0 - top path
    ['g', 'g', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'p1', 'g'],
    # Row 1
    ['g', 'g', 'pv', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'pv', 'g'],
    # Row 2 
    ['g', 'p4', 'p3', 'g', 'b', 'b', 'g', 'g', 'g', 'g', 'pv', 'g'],
    # Row 3
    ['g', 'pv', 'g', 'g', 'b', 'b', 'l', 'l', 'l', 'g', 'pv', 'g'],
    # Row 4 - center with tower
    ['g', 'pv', 'g', 'g', 'g', 'r', 's', 's', 's', 'g', 'pv', 'g'],
    # Row 5
    ['g', 'pv', 'g', 'b', 'b', 'g', 's', 'x', 's', 'g', 'pv', 'g'],
    # Row 6
    ['g', 'pv', 'b', 'b', 'b', 'g', 's', 's', 's', 'l', 'pv', 'g'],
    # Row 7
    ['g', 'pv', 'g', 'g', 'g', 'g', 'r', 'g', 'l', 'g', 'pv', 'g'],
    # Row 8 - bottom path
    ['g', 'p2', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'p3', 'g'],
]

legend = {
    'g': 'grass',
    'ph': 'path_h',
    'pv': 'path_v',
    'p1': 'path_tl',
    'p2': 'path_tr',
    'p3': 'path_bl',
    'p4': 'path_br',
    'b': 'bush',
    'r': 'rock',
    'l': 'grass_light',  # Light green placement spots
    's': 'sand',  # Sandy area for tower
    'x': 'tower',  # Tower position marker
}

def load_tile(tile_name):
    """Load a tile image"""
    if tile_name in tiles:
        tile_path = os.path.join(base_path, tiles[tile_name])
        if os.path.exists(tile_path):
            return Image.open(tile_path).convert('RGBA')
    return None

def create_final_map():
    """Create the final tower defense map"""
    
    width = 12 * TILE_SIZE
    height = 9 * TILE_SIZE
    
    # Create base image
    image = Image.new('RGBA', (width, height), (100, 150, 100, 255))
    
    # Draw base tiles
    for y, row in enumerate(grid):
        for x, code in enumerate(row):
            if code in legend:
                if code == 'x':
                    # Skip tower marker, will place tower later
                    tile = load_tile('sand')
                else:
                    tile_name = legend[code]
                    tile = load_tile(tile_name)
                
                if tile:
                    image.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Place the central tower at position marked with 'x'
    tower_x = 7
    tower_y = 5
    
    # First place the tower base
    base = load_tile('tower_base')
    if base:
        image.paste(base, (tower_x * TILE_SIZE, tower_y * TILE_SIZE), base)
    
    # Then place the red tower on top
    tower = load_tile('tower_red')
    if tower:
        image.paste(tower, (tower_x * TILE_SIZE, tower_y * TILE_SIZE), tower)
    
    # Place enemies on the path
    enemy_positions = [
        (1, 2),   # Left enemy
        (2, 0),   # Top enemy
        (10, 6),  # Right enemy
    ]
    
    for ex, ey in enemy_positions:
        enemy = load_tile('enemy')
        if enemy:
            # Scale to fit on path
            enemy = enemy.resize((50, 50), Image.Resampling.LANCZOS)
            offset = 7
            image.paste(enemy, (ex * TILE_SIZE + offset, ey * TILE_SIZE + offset), enemy)
    
    # Add crosshair/target marker
    crosshair = load_tile('crosshair')
    if crosshair:
        # Place at position (7, 3)
        image.paste(crosshair, (7 * TILE_SIZE, 3 * TILE_SIZE), crosshair)
    
    return image

if __name__ == "__main__":
    print("Creating final tower defense map...")
    final_map = create_final_map()
    final_map.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")
    print(f"Size: {final_map.size}")