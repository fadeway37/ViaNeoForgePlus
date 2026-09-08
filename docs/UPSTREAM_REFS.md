# Upstream reference synchronization — 5.0.1

Snapshot: **September 8, 2026**. All changes described here are local; no remote branches or tags were pushed or deleted.

## Release provenance

| Reference | Meaning |
| --- | --- |
| `ver/26.2`, `5.0.1` | Tested ViaNeoForgePlus 5.0.1 port and its release tag |
| First merge parent `9db4f9a6958442be11e59cb7bb1232d08a58deb1` | Existing ViaNeoForgePlus 4.6.3 history |
| Second merge parent `707b0e0b44fe33f78e5a2f89f37eefec861ec660` | Exact ViaFabricPlus upstream 5.0.1 release |
| `refs/upstream/tags/5.0.1` | Original Fabric release tag, retained unchanged |
| `upstream/ver/26.2` | Upstream branch tracking ref; its newer tip is **not** the release baseline |

Upstream: https://github.com/ViaVersion/ViaFabricPlus

Origin: https://github.com/fadeway37/ViaNeoForgePlus.git

The previous port's original Fabric base was `d840856d29bdfc70bf26679aae17435e3bc0a05d`.
At synchronization, the newer upstream branch tip was `f8330f3a3f2e5c3e21530fa30ba1c1a35d51b6a0`.
Do not substitute that tip for the requested 5.0.1 release.

## Names versus implementation

The ordinary local namespace contains **20 branches and 139 tags**, exactly matching the upstream **names** in this snapshot.
Matching names do not mean matching commit IDs or that every historical version has been ported to NeoForge.

- `ver/26.2` and `5.0.1` contain this tested NeoForge port.
- The existing NeoForge `ver/26.1` branch was preserved at `79f92eb5409661cb546e9912376d7026ad8e3db4`.
- Existing NeoForge tags `4.5.6`, `4.6.2` and `4.6.3` retain their original local targets.
- Other historical branches/tags mirror upstream history. They are **not newly tested NeoForge releases**.

The synchronized branch names are:

~~~text
beta/biome_fixes
experimental/disable_movement_world_limit
ver/1.19.3
ver/1.19.4
ver/1.20.1
ver/1.20.4
ver/1.20.6
ver/1.21
ver/1.21.1
ver/1.21.10
ver/1.21.11
ver/1.21.3
ver/1.21.4
ver/1.21.5
ver/1.21.6
ver/1.21.7
ver/1.21.8
ver/1.21.9
ver/26.1
ver/26.2
~~~

## Recoverable old refs

All original local heads and tags were archived before alignment: **143 refs** under
`refs/archive/neoforge/pre-5.0.1/heads/*` and `refs/archive/neoforge/pre-5.0.1/tags/*`.

Two names absent upstream were removed only from the ordinary local namespace:

- `main` → `refs/archive/neoforge/pre-5.0.1/heads/main`, commit `1ed80b10f43ccbfa23a7d0c51d8af940610de7ba`.
- `4.6.2-neoforge.1` → `refs/archive/neoforge/pre-5.0.1/tags/4.6.2-neoforge.1`, commit `926e86c368c3ecb4c3d8a47b2f7b81633a8bc888`.

Their commits remain reachable and recoverable. No project files or old client instances were deleted by ref alignment.
These nonstandard archive refs are local and are not transferred by an ordinary branch/tag push.

## Inspect and maintain

~~~sh
git log -1 --format='%H %P' 5.0.1
git show refs/upstream/tags/5.0.1
git for-each-ref --format='%(refname)' refs/archive/neoforge/pre-5.0.1
git for-each-ref --format='%(refname:strip=2)' refs/heads refs/tags
git ls-remote --heads --tags --refs upstream
~~~

The upstream remote uses `tagopt=--no-tags`. Future fetches can explicitly update
`refs/remotes/upstream/*` and `refs/upstream/tags/*` without overwriting NeoForge release tags.
Review any future branch/tag deletions or publication separately; never force-update a published release merely to match upstream objects.

The independent Bedrock repository follows its own `main` branch and `1.0.0` tag, not the core's version number.
