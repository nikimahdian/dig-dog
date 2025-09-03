import os
from PIL import Image, ImageDraw, ImageFont
import math

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

# More accurate tile mapping based on visual analysis
tile_mapping = {
    'grass': 'towerDefense_tile024.png',
    'path_h': 'towerDefense_tile050.png',  # Horizontal brown path
    'path_v': 'towerDefense_tile001.png',  # Vertical brown path  
    'path_tl': 'towerDefense_tile002.png',  # Top-left corner
    'path_tr': 'towerDefense_tile003.png',  # Top-right corner
    'path_bl': 'towerDefense_tile004.png',  # Bottom-left corner
    'path_br': 'towerDefense_tile005.png',  # Bottom-right corner
    'sand': 'towerDefense_tile093.png',
    'water': 'towerDefense_tile070.png',
    'rock1': 'towerDefense_tile135.png',
    'rock2': 'towerDefense_tile136.png',
    'tree1': 'towerDefense_tile130.png',
    'tree2': 'towerDefense_tile131.png',
    'tree3': 'towerDefense_tile132.png',
    'bush': 'towerDefense_tile133.png',
    'tower_base': 'towerDefense_tile181.png',
    'tower_cannon': 'towerDefense_tile206.png',
    'tower_missile': 'towerDefense_tile250.png',
    'tower_gun': 'towerDefense_tile249.png',
    'enemy_tank': 'towerDefense_tile245.png',
    'enemy_soldier': 'towerDefense_tile246.png',
    'missile': 'towerDefense_tile251.png',
    'bullet': 'towerDefense_tile272.png',
    'coin': 'towerDefense_tile274.png',
    'crater': 'towerDefense_tile019.png',
}

# More accurate grid layout based on Sample.png
# g=grass, ph=path horizontal, pv=path vertical, p1-p4=corners
# s=sand, w=water, r=rock, t=tree, b=bush
grid_layout = [
    ['g','g','r1','r2','g','g','g','g','g','g','g','g','g','t1','g'],
    ['g','g','g','g','g','g','g','g','p4','ph','ph','ph','p1','g','g'],
    ['g','g','g','g','g','g','r1','g','pv','g','g','g','pv','g','t2'],
    ['t3','g','g','g','g','g','g','g','pv','g','c1','g','pv','g','g'],
    ['g','g','g','g','g','g','g','g','pv','g','g','g','pv','g','g'],
    ['g','g','p4','ph','ph','ph','ph','ph','p3','g','g','g','pv','g','g'],
    ['g','b','pv','g','g','g','g','g','g','g','g','g','pv','s','s'],
    ['g','g','pv','g','r2','g','g','g','g','g','g','g','p2','ph','ph'],
    ['g','g','pv','g','g','g','g','g','g','g','g','g','g','g','s'],
    ['g','g','p2','ph','p1','g','g','g','g','g','w','w','w','w','s'],
    ['t1','g','g','g','pv','g','g','g','g','w','w','w','r1','w','s'],
    ['g','g','g','g','p2','ph','ph','ph','ph','w','w','w','w','w','s'],
    ['g','g','b','g','g','g','g','g','g','s','w','w','w','w','s'],
    ['g','g','g','g','g','g','g','g','g','s','s','s','s','s','s'],
]

def load_tile(tile_name):
    """Load a tile image from the assets folder"""
    if tile_name in tile_mapping:
        tile_path = os.path.join(base_path, tile_mapping[tile_name])
    else:
        # Handle direct tile references
        tile_path = os.path.join(base_path, f"towerDefense_{tile_name}.png")
    
    if os.path.exists(tile_path):
        return Image.open(tile_path).convert('RGBA')
    else:
        print(f"Warning: Tile {tile_name} ({tile_path}) not found")
        # Return magenta placeholder
        placeholder = Image.new('RGBA', (TILE_SIZE, TILE_SIZE), (255, 0, 255, 255))
        return placeholder

def create_game_map():
    """Create the tower defense game map matching Sample.png"""
    
    grid_width = len(grid_layout[0])
    grid_height = len(grid_layout)
    
    # Create main map area
    map_width = grid_width * TILE_SIZE
    map_height = grid_height * TILE_SIZE
    
    game_map = Image.new('RGBA', (map_width, map_height), (100, 150, 100, 255))
    
    # Tile code mapping
    tile_codes = {
        'g': 'grass',
        'ph': 'path_h',
        'pv': 'path_v',
        'p1': 'path_tl',
        'p2': 'path_tr', 
        'p3': 'path_bl',
        'p4': 'path_br',
        's': 'sand',
        'w': 'water',
        'r1': 'rock1',
        'r2': 'rock2',
        't1': 'tree1',
        't2': 'tree2',
        't3': 'tree3',
        'b': 'bush',
        'c1': 'crater',
    }
    
    # Draw base tiles
    for y, row in enumerate(grid_layout):
        for x, cell_code in enumerate(row):
            if cell_code in tile_codes:
                tile_name = tile_codes[cell_code]
                tile = load_tile(tile_name)
                game_map.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add towers at specific positions (matching Sample.png)
    towers = [
        (2, 5, 'tower_gun'),     # Left path tower
        (8, 3, 'tower_cannon'),  # Center tower
        (12, 7, 'tower_missile'), # Right path tower
    ]
    
    for x, y, tower_type in towers:
        # Place tower base first
        base_tile = load_tile('tower_base')
        game_map.paste(base_tile, (x * TILE_SIZE, y * TILE_SIZE), base_tile)
        # Place tower on top
        tower_tile = load_tile(tower_type)
        game_map.paste(tower_tile, (x * TILE_SIZE, y * TILE_SIZE), tower_tile)
    
    # Add enemies on the path
    enemies = [
        (8, 5, 'enemy_tank'),    # Enemy on path
        (2, 7, 'enemy_soldier'), # Enemy on left path
    ]
    
    for x, y, enemy_type in enemies:
        enemy_tile = load_tile(enemy_type)
        game_map.paste(enemy_tile, (x * TILE_SIZE, y * TILE_SIZE), enemy_tile)
    
    # Add projectiles
    projectiles = [
        (3, 5, 'bullet'),
        (8, 4, 'missile'),
    ]
    
    for x, y, proj_type in projectiles:
        proj_tile = load_tile(proj_type)
        # Center projectiles in their tiles
        offset = TILE_SIZE // 4
        game_map.paste(proj_tile, (x * TILE_SIZE + offset, y * TILE_SIZE + offset), proj_tile)
    
    # Add coins
    coins = [
        (3, 7), (4, 7), (3, 8)
    ]
    
    for x, y in coins:
        coin_tile = load_tile('coin')
        coin_small = coin_tile.resize((TILE_SIZE // 2, TILE_SIZE // 2), Image.Resampling.LANCZOS)
        offset = TILE_SIZE // 4
        game_map.paste(coin_small, (x * TILE_SIZE + offset, y * TILE_SIZE + offset), coin_small)
    
    return game_map

if __name__ == "__main__":
    print("Generating tower defense map (v2)...")
    game_map = create_game_map()
    
    # Save the map
    game_map.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")