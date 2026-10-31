const fs = require('node:fs');
const path = require('node:path');
const { entries, read } = require('./inspect-legacy.cjs');
const project = path.resolve(__dirname, '..');
const archive = new Map(entries.map(entry => [entry.name, entry]));
const materials = new Map();
for (const name of ['ru.lolienergistics.iilIi1laND', 'ru.loliland.le.common.li0iOILAND']) {
  const lines = fs.readFileSync(path.join(__dirname, 'lolienergistics', `${name}.bytecode.txt`), 'utf8').split('\n');
  let recent = [];
  for (const line of lines) {
    const match = line.match(/\/\/ String ([a-z][a-z0-9_-]*)\s*$/);
    if (match) recent.push(match[1]);
    if (/invokestatic.*Method (?:ru\/loliland\/le\/common\/li0iOILAND\.)?I1O1I1LaNd:\(Ljava\/lang\/String;Ljava\/lang\/String;\)Lnet\/minecraft\/item\/Item;/.test(line)) {
      const [id, group] = recent.slice(-2);
      const resource = `assets/lolienergistics/textures/items/${group}/${id}.png`;
      if (id && archive.has(resource)) materials.set(id, { id, group, resource, kind: 'material', legacyRegistration: name });
    }
    if (/invokestatic.*I1O1I1LaNd:\(Lnet\/minecraft\/creativetab\/CreativeTabs;Ljava\/lang\/String;Ljava\/lang\/String;\)Lnet\/minecraft\/item\/Item;/.test(line)) {
      const [group, id] = recent.slice(-2);
      const resource = `assets/lolienergistics/textures/items/${group}/${id}.png`;
      if (id && archive.has(resource)) materials.set(id, { id, group, resource, kind: 'material', legacyRegistration: name });
    }
    if (/invoke|return|pop/.test(line)) recent = [];
  }
}
function copyResource(oldPath, newPath) {
  const entry = archive.get(oldPath);
  if (!entry) throw new Error(`Missing legacy asset ${oldPath}`);
  fs.mkdirSync(path.dirname(newPath), { recursive: true });
  fs.writeFileSync(newPath, read(entry));
  if (archive.has(oldPath + '.mcmeta')) fs.writeFileSync(newPath + '.mcmeta', read(archive.get(oldPath + '.mcmeta')));
}
const imported = [...materials.values()].sort((a, b) => a.id.localeCompare(b.id));
for (const cell of [['advanced_cell', 10000], ['ultimate_cell', 100000], ['quantum_cell', 1000000]]) {
  imported.push({ id: cell[0], group: 'cell', resource: `assets/lolienergistics/textures/items/cell/${cell[0]}.png`, kind: 'fluid_cell', capacity: cell[1] });
}
for (const item of imported) {
  const texture = `item/legacy/${item.group}/${item.id}`;
  copyResource(item.resource, path.join(project, `resources/assets/loli_labs/textures/${texture}.png`));
  const model = path.join(project, `resources/assets/loli_labs/models/item/${item.id}.json`);
  fs.mkdirSync(path.dirname(model), { recursive: true });
  fs.writeFileSync(model, JSON.stringify({ parent: 'minecraft:item/generated', textures: { layer0: `loli_labs:${texture}` } }));
}
const catalogPath = path.join(project, 'resources/assets/loli_labs/legacy-items.json');
const previous = fs.existsSync(catalogPath) ? JSON.parse(fs.readFileSync(catalogPath, 'utf8')) : [];
const ids = new Set(imported.map(item=>item.id));
fs.writeFileSync(catalogPath, JSON.stringify([...imported, ...previous.filter(item=>!ids.has(item.id))], null, 2));
const java = `package tmr.lolilabs;\n\npublic final class LegacyMaterials {\n    public static final String[] IDS = {\n${[...materials.keys()].sort().map(id => `        "${id}"`).join(',\n')}\n    };\n}\n`;
fs.writeFileSync(path.join(project, 'src/tmr/lolilabs/LegacyMaterials.java'), java);
const language = path.join(project, 'resources/assets/loli_labs/lang/en_us.json');
const en = JSON.parse(fs.readFileSync(language, 'utf8'));
for (const item of imported) en[`item.loli_labs.${item.id}`] = item.id.split('_').map(word => word[0].toUpperCase() + word.slice(1)).join(' ');
Object.assign(en, { 'itemGroup.loli_labs': 'Loli Labs', 'item.loli_labs.advanced_cell': 'Advanced Fluid Cell', 'item.loli_labs.ultimate_cell': 'Ultimate Fluid Cell', 'item.loli_labs.quantum_cell': 'Quantum Fluid Cell', 'tooltip.loli_labs.fluid_capacity': 'Capacity: %s mB', 'tooltip.loli_labs.fluid_stored': '%s: %s mB', 'tooltip.loli_labs.fluid_empty': 'Empty' });
fs.writeFileSync(language, JSON.stringify(en, null, 2));
const ruFile = path.join(project, 'resources/assets/loli_labs/lang/ru_ru.json');
const ru = JSON.parse(fs.readFileSync(ruFile, 'utf8'));
Object.assign(ru, { 'itemGroup.loli_labs': 'Loli Labs', 'item.loli_labs.advanced_cell': 'Улучшенная жидкостная ячейка', 'item.loli_labs.ultimate_cell': 'Совершенная жидкостная ячейка', 'item.loli_labs.quantum_cell': 'Квантовая жидкостная ячейка', 'tooltip.loli_labs.fluid_capacity': 'Вместимость: %s мВ', 'tooltip.loli_labs.fluid_stored': '%s: %s мВ', 'tooltip.loli_labs.fluid_empty': 'Пусто' });
fs.writeFileSync(ruFile, JSON.stringify(ru, null, 2));
console.log(`Imported ${materials.size} verified material registrations and 3 fluid cells.`);
