const fs = require('node:fs');
const path = require('node:path');
const { entries, read, classes } = require('./inspect-legacy.cjs');
const project = path.resolve(__dirname, '..');
const assets = path.join(project, 'resources/assets/loli_labs');
const archive = new Map(entries.map(entry => [entry.name, entry]));
const catalogPath = path.join(assets, 'legacy-items.json');
const catalog = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
const enPath = path.join(assets, 'lang/en_us.json'), ruPath = path.join(assets, 'lang/ru_ru.json');
const en = JSON.parse(fs.readFileSync(enPath, 'utf8')), ru = JSON.parse(fs.readFileSync(ruPath, 'utf8'));
const added = [];
function write(relative, content) { const file = path.join(assets, relative); fs.mkdirSync(path.dirname(file), {recursive:true}); fs.writeFileSync(file, content); }
function copy(old, relative) { if (!archive.has(old)) throw new Error(`Missing ${old}`); write(relative, read(archive.get(old))); if (archive.has(old+'.mcmeta')) write(relative+'.mcmeta',read(archive.get(old+'.mcmeta'))); }
function item(id, kind, name, texture, extra = {}) {
  added.push({ id, kind, name, resource: texture, ...extra });
  en[`item.loli_labs.${id}`] = name; ru[`item.loli_labs.${id}`] = name;
}
function objItem(id, oldModel, texturePath, textureId, filter) {
  let text = read(archive.get(oldModel)).toString('utf8');
  if (filter) {
    let group = '', keep = [];
    for (const line of text.split('\n')) { if (/^[go] /.test(line)) group = line.slice(2).trim(); if (!line.startsWith('f ') || filter(group)) keep.push(line); }
    text = keep.join('\n');
  }
  const vertices = [...text.matchAll(/^v\s+([-\d.eE]+)\s+([-\d.eE]+)\s+([-\d.eE]+)/gm)].map(m => m.slice(1).map(Number));
  const bounds = [0,1,2].map(axis=>[Math.min(...vertices.map(v=>v[axis])), Math.max(...vertices.map(v=>v[axis]))]);
  const scale = 0.95 / Math.max(...bounds.map(([lo,hi])=>hi-lo));
  text = text.replace(/^v\s+([-\d.eE]+)\s+([-\d.eE]+)\s+([-\d.eE]+)/gm, (_,x,y,z)=>'v '+[x,y,z].map((v,axis)=>(Number(v)-(bounds[axis][0]+bounds[axis][1])/2)*scale+0.5).join(' '));
  text = text.replace(/^mtllib .*$/gm, '').replace(/^usemtl .*$/gm,'');
  text = 'mtllib '+id+'.mtl\nusemtl legacy\n'+text;
  write(`models/legacy/${id}.obj`,text);
  write(`models/legacy/${id}.mtl`,`newmtl legacy\nKd 1.0 1.0 1.0\nKa 1.0 1.0 1.0\nd 1.0\nmap_Kd loli_labs:${textureId}\n`);
  copy(texturePath,`textures/${textureId}.png`);
  write(`models/item/${id}.json`,JSON.stringify({parent:'minecraft:item/handheld',loader:'forge:obj',model:`loli_labs:models/legacy/${id}.obj`,flip_v:true,automatic_culling:false,shade_quads:true,display:{gui:{rotation:[0,0,-25],scale:[0.9,0.9,0.9]},thirdperson_righthand:{rotation:[0,90,-35],translation:[0,3,0],scale:[0.85,0.85,0.85]},firstperson_righthand:{rotation:[0,90,-25],translation:[0,2,0],scale:[0.85,0.85,0.85]}}}));
}
const swords = classes.find(c=>c.name==='ru/lolienergistics/IO11O0LANd.class').strings;
for (let i=0;i<swords.length;i++) if (/^sword\d+$/.test(swords[i])) {
  const id=swords[i],name=swords[i+1];
  item(id,'sword',name,`assets/lolienergistics/textures/swords/${id}.png`);
  objItem(id,`assets/lolienergistics/models/swords/${id}.obj`,`assets/lolienergistics/textures/swords/${id}.png`,`legacy/swords/${id}`);
}
const armorStrings=classes.find(c=>c.name==='ru/lolienergistics/lIOILand.class').strings;
for (const style of ['king1','king2','king3','king4','king5','king6','king7','king8','gold','dark','satan','hero','oson','leon']) {
  const index=armorStrings.indexOf(style);
  const names=armorStrings.slice(index+1).filter(s=>/[А-Яа-я]/.test(s)).slice(0,4);
  const texture=/^king|gold/.test(style)?style:style[0].toUpperCase()+style.slice(1);
  const mesh=/^king/.test(style)?'king':style==='gold'?'gold':texture;
  copy(`assets/lolienergistics/textures/armors/${texture}.png`,`textures/legacy/armor/${texture}.png`);
  const parts=['head','chest','legs','feet'],types=['HELMET','CHESTPLATE','LEGGINGS','BOOTS'];
  for (let p=0;p<parts.length;p++) {
    const id=`electric_${parts[p]}_${style}`;
    const oldModel=`assets/lolienergistics/models/armors/${mesh}${/^king|gold/.test(style)?'':p===0?'head':p===1?'body':'leg'}.obj`;
    item(id,'armor',names[p],`assets/lolienergistics/textures/armors/${texture}.png`,{armorType:types[p],armorTexture:texture,armorModel:mesh});
    objItem(id,oldModel,`assets/lolienergistics/textures/armors/${texture}.png`,`legacy/armor/${texture}`,
      group=>p===0?/head/i.test(group):p===1?/body|arm/i.test(group):/leg/i.test(group));
    const original=`models/worn/${mesh}${/^king|gold/.test(style)?'':p===0?'head':p===1?'body':'leg'}.obj`;
    copy(oldModel,original);
  }
}
const upgradeStrings=classes.find(c=>c.name==='ru/lolienergistics/l11lLANd.class').strings;
const upgradeKeys={electric_item_power:'LabPowerLevel',sword_radius:'LabRadius',sword_attack:'LabDamage',sword_fire:'LabFire',sword_vampirism:'LabVampirism',sword_poison:'LabPoison',sword_confusion:'LabConfusion',sword_blindness:'LabBlindness',sword_weakness:'LabWeakness',sword_wither:'LabWither',sword_kickback:'LabKnockback',armor_night_vision:'LabNightVision',armor_clear_potions:'LabClearEffects',armor_solar:'LabSolar',armor_water_protect:'LabWaterBreathing',armor_speed:'LabSpeed',armor_jump:'LabJump',armor_ext:'LabExtinguish',armor_fall:'LabFallProtection',armor_jetpack:'LabFlight',armor_food:'LabFood',armor_regen:'LabRegen',armor_speed_creative_fly:'LabFastFlight'};
for(let i=0;i<upgradeStrings.length;i++) if(/^upgrade_/.test(upgradeStrings[i])) {
  const id=upgradeStrings[i], key=id.replace(/^upgrade_/,'').replace(/\d+$/,''),level=Number(id.match(/\d+$/)?.[0]??0);
  if (!upgradeKeys[key]) throw new Error(`Unsupported upgrade ${id}`);
  const old=`assets/lolienergistics/textures/items/upgrades/${id}.png`;
  if(!archive.has(old)) continue;
  copy(old,`textures/item/legacy/upgrades/${id}.png`);
  write(`models/item/${id}.json`,JSON.stringify({parent:'minecraft:item/generated',textures:{layer0:`loli_labs:item/legacy/upgrades/${id}`}}));
  item(id,'upgrade',upgradeStrings[i+1],old,{upgradeKey:upgradeKeys[key],upgradeValue:level,upgradeTarget:key.startsWith('sword')?'sword':key.startsWith('armor')?'armor':'any'});
}
for(const [id,capacity] of [['portable_charging_module',100000000],['advanced_lappack',10000000],['ultimate_lappack',100000000]]) {
  const old=`assets/lolienergistics/textures/items/${id}.png`;
  copy(old,`textures/item/legacy/${id}.png`);
  write(`models/item/${id}.json`,JSON.stringify({parent:'minecraft:item/generated',textures:{layer0:`loli_labs:item/legacy/${id}`}}));
  item(id,'battery',id.split('_').join(' '),old,{capacity});
}
const existing=new Set(catalog.map(item=>item.id));
const combined=[...catalog,...added.filter(item=>!existing.has(item.id))];
fs.writeFileSync(catalogPath,JSON.stringify(combined,null,2));
let java='package tmr.lolilabs;\n\nimport net.minecraft.world.item.ArmorItem;\n\npublic final class LegacyEquipment {\n    public static void register() {\n';
for(const it of added) {
  const supplier=it.kind==='sword'?'new LabSword()':it.kind==='armor'?`new LabArmor(ArmorItem.Type.${it.armorType}, "${it.armorTexture}")`:it.kind==='upgrade'?`new LabUpgrade("${it.upgradeKey}", ${it.upgradeValue}, "${it.upgradeTarget}")`:`new LabBattery(${it.capacity})`;
  java+=`        LabItems.CATALOG.put("${it.id}", LabItems.ITEMS.register("${it.id}", () -> ${supplier}));\n`;
}
java+='    }\n}\n';
fs.writeFileSync(path.join(project,'src/tmr/lolilabs/LegacyEquipment.java'),java);
Object.assign(en,{'tooltip.loli_labs.energy':'Energy: %s / %s FE','tooltip.loli_labs.charging':'Charges carried equipment. Right-click to toggle.','tooltip.loli_labs.upgrade':'Hold equipment in the other hand and right-click to install.','message.loli_labs.charging_off':'Inventory charging disabled','message.loli_labs.charging_on':'Inventory charging enabled','message.loli_labs.upgrade_installed':'Installed: %s'});
Object.assign(ru,{'tooltip.loli_labs.energy':'Энергия: %s / %s FE','tooltip.loli_labs.charging':'Заряжает предметы в инвентаре. ПКМ — переключить.','tooltip.loli_labs.upgrade':'Возьмите снаряжение во вторую руку и нажмите ПКМ для установки.','message.loli_labs.charging_off':'Зарядка инвентаря выключена','message.loli_labs.charging_on':'Зарядка инвентаря включена','message.loli_labs.upgrade_installed':'Установлено: %s'});
fs.writeFileSync(enPath,JSON.stringify(en,null,2));fs.writeFileSync(ruPath,JSON.stringify(ru,null,2));
console.log(`Imported ${added.filter(x=>x.kind==='sword').length} swords, ${added.filter(x=>x.kind==='armor').length} armor pieces, ${added.filter(x=>x.kind==='upgrade').length} upgrades and 3 charging items.`);
