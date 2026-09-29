# Violet 26.2

I don't like having 20 different mods, some conflicting, some being also hacked clients, so I compiled
all the most essential features into one mod - Violet.

Worth noting that the code is heavily based / copied from NoFrills by WhatYouThing, huge credit to them
https://github.com/WhatYouThing/NoFrills

## Features

<details>
<summary>Click to expand feature list</summary>

- **Player**
    - **Break Delay**: Removes block break cooldown. (may be considered a cheat)
    - **Hotbar Scroll**: Allows you to lock hotbar scrolling, or prevent overflow.
    - **No Front Perspective**: Removes front perspective from third person camera view.
    - **Sneak Fix**: Fixes sneaking animation playing twice when rapidly uncrouching.
    - **Use Delay**: Also known as instaplace. (may be considered a cheat or a macro)

- **Movement**
    - **Auto Sprint**: Essentially vanilla autosprint but doesnt disable on death / world change.
    - **Jump Cooldown**: Removes jump cooldown. (may be considered a cheat)

- **Render**
    - **Debug Screen**: Allows you to customize float precision of certain F3 screen components.
    - **Fullbright**: You know it, you love it.
    - **Held Item Tooltip**: Hides the text that shows when switching items.
    - **Low Fire**: Lower Fire texture by changing matrices during rendering. Also has no render option
    - **No Break Particles**: Hides the particles appearing when breaking a block.
    - **Time Changer**: Allows you to change client-side time of the day.
    - **Tooltip Scale**: Allows you to set custom size of the tooltip, or to make it always fit on the screen.
    - **Viewmodel**: Customize your hand / held item render !
    - **Zoom**: Optifine-like zoom.

- **Chat**
    - **Chat Cleaner**: Cancel specific messages / patterns from displaying on chat. Supports regex.
    - **Chat Rules**: Allows you to add an alert when specific message in the chat appears, or just cancel it.
    - **Chat Tweaks**: Copy line key, Keep chat history, Longer chat history.
    - **Command Aliases**: Allows you to add aliases to already existing commands, so like /gm1 -> /gamemode creative.
    - **Command Tooltip**: Reveals the command that hovered click event would run, but at the moment seems to be flawed.
- **Misc**
    - **Basic ClickGui Customization**: Accent color, Open Key, Close behaviour.
    - **Command Keybinds**: Allows you to bind command to a key, along with a server filter (the keybind will work only on specific server provided)
    - **No Fps Limiter**: For some reason there's no way to disable limit fps "option" in minecraft.
    - **No Confirm Screen**: Skips the "Confirm command execution" screen.
    - **No Loading Screen**: Skips "Loading Terrain" screen.
    - **No Server Pack**: ~~Skips server resource packs when enabled.~~ Also allows to dump all resource pack information.
    - **Reconnect button**: Adds a "Reconnect" button to Disconnect screen, also features auto reconnect.
    - **Violet Commands**: Read more below, allows to customize prefix & behavior.
</details>

<details>
<summary>Click to expand command list</summary>

- **Mod Commands** (accessed under `.examplecommand` by default)
    - **`crash`**: Crashes the game.
    - **`hclip (int)`**: Teleports you horizontally in the direction you're looking.
    - **`vclip (int)`**: Teleports you vertically.
    - **`gm (0-3)`**: Changes your client-side gamemode.
    - **`rotation (set/add) (yaw) (pitch)`**: Updates your player rotation to specified data.
    - **`say (string)`**: Says a thing. try out doing `.say <space character>`.
    - **`session`**: Copies your session id (token) to a clipboard. plz dont share it with anyone.
    - **`setpos (x) (y) (z)`**: Teleports you to provided coordinates.
    - **`set(x/y/z) (int)`**: Sets your X/Y/Z to provided value. example use: `.sety 320` -> teleports to y320.
    - **`timer (start/stop) <seconds>`**: Starts a counting down timer that will notify once finished.
    - **`silly`**: Test thing I refuse to delete. currently removes viewmodel limitations.
  

- **Other Commands** (under `/` prefix)
    - **`violet`**: opens the main config menu. Also works as `/v`
    - **`enchantv`**: Essentially better /enchant command, requires creative mode as normal /enchant is handled server-side.

</details>

<details>
<summary>Click to expand HUD element list</summary>

- **FPS**: Displays your FPS.
- **TPS**: Displays the real time TPS of the server, and optionally the average TPS.
- **Lag Meter**: Displays the time since last recevied server tick.
- **Ping**: Displays your ping.
- **Day**: Displays the day that the server world is on.
- **Armor**: Displays your current armor set.
- **Inventory**: Displays the contents of your inventory.

</details>

## Installation

- Download the latest release from [here](https://github.com/CatGirlEmily/violet/releases)
- Additional dependencies needed to launch the mod:
    - [Fabric API](https://modrinth.com/mod/fabric-api)
    - [owo-lib](https://modrinth.com/mod/owo-lib) (Can be also installed just by launching the game without it and clicking the prompt)
- Find the gui under /violet command !

## Incompatibilities

- Some 3d rendering features may not work with shaders.
- Input boxes in the mod's GUI won't detect any changes with the Caxton mod installed.
- The mod may not work with third party loaders such as Lunar or Feather, only Fabric is supported.
    - If you aren't using Fabric already, it is recommended you upgrade to either
      the [Modrinth App](https://modrinth.com/app) or the [Prism Launcher](https://prismlauncher.org/). Automatic mod
      updates, instance management, and most
      importantly: [no selling of your personal data](https://www.lunarclient.com/do-not-sell-or-share-my-personal-information).

## Credits

- [Orbit](https://github.com/MeteorDevelopment/orbit): Extremely fast event system i love you orbit have my kids.
- [NoFrills](https://github.com/WhatYouThing/NoFrills): Literally 90% of the code is based on nofrills.