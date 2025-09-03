import os
from PIL import Image, ImageDraw, ImageFont
import numpy as np

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
sample_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample.png"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

# The Sample.png appears to be 918x515 pixels
# Looking at the grid, it seems to be approximately 15 columns x 8 rows
# With UI on the right side

TILE_SIZE = 64  # Standard tile size

# Based on careful observation of Sample.png, here's the exact layout:
# The path is brown/dirt colored and forms a winding pattern
# Green is grass, gray is water/stone, sand/tan is desert area
# Towers are placed on specific positions with red/gray coloring

# Exact tile mapping based on visual inspection and prompt requirements
tiles = {
    # Grass and ground
    'grass': 'towerDefense_tile024.png',  # Green grass
    
    # Path tiles (brown/dirt)
    'path_h': 'towerDefense_tile050.png',   # Horizontal path
    'path_v': 'towerDefense_tile001.png',   # Vertical path
    'path_corner_tl': 'towerDefense_tile002.png',  # Top-left corner
    'path_corner_tr': 'towerDefense_tile003.png',  # Top-right corner  
    'path_corner_bl': 'towerDefense_tile004.png',  # Bottom-left corner
    'path_corner_br': 'towerDefense_tile005.png',  # Bottom-right corner
    
    # Sand/desert
    'sand': 'towerDefense_tile093.png',
    
    # Water tiles
    'water': 'towerDefense_tile070.png',
    'water_edge': 'towerDefense_tile071.png',
    
    # Obstacles
    'rock1': 'towerDefense_tile135.png',
    'rock2': 'towerDefense_tile136.png',
    'tree1': 'towerDefense_tile130.png',
    'tree2': 'towerDefense_tile131.png',
    'tree_big': 'towerDefense_tile132.png',
    'bush': 'towerDefense_tile133.png',
    
    # Towers and weapons
    'tower_base': 'towerDefense_tile180.png',
    'tower_gun': 'towerDefense_tile249.png',
    'tower_cannon': 'towerDefense_tile206.png',
    'tower_missile': 'towerDefense_tile250.png',
    
    # Enemies
    'enemy_tank': 'towerDefense_tile245.png',
    'enemy_soldier': 'towerDefense_tile246.png',
    
    # Projectiles
    'missile': 'towerDefense_tile251.png',
    'bullet': 'towerDefense_tile272.png',
    
    # UI and pickups
    'coin': 'towerDefense_tile274.png',
}

# Exact grid layout matching Sample.png (15x8 grid)
# Looking at Sample.png tile by tile:
grid = [
    # Row 0 (top)
    ['g', 'g', 'r1', 'r2', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 't2', 'g'],
    # Row 1 - path enters from top
    ['g', 'g', 'g', 'g', 'g', 'g', 'g', 'pc4', 'ph', 'ph', 'ph', 'pc1', 'g', 'tb', 'g'],
    # Row 2
    ['t2', 'g', 'g', 'g', 'g', 'g', 'g', 'pv', 'g', 'g', 'g', 'pv', 'g', 'g', 'g'],
    # Row 3
    ['g', 'g', 'g', 'g', 'tb', 'g', 'g', 'pv', 'g', 'g', 'g', 'pv', 's', 's', 's'],
    # Row 4 - path turns
    ['g', 'g', 'pc4', 'ph', 'ph', 'ph', 'ph', 'pc3', 'g', 'g', 'g', 'pv', 's', 's', 's'],
    # Row 5
    ['b', 'g', 'pv', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'pc2', 'ph', 's', 's'],
    # Row 6
    ['g', 'g', 'pv', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 's', 's'],
    # Row 7 - path continues
    ['g', 'g', 'pc2', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 'ph', 's', 's'],
]

# Legend for grid codes
legend = {
    'g': 'grass',
    'ph': 'path_h',
    'pv': 'path_v',
    'pc1': 'path_corner_tl',
    'pc2': 'path_corner_tr',
    'pc3': 'path_corner_bl',
    'pc4': 'path_corner_br',
    's': 'sand',
    'w': 'water',
    'we': 'water_edge',
    'r1': 'rock1',
    'r2': 'rock2',
    't1': 'tree1',
    't2': 'tree2',
    'tb': 'tree_big',
    'b': 'bush',
}

def load_tile(tile_name):
    """Load a tile image"""
    if tile_name in tiles:
        tile_path = os.path.join(base_path, tiles[tile_name])
    else:
        return None
    
    if os.path.exists(tile_path):
        return Image.open(tile_path).convert('RGBA')
    else:
        print(f"Warning: Tile {tile_name} not found at {tile_path}")
        return Image.new('RGBA', (TILE_SIZE, TILE_SIZE), (255, 0, 255, 255))

def create_exact_recreation():
    """Create exact recreation of Sample.png"""
    
    # Create the full image (918x515 to match sample)
    full_width = 918
    full_height = 515
    
    full_image = Image.new('RGBA', (full_width, full_height), (58, 110, 165, 255))  # Blue-gray background
    
    # Draw the grid tiles
    for y, row in enumerate(grid):
        for x, cell_code in enumerate(row):
            if cell_code in legend:
                tile_name = legend[cell_code]
                tile = load_tile(tile_name)
                if tile:
                    # Resize tile to fit the grid properly
                    tile = tile.resize((61, 64), Image.Resampling.LANCZOS)
                    full_image.paste(tile, (x * 61, y * 64), tile)
    
    # Add specific game elements on top
    
    # Towers (with bases)
    tower_positions = [
        (2, 5, 'tower_gun'),      # Left tower on path
        (7, 2, 'tower_cannon'),   # Center tower
        (11, 5, 'tower_missile'),  # Right tower
    ]
    
    for x, y, tower_type in tower_positions:
        base = load_tile('tower_base')
        tower = load_tile(tower_type)
        if base and tower:
            base = base.resize((61, 64), Image.Resampling.LANCZOS)
            tower = tower.resize((61, 64), Image.Resampling.LANCZOS)
            full_image.paste(base, (x * 61, y * 64), base)
            full_image.paste(tower, (x * 61, y * 64), tower)
    
    # Enemies on path
    enemies = [
        (7, 4, 'enemy_tank'),
        (2, 6, 'enemy_soldier'),
    ]
    
    for x, y, enemy_type in enemies:
        enemy = load_tile(enemy_type)
        if enemy:
            enemy = enemy.resize((50, 50), Image.Resampling.LANCZOS)
            full_image.paste(enemy, (x * 61 + 5, y * 64 + 7), enemy)
    
    # Projectiles
    projectiles = [
        (3, 5, 'missile'),
        (7, 3, 'bullet'),
    ]
    
    for x, y, proj_type in projectiles:
        proj = load_tile(proj_type)
        if proj:
            proj = proj.resize((30, 30), Image.Resampling.LANCZOS)
            full_image.paste(proj, (x * 61 + 15, y * 64 + 17), proj)
    
    # Coins
    coins = [(3, 6), (4, 6), (5, 6)]
    
    for x, y in coins:
        coin = load_tile('coin')
        if coin:
            coin = coin.resize((25, 25), Image.Resampling.LANCZOS)
            full_image.paste(coin, (x * 61 + 18, y * 64 + 20), coin)
    
    # Add UI text and labels
    draw = ImageDraw.Draw(full_image)
    
    # Try to load font
    try:
        font = ImageFont.truetype("arial.ttf", 14)
        font_large = ImageFont.truetype("arial.ttf", 20)
    except:
        font = ImageFont.load_default()
        font_large = font
    
    # Add "CC0 1.0" at bottom left
    draw.text((20, full_height - 30), "CC0 1.0", fill=(255, 255, 255, 255), font=font)
    
    # Add "KENNEY" at bottom right
    draw.text((full_width - 100, full_height - 35), "KENNEY", fill=(255, 255, 255, 255), font=font_large)
    
    # Add small disclaimer text
    disclaimer = "This content is free to use in personal, educational and commercial projects."
    draw.text((110, full_height - 30), disclaimer, fill=(255, 255, 255, 200), font=font)
    draw.text((110, full_height - 15), "No need to ask permission. Support us by crediting, this is not mandatory.",
              fill=(255, 255, 255, 200), font=font)
    
    return full_image

if __name__ == "__main__":
    print("Creating exact recreation of Sample.png...")
    recreation = create_exact_recreation()
    recreation.save(output_path, 'PNG')
    print(f"Saved to: {output_path}")
    print(f"Image size: {recreation.size}")