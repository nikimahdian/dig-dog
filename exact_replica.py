import os
from PIL import Image, ImageDraw

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

# After careful analysis of the sample image
tiles = {
    # Ground tiles
    'grass': 'towerDefense_tile024.png',  # Main green grass
    
    # Path tiles (brown/tan)
    'path_h': 'towerDefense_tile050.png',  # Horizontal path
    'path_v': 'towerDefense_tile001.png',  # Vertical path  
    'path_tl': 'towerDefense_tile002.png',  # Top-left corner
    'path_tr': 'towerDefense_tile003.png',  # Top-right corner
    'path_bl': 'towerDefense_tile004.png',  # Bottom-left corner
    'path_br': 'towerDefense_tile005.png',  # Bottom-right corner
    
    # Special tiles
    'light_base': 'towerDefense_tile092.png',  # Light tan/cream base
    'placement': 'towerDefense_tile268.png',  # Green placement square
    
    # Obstacles
    'bush': 'towerDefense_tile130.png',  # Small tree/bush
    'rock': 'towerDefense_tile137.png',  # Gray rock
    
    # Tower
    'tower_base': 'towerDefense_tile180.png',
    'tower': 'towerDefense_tile249.png',  # Tower with red/gray
    
    # Enemies
    'enemy_tank': 'towerDefense_tile270.png',  # Red tank
    'enemy_blue': 'towerDefense_tile244.png',  # Blue enemy
    
    # Effects
    'explosion': 'towerDefense_tile295.png',  # Explosion/exhaust
    'crosshair': 'towerDefense_tile015.png',  # Pink/purple crosshair
}

def load_tile(tile_name):
    """Load a tile image"""
    if tile_name in tiles:
        tile_path = os.path.join(base_path, tiles[tile_name])
        if os.path.exists(tile_path):
            return Image.open(tile_path).convert('RGBA')
    # Try direct tile number
    if isinstance(tile_name, str) and tile_name.isdigit():
        tile_path = os.path.join(base_path, f'towerDefense_tile{int(tile_name):03d}.png')
        if os.path.exists(tile_path):
            return Image.open(tile_path).convert('RGBA')
    return None

def create_exact_replica():
    """Create exact replica of the sample image"""
    
    # The sample appears to be about 12x9 tiles
    width = 12 * TILE_SIZE
    height = 9 * TILE_SIZE
    
    # Create base image with green background
    image = Image.new('RGBA', (width, height), (76, 209, 55, 255))
    
    # First, draw all grass tiles
    grass = load_tile('grass')
    if grass:
        for y in range(9):
            for x in range(12):
                image.paste(grass, (x * TILE_SIZE, y * TILE_SIZE), grass)
    
    # Draw the path - brown rectangular loop
    # Top horizontal path (row 1)
    for x in range(2, 11):
        if x == 2:
            tile = load_tile('path_br')
        elif x == 10:
            tile = load_tile('path_tl')
        else:
            tile = load_tile('path_h')
        if tile:
            image.paste(tile, (x * TILE_SIZE, 1 * TILE_SIZE), tile)
    
    # Bottom horizontal path (row 7)
    for x in range(2, 11):
        if x == 2:
            tile = load_tile('path_tr')
        elif x == 10:
            tile = load_tile('path_bl')
        else:
            tile = load_tile('path_h')
        if tile:
            image.paste(tile, (x * TILE_SIZE, 7 * TILE_SIZE), tile)
    
    # Left vertical path (column 2)
    for y in range(2, 7):
        tile = load_tile('path_v')
        if tile:
            image.paste(tile, (2 * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Right vertical path (column 10)
    for y in range(2, 7):
        tile = load_tile('path_v')
        if tile:
            image.paste(tile, (10 * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add light tan/cream base tiles in center (3 tiles horizontal at row 3)
    for i in range(3):
        tile = load_tile('092')  # Light cream/tan tile
        if tile:
            image.paste(tile, ((5 + i) * TILE_SIZE, 3 * TILE_SIZE), tile)
    
    # Place tower in the middle of the cream base
    tower_base = load_tile('180')  # Gray base
    tower = load_tile('249')  # Tower
    if tower_base and tower:
        image.paste(tower_base, (6 * TILE_SIZE, 3 * TILE_SIZE), tower_base)
        image.paste(tower, (6 * TILE_SIZE, 3 * TILE_SIZE), tower)
    
    # Add green placement squares (light green outlines)
    placement_positions = [
        (3, 0), (4, 0),  # Top left corner
        (0, 3), (0, 4), (0, 5),  # Left side
        (4, 2), (5, 2),  # Above tower
        (9, 5), (9, 6),  # Right side
        (5, 7), (6, 8), (8, 7),  # Bottom area
    ]
    
    draw = ImageDraw.Draw(image)
    for px, py in placement_positions:
        left = px * TILE_SIZE + 2
        top = py * TILE_SIZE + 2
        right = left + TILE_SIZE - 4
        bottom = top + TILE_SIZE - 4
        # Draw light green square outline
        draw.rectangle([left, top, right, bottom], outline=(150, 255, 150, 200), width=3)
    
    # Add bushes/small trees on path edges
    bush_positions = [
        (2, 2), (3, 2),  # Top left of path
        (2, 3), (3, 3), (4, 3),  # Left side
        (2, 5), (3, 5), (4, 5),  # Bottom left
    ]
    
    bush = load_tile('130')  # Small green bush/tree
    if bush:
        bush_small = bush.resize((20, 20), Image.Resampling.LANCZOS)
        for bx, by in bush_positions:
            # Place on path edge
            image.paste(bush_small, (bx * TILE_SIZE + 5, by * TILE_SIZE + 5), bush_small)
    
    # Add rocks in center area
    rock = load_tile('137')
    if rock:
        rock_small = rock.resize((40, 40), Image.Resampling.LANCZOS)
        image.paste(rock_small, (5 * TILE_SIZE + 12, 4 * TILE_SIZE + 12), rock_small)
        image.paste(rock_small, (7 * TILE_SIZE + 12, 6 * TILE_SIZE + 12), rock_small)
    
    # Add red tank enemy with exhaust on left path
    tank = load_tile('270')
    if not tank:
        tank = load_tile('245')  # Alternative tank
    if tank:
        tank_sized = tank.resize((50, 50), Image.Resampling.LANCZOS)
        image.paste(tank_sized, (2 * TILE_SIZE + 7, 4 * TILE_SIZE + 7), tank_sized)
    
    # Add explosion/exhaust effect behind tank
    explosion = load_tile('295')
    if explosion:
        expl_small = explosion.resize((30, 30), Image.Resampling.LANCZOS)
        image.paste(expl_small, (2 * TILE_SIZE + 40, 4 * TILE_SIZE + 17), expl_small)
    
    # Add blue enemies on top path
    blue_enemy = load_tile('244')
    if blue_enemy:
        blue_small = blue_enemy.resize((30, 30), Image.Resampling.LANCZOS)
        image.paste(blue_small, (4 * TILE_SIZE + 17, 1 * TILE_SIZE + 17), blue_small)
        image.paste(blue_small, (5 * TILE_SIZE + 17, 1 * TILE_SIZE + 17), blue_small)
    
    # Add pink/purple crosshair in center
    crosshair = load_tile('015')
    if crosshair:
        cross_small = crosshair.resize((40, 40), Image.Resampling.LANCZOS)
        image.paste(cross_small, (6 * TILE_SIZE + 12, 5 * TILE_SIZE + 12), cross_small)
    
    # Add dark circle outline in bottom left corner
    draw.ellipse([10, height - 74, 74, height - 10], outline=(100, 100, 100, 150), width=3)
    
    return image

if __name__ == "__main__":
    print("Creating exact replica...")
    replica = create_exact_replica()
    replica.save(output_path, 'PNG')
    print(f"Saved to: {output_path}")
    print(f"Size: {replica.size}")