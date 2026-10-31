const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');
const root = path.resolve(__dirname, '../..');
const jar = fs.readFileSync(path.join(root, 'assets/og mods/LoliEnergistics-1.7.10-client.jar'));
let end = jar.length - 22;
while (jar.readUInt32LE(end) !== 0x06054b50) end--;
let position = jar.readUInt32LE(end + 16);
const entries = [];
for (let index = 0; index < jar.readUInt16LE(end + 10); index++) {
  const length = jar.readUInt16LE(position + 28);
  const name = jar.toString('utf8', position + 46, position + 46 + length);
  const compressed = jar.readUInt32LE(position + 20);
  const method = jar.readUInt16LE(position + 10);
  const offset = jar.readUInt32LE(position + 42);
  entries.push({ name, compressed, method, offset });
  position += 46 + length + jar.readUInt16LE(position + 30) + jar.readUInt16LE(position + 32);
}
function read(entry) {
  const start = entry.offset + 30 + jar.readUInt16LE(entry.offset + 26) + jar.readUInt16LE(entry.offset + 28);
  const bytes = jar.subarray(start, start + entry.compressed);
  return entry.method === 8 ? zlib.inflateRawSync(bytes) : bytes;
}
function parseClass(bytes) {
  let offset = 10;
  const pool = [null];
  const utf8 = [];
  const strings = [];
  for (let i = 1; i < bytes.readUInt16BE(8); i++) {
    const tag = bytes[offset++];
    if (tag === 1) {
      const size = bytes.readUInt16BE(offset); offset += 2;
      pool[i] = bytes.toString('utf8', offset, offset + size); offset += size;
      utf8.push(pool[i]);
    } else if ([7, 8, 16, 19, 20].includes(tag)) {
      pool[i] = { tag, index: bytes.readUInt16BE(offset) }; offset += 2;
    } else if ([3, 4, 9, 10, 11, 12, 17, 18].includes(tag)) { offset += 4; }
    else if ([5, 6].includes(tag)) { offset += 8; i++; }
    else if (tag === 15) { offset += 3; }
    else throw new Error(`Unknown constant tag ${tag}`);
  }
  for (const value of pool) if (value?.tag === 8) strings.push(pool[value.index]);
  const superIndex = bytes.readUInt16BE(offset + 4);
  return { superclass: superIndex ? pool[pool[superIndex].index] : null, strings, utf8 };
}
const classes = entries.filter(e => e.name.endsWith('.class')).map(e => ({ name: e.name, ...parseClass(read(e)) }));
const textures = entries.filter(e => /^assets\/lolienergistics\/textures\/items\/.*\.png$/.test(e.name)).map(e => e.name);
const classMap = new Map(classes.map(c => [c.name.replace(/\.class$/, ''), c]));
function ancestry(c) {
  const seen = new Set();
  const ancestors = [];
  while (c?.superclass && !seen.has(c.superclass)) {
    seen.add(c.superclass); ancestors.push(c.superclass); c = classMap.get(c.superclass);
  }
  return ancestors;
}
const itemClasses = classes.filter(c => ancestry(c).some(s => /(^net\/minecraft\/item\/|Item(Cell|Armor|Tool|Electric|Sword))/.test(s)));
const output = path.join(__dirname, 'lolienergistics');
fs.mkdirSync(output, { recursive: true });
fs.writeFileSync(path.join(output, 'class-index.json'), JSON.stringify(classes, null, 2));
fs.writeFileSync(path.join(output, 'item-classes.json'), JSON.stringify(itemClasses, null, 2));
fs.writeFileSync(path.join(output, 'item-textures.json'), JSON.stringify(textures, null, 2));
if (require.main === module) console.log(JSON.stringify({ classes: classes.length, itemClasses: itemClasses.length, itemTextures: textures.length,
  externalParents: [...new Set(itemClasses.map(c => ancestry(c).at(-1)))],
  readableStrings: itemClasses.flatMap(c => c.strings.filter(s => /[А-Яа-я]{4}|lolienergistics:|cell|vajra|infinity|ingot/.test(s)).map(s => ({class:c.name,text:s}))).slice(0, 45)
}, null, 2));
module.exports = { entries, read, classes, itemClasses };
