// Original images remain byte-for-byte unchanged; the screen composes regions.
const fs=require('fs'),path=require('path');
const root=__dirname,dst=path.join(root,'resources-1.3.1');
if(!fs.existsSync(dst))fs.cpSync(path.join(root,'resources-1.3.0'),dst,{recursive:true});
const old=require('./import/inspect-legacy.cjs'),entries=new Map(old.entries.map(e=>[e.name,e]));
const manifest=[];
function copy(name,id){const original='assets/lolienergistics/textures/gui/'+name;
 const target='assets/loli_labs/textures/gui/legacy/'+id+'.png';
 fs.mkdirSync(path.dirname(path.join(dst,target)),{recursive:true});fs.writeFileSync(path.join(dst,target),old.read(entries.get(original)));manifest.push({original,target});}
copy('gui_solar_panel.png','solar');copy('gui_unifier.png','unifier');
for(let tier=0;tier<10;tier++)for(const batch of [1,6,12])copy('GuiMatterSolidT'+tier+'X'+batch+'.png','matter_solid_t'+tier+'_x'+batch);
fs.writeFileSync(path.join(dst,'assets/loli_labs/gui-assets.json'),JSON.stringify(manifest,null,2));
const meta=path.join(dst,'META-INF/mods.toml');fs.writeFileSync(meta,fs.readFileSync(meta,'utf8').replace('version="1.3.0"','version="1.3.1"'));
for(const language of ['ru_ru','en_us']){const file=path.join(dst,'assets/loli_labs/lang/'+language+'.json'),lang=JSON.parse(fs.readFileSync(file));Object.assign(lang,language==='ru_ru'?{'gui.loli_labs.charge':'Зарядка FE-предметов','gui.loli_labs.output':'Выход материи','gui.loli_labs.panels':'Панели в объединителе'}:{'gui.loli_labs.charge':'FE item charging','gui.loli_labs.output':'Matter output','gui.loli_labs.panels':'Combined panels'});fs.writeFileSync(file,JSON.stringify(lang,null,2));}
console.log('Copied 32 original GUI backgrounds without modifying PNGs.');
