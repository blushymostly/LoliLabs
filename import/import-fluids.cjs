const fs=require('node:fs'),path=require('node:path');
const {entries,read}=require('./inspect-legacy.cjs');
const archive=new Map(entries.map(e=>[e.name,e])),project=path.resolve(__dirname,'..'),assets=path.join(project,'resources/assets/loli_labs');
const catalogFile=path.join(assets,'legacy-items.json'),catalog=JSON.parse(fs.readFileSync(catalogFile));
const enFile=path.join(assets,'lang/en_us.json'),ruFile=path.join(assets,'lang/ru_ru.json');
const en=JSON.parse(fs.readFileSync(enFile)),ru=JSON.parse(fs.readFileSync(ruFile));
function write(rel,data){const f=path.join(assets,rel);fs.mkdirSync(path.dirname(f),{recursive:true});fs.writeFileSync(f,data);}
function copy(old,rel){if(!archive.has(old))throw new Error(old);write(rel,read(archive.get(old)));if(archive.has(old+'.mcmeta'))write(rel+'.mcmeta',read(archive.get(old+'.mcmeta')));}
const names=entries.filter(e=>/^assets\/lolienergistics\/textures\/items\/[a-z_]+_bucket.png$/.test(e.name)).map(e=>path.basename(e.name,'.png').replace(/_bucket$/,''));
const added=[];
for(const name of names){
 const id=name+'_bucket',resource=`assets/lolienergistics/textures/items/${id}.png`;
 copy(resource,`textures/item/legacy/buckets/${id}.png`);
 write(`models/item/${id}.json`,JSON.stringify({parent:'minecraft:item/generated',textures:{layer0:`loli_labs:item/legacy/buckets/${id}`}}));
 for(const suffix of ['still','flow']){
  const old=`assets/lolienergistics/textures/blocks/fluids/${name==='concentrated_mercury'?'molten_iridium_ore':name}_${suffix}.png`;
  copy(old,`textures/block/legacy/fluids/${name}_${suffix}.png`);
  // All legacy animated sheets use square frames; modern Forge needs explicit metadata.
  const png=read(archive.get(old)),w=png.readUInt32BE(16),h=png.readUInt32BE(20);
  if(h>w && !archive.has(old+'.mcmeta')) write(`textures/block/legacy/fluids/${name}_${suffix}.png.mcmeta`,JSON.stringify({animation:{frametime:2,width:w,height:w}}));
 }
 write(`blockstates/${name}.json`,JSON.stringify({variants:{'':{model:'minecraft:block/water'}}}));
 const display=name.split('_').map(w=>w[0].toUpperCase()+w.slice(1)).join(' ');
 en[`item.loli_labs.${id}`]=display+' Bucket';ru[`item.loli_labs.${id}`]='Ведро: '+display;
 en[`fluid.loli_labs.${name}`]=display;ru[`fluid.loli_labs.${name}`]=display;
 added.push({id,kind:'bucket',resource,fluid:name});
}
const ids=new Set(added.map(x=>x.id));fs.writeFileSync(catalogFile,JSON.stringify([...catalog.filter(x=>!ids.has(x.id)),...added],null,2));
fs.writeFileSync(path.join(project,'src/tmr/lolilabs/LegacyFluids.java'),`package tmr.lolilabs;\npublic final class LegacyFluids { public static void register(){\n${names.map(n=>`LabFluids.add("${n}");`).join('\n')}\n} }`);
fs.writeFileSync(enFile,JSON.stringify(en,null,2));fs.writeFileSync(ruFile,JSON.stringify(ru,null,2));console.log(`Imported ${added.length} working fluid buckets with original still/flow textures.`);
