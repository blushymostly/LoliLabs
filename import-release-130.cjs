const fs=require('fs'),path=require('path');
const root=__dirname,src=path.join(root,'resources-testing'),dst=path.join(root,'resources-1.3.0');
const old=require('./import/inspect-legacy.cjs'),archive=new Map(old.entries.map(e=>[e.name,e]));
const excluded=new Set(['advanced_windmill_blade','windmill_blade','armor_crafting_core','sword_crafting_core','osmium_wire','super_conductor','super_conductor_cover','efficient_injector_upgrade','thermal_protection_upgrade','oreglow_coating','upgrade_recycler','upgrade_cardiacamplifier','upgrade_demineralizer',...Array.from({length:5},(_,i)=>'wind_rotor'+i)]);
const read=p=>JSON.parse(fs.readFileSync(path.join(src,p),'utf8'));
function put(p,value){const f=path.join(dst,p);fs.mkdirSync(path.dirname(f),{recursive:true});fs.writeFileSync(f,typeof value==='string'||Buffer.isBuffer(value)?value:JSON.stringify(value,null,2));}
function asset(oldPath,newPath){if(!archive.has(oldPath))throw Error('Missing '+oldPath);const bytes=old.read(archive.get(oldPath));put(newPath,bytes);if(archive.has(oldPath+'.mcmeta'))fixMeta(newPath,bytes,old.read(archive.get(oldPath+'.mcmeta')).toString());}
let repaired=0;
function fixMeta(p,png,text){const cleaned=text.replace(/^\uFEFF/,'').replace(/,\s*([}\]])/g,'$1').replace(/([{,]\s*)([A-Za-z_]\w*)\s*:/g,'$1"$2":');let meta=JSON.parse(cleaned),a=meta.animation;if(a){const w=png.readUInt32BE(16),h=png.readUInt32BE(20);let fw=a.width||Math.min(w,h),fh=a.height||fw;if(w%fw||h%fh)throw Error('Invalid dimensions '+p);const frames=w/fw*h/fh;if(a.frames)a.frames=a.frames.filter(f=>(typeof f==='number'?f:f.index)>=0&&(typeof f==='number'?f:f.index)<frames);a.frametime=Math.max(1,a.frametime||1);if(!a.frames?.length)delete a.frames;}if(cleaned!==text)repaired++;put(p+'.mcmeta',meta);}
const items=read('assets/loli_labs/legacy-items.json').filter(x=>!excluded.has(x.id));
const ids=new Set(items.map(x=>x.id));
put('assets/loli_labs/legacy-items.json',items);
for(const x of items){const p='assets/loli_labs/models/item/'+x.id+'.json',m=read(p);put(p,m);for(const t of Object.values(m.textures)){const r='assets/loli_labs/textures/'+t.split(':')[1]+'.png',png=fs.readFileSync(path.join(src,r));put(r,png);if(fs.existsSync(path.join(src,r+'.mcmeta')))fixMeta(r,png,fs.readFileSync(path.join(src,r+'.mcmeta'),'utf8'));}const rec=read('data/loli_labs/recipes/'+x.id+'.json');for(const ing of rec.ingredients)if(ing.item?.startsWith('loli_labs:')&&!ids.has(ing.item.split(':')[1]))ing.item='mekanism:alloy_atomic';put('data/loli_labs/recipes/'+x.id+'.json',rec);}
put('pack.mcmeta',read('pack.mcmeta'));
let toml=fs.readFileSync(path.join(src,'META-INF/mods.toml'),'utf8').replace('version="1.2.0"','version="1.3.0"').replace(/description='''[\s\S]*?'''/,"description='''Loli Labs: all-weather panels, energy unifier, solid matter generators and crafting materials for TMR.'''");put('META-INF/mods.toml',toml);
const en=read('assets/loli_labs/lang/en_us.json'),ru=read('assets/loli_labs/lang/ru_ru.json');for(const id of excluded){delete en['item.loli_labs.'+id];delete ru['item.loli_labs.'+id];}
const definitions=[];
function block(id,name,english,kind,rate,top,side,bottom='bottom',matter='',tier=0,batch=1){
 const tex={};for(const [face,file] of Object.entries({top,side,bottom})){const p='assets/loli_labs/textures/block/machines/'+id+'_'+face+'.png';asset('assets/lolienergistics/textures/blocks/'+file+'.png',p);tex[face]='loli_labs:block/machines/'+id+'_'+face;}
 const model={parent:'minecraft:block/cube_bottom_top',textures:tex};
 if(kind===2){
  const png=old.read(archive.get('assets/lolienergistics/textures/blocks/'+top+'.png'));
  const columns=png.readUInt32BE(16)/png.readUInt32BE(20);
  if(columns<6)throw Error('Unexpected machine atlas '+id);
  const faces={};for(const [i,face] of ['down','up','north','south','west','east'].entries())faces[face]={uv:[16*i/columns,0,16*(i+1)/columns,16],texture:'#side',cullface:face};
  model.parent='minecraft:block/block';model.textures.particle=tex.side;model.elements=[{from:[0,0,0],to:[16,16,16],faces}];
 }
 put('assets/loli_labs/models/block/'+id+'.json',model);
 put('assets/loli_labs/models/item/'+id+'.json',{parent:'loli_labs:block/'+id});
 put('assets/loli_labs/blockstates/'+id+'.json',{variants:{'':{model:'loli_labs:block/'+id}}});
 put('data/loli_labs/loot_tables/blocks/'+id+'.json',{type:'minecraft:block',pools:[{rolls:1,entries:[{type:'minecraft:item',name:'loli_labs:'+id,functions:[{function:'minecraft:copy_nbt',source:'block_entity',ops:['LabEnergy','LabInventory','LabProgress'].map(v=>({source:v,target:'BlockEntityTag.'+v,op:'replace'}))}]}],conditions:[{condition:'minecraft:survives_explosion'}]}]});
 en['block.loli_labs.'+id]=english;ru['block.loli_labs.'+id]=name;
 definitions.push({id,kind,rate,matter,tier,batch});
}
const basic=[['dispersion','Дисперсионная'],['gravitomagnetic','Гравимагнитная'],['anisotropic','Анизотропическая'],['fluctuation','Флуктуационная'],['aberration','Аберрационная'],['parametric','Параметрическая'],['chronodynamic','Хронодинамическая'],['antimaterial','Антиматериальная']];
for(let i=0;i<basic.length;i++){const [p,n]=basic[i];block(p+'_solar_panel',n+' солнечная панель',p[0].toUpperCase()+p.slice(1)+' Solar Panel',0,131072*2**i,p+'_solar_panel_top',p+'_solar_panel_side');}
block('sparkling_panel','Искрящаяся солнечная панель','Sparkling Solar Panel',0,33554432,'sparkling_panel_top','sparkling_panel_side','sparkling_panel_bottom');
for(const [i,p] of ['asmodeus','belphegor','beelzebub','leviathan','lucifer_ultimate'].entries()){const names=['Демоническая панель Асмодея','Демоническая панель Бельфегора','Демоническая панель Вельзевула','Демоническая панель Левиафана','Ультимативная демоническая панель'];block(p+'_demonic_panel',names[i],p.replaceAll('_',' ')+' Demonic Panel',0,134217728*2**i,'bloodmagic/'+p+'_demonic_panel_top','bloodmagic/'+p+'_demonic_panel_side');}
for(const [i,p] of ['chaos_guardian','cold_guardian','flame_guardian','infinity_guardian','infinity_demon_guardian','dragon_slayer_ultimate'].entries()){const names=['Панель дракона хаоса','Панель дракона льда','Панель дракона пламени','Панель дракона бесконечности','Панель демонического дракона','Ультимативная панель убийцы драконов'];block(p+'_panel',names[i],p.replaceAll('_',' ')+' Panel',0,268435456*2**i,'draconic_evolution/'+p+'_panel_top','draconic_evolution/'+p+'_panel_side');}
block('energy_unifier','Энергетический объединитель','Energy Unifier',1,0,'unifier_top','unifier_side','unifier_bottom');
// Verified from ContainerMatterSolid.getMatterItemId in the legacy bytecode.
const matters=['matter','matter_loose_pink','matter_luminous_pink','matter_mutated_violet_turquoise','matter_turquoise','matter_purple','matter_luminous_turquoise','matter_luminous_red','matter_loose_blue','matter_sparkling'];
const matterNames=['Твёрдая материя','Рыхлая розовая материя','Светящаяся розовая материя','Мутировавшая фиолетово-бирюзовая материя','Бирюзовая материя','Фиолетовая материя','Светящаяся бирюзовая материя','Светящаяся красная материя','Рыхлая синяя материя','Искрящаяся материя'];
for(let i=0;i<matters.length;i++)ru['item.loli_labs.'+matters[i]]=matterNames[i];
for(let tier=0;tier<10;tier++)for(const batch of [1,6,12]){const t='machine/MatterSolidT'+tier+(batch===1?'':'X'+batch);block('matter_solid_t'+tier+'_x'+batch,'Генератор твёрдой материи T'+tier+' ×'+batch,'Solid Matter Generator T'+tier+' x'+batch,2,0,t,t,t,matters[tier],tier,batch);}
function recipe(id,ingredients){put('data/loli_labs/recipes/'+id+'.json',{type:'minecraft:crafting_shapeless',ingredients:ingredients.map(item=>({item})),result:{item:'loli_labs:'+id}});}
const panels=definitions.filter(x=>x.kind===0);
for(let i=0;i<panels.length;i++){const p=panels[i];const prev=i===0?'solarflux:sp_8':'loli_labs:'+panels[i-1].id;const core=i<8?'loli_labs:'+(basic[i][0]==='chronodynamic'?'chromodynamic':basic[i][0])+'_core':i===8?'loli_labs:sparkling_core':'mekanism:alloy_atomic';recipe(p.id,[prev,core,'mekanism:energy_tablet','avaritia:infinity_ingot']);}
recipe('energy_unifier',['loli_labs:core_unifier_neutronium','mekanism:steel_casing','mekanism:energy_tablet','minecraft:chest']);
for(const d of definitions.filter(x=>x.kind===2)){const prev=d.batch!==1?'loli_labs:matter_solid_t'+d.tier+'_x'+(d.batch===6?1:6):d.tier===0?'mekanism:steel_casing':'loli_labs:matter_solid_t'+(d.tier-1)+'_x1';recipe(d.id,[prev,'loli_labs:core_unifier_fluid_matter','mekanism:energy_tablet',d.batch===1?'minecraft:diamond':d.batch===6?'minecraft:emerald':'minecraft:nether_star']);}
// Matter is produced by generators, not by a dye-based crafting shortcut.
for(const id of matters){const f=path.join(dst,'data/loli_labs/recipes/'+id+'.json');if(fs.existsSync(f))fs.unlinkSync(f);}
Object.assign(ru,{'gui.loli_labs.energy':'Энергия: %s / %s FE','gui.loli_labs.generation':'Генерация: %s FE/тик','gui.loli_labs.cost':'Цикл: %s FE → %s шт.','gui.loli_labs.panels':'Положите панели в слоты','gui.loli_labs.output':'Выход материи (автовывод трубами)','gui.loli_labs.charge':'Слоты зарядки FE-предметов','tooltip.loli_labs.all_weather':'Работает всегда: день, ночь, любая погода','tooltip.loli_labs.unifier':'27 слотов, до 64 панелей в каждом. Мощность суммируется.'});
Object.assign(en,{'gui.loli_labs.energy':'Energy: %s / %s FE','gui.loli_labs.generation':'Generation: %s FE/t','gui.loli_labs.cost':'Cycle: %s FE → %s items','gui.loli_labs.panels':'Insert panels into slots','gui.loli_labs.output':'Matter output (pipe extraction supported)','gui.loli_labs.charge':'FE item charging slots','tooltip.loli_labs.all_weather':'Always active: day, night, any weather','tooltip.loli_labs.unifier':'27 slots, up to 64 panels each. Generation is summed.'});
asset('assets/lolienergistics/textures/items/vajra.png','assets/loli_labs/textures/item/vajra.png');
put('assets/loli_labs/models/item/vajra.json',{parent:'minecraft:item/handheld',textures:{layer0:'loli_labs:item/vajra'}});
recipe('vajra',['mekanism:atomic_disassembler','loli_labs:quantum_core','avaritia:infinity_ingot']);
en['item.loli_labs.vajra']='Vajra';ru['item.loli_labs.vajra']='Ваджра';
put('assets/loli_labs/lang/en_us.json',en);put('assets/loli_labs/lang/ru_ru.json',ru);put('assets/loli_labs/machines.json',definitions);
put('data/minecraft/tags/blocks/mineable/pickaxe.json',{replace:false,values:definitions.map(d=>'loli_labs:'+d.id)});
const tags=path.join(src,'data/forge/tags/items');function walk(dir,rel=''){for(const e of fs.readdirSync(dir,{withFileTypes:true})){const p=rel+e.name;if(e.isDirectory())walk(path.join(dir,e.name),p+'/');else{const t=read('data/forge/tags/items/'+p);t.values=t.values.filter(v=>typeof v==='string'&&(!v.startsWith('loli_labs:')||ids.has(v.split(':')[1])));if(t.values.length)put('data/forge/tags/items/'+p,t);}}}walk(tags);
fs.writeFileSync(path.join(root,'src/tmr/lolilabs/LegacyMaterials.java'),'package tmr.lolilabs;\npublic final class LegacyMaterials { public static final String[] IDS = {\n'+items.map(x=>'"'+x.id+'"').join(',\n')+'\n}; }');
const lines=definitions.map(d=>'        add("'+d.id+'", '+d.kind+', '+d.rate+'L, "'+d.matter+'", '+d.tier+', '+d.batch+');');
fs.writeFileSync(path.join(root,'src/tmr/lolilabs/MachineDefinitions.java'),'package tmr.lolilabs;\npublic final class MachineDefinitions { public static void register() {\n'+lines.join('\n')+'\n} private static void add(String id,int kind,long rate,String matter,int tier,int batch){LabMachines.add(id,kind,rate,matter,tier,batch);} }');
fs.writeFileSync(path.join(root,'EXCLUSIONS.json'),JSON.stringify({request:'2026-10-04',excludedMaterials:[...excluded],excludedMechanics:['custom swords','custom armor','equipment upgrades','windmills','wires and superconductors','wind turbines and rotors','Thaumcraft / IC2 / Mekanism duplicate equipment','quantum miner','drill'],requiredException:'vajra',remainingItems:'deferred/pending-items.json'},null,2));
console.log(items.length+' materials; '+definitions.length+' machines; repaired '+repaired+' malformed animation files.');
