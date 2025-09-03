import os
from PIL import Image, ImageDraw, ImageFont

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

# The Sample.png is 918x515 pixels
# Analyzing it carefully, it appears to have around 15 tiles horizontally

# After careful analysis of the Sample.png image:
# - The brown path makes a snake-like pattern through the map
# - Green areas are grass
# - There's a gray/blue water area on the right side  
# - Sandy/desert area transitions between grass and water
# - Trees, rocks, and bushes are scattered as obstacles
# - Three main tower positions with different tower types
# - Enemy units on the path
# - Projectiles in flight
# - UI elements and labels

# Core tiles needed
tiles = {
    # Ground tiles
    'grass': 'towerDefense_tile024.png',
    'sand': 'towerDefense_tile093.png', 
    'water': 'towerDefense_tile070.png',
    
    # Path tiles (brown)
    'dirt': 'towerDefense_tile050.png',  # Plain dirt/path
    'path_h': 'towerDefense_tile050.png',  # Horizontal
    'path_v': 'towerDefense_tile001.png',  # Vertical
    'path_tl': 'towerDefense_tile002.png',  # Top-left corner
    'path_tr': 'towerDefense_tile003.png',  # Top-right corner
    'path_bl': 'towerDefense_tile004.png',  # Bottom-left corner
    'path_br': 'towerDefense_tile005.png',  # Bottom-right corner
    
    # Obstacles
    'rock1': 'towerDefense_tile135.png',
    'rock2': 'towerDefense_tile136.png',
    'tree': 'towerDefense_tile130.png',
    'bush': 'towerDefense_tile133.png',
    
    # Tower components
    'base_gray': 'towerDefense_tile180.png',
    'base_red': 'towerDefense_tile181.png',
    'tower1': 'towerDefense_tile249.png',  # Green base turret
    'tower2': 'towerDefense_tile206.png',  # Red cannon
    'tower3': 'towerDefense_tile250.png',  # Missile launcher
    
    # Units
    'enemy1': 'towerDefense_tile245.png',
    'enemy2': 'towerDefense_tile246.png',
    
    # Projectiles  
    'missile': 'towerDefense_tile251.png',
    'bullet': 'towerDefense_tile272.png',
    
    # Collectibles
    'coin': 'towerDefense_tile274.png',
}

# Recreating the exact grid from Sample.png
# Looking tile by tile at the sample image:
# The map is approximately 15 tiles wide and 8 tiles tall
grid_map = """
..RR........T..
......xSSSSx.T.
......L....L...
....T.L....LSss
xxSSSS'....LSss
bL.........ySSs
.L.............
.ySSSSSSSSSSSSs
"""

# More detailed grid with all elements
# Analyzing Sample.png more carefully:
detailed_grid = [
    # Row 0
    ['grass', 'grass', 'rock1', 'rock2', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'tree', 'grass'],
    # Row 1 - path starts
    ['grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'path_br', 'path_h', 'path_h', 'path_h', 'path_tl', 'grass', 'tree', 'grass'],
    # Row 2
    ['tree', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'path_v', 'grass', 'grass', 'grass', 'path_v', 'grass', 'grass', 'grass'],
    # Row 3
    ['grass', 'grass', 'grass', 'grass', 'tree', 'grass', 'grass', 'path_v', 'grass', 'grass', 'grass', 'path_v', 'grass', 'sand', 'sand'],
    # Row 4 - path turns left
    ['grass', 'grass', 'path_br', 'path_h', 'path_h', 'path_h', 'path_h', 'path_bl', 'grass', 'grass', 'grass', 'path_v', 'sand', 'sand', 'sand'],
    # Row 5
    ['bush', 'grass', 'path_v', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'path_tr', 'path_h', 'sand', 'sand'],
    # Row 6
    ['grass', 'grass', 'path_v', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'grass', 'sand', 'water'],
    # Row 7 - path exits right
    ['grass', 'grass', 'path_tr', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'path_h', 'sand', 'water'],
]

def load_tile(tile_name):
    """Load a tile image from the assets"""
    if tile_name in tiles:
        tile_path = os.path.join(base_path, tiles[tile_name])
        if os.path.exists(tile_path):
            return Image.open(tile_path).convert('RGBA')
    
    # Return placeholder if not found
    placeholder = Image.new('RGBA', (64, 64), (255, 0, 255, 255))
    return placeholder

def create_accurate_map():
    """Create an accurate recreation of Sample.png"""
    
    # Match Sample.png dimensions
    width = 918
    height = 515
    
    # Create the base image with the blue-gray background
    image = Image.new('RGBA', (width, height), (58, 110, 165, 255))
    
    # Calculate tile dimensions to fit the grid
    tile_width = 61  # 918 / 15 = 61.2
    tile_height = 64  # 515 / 8 = 64.375
    
    # Draw the base grid
    for y, row in enumerate(detailed_grid):
        for x, tile_type in enumerate(row):
            tile = load_tile(tile_type)
            if tile:
                # Resize to fit grid
                tile_resized = tile.resize((tile_width, tile_height), Image.Resampling.LANCZOS)
                image.paste(tile_resized, (x * tile_width, y * tile_height), tile_resized)
    
    # Add game elements on top of the base grid
    
    # Place towers at strategic positions
    tower_positions = [
        (2, 5, 'base_gray', 'tower1'),   # Left tower
        (7, 2, 'base_red', 'tower2'),    # Center tower  
        (11, 5, 'base_gray', 'tower3'),  # Right tower
    ]
    
    for x, y, base_type, tower_type in tower_positions:
        base = load_tile(base_type)
        tower = load_tile(tower_type)
        base_resized = base.resize((tile_width, tile_height), Image.Resampling.LANCZOS)
        tower_resized = tower.resize((tile_width, tile_height), Image.Resampling.LANCZOS)
        
        pos_x = x * tile_width
        pos_y = y * tile_height
        
        image.paste(base_resized, (pos_x, pos_y), base_resized)
        image.paste(tower_resized, (pos_x, pos_y), tower_resized)
    
    # Add enemies on the path
    enemy_positions = [
        (7, 4, 'enemy1'),   # Tank on path
        (2, 6, 'enemy2'),   # Soldier on path
    ]
    
    for x, y, enemy_type in enemy_positions:
        enemy = load_tile(enemy_type)
        enemy_resized = enemy.resize((50, 50), Image.Resampling.LANCZOS)
        pos_x = x * tile_width + 5
        pos_y = y * tile_height + 7
        image.paste(enemy_resized, (pos_x, pos_y), enemy_resized)
    
    # Add projectiles
    projectile_positions = [
        (3, 5, 'missile'),
        (8, 3, 'bullet'),
    ]
    
    for x, y, proj_type in projectile_positions:
        proj = load_tile(proj_type)
        proj_resized = proj.resize((30, 30), Image.Resampling.LANCZOS)
        pos_x = x * tile_width + 15
        pos_y = y * tile_height + 17
        image.paste(proj_resized, (pos_x, pos_y), proj_resized)
    
    # Add coins
    coin_positions = [(3, 6), (4, 6), (5, 6)]
    
    for x, y in coin_positions:
        coin = load_tile('coin')
        coin_resized = coin.resize((25, 25), Image.Resampling.LANCZOS)
        pos_x = x * tile_width + 18
        pos_y = y * tile_height + 20
        image.paste(coin_resized, (pos_x, pos_y), coin_resized)
    
    # Add UI text
    draw = ImageDraw.Draw(image)
    
    try:
        font_small = ImageFont.truetype("arial.ttf", 12)
        font_large = ImageFont.truetype("arial.ttf", 20)
    except:
        font_small = ImageFont.load_default()
        font_large = font_small
    
    # CC0 1.0 label
    draw.text((20, height - 30), "CC0 1.0", fill=(255, 255, 255, 255), font=font_large)
    
    # Disclaimer text
    draw.text((110, height - 30), 
              "This content is free to use in personal, educational and commercial projects.",
              fill=(255, 255, 255, 200), font=font_small)
    draw.text((110, height - 15),
              "No need to ask permission. Support us by crediting, this is not mandatory.",
              fill=(255, 255, 255, 200), font=font_small)
    
    # KENNEY label
    draw.text((width - 100, height - 35), "KENNEY", fill=(255, 255, 255, 255), font=font_large)
    
    return image

if __name__ == "__main__":
    print("Creating accurate tower defense map...")
    map_image = create_accurate_map()
    map_image.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")
    print(f"Dimensions: {map_image.size}")