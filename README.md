### **🌊DO NOT ENTER WORLDS THAT YOU DO NOT WANT TO FLOOD - THE NEW CHUNKS WILL BE AUTOMATICALLY FLOODED**

### ABOUT
**Inundated** is a world generation mod that floods your entire overworld, nether and end dimensions — every cave, every mountain, every block of air from **Y 320 to -64** — with water.
It injects **at the very end** of vanilla chunk generation, meaning biomes, terrain, ores, and structures **stay untouched.**

All _air_ and _cave_air_ blocks are replaced with water, and all **waterloggable blocks** are properly **waterlogged**.
This keeps leaves, slabs, fences, and other blocks visually consistent underwater.

And yet - torches, sugarcanes, nether vegetation **left as a feature** and grass/flowers will eventually flood itself and dropped as item. (does all of this need to be replaced with water too?)

### NON-GENERATION FEATURE
There’s also a legacy command:
_/inundated start_ — it floods nearby chunks **manually.**
This is useful for non-flooded chunks that were generated **before installing the mod.**

⚠️ **Be careful:** this process is **resource-intensive**. It recalculates every air block in the chunk and updates all surrounding water, triggering physics and neighbor updates.
When used with mods like **Particular** (that add 3D splash particles), it may cause **lag spikes** or even **crashes.**

The command finishes after flooding nearby chunks — you can run it **again to continue**.

### KNOWN ISSUES
1. Generation of any new chunks is now **many times slower** than in vanilla.
2. Updating the big **lava lakes** in the nether will probably cause a **lag spike**.
3. The End is flooded at a lower level of generation, causing the world to disappear chorus; end crystals have pockets of air; the portal isn't generated **until you kill a dragon**; and end cities are barely flooded.

### CREDITS
Almost all the code is written by ChatGPT, I just used my promt engineering skills, logic bulding and _inundated_ with solving a bunch of errors, the solution to which I had to look for myself in yarn mappings.
I'm surprised it works at all, but I really wanted to make this mod and make it open source, since its alternatives are behind a paywall.
