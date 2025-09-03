import os
from PIL import Image, ImageDraw, ImageFont

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

# Looking at the sample, the path tiles and layout need adjustment
tile_mapping = {
    'grass': 'towerDefense_tile024.png',
    'path_h': 'towerDefense_tile050.png',
    'path_v': 'towerDefense_tile001.png',
    'path_tl': 'towerDefense_tile002.png', 
    'path_tr': 'towerDefense_tile003.png',
    'path_bl': 'towerDefense_tile004.png',
    'path_br': 'towerDefense_tile005.png',
    'sand': 'towerDefense_tile093.png',
    'water': 'towerDefense_tile070.png',
    'water_edge': 'towerDefense_tile071.png',
    'rock1': 'towerDefense_tile135.png',
    'rock2': 'towerDefense_tile136.png', 
    'tree1': 'towerDefense_tile130.png',
    'tree2': 'towerDefense_tile131.png',
    'tree_big': 'towerDefense_tile132.png',
    'bush': 'towerDefense_tile133.png',
    'tower_base': 'towerDefense_tile180.png',
    'tower_top': 'towerDefense_tile249.png',
    'tower_red': 'towerDefense_tile206.png',
    'tower_missile': 'towerDefense_tile250.png',
    'enemy1': 'towerDefense_tile245.png',
    'enemy2': 'towerDefense_tile246.png',
    'missile': 'towerDefense_tile252.png',
    'coin': 'towerDefense_tile274.png',
    'crater': 'towerDefense_tile019.png',
    'target': 'towerDefense_tile180.png',
}

# Recreating the exact layout from Sample.png
# The path creates an S-shape from top to bottom
grid_layout = [
    # Row 0
    ['g','g','r1','r2','g','g','g','g','g','g','g','g','g','g','g','t2','g'],
    # Row 1 - path starts here from right
    ['g','g','g','g','g','g','g','g','g','p4','ph','ph','ph','p1','g','tr','g'],
    # Row 2
    ['g','g','g','g','g','g','r1','g','g','pv','g','g','g','pv','g','g','g'],
    # Row 3
    ['tr','g','g','g','g','g','g','g','g','pv','g','g','g','pv','g','s','s'],
    # Row 4
    ['g','g','g','g','g','g','g','g','g','pv','g','g','g','pv','g','s','s'],
    # Row 5 - path turns left
    ['g','g','g','p4','ph','ph','ph','ph','ph','p3','g','g','g','pv','g','s','s'],
    # Row 6
    ['g','b','g','pv','g','g','g','g','g','g','g','g','g','pv','s','s','s'],
    # Row 7
    ['g','g','g','pv','g','g','g','g','g','g','g','g','g','p2','ph','ph','s'],
    # Row 8
    ['g','g','g','pv','g','g','g','g','g','g','g','g','g','g','g','s','s'],
    # Row 9 - path turns right
    ['g','g','g','p2','ph','p1','g','g','g','g','g','we','w','w','w','w','s'],
    # Row 10
    ['t1','g','g','g','g','pv','g','g','g','g','we','w','w','r1','w','w','s'],
    # Row 11 - path continues right
    ['g','g','g','g','g','p2','ph','ph','ph','ph','ph','w','w','w','w','w','s'],
    # Row 12
    ['g','g','b','g','g','g','g','g','g','g','s','w','w','w','w','w','s'],
    # Row 13
    ['g','g','g','g','g','g','g','g','g','s','s','s','s','s','s','s','s'],
]

def load_tile(tile_name):
    """Load a tile image from the assets folder"""
    if tile_name in tile_mapping:
        tile_path = os.path.join(base_path, tile_mapping[tile_name])
    else:
        tile_path = os.path.join(base_path, f"towerDefense_{tile_name}.png")
    
    if os.path.exists(tile_path):
        return Image.open(tile_path).convert('RGBA')
    else:
        print(f"Warning: Tile {tile_name} not found")
        return Image.new('RGBA', (TILE_SIZE, TILE_SIZE), (255, 0, 255, 255))

def create_game_map():
    """Create the tower defense game map exactly matching Sample.png"""
    
    grid_width = 17  # Match sample width
    grid_height = 14  # Match sample height
    
    # Total width includes UI space
    total_width = int(grid_width * TILE_SIZE * 1.15)  # Extra space for UI
    total_height = grid_height * TILE_SIZE
    
    # Create the full image
    full_image = Image.new('RGBA', (total_width, total_height), (100, 100, 100, 255))
    
    # Create game map area
    game_map = Image.new('RGBA', (grid_width * TILE_SIZE, grid_height * TILE_SIZE), (100, 150, 100, 255))
    
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
        'we': 'water_edge',
        'r1': 'rock1',
        'r2': 'rock2',
        't1': 'tree1',
        't2': 'tree2',
        'tr': 'tree_big',
        'b': 'bush',
    }
    
    # Draw base tiles
    for y, row in enumerate(grid_layout):
        for x in range(len(row)):
            if x < len(row):
                cell_code = row[x]
                if cell_code in tile_codes:
                    tile_name = tile_codes[cell_code]
                    tile = load_tile(tile_name)
                    game_map.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add towers with bases - matching sample positions
    towers = [
        (3, 6, 'tower_base', 'tower_top'),     # Left tower
        (9, 3, 'tower_base', 'tower_red'),     # Center tower  
        (13, 7, 'tower_base', 'tower_missile'), # Right tower
    ]
    
    for x, y, base_type, top_type in towers:
        base_tile = load_tile(base_type)
        top_tile = load_tile(top_type)
        game_map.paste(base_tile, (x * TILE_SIZE, y * TILE_SIZE), base_tile)
        game_map.paste(top_tile, (x * TILE_SIZE, y * TILE_SIZE), top_tile)
    
    # Add enemies on the path
    enemies = [
        (9, 5, 'enemy1'),  # Enemy on vertical path
        (3, 8, 'enemy2'),  # Enemy on path
    ]
    
    for x, y, enemy_type in enemies:
        enemy_tile = load_tile(enemy_type)
        game_map.paste(enemy_tile, (x * TILE_SIZE, y * TILE_SIZE), enemy_tile)
    
    # Add projectiles
    projectiles = [
        (4, 6, 'missile'),  # Missile from left tower
        (10, 4, 'missile'), # Missile from center tower
    ]
    
    for x, y, proj_type in projectiles:
        proj_tile = load_tile(proj_type)
        game_map.paste(proj_tile, (x * TILE_SIZE, y * TILE_SIZE), proj_tile)
    
    # Add coins
    coins = [
        (4, 7), (5, 7), (4, 8)
    ]
    
    for x, y in coins:
        coin_tile = load_tile('coin')
        coin_small = coin_tile.resize((32, 32), Image.Resampling.LANCZOS)
        game_map.paste(coin_small, (x * TILE_SIZE + 16, y * TILE_SIZE + 16), coin_small)
    
    # Paste game map onto full image
    full_image.paste(game_map, (0, 0))
    
    # Add UI elements on the right side
    draw = ImageDraw.Draw(full_image)
    ui_x = grid_width * TILE_SIZE + 20
    
    # Try to load a font
    try:
        font = ImageFont.truetype("arial.ttf", 20)
        font_large = ImageFont.truetype("arial.ttf", 24)
    except:
        font = ImageFont.load_default()
        font_large = font
    
    # Add "KENNEY" text at bottom right
    draw.text((ui_x - 10, total_height - 60), "KENNEY", fill=(255, 255, 255, 255), font=font_large)
    
    # Add CC0 1.0 text at bottom left
    draw.text((20, total_height - 40), "CC0 1.0", fill=(255, 255, 255, 255), font=font)
    
    return full_image

if __name__ == "__main__":
    print("Generating final tower defense map...")
    game_map = create_game_map()
    game_map.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")