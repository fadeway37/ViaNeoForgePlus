# Usage Guide

## Opening ViaNeoForgePlus

On the Multiplayer or Direct Connection screen, select **ViaNeoForgePlus**. The protocol screen lets you choose a
global target version, open settings, and access supported server lists. You can also open Settings from NeoForge's
mod list.

On the Add/Edit Server screen, the version button selects a protocol for that server only. The selection is stored in
`servers.dat`. Selecting the reset option returns the server to automatic/global selection.

## Commands

ViaVersion commands are exposed under `/vianeoforgeplus`. `/viafabricplus` remains an alias for scripts and users of
the upstream mod. Important subcommands include:

- `/vianeoforgeplus settings`
- `/vianeoforgeplus settime <time>` for Classic protocols that support client-side time changes
- `/vianeoforgeplus listextensions` for Classic Protocol Extension information

## Configuration

ViaNeoForgePlus stores its own files under `config/vianeoforgeplus/`, including `settings.json` and `accounts.json`.
The embedded translators also create their normal files there, such as `viaversion.yml`, `viabackwards.yml`,
`vialegacy.yml`, `viaaprilfools.yml`, and `viabedrock.yml`.

The Debug settings are intended for diagnostics. Defaults are recommended unless you are investigating a specific
protocol issue.

## Bedrock Edition

Bedrock support is experimental. Configure a Microsoft/Bedrock account from Authentication settings before joining
online-mode Bedrock servers or opening Bedrock Realms. Some Bedrock features may remain incomplete in ViaBedrock.

## ClassiCube and BetaCraft

The Server Lists screen exposes the supported ClassiCube and BetaCraft lists. ClassiCube authentication, including its
multi-factor prompt, is available from the same UI.

## Scope

ViaNeoForgePlus is client-side and primarily affects multiplayer connections. It does not need to be installed on the
server and does not convert old resource packs.
