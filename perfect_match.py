import os
from PIL import Image, ImageDraw

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

def load_tile(tile_num):
    """Load a tile by number"""
    if isinstance(tile_num, int):
        tile_path = os.path.join(base_path, f'towerDefense_tile{tile_num:03d}.png')
    else:
        tile_path = os.path.join(base_path, f'towerDefense_tile{tile_num}.png')
    
    if os.path.exists(tile_path):
        return Image.open(tile_path).convert('RGBA')
    return None

def create_perfect_match():
    """Create perfect match of the sample image"""
    
    # Looking at the sample, it's clearly a 12x9 grid
    width = 12 * TILE_SIZE
    height = 9 * TILE_SIZE
    
    # Create base with green grass
    image = Image.new('RGBA', (width, height), (76, 209, 55, 255))
    
    # First layer: all grass
    grass = load_tile(24)  # Green grass tile
    for y in range(9):
        for x in range(12):
            if grass:
                image.paste(grass, (x * TILE_SIZE, y * TILE_SIZE), grass)
    
    # The exact grid from the sample (12x9):
    # Looking row by row at the sample image
    
    # Define the exact tile layout
    grid = [
        # Row 0
        [24, 24, 50, 50, 50, 50, 50, 50, 50, 50, 2, 24],
        # Row 1  
        [24, 24, 1, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 2
        [24, 5, 4, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 3
        [24, 1, 24, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 4
        [24, 1, 24, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 5
        [24, 1, 24, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 6
        [24, 1, 24, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 7
        [24, 1, 24, 24, 24, 24, 24, 24, 24, 24, 1, 24],
        # Row 8
        [24, 3, 50, 50, 50, 50, 50, 50, 50, 50, 4, 24],
    ]
    
    # Draw the brown path based on grid
    for y in range(9):
        for x in range(12):
            tile_num = grid[y][x]
            if tile_num != 24:  # Not grass
                tile = load_tile(tile_num)
                if tile:
                    image.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add inner path rectangle (cutting through the middle)
    # Top horizontal of inner path (row 2)
    for x in range(2, 10):
        if x == 2:
            tile = load_tile(5)  # Bottom-right corner
        elif x == 9:
            tile = load_tile(2)  # Top-left corner
        else:
            tile = load_tile(50)  # Horizontal path
        if tile:
            image.paste(tile, (x * TILE_SIZE, 2 * TILE_SIZE), tile)
    
    # Bottom horizontal of inner path (row 6)
    for x in range(2, 10):
        if x == 2:
            tile = load_tile(3)  # Top-right corner
        elif x == 9:
            tile = load_tile(4)  # Bottom-left corner
        else:
            tile = load_tile(50)  # Horizontal path
        if tile:
            image.paste(tile, (x * TILE_SIZE, 6 * TILE_SIZE), tile)
    
    # Fix vertical paths to connect properly
    for y in range(3, 6):
        tile = load_tile(1)  # Vertical path
        if tile:
            image.paste(tile, (2 * TILE_SIZE, y * TILE_SIZE), tile)
            image.paste(tile, (9 * TILE_SIZE, y * TILE_SIZE), tile)
    
    # Add the cream/beige platform (3 tiles wide) in upper right area
    # These appear to be special tiles with wavy edges
    platform_tiles = [93, 93, 93]  # Sand/tan colored tiles
    for i, tile_num in enumerate(platform_tiles):
        tile = load_tile(tile_num)
        if tile:
            image.paste(tile, ((5 + i) * TILE_SIZE, 2 * TILE_SIZE), tile)
    
    # Place tower in center of platform (position 6, 2)
    tower_base = load_tile(181)  # Silver/gray base
    tower = load_tile(206)  # Red tower
    if tower_base and tower:
        image.paste(tower_base, (6 * TILE_SIZE, 2 * TILE_SIZE), tower_base)
        image.paste(tower, (6 * TILE_SIZE, 2 * TILE_SIZE), tower)
    
    # Add small green bushes ON the path
    bush_positions = [
        (1, 2), (1, 3), (1, 4), (1, 5),  # Left path bushes
        (2, 1), (3, 1),  # Top path bushes
    ]
    
    bush = load_tile(130)  # Small bush/tree
    if bush:
        bush_small = bush.resize((25, 25), Image.Resampling.LANCZOS)
        for bx, by in bush_positions:
            image.paste(bush_small, (bx * TILE_SIZE + 5, by * TILE_SIZE + 40), bush_small)
    
    # Add rocks in center area
    rock = load_tile(137)  # Gray rock
    if rock:
        rock_sized = rock.resize((45, 45), Image.Resampling.LANCZOS)
        image.paste(rock_sized, (4 * TILE_SIZE + 10, 3 * TILE_SIZE + 10), rock_sized)
        image.paste(rock_sized, (5 * TILE_SIZE + 10, 5 * TILE_SIZE + 10), rock_sized)
    
    # Add enemies
    # Red tank with exhaust on left path
    tank = load_tile(245)  # Red tank
    exhaust = load_tile(295)  # Exhaust/explosion
    if tank:
        tank_sized = tank.resize((50, 50), Image.Resampling.LANCZOS)
        image.paste(tank_sized, (1 * TILE_SIZE + 7, 4 * TILE_SIZE + 7), tank_sized)
    if exhaust:
        exh_small = exhaust.resize((30, 30), Image.Resampling.LANCZOS)
        image.paste(exh_small, (1 * TILE_SIZE + 45, 4 * TILE_SIZE + 17), exh_small)
    
    # Blue enemies on top path
    blue_enemy = load_tile(244)  # Blue enemy
    if blue_enemy:
        blue_small = blue_enemy.resize((35, 35), Image.Resampling.LANCZOS)
        image.paste(blue_small, (4 * TILE_SIZE + 15, 0 * TILE_SIZE + 15), blue_small)
        image.paste(blue_small, (5 * TILE_SIZE + 15, 0 * TILE_SIZE + 15), blue_small)
    
    # Add light green placement squares
    draw = ImageDraw.Draw(image)
    placement_positions = [
        (0, 3), (0, 4),  # Left side
        (3, 2), (4, 2),  # Above inner area
        (7, 5), (8, 5),  # Right of center
        (5, 7), (7, 7),  # Bottom area
    ]
    
    for px, py in placement_positions:
        left = px * TILE_SIZE + 4
        top = py * TILE_SIZE + 4
        right = left + TILE_SIZE - 8
        bottom = top + TILE_SIZE - 8
        draw.rectangle([left, top, right, bottom], outline=(150, 255, 150, 180), width=3)
    
    # Add pink crosshair in center
    crosshair = load_tile(15)  # Crosshair
    if crosshair:
        cross_sized = crosshair.resize((40, 40), Image.Resampling.LANCZOS)
        image.paste(cross_sized, (6 * TILE_SIZE + 12, 4 * TILE_SIZE + 12), cross_sized)
    
    # Add circle outline in bottom left
    draw.ellipse([15, height - 75, 75, height - 15], outline=(100, 100, 100, 150), width=3)
    
    # Add dark spot/crater effect in grass
    crater = load_tile(19)
    if crater:
        crater_small = crater.resize((40, 40), Image.Resampling.LANCZOS)
        image.paste(crater_small, (8 * TILE_SIZE + 12, 4 * TILE_SIZE + 12), crater_small)
    
    return image

if __name__ == "__main__":
    print("Creating perfect match...")
    perfect = create_perfect_match()
    perfect.save(output_path, 'PNG')
    print(f"Saved to: {output_path}")
    print(f"Size: {perfect.size}")