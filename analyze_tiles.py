import os
import numpy as np
from PIL import Image
import json

base_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\PNG\Default size"
sample_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\smaple.jpg"
output_path = r"C:\Users\nikim\OneDrive\Desktop\proj4\kenney_tower-defense-top-down\Sample_new.png"

TILE_SIZE = 64

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
    right = left + tile_size
    bottom = top + tile_size
    
    if right > image.width or bottom > image.height:
        return None
    
    tile = image.crop((left, top, right, bottom))
    return np.array(tile.convert('RGBA'))

def calculate_similarity(tile1, tile2):
    """Calculate similarity between two tiles using MSE"""
    if tile1 is None or tile2 is None:
        return 0
    
    # Resize if needed
    if tile1.shape != tile2.shape:
        return 0
    
    # Calculate mean squared error
    mse = np.mean((tile1.astype(float) - tile2.astype(float)) ** 2)
    # Convert to similarity (1 = identical, 0 = very different)
    similarity = 1.0 / (1.0 + mse / 1000.0)
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

def analyze_sample():
    """Analyze the sample image tile by tile"""
    print("Loading sample image...")
    sample = Image.open(sample_path)
    
    print("Loading all tiles...")
    all_tiles = load_all_tiles()
    print(f"Loaded {len(all_tiles)} tiles")
    
    # Calculate grid dimensions
    grid_width = sample.width // TILE_SIZE
    grid_height = sample.height // TILE_SIZE
    
    print(f"Sample dimensions: {sample.width}x{sample.height}")
    print(f"Grid size: {grid_width}x{grid_height} tiles")
    
    # Analyze each tile position
    grid_mapping = []
    
    for y in range(grid_height):
        row = []
        for x in range(grid_width):
            sample_tile = extract_tile_from_image(sample, x, y, TILE_SIZE)
            if sample_tile is not None:
                best_tile, similarity = find_best_matching_tile(sample_tile, all_tiles)
                row.append({
                    'tile': best_tile,
                    'similarity': round(similarity, 3)
                })
                print(f"Position ({x},{y}): Tile {best_tile:03d} (similarity: {similarity:.3f})")
            else:
                row.append({'tile': None, 'similarity': 0})
        grid_mapping.append(row)
    
    return grid_mapping, grid_width, grid_height

def create_recreation_from_mapping(grid_mapping, grid_width, grid_height):
    """Create a recreation based on the tile mapping"""
    width = grid_width * TILE_SIZE
    height = grid_height * TILE_SIZE
    
    recreation = Image.new('RGBA', (width, height), (0, 255, 0, 255))
    
    for y, row in enumerate(grid_mapping):
        for x, tile_info in enumerate(row):
            if tile_info['tile'] is not None:
                tile_num = tile_info['tile']
                tile_path = os.path.join(base_path, f'towerDefense_tile{tile_num:03d}.png')
                if os.path.exists(tile_path):
                    tile = Image.open(tile_path).convert('RGBA')
                    tile = tile.resize((TILE_SIZE, TILE_SIZE), Image.Resampling.LANCZOS)
                    recreation.paste(tile, (x * TILE_SIZE, y * TILE_SIZE), tile)
    
    return recreation

def compare_images(image1_path, image2_path):
    """Compare two images tile by tile"""
    img1 = Image.open(image1_path)
    img2 = Image.open(image2_path)
    
    # Ensure same size
    if img1.size != img2.size:
        print(f"Size mismatch: {img1.size} vs {img2.size}")
        # Resize to match
        img2 = img2.resize(img1.size, Image.Resampling.LANCZOS)
    
    grid_width = img1.width // TILE_SIZE
    grid_height = img1.height // TILE_SIZE
    
    total_similarity = 0
    tile_count = 0
    
    differences = []
    
    for y in range(grid_height):
        for x in range(grid_width):
            tile1 = extract_tile_from_image(img1, x, y, TILE_SIZE)
            tile2 = extract_tile_from_image(img2, x, y, TILE_SIZE)
            
            if tile1 is not None and tile2 is not None:
                similarity = calculate_similarity(tile1, tile2)
                total_similarity += similarity
                tile_count += 1
                
                if similarity < 0.8:  # Flag significant differences
                    differences.append({
                        'position': (x, y),
                        'similarity': round(similarity, 3)
                    })
    
    average_similarity = total_similarity / tile_count if tile_count > 0 else 0
    
    print(f"\nOverall similarity: {average_similarity:.1%}")
    print(f"Tiles with low similarity (<80%):")
    for diff in differences[:10]:  # Show first 10 differences
        print(f"  Position {diff['position']}: {diff['similarity']:.1%}")
    
    return average_similarity, differences

if __name__ == "__main__":
    print("=== Analyzing Sample Image ===")
    grid_mapping, grid_width, grid_height = analyze_sample()
    
    print("\n=== Creating Recreation ===")
    recreation = create_recreation_from_mapping(grid_mapping, grid_width, grid_height)
    recreation.save(output_path)
    print(f"Saved recreation to: {output_path}")
    
    print("\n=== Comparing Sample vs Recreation ===")
    similarity, differences = compare_images(sample_path, output_path)
    
    # Save the grid mapping for reference
    with open('grid_mapping.json', 'w') as f:
        json.dump(grid_mapping, f, indent=2)
    print("\nGrid mapping saved to grid_mapping.json")