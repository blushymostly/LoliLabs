# Источники GUI и поведения панелей, 1.4.2

Рабочая папка: `D:/Prism/instances/TMR-dev/loli-labs`.

## OG LoliEnergistics

Проверен `TMR-dev/assets/og mods/LoliEnergistics-1.7.10-client.jar`.
Присланный пользователем `TMR-dev/assets/gui/gui_solar_panel.png` побайтно совпадает
с `assets/lolienergistics/textures/gui/gui_solar_panel.png` внутри этого JAR.
Выбрана именно эта классическая бирюзовая ветка, а не rainbow `gui_solar_panel_1`
и не альтернативные `1GuiMatterSolid...`.

В JAR также есть отдельная, более новая программная Luminous UI-ветка:
`loliland.luminous.ui.ll0iOlLAnd`, `loliland.luminous.ui.i11IllAnd`,
`loliland.common.obf.lIl0iilland`. Она исследована, но её оформление не подменяет
предоставленный пользователем образец. PNG являются ресурсами, а не Java-исходниками.

Классические контейнеры определяют назначение и позиции слотов:

- `ContainerEnergySolarPanel`: шесть зарядок. Повторяющиеся координаты последних
  слотов исправлены: зарядки не перекрываются.
- `ContainerEnergyUnifierBasic`: 4 ряда ×9 =36 слотов.
- `ContainerMatterSolid`: пары вход/выход ×1/×6/×12, четыре улучшения и зарядка справа.

Все 34 выбранные GUI PNG перечислены в `resources-1.4.2/assets/loli_labs/gui-assets.json`.
Верификатор сравнивает их содержимое с JAR побайтно.

## IC2Modern — актуальные основные исходники

- `src/com/ic2ceu/icceu/MachineBlock.java`: размещение, инструменты, обновление кабелей.
- `MachineEntity.java`: facing, направленный EU-выход, enabled/muted и сохранение.
- `ToolGrid.java`, `ToolHighlight.java`: выбор стороны и графические подсказки.
- `tool/ToolHelper.java`: корректный расход инструмента и звук действия.
- `mui/AdvancedSolarUI.java`, `mui/MachineUIPanel.java`: ModularSlot, ProgressWidget,
  синхронизация и переключатель питания.
- `resources/assets/ic2modern/models/block/quantum_solar_panel_*.json`: коллектор сверху
  и независимый от него маркер выхода на противоположной facing стороне.

## GregTech Modern — локальные основные исходники

- `common/machine/electric/BatteryBufferMachine.java`, `buildMainUI`:
  прогресс энергии, группа зарядных слотов и динамические подсказки.
- `api/mui/GTGuiScreen.java`: стандартный экран ModularUI.
- `api/item/tool/IToolGridHighlight.java`: поведение сетки инструментов.

Используются API и шаблоны построения ModularUI; обязательной зависимости
Loli Labs от GregTech нет. Код подсказок в `LabPanelClient.java` помечен
LGPL-3.0-or-later, как исходные реализации IC2Modern/GTCEu.
