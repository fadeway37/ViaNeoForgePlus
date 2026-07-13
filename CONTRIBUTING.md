# Contributing to ViaNeoForgePlus

Thanks for your interest in contributing! This guide will walk you through setting up a workspace, updating
translations, adding features, and maintaining high-quality contributions.

---

## Setting Up a Workspace

ViaNeoForgePlus uses **Gradle** and ModDevGradle. Make sure you have it
installed: [Gradle Installation Guide](https://gradle.org/install/).

1. Enter the project folder:

   ```bash
   cd ViaNeoForgePlus
   ```
2. Compile the project:

   ```bash
   ./gradlew build
   ```
3. Open the project as a **Gradle project** in your preferred IDE.
4. Run the client with `./gradlew runClient`.

---

## Updating Translation Files

If you want to help translate ViaFabricPlus, you can do so on [Crowdin](https://crowdin.com/project/viafabricplus).

Translation keys retain the upstream `viafabricplus` namespace. Keep translated product-name values as
`ViaNeoForgePlus` in this port.

---

## Adding a New Feature or Fixing a Bug

1. Start from the current active `ver/<version>` branch.
2. Create a branch (e.g. `feature/fix-xyz` or `fix/fix-xyz`).
3. Open your pull request against that same `ver/<version>` branch.
4. Implement and test your changes thoroughly.
5. Write clean, readable code (descriptive names, no clutter).
6. Follow [Google's Java Code Style](https://google.github.io/styleguide/javaguide.html).
7. If you modify the API:
    - Update documentation in `docs/`
    - Add Javadocs to your code
    - Avoid breaking backwards compatibility unless absolutely necessary
8. Open a pull request and wait for review.

---

## Adding Protocol Fixes

Protocol fixes are the **heart of ViaNeoForgePlus**. They're what make this project unique, so it's important to add only
relevant and correct changes.

Guidelines for fixes:

- Only add fixes that **affect gameplay or server communication**
- Avoid purely **visual-only tweaks**
- If unsure, ask in the [ViaVersion Discord](https://discord.gg/viaversion)
- The most useful fixes usually involve **movement or networking**

Proof is required:

- Show that your fix matches real game changes
- Provide a source diff (if available)

Remember: fixes should reflect actual **historical behavior**, not cosmetic adjustments.

---

## Maintaining the Mod

For details on ongoing development, see [MAINTAINING.md](docs/MAINTAINING.md).
