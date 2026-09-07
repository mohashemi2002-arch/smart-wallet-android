#!/bin/bash
echo "Downloading zip..."
curl -L -o logos.zip "https://github.com/amastaneh/IranianBankLogos/archive/refs/heads/master.zip"

echo "Extracting..."
unzip -q logos.zip -d logos_temp

mkdir -p app/src/main/res/drawable

declare -A map
map=( ["bmi"]="melli" ["bsi"]="saderat" ["mellat"]="mellat"
      ["tejarat"]="tejarat" ["sepah"]="sepah" ["bki"]="keshavarzi"
      ["maskan"]="maskan" ["rb"]="refah" ["sb"]="saman"
      ["bpi"]="pasargad" ["parsian"]="parsian" ["shahr"]="shahr"
      ["ba"]="ayandeh" ["day"]="dey" ["sina"]="sina"
      ["en"]="en" ["kar"]="karafarin" ["resalat"]="resalat"
      ["mehriran"]="mehr" ["sarmayeh"]="sarmayeh" ["tourism"]="gardeshgari"
      ["iz"]="iz" ["me"]="khavarmiyaneh" ["post"]="post"
      ["tt"]="tt" ["bim"]="bim" ["edbi"]="edbi" ["melal"]="melal" )

echo "Copying logos..."
for file in logos_temp/IranianBankLogos-master/resources/images/*-300-wc.png; do
    filename=$(basename "$file")
    basename="${filename%-300-wc.png}"
    slug="${map[$basename]}"
    if [ -n "$slug" ]; then
        cp "$file" "app/src/main/res/drawable/ic_bank_${slug}.png"
        echo "Saved ic_bank_${slug}.png"
    fi
done

echo "Cleaning up..."
rm logos.zip
rm -rf logos_temp
echo "Done!"
