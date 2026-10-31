const fs=require('fs'),path=require('path');
const root=__dirname,resources=path.join(root,'resources-1.4.1');
const replacements={
 advanced_light_absorbing_plate:'ic2modern:irradiant_reinforced_plate',
 bio_fuel:'ic2modern:bio_chaff',coke_coal:'minecraft:coal',
 compressed_diamond:'ic2modern:industrial_diamond',compressed_obsidian:'minecraft:obsidian',
 constantan_dust:'ic2modern:bronze_dust',constantan_ingot:'ic2modern:bronze_ingot',
 crystal_ypsilonium:'ic2modern:quantum_core',energy_crystal:'ic2modern:energy_crystal',
 enriched_sunnarium:'ic2modern:enriched_sunnarium',enriched_sunnarium_alloy:'ic2modern:enriched_sunnarium_alloy',
 gladia_ingot:'ic2modern:iridium_ingot',hdpe_pellet:'ic2modern:rubber',hdpe_rod:'ic2modern:rubber',hdpe_sheet:'ic2modern:rubber',
 hop_graphite_ingot:'ic2modern:carbon_plate',hot_iron_ingot:'minecraft:iron_ingot',ingot_iridium:'ic2modern:iridium_ingot',ingot_rose_gold:'minecraft:gold_ingot',
 iridium_iron_plate:'ic2modern:iridium_iron_plate',irradiant_glass_plane:'ic2modern:irradiant_glass',irradiant_reinforced_plate:'ic2modern:irradiant_reinforced_plate',
 magnetron:'ic2modern:coil',part_waterwheel:'ic2modern:electric_motor',quantum_core:'ic2modern:quantum_core',reinforced_iridium_iron_plate:'ic2modern:reinforced_iridium_iron_plate',
 slag:'ic2modern:slag',soaked_sticks:'minecraft:stick',substrate:'ic2modern:bio_chaff',sunnarium:'ic2modern:sunnarium',sunnarium_alloy:'ic2modern:sunnarium_alloy',sunnarium_part:'ic2modern:sunnarium_piece',
 item_import_upgrade:'ic2modern:pulling_upgrade',item_export_upgrade:'ic2modern:ejector_upgrade'
};
const read=p=>JSON.parse(fs.readFileSync(path.join(resources,p)));
const write=(p,j)=>fs.writeFileSync(path.join(resources,p),JSON.stringify(j,null,2));
const materials=read('assets/loli_labs/legacy-items.json');
for(const id of Object.keys(replacements)){
 for(const p of ['assets/loli_labs/models/item/'+id+'.json','data/loli_labs/recipes/'+id+'.json']){const f=path.join(resources,p);if(fs.existsSync(f))fs.unlinkSync(f);}
}
write('assets/loli_labs/legacy-items.json',materials.filter(x=>!replacements[x.id]));
for(const file of fs.readdirSync(path.join(resources,'data/loli_labs/recipes'))){
 const p='data/loli_labs/recipes/'+file,j=read(p);for(const item of j.ingredients||[])if(replacements[item.item?.replace('loli_labs:','')])item.item=replacements[item.item.slice('loli_labs:'.length)];write(p,j);
}
for(const lang of ['en_us','ru_ru']){const p='assets/loli_labs/lang/'+lang+'.json',j=read(p);for(const id of Object.keys(replacements))delete j['item.loli_labs.'+id];write(p,j);}
// Removed icons are no longer packaged; leave shared textures intact.
const used=new Set();for(const f of fs.readdirSync(path.join(resources,'assets/loli_labs/models/item'))){const j=read('assets/loli_labs/models/item/'+f);for(const v of Object.values(j.textures||{}))used.add(v);}
for(const m of materials.filter(x=>replacements[x.id])){const model=JSON.parse(fs.readFileSync(path.join(root,'resources-1.4.0/assets/loli_labs/models/item/'+m.id+'.json')));for(const v of Object.values(model.textures||{}))if(v.startsWith('loli_labs:')&&!used.has(v)){for(const suffix of ['.png','.png.mcmeta']){const f=path.join(resources,'assets/loli_labs/textures',v.split(':')[1]+suffix);if(fs.existsSync(f))fs.unlinkSync(f);}}}
for(const g of read('assets/loli_labs/gui-assets.json')){const f=path.join(resources,g.target);if(fs.existsSync(f))fs.unlinkSync(f);}
write('assets/loli_labs/gui-assets.json',[]);
write('assets/loli_labs/removed-items.json',replacements);
let source=fs.readFileSync(path.join(root,'src/tmr/lolilabs/LegacyMaterials.java'),'utf8');for(const id of Object.keys(replacements))source=source.replace(new RegExp('"'+id+'",?\\r?\\n','g'),'');fs.writeFileSync(path.join(root,'src/tmr/lolilabs/LegacyMaterials.java'),source);
const aliases='package tmr.lolilabs;\npublic final class RemovedItems {\n public static final java.util.Map<String,String> ALIASES=java.util.Map.ofEntries(\n'+Object.entries(replacements).map(([a,b])=>'java.util.Map.entry("'+a+'","'+b+'")').join(',\n')+');\n}\n';fs.writeFileSync(path.join(root,'src/tmr/lolilabs/RemovedItems.java'),aliases);
for(const file of ['build.ps1','check-runtime.ps1','verify-release.cjs','src/tmr/lolilabs/ReleaseRuntimeChecks.java']){const p=path.join(root,file);let s=fs.readFileSync(p,'utf8').replaceAll('1.4.0','1.4.1').replaceAll('351','317');if(file==='build.ps1')s=s.replace("'LegacyMaterials.java'","'RemovedItems.java', 'LegacyMaterials.java'");if(file==='check-runtime.ps1')s=s.replace('cofh_core-|thermal_foundation-|','');if(file==='verify-release.cjs')s=s.replace("ids.add('item_import_upgrade');ids.add('item_export_upgrade');",'');fs.writeFileSync(p,s);}
const meta=path.join(resources,'META-INF/mods.toml');fs.writeFileSync(meta,fs.readFileSync(meta,'utf8').replace('1.4.0','1.4.1'));
const ex=JSON.parse(fs.readFileSync(path.join(root,'EXCLUSIONS.json')));ex.excludedMaterials=[...new Set([...ex.excludedMaterials,...Object.keys(replacements)])];fs.writeFileSync(path.join(root,'EXCLUSIONS.json'),JSON.stringify(ex,null,2));
console.log('Removed '+Object.keys(replacements).length+' items; recipes and aliases updated.');
