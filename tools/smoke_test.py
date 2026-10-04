#!/usr/bin/env python3
"""Boots a real dedicated server with the built jar and its dependencies, then exercises everything the mod adds.

It places every block, summons every entity, runs every loot table, generates a trial chamber and looks up the new
biomes, then fails if the server log contains an error or a rejected command. Run it before pushing:

    ./gradlew build && python3 tools/smoke_test.py

Options:
    --jar PATH         mod jar to test (default: the newest build/libs/villager-news-addon-port-*.jar)
    --server-dir DIR   reuse a server directory that already has fabric-server-launch.jar and its libraries
"""
import argparse, glob, json, os, re, shutil, subprocess, sys, time, urllib.parse, urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
JAVA = os.path.join(ROOT, "src", "main", "java")
UA = {"User-Agent": "vn-backport-smoke-test"}


def props():
    out = {}
    for line in open(os.path.join(ROOT, "gradle.properties")):
        if "=" in line and not line.startswith("#"):
            k, v = line.strip().split("=", 1)
            out[k.strip()] = v.strip()
    return out


def fetch(url, dest):
    if os.path.exists(dest) and os.path.getsize(dest) > 0:
        return
    req = urllib.request.Request(url, headers=UA)
    with urllib.request.urlopen(req, timeout=120) as r, open(dest, "wb") as f:
        shutil.copyfileobj(r, f)


def modrinth_jar(project, version_prefix=None):
    q = urllib.parse.urlencode({"game_versions": json.dumps(["1.20.1"]), "loaders": json.dumps(["fabric"])})
    req = urllib.request.Request("https://api.modrinth.com/v2/project/%s/version?%s" % (project, q), headers=UA)
    versions = json.load(urllib.request.urlopen(req, timeout=30))
    v = next(x for x in versions if version_prefix is None or x["version_number"].startswith(version_prefix))
    f = next(f for f in v["files"] if f["primary"])
    return f["filename"], f["url"]


def ids():
    blocks = []
    for ns in ("backport", "pale_garden", "villager-news-addon-port"):
        d = os.path.join(RES, "assets", ns, "blockstates")
        if os.path.isdir(d):
            blocks += [ns + ":" + f[:-5] for f in sorted(os.listdir(d)) if f.endswith(".json")]
    entities = []
    for src, ns in (("backport/BackportEntities.java", "backport"), ("palegarden/PaleEntities.java", "pale_garden")):
        text = open(os.path.join(JAVA, "com", src)).read()
        for name in re.findall(r'\.id\("([a-z_]+)"\)', text):
            if name not in ("wind_charge", "breeze_wind_charge", "variant_egg", "ice_ball", "pale_oak_boat", "pale_oak_chest_boat", "poplar_boat", "poplar_chest_boat"):
                entities.append(ns + ":" + name)
    loot = []
    for ns in ("backport", "pale_garden"):
        base = os.path.join(RES, "data", ns, "loot_tables")
        for r, _, fs in os.walk(base):
            loot += [ns + ":" + os.path.relpath(os.path.join(r, f), base)[:-5].replace(os.sep, "/") for f in fs if f.endswith(".json")]
    return blocks, sorted(set(entities)), loot


def prepare(server_dir, jar):
    os.makedirs(os.path.join(server_dir, "mods"), exist_ok=True)
    launcher = os.path.join(server_dir, "fabric-server-launch.jar")
    if not os.path.exists(launcher):
        p = props()
        fetch("https://meta.fabricmc.net/v2/versions/loader/%s/%s/1.0.1/server/jar" % (p["minecraft_version"], p["loader_version"]), launcher)
    for f in glob.glob(os.path.join(server_dir, "mods", "*.jar")):
        os.remove(f)
    for project, prefix in (("fabric-api", "0.92.12"), ("entity-model-features", "3.3.9"), ("entitytexturefeatures", "7.2.4"), ("esf", "0.8.2")):
        name, url = modrinth_jar(project, prefix)
        fetch(url, os.path.join(server_dir, "mods", name))
    shutil.copy(jar, os.path.join(server_dir, "mods", os.path.basename(jar)))
    open(os.path.join(server_dir, "eula.txt"), "w").write("eula=true\n")
    open(os.path.join(server_dir, "server.properties"), "w").write(
        "online-mode=false\nlevel-seed=12345\nspawn-protection=0\nview-distance=4\nsimulation-distance=4\nmax-tick-time=-1\nenable-command-block=true\n")
    shutil.rmtree(os.path.join(server_dir, "world"), ignore_errors=True)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--jar")
    ap.add_argument("--server-dir", default=os.path.join(ROOT, "build", "smoke-server"))
    args = ap.parse_args()
    jars = [j for j in glob.glob(os.path.join(ROOT, "build", "libs", "villager-news-addon-port-*.jar")) if "sources" not in j and "dev" not in j]
    jar = args.jar or max(jars, key=os.path.getmtime)
    blocks, entities, loot = ids()
    print("testing %s: %d blocks, %d entities, %d loot tables" % (os.path.basename(jar), len(blocks), len(entities), len(loot)))
    prepare(args.server_dir, jar)
    log_path = os.path.join(args.server_dir, "smoke.log")
    log = open(log_path, "w")
    proc = subprocess.Popen(["java", "-Xmx2G", "-jar", "fabric-server-launch.jar", "nogui"], cwd=args.server_dir,
                            stdin=subprocess.PIPE, stdout=log, stderr=subprocess.STDOUT, text=True, bufsize=1)

    def say(cmd, wait=0.05):
        proc.stdin.write(cmd + "\n")
        proc.stdin.flush()
        time.sleep(wait)

    def logged():
        log.flush()
        return open(log_path, errors="replace").read()

    deadline = time.time() + 240
    while "Done (" not in logged():
        if proc.poll() is not None or time.time() > deadline:
            break
        time.sleep(1)
    started = "Done (" in logged()
    if started:
        say("gamerule locatorBar false")
        say("gamerule locatorBar true")
        say("forceload add 96 96 260 260")
        time.sleep(25)
        for i, b in enumerate(blocks):
            say("setblock %d 150 %d %s" % (100 + (i % 20) * 8, 100 + (i // 20) * 8, b))
        for i, e in enumerate(entities):
            say("summon %s %d 152 %d {NoAI:1b,PersistenceRequired:1b}" % (e, 100 + i * 4, 90))
        say('summon backport:mannequin 100 152 94 {profile:{name:"Steve"},hidden_layers:["hat"],pose:"crouching"}')
        say("zombiehorsejockey")
        for l in loot:
            say("loot spawn 100 150 100 loot %s" % l)
        say("place structure backport:trial_chambers 0 40 0")
        for target in ("structure backport:trial_chambers", "biome backport:sulfur_caves", "biome backport:ice_caves"):
            say("locate " + target)
        time.sleep(40)
        say("kill @e[type=!player]")
        say("save-all")
        time.sleep(5)
        say("stop")
    try:
        proc.wait(timeout=60)
    except subprocess.TimeoutExpired:
        proc.kill()
    text = logged()
    problems = []
    if not started:
        problems.append("server did not finish starting")
    for line in text.splitlines():
        if re.search(r"/ERROR\]|Exception|FAILED TO|<--\[HERE\]|Unknown (block|entity|item) type|Failed to", line) and "online-mode" not in line:
            problems.append(line.strip()[:200])
    for needed in ('Generated structure "backport:trial_chambers"', "nearest backport:trial_chambers", "nearest backport:sulfur_caves", "nearest backport:ice_caves", "Stopping server"):
        if started and needed not in text:
            problems.append("missing expected log line: " + needed)
    if problems:
        print("SMOKE TEST FAILED (log: %s)" % log_path)
        for p in dict.fromkeys(problems):
            print("  -", p)
        return 1
    print("smoke test passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
