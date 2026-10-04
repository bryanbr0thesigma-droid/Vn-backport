#!/usr/bin/env python3
"""Builds the friends install packs (.mrpack) into dist/.

Dependencies and Fresh Animations are referenced by their Modrinth download links (nothing third-party is
re-hosted); the mod jar itself is referenced by its raw GitHub link, so run this again whenever the jar in dist/ changes.
"""
import hashlib, json, os, urllib.parse, urllib.request, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAR = "villager-news-addon-port-1.3.6-fabric-1.20.1.jar"
RAW = "https://raw.githubusercontent.com/bryanbr0thesigma-droid/Vn-backport/claude/intelligent-ride-syvay9/dist/" + JAR


def api(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "vn-backport-pack-builder"}), timeout=30) as r:
        return json.load(r)


def modrinth_file(project, version_number=None, game="1.20.1", loader="fabric"):
    q = urllib.parse.urlencode({"game_versions": json.dumps([game]), **({"loaders": json.dumps([loader])} if loader else {})})
    versions = api("https://api.modrinth.com/v2/project/%s/version?%s" % (project, q))
    v = next(x for x in versions if version_number is None or x["version_number"].startswith(version_number)) if versions else None
    f = next(f for f in v["files"] if f["primary"]) if v else None
    return f


def entry(path, f, client, server):
    return {"path": path, "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
            "env": {"client": client, "server": server}, "downloads": [f["url"]], "fileSize": f["size"]}


def own_jar():
    data = open(os.path.join(ROOT, "dist", JAR), "rb").read()
    return {"path": "mods/" + JAR, "hashes": {"sha1": hashlib.sha1(data).hexdigest(), "sha512": hashlib.sha512(data).hexdigest()},
            "env": {"client": "required", "server": "required"}, "downloads": [RAW], "fileSize": len(data)}


# Exactly the versions this mod was tested with. Bump them only after re-running tools/smoke_test.py.
PINS = {"fabric-api": "0.92.12", "entity-model-features": "3.3.9", "entitytexturefeatures": "7.2.4", "esf": "0.8.2", "fresh-animations": "1.10.4"}


def build(name, out, with_fabric_api, check=False):
    files = []
    if with_fabric_api:
        files.append(entry("mods/" + modrinth_file("fabric-api", PINS["fabric-api"])["filename"], modrinth_file("fabric-api", PINS["fabric-api"]), "required", "required"))
    for project in ("entity-model-features", "entitytexturefeatures", "esf"):
        f = modrinth_file(project, PINS[project])
        files.append(entry("mods/" + f["filename"], f, "required", "required"))
    files.append(own_jar())
    fa = modrinth_file("fresh-animations", PINS["fresh-animations"], loader=None)
    files.append(entry("resourcepacks/" + fa["filename"], fa, "optional", "unsupported"))
    index = {"formatVersion": 1, "game": "minecraft", "versionId": "1.3.6-friends", "name": name,
             "summary": "Vanilla Extended: the Villager News addon port, the Pale Garden and the newer Minecraft content, for Fabric 1.20.1.",
             "files": files, "dependencies": {"minecraft": "1.20.1", "fabric-loader": "0.16.14"}}
    path = os.path.join(ROOT, "dist", out)
    text = json.dumps(index, indent=2)
    if check:
        have = zipfile.ZipFile(path).read("modrinth.index.json").decode() if os.path.exists(path) else None
        if have != text:
            print("STALE:", out, "- run python3 tools/make-mrpack.py")
            return False
        print("up to date:", out)
        return True
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("modrinth.index.json", text)
    print(out, len(files), "files")
    return True


if __name__ == "__main__":
    import sys
    check = "--check" in sys.argv
    ok = build("Vanilla Extended (PC and server)", "friends-pack-pc-and-server.mrpack", True, check)
    ok = build("Vanilla Extended (QuestCraft)", "friends-pack-questcraft.mrpack", False, check) and ok
    sys.exit(0 if ok else 1)
