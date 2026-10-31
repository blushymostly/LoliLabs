const fs=require('node:fs'),path=require('node:path');
const {entries,read}=require('./inspect-legacy.cjs');
const project=path.resolve(__dirname,'..'),assets=path.join(project,'resources/assets/loli_labs');
const archive=new Map(entries.map(e=>[e.name,e]));
const catalogPath=path.join(assets,'legacy-items.json'),catalog=JSON.parse(fs.readFileSync(catalogPath));
const enFile=path.join(assets,'lang/en_us.json'),ruFile=path.join(assets,'lang/ru_ru.json');
const en=JSON.parse(fs.readFileSync(enFile)),ru=JSON.parse(fs.readFileSync(ruFile));
const added=[];
function copy(old,to){if(!archive.has(old))throw new Error(old);let dest=path.join(assets,to);fs.mkdirSync(path.dirname(dest),{recursive:true});fs.writeFileSync(dest,read(archive.get(old)));if(archive.has(old+'.mcmeta'))fs.writeFileSync(dest+'.mcmeta',read(archive.get(old+'.mcmeta')));}
function add(id,kind,old,supplier,name){
 copy(old,`textures/item/legacy/extra/${id}.png`);
 let model=path.join(assets,`models/item/${id}.json`);fs.mkdirSync(path.dirname(model),{recursive:true});
 fs.writeFileSync(model,JSON.stringify({parent:kind==='tool'?'minecraft:item/handheld':'minecraft:item/generated',textures:{layer0:`loli_labs:item/legacy/extra/${id}`}}));
 added.push({id,kind,resource:old,supplier});en[`item.loli_labs.${id}`]=id.split('_').map(w=>w[0].toUpperCase()+w.slice(1)).join(' ');ru[`item.loli_labs.${id}`]=name??en[`item.loli_labs.${id}`];
}
const root='assets/lolienergistics/textures/items/';
for(const [id,png,area,name] of [
 ['atomic_disassembler','atomic_disassembler',false,'Атомный разборщик'],
 ['vajra','vajra',false,'Ваджра'],['legendary_vajra','asp/legendary_vajra',true,'Легендарная Ваджра'],
 ['advanced_drill','advanced_drill',true,'Улучшенный бур'],['advanced_chainsaw','advanced_chainsaw',false,'Улучшенная цепная пила'],
 ['infinity_pickaxe_ultra','infinity_ultra_pickaxe',true,'Бесконечная ультракирка'],
 ['infinity_ultra_hammer','infinity_ultra_hammer',true,'Бесконечный ультрамолот']]) {
 // The disassembler uses a 3D item model in the original, with no standalone item icon.
 const old=archive.has(root+png+'.png')?root+png+'.png':'assets/lolienergistics/textures/models/item/AtomicDisassembler.png';
 add(id,'tool',old,id.startsWith('infinity_')?'new LabInfinityPickaxe()':`new LabMiningTool(${area})`,name);
}
add('gravi_tool','tool',root+'gravi_tool_wrench.png','new LabConfigurator()','Гравитационный инструмент');
add('quantum_assembly_tool','tool',root+'quantum_assembly_tool.png','new LabConfigurator()','Квантовый сборочный инструмент');
add('manual_pump','tool',root+'tools/manual_pump.png','new LabPump()','Ручной насос');
add('relocator','tool',root+'relocator.png','new LabRelocator()','Релокатор');
add('flask','fluid_cell',root+'flask.png','new FluidCellItem(1000)','Колба');
for(const [id,type,icon,worn,solar,features,name] of [
 ['advanced_solar_helmet','HELMET','asp/advanced_solar_helmet','advancedSolarHelmet',1,[],'Улучшенный солнечный шлем'],
 ['hybrid_solar_helmet','HELMET','asp/hybrid_solar_helmet','hybridSolarHelmet',2,[],'Гибридный солнечный шлем'],
 ['ultimate_solar_helmet','HELMET','asp/ultimate_solar_helmet','ultimateSolarHelmet',3,[],'Совершенный солнечный шлем'],
 ['advanced_jetpack','CHESTPLATE','advanced_jetpack','advanced_jetpack',0,['LabFlight','LabFallProtection'],'Улучшенный джетпак'],
 ['gravi_chestplate','CHESTPLATE','gravi_chestplate','gravi_chestplate',0,['LabFlight','LabFastFlight','LabFallProtection'],'Гравитационный нагрудник'],
 ['advanced_nano_chestplate','CHESTPLATE','advanced_nano_chestplate','advanced_nano_chestplate',0,['LabFlight','LabFallProtection'],'Улучшенный нанонагрудник'],
 ['electric_boots_traveller','BOOTS','armor/boots_electric','electricboots',0,['LabSpeed','LabJump','LabManaDiscount'],'Электрические ботинки путешественника'],
 ['nano_boots_traveller','BOOTS','armor/boots_nano','nanobootstravel',0,['LabSpeed','LabJump','LabFallProtection','LabManaDiscount'],'Наноботинки путешественника'],
 ['quantum_boots_traveller','BOOTS','armor/boots_quantum','quantumbootstravel',0,['LabSpeed','LabJump','LabFallProtection','LabManaDiscount'],'Квантовые ботинки путешественника'],
 ['infused_quantum_chest','CHESTPLATE','armor/armor_quantum_chest','quantum',0,['LabFlight','LabFallProtection','LabManaDiscount'],'Наполненный квантовый нагрудник'],
 ['electric_goggles','HELMET','armor/goggles_electric','electricgoggles',0,['LabNightVision','LabManaDiscount'],'Электрические очки'],
 ['nano_thaumic_helmet','HELMET','armor/goggles_nano','thaumicnanohelmet',0,['LabNightVision','LabManaDiscount'],'Таумовый наношлем'],
 ['quantum_thaumic_helmet','HELMET','armor/goggles_quantum','thaumicquantumhelmet',0,['LabNightVision','LabWaterBreathing','LabClearEffects','LabManaDiscount'],'Таумовый квантовый шлем'],
 ['solar_helmet_revealing','HELMET','armor/goggles_solar','solarrevealinghelmet',3,['LabNightVision','LabManaDiscount'],'Солнечный шлем откровения']]) {
 const old=`assets/lolienergistics/textures/${archive.has(`assets/lolienergistics/textures/armors/${worn}.png`)?'armors':'models'}/${worn}.png`;
 copy(old,`textures/legacy/armor/${worn}.png`);
 add(id,'tech_armor',root+icon+'.png',`new LabTechArmor(ArmorItem.Type.${type}, "${worn}", ${solar}${features.map(f=>`, "${f}"`).join('')})`,name);
}
for(const group of ['base','quant','integral'])for(const name of ['microchip','transistor','capacitor','diode','resistor','conductive_contour']) add(`${group}_${name}`,'material',root+`components/${group}/${name}.png`,'new Item(new Item.Properties())');
for(const name of ['infinity_nugget','infinity_dust','infinity_dragon_awakened_heart','neutronium_catalyst','infinity_singularity','red_energon','dark_energon','energy_pulsating_crystal','energized_crystal_fluorite','energized_crystal_ametrine','energized_crystal_chrysolite'])add(name,'material',root+name+'.png','new Item(new Item.Properties())');
add('upgrade_armor_fly','upgrade',root+'upgrades/upgrade_armor_fly.png','new LabUpgrade("LabFlight",0,"armor")','Улучшение: полёт');
const ids=new Set(added.map(x=>x.id));
fs.writeFileSync(catalogPath,JSON.stringify([...catalog.filter(x=>!ids.has(x.id)),...added],null,2));
fs.writeFileSync(path.join(project,'src/tmr/lolilabs/LegacyTools.java'),`package tmr.lolilabs;\nimport net.minecraft.world.item.ArmorItem;\nimport net.minecraft.world.item.Item;\npublic final class LegacyTools { public static void register() {\n${added.map(x=>`LabItems.CATALOG.put("${x.id}",LabItems.ITEMS.register("${x.id}",()->${x.supplier}));`).join('\n')}\n} }\n`);
Object.assign(en,{'message.loli_labs.area_on':'3x3 mining enabled','message.loli_labs.area_off':'3x3 mining disabled','message.loli_labs.pump_radius':'Pump area: %s x %s','message.loli_labs.pumped':'Collected: %s mB','message.loli_labs.point_saved':'Saved teleport point %s','message.loli_labs.point_selected':'Selected teleport point %s','tooltip.loli_labs.relocator':'Sneak-use a block: save point. Sneak-use air: select point. Use: teleport (100,000 FE).'});
Object.assign(ru,{'message.loli_labs.area_on':'Добыча 3×3 включена','message.loli_labs.area_off':'Добыча 3×3 выключена','message.loli_labs.pump_radius':'Размер области насоса: %s','message.loli_labs.pumped':'Собрано: %s мВ','message.loli_labs.point_saved':'Сохранена точка телепортации %s','message.loli_labs.point_selected':'Выбрана точка телепортации %s','tooltip.loli_labs.relocator':'Shift+ПКМ по блоку — сохранить точку; в воздухе — выбрать. ПКМ — телепорт (100 000 FE).'});
en['message.loli_labs.pump_radius']='Pump area size: %s';
fs.writeFileSync(enFile,JSON.stringify(en,null,2));fs.writeFileSync(ruFile,JSON.stringify(ru,null,2));
console.log(`Imported ${added.length} additional tools, technical armor and component variants.`);
