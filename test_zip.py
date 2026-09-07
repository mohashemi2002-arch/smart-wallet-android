import urllib.request
import zipfile
import os

url = "https://github.com/amastaneh/IranianBankLogos/archive/refs/heads/master.zip"
zip_path = "logos.zip"

print("Downloading zip...")
urllib.request.urlretrieve(url, zip_path)

with zipfile.ZipFile(zip_path, 'r') as zip_ref:
    for name in zip_ref.namelist():
        if ".png" in name:
            print(name)
