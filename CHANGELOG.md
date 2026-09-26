## __9.26.1__
*(Month.Year.Iteration)*

## General Changes and Fixes
- **[Compatibility]** Usable with LATEST KK Release and Beyond.
- **[NEW]** Chirithy now has dialogs and can act as a guide!
- **[EFM]**, **[NEW]** Equipping the Guard ability now gives you EFM's Guard Skill.
- **[EFM]**, **[FIX]** Attack Command Animations now play again.
- **[EFM]**, **[FIX]** Forms now have move sets again (Thank you, Nathan)
- **[EFM]**, **[FIX]** Sliding Dash not doing damage while in Fight Mode.
- **[FIX]** Tonberry (Spirit) immortality bug.
- **[FIX]** Tonberry (Spirit) no longer has a full bright model.
- **[FIX]** Tonberry (Spirit) now emits light like its hostile counterpart.
- **[FIX]** Tonberry should (hopefully) remove light sources it places upon death.
- **[FIX]** Dream Eaters now respect teleports of any kind and can now join us when dimension hopping.
- **[FIX]** Celestriad fixed from Form Boost back to Fire Boost. (When did that even happen..?)
- **[CHANGE/FIX]** Light and Dark Forms no longer require Quick Run to use their growth abilities. (The abilities themselves outside of form still do however.)
- **[CHANGE]** Added Cooldown to summoning Dream Eaters, it lasts 15 seconds. Is configurable.
- **[CHANGE]** Added Cooldown mentioned above for if the Dream Eater dies.
- **[CHANGE]** Changed 'Darkness Boost' to 'Dark Boost'
- **[CHANGE]** The 'Walker' abilities no longer require you to sprint to activate.
- **[CHANGE]** Zettaflare now requires and consumes Max Drive and Max Focus to cast. Otherwise, cast fails.
- **[CHANGE]** Added config option to turn off Chirithy's chat messages when casting spells. This will be found in the file `kkremind-client.toml`
- **[CHANGE]** EXP Ring now has the **NEW** EXP Converter Ability.
- **[CHANGE]** Light Form should be easier/faster to level now.
- **[CHANGE]** Dream Eaters now register as Party Members.
- **[CHANGE]** Zantetsuken's melding recipe is now Sonic Blade + Stopga or Dark Haze + Stopga to reflect its recipe in BBS.
- **[CHANGE]** Normal attacks now contribute to the Situation Gauge.
- **[CHANGE]** Changed a few attack commands names from "Strike" to "Edge".
- **[CHANGE]** Removed all Strike/Edge Attacks from Moogle Shop.
- **[EFM]**, **[CHANGE]** Blizzard Edge, Water Edge, and Dark Edge now use a jumping attack animation.
- **[REWORK]** Light Step Rework - It now makes you move in an arcing motion
- **[REWORK]** Dark Step Rework - It now makes you -blink- a few blocks in the direction you're facing, leaving an afterimage.

## New Drive Form Magic Loadouts
- Drive Forms can now have their own dedicated magic loadouts.
- Entering a supported Form temporarily replaces your equipped spells with that Form’s configured magic.
- Leaving the Form restores your original spell loadout exactly as it was.
- Form loadouts are protected while active, preventing equipped spells from being removed or replaced.
- The system is fully data-driven, allowing Form-specific spell loadouts to be added or changed through data files without additional hard-coded logic.
- Only Light, Dark, and **LIMIT** Forms have this system! (for now)

### Light Form Loadout:
- Light Strike
- Lightga Surge
- Faith
- Faith (Re:CoM)
- Curaga
- Holyga

### Dark Form Loadout:
- Dark Strike
- Darkga Surge
- Ruinga
- Comet
- Dark Firaga
- Curaga

### Limit Form Loadout:
- Sonic Blade
- Ars Arcanum
- Zantetsuken
- **Shotlock**: Ragnarok

## New Status Effect
### Doom
After the countdown reaches Zero on the afflicted target... they die.

## New Melding Recipes
- Fire Strike: Poison Edge + Fira
- Blizzard Edge: Quick Blitz + Blizzard, Sliding Dash + Blizzard, Quick Blitz + Blizzara, Sliding Dash + Blizzara
- Thunder Strike: Poison Edge + Thundara
- Water Edge: Quick Blitz + Water, Sliding Dash + Water, Quick Blitz + Watera, Sliding Dash + Watera
- Aero Strike: Poison Edge + Aerora
- Light Strike: Quick Blitz + Cure, Sliding Dash + Cure
- Dark Edge: Quick Blitz + Blackout, Sliding Dash + Blackout
- Binding Strike: Quick Blitz + Bind

## New Spell
### **Recall** - *Re:Mind Original*
- Cost: ALL MP
- Teleports you to your respawn point.
- Meld Recipe: Warp + Curaga

## New Attacks
### Poison Edge
- Cost: 20 MP
- Perform a jumping attack that poisons some foes.
- Meld Recipe: Quick Blitz + Poison, Sliding Dash + Poison
### Dark Haze
- Cost: 40 MP
- Cloak yourself in darkness, then charge at faraway enemies. The attack has a chance of dooming them, leaving them five seconds to live.
- Meld Recipe: Zero Gravity + Firaga Surge, Darkga Surge + Sliding Dash
### Sonic Blade
- Cost: 20 MP
- Press the **R** at the right times to perform a series of charging attacks that will reach faraway enemies.
- Meld Recipes: Blitz + Dark Haze
### Chaos Blade
- Cost: 40 MP
- Press the **R** button at the right times to perform a series of charging attacks that blind or bind some foes.
- Meld Recipe: Sonic Blade + Dark Haze
### Aerial Slam
- Cost: 40 MP
- Press the **R** button at the right time to knock enemies into the air and then smack them back down.
- Meld Recipe: Aerora + Fire Strike, Fire Surge + Aero
### Ars Arcanum
- Cost: 75 MP
- Assail enemies with a ferocious string of blows. Press the **R** button at the end to tack on a few extra hits for good measure.
- Meld Recipe: (10% Chance) Quick Blitz + Slot Edge, (5% Chance) Quick Blitz + Cura, (5% Chance) Blizzard Edge + Cura, (5% Chance) Sliding Dash + Blizzard, (5% Chance) Quick Blitz + Blizzard, (5% Chance) Sliding Dash + Blizzara, (5% Chance) Quick Blitz + Blizzara, (10% Chance) Fire Surge + Aerora, Aerial Slam + Blitz

**NOTE:** Ars Arcanum and Sonic Blade's finishing RCs grant Limit Form EXP.

## New Abilities
### **Silence Heart**
- Cost: N/A
- Description: On-hit, afflict the victim with the Silence status effect. *Scales with Critical Boost*
- How to Obtain: Equip *Entropy's Requiem* keychain. (See below)

### **Dark Entourage**
- Cost: N/A
- Description: Summon 2 Shadows to fight by your side.
- How to Obtain: Equip *Callous Cavaloir* keychain. (See below)

### **EXP Converter**
- Cost: 10 AP
- Description: When picking up XP orbs, gain the value of it as Kingdom Keys EXP.
- How to Obtain: EXP Ring

## New Enemies!
### **The Bomb Family** *from the FINAL FANTASY series.*

There are 3 Enemies in this family!
- Bomb, The weakest of the Bomb Family.
- Grenade, the middle ground of the Bomb Family.
- Volcano, the strongest of the Bomb Family.

What do they do?
- Heal and Grow from Fire Damage.
- Take x2 Ice/Water Damage.
- Can cast Fire magic, which spell they cast scales with their level. (Fire, Fira, Firaga, and Firaza)
- Melee Attack.
- Self-Destruct while low HP OR after using any Fire commands/spells 3 times.
- **VERY RARE** Chance to drop either Fire, Fira, or Firaga.
- Chance to drop ANY Blazing synthesis material. Odds increase with Grenades, even higher with Volcano.

Where do they spawn?
- Bombs, near lava in the Overworld and anywhere in the Nether.
- Grenades and Volcanos, **ONLY** in the Nether.

## New Keyblades
### Commission/Gift Keyblades

- Guardian's Light, a **gift** for ***KaliArchon***
  - Description: 
  - Base Stats: 8/8
  - Base Ability: Way to Light
- Callous Cavaloir , a **gift** for **CanaliaRose**
  - Description: First ever recording of this keyblade was seen with an ancient welder who came from nothing. There were never other recordings of it until recently some say it's a keyblade made by someone forgotten, while others say it always existed in people that let go of their hearts.
  - Base Stats: 5/10
  - Base Ability: **[NEW]** Dark Entourage
- Nebula Records, commissioned by DogofTheRoad
  - Description: A very musically themed Keyblade with high emphasis on casting and the rhythm of casting.
  - Base Stats: 8/10
  - Base Ability: MP Walker
- Entropy's Requiem, commissioned by ConstellationDragon
  - Description: This keyblade glimmered into existence upon the shattering of Ragnarök's Requiem, a blade forged tenaciously by a blacksmith, and the freeing weight of its wielder when he was finally liberated from the burdens of his inner Darkness. It strives to show him the Freedom within the Stars.
  - Base Stats: 10/10
  - Base Ability: **[NEW]** Silence Heart
  - Custom Form: Draconic Liberation
- Maddening Corruption, commissioned by TheFeralKitsune
  - Description: A keyblade that's been corrupted by Phazon
  - Base Stats: 13/6
  - Base Ability: Dark Power

### What-If Keyblades
*Inspired by and based on Marduk-Kurios's Designs*
- Union Ultima (Upright)
  - Description: “A supreme Keyblade born from bonds united as one. Its radiant edge carries the strength of every heart that refuses to stand alone.”
  - Base Stats: 8/8
  - Base Ability: Way to Light
- Union Ultima (Reversed)
  - Description: “A supreme Keyblade born from bonds reflected through darkness. Its ominous edge draws strength from the other side of the heart, where even light casts a shadow.”
  - Base Stats: 8/8
  - Base Ability: Dark Power