import urllib.request
import zipfile
import os
import shutil

url = "https://github.com/amastaneh/IranianBankLogos/archive/refs/heads/master.zip"
zip_path = "logos.zip"
extract_path = "logos_temp"
drawable_dir = "app/src/main/res/drawable"

if not os.path.exists(drawable_dir):
    os.makedirs(drawable_dir)

print("Downloading zip...")
urllib.request.urlretrieve(url, zip_path)

print("Extracting...")
with zipfile.ZipFile(zip_path, 'r') as zip_ref:
    zip_ref.extractall(extract_path)

# Map original filename (without extension) to required slug
slug_mapping = {
    'melli': 'melli',
    'saderat': 'saderat',
    'mellat': 'mellat',
    'tejarat': 'tejarat',
    'sepah': 'sepah',
    'keshavarzi': 'keshavarzi',
    'maskan': 'maskan',
    'refah': 'refah',
    'saman': 'saman',
    'blu': 'blubank',
    'pasargad': 'pasargad',
    'parsian': 'parsian',
    'shahr': 'shahr',
    'ayandeh': 'ayandeh',
    'dey': 'dey',
    'sina': 'sina',
    'en': 'en',
    'karafarin': 'karafarin',
    'sarmayeh': 'sarmayeh',
    'gardeshgari': 'gardeshgari',
    'iz': 'iz',
    'khavarmiyaneh': 'khavarmiyaneh',
    'resalat': 'resalat',
    'mehr': 'mehr',
    'post': 'post',
    'tt': 'tt',
    'bim': 'bim',
    'edbi': 'edbi',
    'cbi': 'cbi',
    'melal': 'melal',
    'noor': 'noor',
    'caspian': 'caspian'
}

png_dir = os.path.join(extract_path, "IranianBankLogos-master", "assets", "png")

if os.path.exists(png_dir):
    for filename in os.listdir(png_dir):
        if filename.endswith(".png"):
            base_name = filename[:-4]
            slug = slug_mapping.get(base_name, base_name)
            src_file = os.path.join(png_dir, filename)
            dest_file = os.path.join(drawable_dir, f"ic_bank_{slug}.png")
            shutil.copy2(src_file, dest_file)
            print(f"Copied {filename} to ic_bank_{slug}.png")

# Cleanup
os.remove(zip_path)
shutil.rmtree(extract_path)
print("Done!")
