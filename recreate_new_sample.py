import os
from PIL import Image, ImageDraw

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

# Based on the new sample image, I need these tiles:
tiles = {
    # Ground tiles
    'grass': 'towerDefense_tile024.png',  # Green grass
    
    # Path tiles (brown/dirt)
    'path_h': 'towerDefense_tile050.png',   # Horizontal path
    'path_v': 'towerDefense_tile001.png',   # Vertical path
    'path_tl': 'towerDefense_tile002.png',  # Top-left corner
    'path_tr': 'towerDefense_tile003.png',  # Top-right corner
    'path_bl': 'towerDefense_tile004.png',  # Bottom-left corner
    'path_br': 'towerDefense_tile005.png',  # Bottom-right corner
    
    # Tower placement spots (light green squares)
    'spot': 'towerDefense_tile162.png',  # Tower placement spot
    
    # Obstacles
    'rock': 'towerDefense_tile136.png',  # Rock obstacle
    'bush': 'towerDefense_tile133.png',  # Bush/tree
    'tree': 'towerDefense_tile130.png',  # Tree
    
    # Tower components  
    'tower_base': 'towerDefense_tile181.png',  # Tower base (gray/silver)
    'tower_red': 'towerDefense_tile206.png',   # Red cannon tower
    
    # Enemy units
    'enemy_tank': 'towerDefense_tile245.png',  # Red tank
    
    # UI/Other
    'target': 'towerDefense_tile015.png',  # Target/crosshair
}

# Looking at the new sample image, it appears to be about 12x9 tiles
# The path forms a rectangular loop around the edges
# Green grass fills the center and corners

# Grid layout based on the new sample (12 columns x 9 rows)
grid = [
    # Row 0 - top edge
    ['g', 'g', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'p1', 'g'],
    # Row 1
    ['g', 'g', 'pv', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'pv', 'g'],
    # Row 2
    ['g', 'p4', 'p3', 'g', 't', 't', 'g', 'g', 'g', 'g', 'pv', 'g'],
    # Row 3
    ['g', 'pv', 'g', 'g', 't', 't', 's', 's', 's', 'g', 'pv', 'g'],
    # Row 4 - middle row with tower
    ['g', 'pv', 'g', 'g', 'g', 'r', 'g', 'g', 'g', 'g', 'pv', 'g'],
    # Row 5
    ['g', 'pv', 'g', 't', 't', 'g', 'g', 'x', 'g', 's', 'pv', 'g'],
    # Row 6
    ['g', 'pv', 't', 't', 't', 'g', 'r', 'g', 'g', 's', 'pv', 'g'],
    # Row 7
    ['g', 'pv', 'g', 'g', 'g', 'g', 'g', 'g', 's', 'g', 'pv', 'g'],
    # Row 8 - bottom edge
    ['g', 'p2', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'p3', 'g'],
]

# Legend for grid codes
legend = {
    'g': 'grass',
    'ph': 'path_h',
    'pv': 'path_v',
    'p1': 'path_tl',
    'p2': 'path_tr', 
    'p3': 'path_bl',
    'p4': 'path_br',
    't': 'bush',  # Trees/bushes
    'r': 'rock',  # Rock
    's': 'spot',  # Tower placement spot (light green square)
    'x': 'target',  # Crosshair/target marker
}

def load_tile(tile_name):
    """Load a tile image from the assets"""
    if tile_name in tiles:
        tile_path = os.path.join(base_path, tiles[tile_name])
        if os.path.exists(tile_path):
            return Image.open(tile_path).convert('RGBA')
    return None

def create_tower_defense_map():
    """Create the tower defense map matching the new sample"""
    
    # Calculate image dimensions
    grid_width = 12
    grid_height = 9
    
    width = grid_width * TILE_SIZE
    height = grid_height * TILE_SIZE
    
    # Create the base image
    image = Image.new('RGBA', (width, height), (100, 150, 100, 255))
    
    # Draw the base grid
    for y, row in enumerate(grid):
        for x, cell_code in enumerate(row):
            if cell_code in legend:
                tile_name = legend[cell_code]
                tile = load_tile(tile_name)
                if tile:
                    image.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add the central tower (3x3 area in the center)
    # The tower appears to be at approximately position (5, 4)
    tower_x = 5
    tower_y = 4
    
    # Place light colored base tiles first (3x3 area)
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            spot_tile = load_tile('spot')
            if spot_tile:
                # Make these tiles lighter/sand colored
                image.paste(spot_tile, ((tower_x + dx) * TILE_SIZE, (tower_y + dy) * TILE_SIZE), spot_tile)
    
    # Place the tower base and red cannon in center
    tower_base = load_tile('tower_base')
    tower_red = load_tile('tower_red')
    
    if tower_base and tower_red:
        image.paste(tower_base, (tower_x * TILE_SIZE, tower_y * TILE_SIZE), tower_base)
        image.paste(tower_red, (tower_x * TILE_SIZE, tower_y * TILE_SIZE), tower_red)
    
    # Add enemy tanks on the path
    enemy_positions = [
        (0, 2),  # Left side tank
        (2, 0),  # Top tank  
        (10, 6),  # Right side tank
    ]
    
    for x, y in enemy_positions:
        enemy = load_tile('enemy_tank')
        if enemy:
            # Scale down slightly to fit on path
            enemy = enemy.resize((50, 50), Image.Resampling.LANCZOS)
            offset = (TILE_SIZE - 50) // 2
            image.paste(enemy, (x * TILE_SIZE + offset, y * TILE_SIZE + offset), enemy)
    
    # Add some visual enhancements
    draw = ImageDraw.Draw(image)
    
    # Add subtle grid lines for tower placement spots
    spot_positions = [(6, 3), (8, 3), (9, 5), (8, 5), (3, 7)]
    for x, y in spot_positions:
        left = x * TILE_SIZE
        top = y * TILE_SIZE
        right = left + TILE_SIZE
        bottom = top + TILE_SIZE
        # Draw light green outline
        draw.rectangle([left+2, top+2, right-2, bottom-2], outline=(150, 255, 150, 100), width=2)
    
    return image

if __name__ == "__main__":
    print("Creating tower defense map based on new sample...")
    map_image = create_tower_defense_map()
    map_image.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")
    print(f"Dimensions: {map_image.size}")