import os
from PIL import Image
import numpy as np

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

tile_mapping = {
    'grass': 'towerDefense_tile024.png',
    'path_straight_h': 'towerDefense_tile050.png',
    'path_straight_v': 'towerDefense_tile001.png',
    'path_corner_tl': 'towerDefense_tile002.png',
    'path_corner_tr': 'towerDefense_tile003.png',
    'path_corner_bl': 'towerDefense_tile004.png',
    'path_corner_br': 'towerDefense_tile005.png',
    'sand': 'towerDefense_tile093.png',
    'water': 'towerDefense_tile070.png',
    'water_edge_l': 'towerDefense_tile071.png',
    'water_edge_r': 'towerDefense_tile072.png',
    'water_edge_t': 'towerDefense_tile073.png',
    'water_edge_b': 'towerDefense_tile074.png',
    'water_corner_tl': 'towerDefense_tile046.png',
    'water_corner_tr': 'towerDefense_tile047.png',
    'water_corner_bl': 'towerDefense_tile069.png',
    'water_corner_br': 'towerDefense_tile048.png',
    'tower_base': 'towerDefense_tile181.png',
    'tower_gun': 'towerDefense_tile249.png',
    'tower_missile': 'towerDefense_tile250.png',
    'tower_cannon': 'towerDefense_tile206.png',
    'rock1': 'towerDefense_tile135.png',
    'rock2': 'towerDefense_tile136.png',
    'tree1': 'towerDefense_tile130.png',
    'tree2': 'towerDefense_tile131.png',
    'tree3': 'towerDefense_tile132.png',
    'bush': 'towerDefense_tile133.png',
    'projectile_missile': 'towerDefense_tile251.png',
    'projectile_bullet': 'towerDefense_tile272.png',
    'projectile_rocket': 'towerDefense_tile252.png',
    'coin': 'towerDefense_tile274.png',
    'enemy1': 'towerDefense_tile245.png',
    'enemy2': 'towerDefense_tile246.png',
    'enemy3': 'towerDefense_tile247.png',
    'path_end': 'towerDefense_tile007.png',
    'path_start': 'towerDefense_tile008.png',
}

grid_layout = [
    ['g','g','r1','r2','g','g','g','g','g','g','g','g','g','g','g'],
    ['g','g','g','g','g','g','ph','ph','ph','pc1','g','g','t1','g','g'],
    ['t2','g','g','g','g','g','pv','g','g','pv','g','g','g','g','g'],
    ['g','g','t3','g','g','g','pv','g','g','pc2','ph','ph','pc1','g','g'],
    ['g','g','g','g','g','g','pv','g','g','g','g','g','pv','g','g'],
    ['g','pc4','ph','ph','ph','ph','pc3','g','g','g','g','g','pv','b','g'],
    ['g','pv','g','g','g','g','g','g','g','g','g','g','pv','g','s'],
    ['g','pv','g','g','tc','g','g','g','tm','g','g','g','pc2','ph','s'],
    ['g','pv','g','b','g','g','g','g','g','g','g','g','g','g','s'],
    ['g','pc2','ph','ph','pc1','g','g','tg','g','g','we','w','w','w','wc2'],
    ['g','g','g','g','pv','g','g','g','g','g','we','w','w','w','s'],
    ['t1','g','g','g','pc2','ph','ph','ph','ph','ph','wc3','w','w','w','s'],
    ['g','g','g','g','g','g','g','g','g','g','wb','wc4','w','w','s'],
    ['g','g','g','g','g','g','g','g','g','g','g','s','s','s','s'],
    ['g','g','g','g','g','g','g','g','g','g','g','s','s','s','s'],
]

towers = [
    (6, 7, 'tower_cannon'),
    (8, 7, 'tower_missile'),
    (7, 9, 'tower_gun'),
]

enemies = [
    (6, 1, 'enemy1'),
    (9, 3, 'enemy2'),
]

projectiles = [
    (6, 6, 'projectile_bullet'),
    (8, 8, 'projectile_missile'),
]

coins = [
    (3, 8),
    (4, 8),
    (3, 9),
]

tile_codes = {
    'g': 'grass',
    'ph': 'path_straight_h',
    'pv': 'path_straight_v',
    'pc1': 'path_corner_tl',
    'pc2': 'path_corner_tr',
    'pc3': 'path_corner_bl',
    'pc4': 'path_corner_br',
    's': 'sand',
    'w': 'water',
    'we': 'water_edge_l',
    'wr': 'water_edge_r',
    'wt': 'water_edge_t',
    'wb': 'water_edge_b',
    'wc1': 'water_corner_tl',
    'wc2': 'water_corner_tr',
    'wc3': 'water_corner_bl',
    'wc4': 'water_corner_br',
    'r1': 'rock1',
    'r2': 'rock2',
    't1': 'tree1',
    't2': 'tree2',
    't3': 'tree3',
    'b': 'bush',
    'tg': 'tower_gun',
    'tm': 'tower_missile',
    'tc': 'tower_cannon',
}

def load_tile(tile_name):
    """Load a tile image from the assets folder"""
    tile_path = os.path.join(base_path, tile_mapping[tile_name])
    if os.path.exists(tile_path):
        return Image.open(tile_path).convert('RGBA')
    else:
        print(f"Warning: Tile {tile_name} ({tile_path}) not found, using placeholder")
        placeholder = Image.new('RGBA', (TILE_SIZE, TILE_SIZE), (255, 0, 255, 255))
        return placeholder

def create_game_map():
    """Create the tower defense game map"""
    grid_width = len(grid_layout[0])
    grid_height = len(grid_layout)
    
    map_width = grid_width * TILE_SIZE + 200
    map_height = grid_height * TILE_SIZE
    
    game_map = Image.new('RGBA', (map_width, map_height), (100, 100, 100, 255))
    
    for y, row in enumerate(grid_layout):
        for x, cell_code in enumerate(row):
            if cell_code in tile_codes:
                tile_name = tile_codes[cell_code]
                tile = load_tile(tile_name)
                game_map.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    for x, y, enemy_type in enemies:
        enemy_tile = load_tile(enemy_type)
        game_map.paste(enemy_tile, (x * TILE_SIZE, y * TILE_SIZE), enemy_tile)
    
    for x, y, proj_type in projectiles:
        proj_tile = load_tile(proj_type)
        game_map.paste(proj_tile, (x * TILE_SIZE, y * TILE_SIZE), proj_tile)
    
    for x, y in coins:
        coin_tile = load_tile('coin')
        offset_x = x * TILE_SIZE + TILE_SIZE // 4
        offset_y = y * TILE_SIZE + TILE_SIZE // 4
        coin_small = coin_tile.resize((TILE_SIZE // 2, TILE_SIZE // 2), Image.Resampling.LANCZOS)
        game_map.paste(coin_small, (offset_x, offset_y), coin_small)
    
    from PIL import ImageDraw, ImageFont
    draw = ImageDraw.Draw(game_map)
    
    ui_x = grid_width * TILE_SIZE + 20
    ui_y = 50
    
    try:
        font = ImageFont.truetype("arial.ttf", 24)
    except:
        font = ImageFont.load_default()
    
    ui_text = [
        "HEALTH: 100",
        "COINS: 250",
        "ROUND: 3",
        "TIME: 2:45",
    ]
    
    for i, text in enumerate(ui_text):
        draw.text((ui_x, ui_y + i * 40), text, fill=(255, 255, 255, 255), font=font)
    
    return game_map

if __name__ == "__main__":
    print("Generating tower defense map...")
    game_map = create_game_map()
    game_map.save(output_path, 'PNG')
    print(f"Map saved to: {output_path}")