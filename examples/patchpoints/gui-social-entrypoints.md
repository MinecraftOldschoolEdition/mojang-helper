# GUI Social Entrypoints

Target files:

- `GuiMainMenu.java`
- `GuiIngameMenu.java`
- `GuiPlayersAndChat.java`
- `GameSettings.java`
- `Minecraft.java`
- `GuiScreen.java`

```java
// GameSettings: keep the keybinding stable so resource packs can localize
// key.social and users get one predictable global Social key.
public KeyBinding keyBindSocial = new KeyBinding("key.social", Keyboard.KEY_O);
```

```java
// Menus: open the actual friends list screen, not a placeholder landing page.
this.mc.openGuiOrFallback(GuiScreenKeys.FRIENDS_LIST, new GuiFriendsList(this), this);
```

```java
// Gameplay input: open Social from normal play.
if (this.gameSettings.keyBindSocial.isPressed()) {
    this.displayGuiScreen(new GuiFriendsList(null));
}
```

```java
// GuiScreen.handleInput: allow the hotkey from menus, but skip text-entry
// surfaces so typing "o" into a field does not yank the player into Social.
if (!isTextEntryScreen() && Keyboard.getEventKeyState()
        && Keyboard.getEventKey() == this.mc.gameSettings.keyBindSocial.keyCode) {
    this.mc.displayGuiScreen(new GuiFriendsList(this));
    return;
}
```
