import os
import numpy as np
from PIL import Image
import json

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
sample_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\smaple.jpg"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

# The sample image appears to be 748x572, which is approximately 11.7 x 8.9 tiles
# Let's use a different tile size that fits better
TILE_SIZE = 68  # 748/11 = 68

def load_all_tiles():
    """Load all tiles and create a dictionary"""
    tiles = {}
    for i in range(300):
        tile_path = os.path.join(base_path, f'towerDefense_tile{i:03d}.png')
        if os.path.exists(tile_path):
            tile = Image.open(tile_path).convert('RGBA')
            tile = tile.resize((TILE_SIZE, TILE_SIZE), Image.Resampling.LANCZOS)
            tiles[i] = np.array(tile)
    return tiles

def extract_tile_from_image(image, x, y, tile_size):
    """Extract a single tile from an image at grid position x, y"""
    left = x * tile_size
    top = y * tile_size
    right = min(left + tile_size, image.width)
    bottom = min(top + tile_size, image.height)
    
    tile = image.crop((left, top, right, bottom))
    
    # Pad if necessary
    if tile.width < tile_size or tile.height < tile_size:
        padded = Image.new('RGBA', (tile_size, tile_size), (0, 0, 0, 0))
        padded.paste(tile, (0, 0))
        tile = padded
    
    return np.array(tile.convert('RGBA'))

def calculate_similarity(tile1, tile2):
    """Calculate similarity between two tiles"""
    if tile1 is None or tile2 is None:
        return 0
    
    if tile1.shape != tile2.shape:
        return 0
    
    # Focus on RGB channels, ignore alpha for now
    diff = np.abs(tile1[:,:,:3].astype(float) - tile2[:,:,:3].astype(float))
    mse = np.mean(diff)
    
    # Convert to similarity
    similarity = np.exp(-mse / 50)  # Adjust scaling factor
    return similarity

def find_best_matching_tile(sample_tile, all_tiles):
    """Find the best matching tile from our tile set"""
    best_match = None
    best_similarity = 0
    
    for tile_num, tile_array in all_tiles.items():
        similarity = calculate_similarity(sample_tile, tile_array)
        if similarity > best_similarity:
            best_similarity = similarity
            best_match = tile_num
    
    return best_match, best_similarity

def analyze_and_recreate():
    """Analyze sample and create exact recreation"""
    print("Loading sample image...")
    sample = Image.open(sample_path)
    print(f"Sample size: {sample.size}")
    
    print("Loading all tiles...")
    all_tiles = load_all_tiles()
    print(f"Loaded {len(all_tiles)} tiles")
    
    # Use 11x8 grid (most likely based on sample)
    grid_width = 11
    grid_height = 8
    actual_tile_w = sample.width / grid_width  # ~68
    actual_tile_h = sample.height / grid_height  # ~71.5
    
    print(f"Grid: {grid_width}x{grid_height}")
    print(f"Actual tile size: {actual_tile_w:.1f}x{actual_tile_h:.1f}")
    
    # Create output image
    output = Image.new('RGBA', (grid_width * 64, grid_height * 64), (76, 209, 55, 255))
    
    # Process each tile
    tile_map = []
    
    for y in range(grid_height):
        row = []
        for x in range(grid_width):
            # Extract from sample using actual tile dimensions
            left = int(x * actual_tile_w)
            top = int(y * actual_tile_h)
            right = min(int((x + 1) * actual_tile_w), sample.width)
            bottom = min(int((y + 1) * actual_tile_h), sample.height)
            
            tile_sample = sample.crop((left, top, right, bottom))
            tile_sample = tile_sample.resize((TILE_SIZE, TILE_SIZE), Image.Resampling.LANCZOS)
            sample_array = np.array(tile_sample.convert('RGBA'))
            
            best_tile, similarity = find_best_matching_tile(sample_array, all_tiles)
            
            if best_tile is not None:
                # Place the best matching tile
                tile_path = os.path.join(base_path, f'towerDefense_tile{best_tile:03d}.png')
                if os.path.exists(tile_path):
                    tile_img = Image.open(tile_path).convert('RGBA')
                    tile_img = tile_img.resize((64, 64), Image.Resampling.LANCZOS)
                    output.paste(tile_img, (x * 64, y * 64), tile_img)
                    
                    print(f"({x:2},{y}): Tile {best_tile:03d} [{similarity:.2f}]", end="  ")
                    if x == grid_width - 1:
                        print()  # New line at end of row
            
            row.append({'tile': best_tile, 'sim': round(similarity, 2)})
        tile_map.append(row)
    
    return output, tile_map

def calculate_overall_similarity(original_path, recreation_path):
    """Calculate overall similarity between two images"""
    orig = Image.open(original_path)
    rec = Image.open(recreation_path)
    
    # Resize recreation to match original
    rec = rec.resize(orig.size, Image.Resampling.LANCZOS)
    
    orig_array = np.array(orig.convert('RGB'))
    rec_array = np.array(rec.convert('RGB'))
    
    # Calculate pixel-wise similarity
    diff = np.abs(orig_array.astype(float) - rec_array.astype(float))
    mse = np.mean(diff)
    
    similarity = (1 - mse / 255) * 100
    
    return similarity

if __name__ == "__main__":
    print("=== Creating Tile-Perfect Recreation ===\n")
    
    recreation, tile_map = analyze_and_recreate()
    recreation.save(output_path)
    print(f"\nSaved to: {output_path}")
    
    # Calculate similarity
    similarity = calculate_overall_similarity(sample_path, output_path)
    print(f"\nOverall similarity: {similarity:.1f}%")
    
    # Save tile map
    with open('tile_map.json', 'w') as f:
        json.dump(tile_map, f, indent=2)
    print("Tile map saved to tile_map.json")