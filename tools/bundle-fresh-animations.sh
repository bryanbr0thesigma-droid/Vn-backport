#!/bin/bash
# Builds a PERSONAL-USE jar of this mod with Fresh Animations and every dependency bundled in.
# Fresh Animations' terms forbid redistributing the pack, so it is downloaded here at build time and the
# resulting jar must not be published or committed. EMF / ETF / ESF (LGPL-3.0) and Fabric API (Apache-2.0)
# are nested as jar-in-jar.
set -euo pipefail
cd "$(dirname "$0")/.."
IN=${1:-build/libs/villager-news-addon-port-1.3.6.jar}
OUT=${2:-dist-private/villager-news-addon-port-1.3.6-fabric-1.20.1-fresh-animations.jar}
W=$(mktemp -d)
mkdir -p "$W/jars" "$(dirname "$OUT")"
dl() { [ -s "$2" ] || curl -sSL --fail -o "$2" "$1"; }
CACHE=${BUNDLE_CACHE:-/tmp/fa}
mkdir -p "$CACHE"
dl https://cdn.modrinth.com/data/50dA9Sha/versions/xN57JJts/FreshAnimations_v1.10.4.zip "$CACHE/FA.zip"
dl https://cdn.modrinth.com/data/4I1XuqiY/versions/1YnYIfno/entity_model_features-3.3.9-1.20.1-fabric.jar "$CACHE/emf.jar"
dl https://cdn.modrinth.com/data/BVzZfTc1/versions/iOlqSm7x/entity_texture_features-7.2.4-1.20.1-fabric.jar "$CACHE/etf.jar"
[ -s "$CACHE/esf.jar" ] || cp /tmp/libs_bak/entity_sound_features-0.8.2-1.20.1-fabric.jar "$CACHE/esf.jar" 2>/dev/null || true
if [ ! -s "$CACHE/esf.jar" ]; then
  U=$(curl -sS -G 'https://api.modrinth.com/v2/project/entity-sound-features/version' --data-urlencode 'game_versions=["1.20.1"]' --data-urlencode 'loaders=["fabric"]' | python3 -c "import json,sys;print(json.load(sys.stdin)[0]['files'][0]['url'])")
  dl "$U" "$CACHE/esf.jar"
fi
[ -s "$CACHE/fabric-api.jar" ] || cp "$(find ~/.gradle -name 'fabric-api-0.92.*+1.20.1.jar' | head -1)" "$CACHE/fabric-api.jar"
cp "$CACHE/emf.jar" "$W/jars/entity_model_features.jar"
cp "$CACHE/etf.jar" "$W/jars/entity_texture_features.jar"
cp "$CACHE/esf.jar" "$W/jars/entity_sound_features.jar"
cp "$CACHE/fabric-api.jar" "$W/jars/fabric-api.jar"
mkdir -p "$W/pack"; (cd "$W/pack" && unzip -q "$CACHE/FA.zip")
python3 - "$IN" "$OUT" "$W" <<'P'
import sys,zipfile,json,os
src,out,w=sys.argv[1:4]
zi=zipfile.ZipFile(src)
zo=zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED)
for i in zi.infolist():
    d=zi.read(i.filename)
    if i.filename=='fabric.mod.json':
        j=json.loads(d)
        j['jars']=[{'file':'META-INF/jars/'+f} for f in sorted(os.listdir(w+'/jars'))]
        d=json.dumps(j,indent=1).encode()
    zo.writestr(i.filename,d)
for f in os.listdir(w+'/jars'):
    zo.write(w+'/jars/'+f,'META-INF/jars/'+f)
for r,_,fs in os.walk(w+'/pack'):
    for f in fs:
        p=os.path.join(r,f)
        zo.write(p,'resourcepacks/fresh_animations/'+os.path.relpath(p,w+'/pack'))
zo.close()
P
rm -rf "$W"
ls -la "$OUT"
