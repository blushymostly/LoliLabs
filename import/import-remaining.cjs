const fs=require('node:fs'),path=require('node:path');
const {entries,read}=require('./inspect-legacy.cjs');
const project=path.resolve(__dirname,'..'),assets=path.join(project,'resources/assets/loli_labs'),archive=new Map(entries.map(e=>[e.name,e]));
const catalogFile=path.join(assets,'legacy-items.json'),catalog=JSON.parse(fs.readFileSync(catalogFile));
const enFile=path.join(assets,'lang/en_us.json'),ruFile=path.join(assets,'lang/ru_ru.json'),en=JSON.parse(fs.readFileSync(enFile)),ru=JSON.parse(fs.readFileSync(ruFile));
const added=[];
function copy(old,to){if(!archive.has(old))throw new Error(old);let f=path.join(assets,to);fs.mkdirSync(path.dirname(f),{recursive:true});fs.writeFileSync(f,read(archive.get(old)));if(archive.has(old+'.mcmeta'))fs.writeFileSync(f+'.mcmeta',read(archive.get(old+'.mcmeta')));}
function add(id,kind,old,supplier,name){
 copy(old,`textures/item/legacy/remaining/${id}.png`);
 let model=path.join(assets,`models/item/${id}.json`);fs.mkdirSync(path.dirname(model),{recursive:true});fs.writeFileSync(model,JSON.stringify({parent:'minecraft:item/generated',textures:{layer0:`loli_labs:item/legacy/remaining/${id}`}}));
 added.push({id,kind,resource:old,supplier});en[`item.loli_labs.${id}`]=id.split('_').map(w=>w[0].toUpperCase()+w.slice(1)).join(' ');ru[`item.loli_labs.${id}`]=name??en[`item.loli_labs.${id}`];
}
const root='assets/lolienergistics/textures/items/';
add('seal_of_sealing','tool',root+'consumable/seal_of_sealing_empty.png','new LabSeal()','Печать запечатывания');
add('focus_energy_ball','mana_tool',root+'focus/energy_ball.png','new LabManaFocus()','Фокус: энергетический шар');
for(const name of ['upgrade_recycler','upgrade_cardiacamplifier','upgrade_demineralizer'])add(name,'material',root+'improvements/'+name+'.png','new Item(new Item.Properties())');
add('upgrade_quantum_generator','eternal_generator',root+'improvements/upgrade_quantum_generator.png','new LabEternalGenerator()','Генератор вечной энергии');
for(const [name,style,label] of [['box_nano','king1','Комплект наноброни'],['box_quantum','king8','Комплект квантовой брони']])add(name,'armor_box',`assets/lolienergistics/textures/models/boxes/${name}.png`,`new LabArmorBox("${style}")`,label);
added.push({id:'electric_scribing_tool',kind:'mana_tool',supplier:'new LabScribingWand()',resource:'botania:item/twig_wand',adaptation:'Powered Botania binding wand'});
fs.writeFileSync(path.join(assets,'models/item/electric_scribing_tool.json'),JSON.stringify({parent:'botania:item/twig_wand'}));
en['item.loli_labs.electric_scribing_tool']='Electric Scribing Tool';ru['item.loli_labs.electric_scribing_tool']='Электрический письменный инструмент';
for(const name of ['upgrade_gas_puller','upgrade_gas_ejector','upgrade_liquid_tightener'])add(name,'io_upgrade',root+'improvements/'+name+'.png',`new LabIOUpgrade(${name.includes('ejector')},TransmissionType.${name.includes('gas')?'GAS':'FLUID'})`);
for(const [base,count,type] of [['upgrade_speed_',2,'SPEED'],['multiply_module_',3,'SPEED'],['upgrade_resource_gen',5,'STONE_GENERATOR'],['upgrade_singular_buck',3,'ENERGY'],['particle_accelerator_',3,'SPEED']]) {
 for(let i=base.endsWith('_')?1:0;i<(base.endsWith('_')?count+1:count);i++)add(base+i,'machine_upgrade',root+'improvements/'+base+i+'.png',`new LabMachineUpgrade(Upgrade.${type},${Math.min(8,2**i)})`);
}
for(let i=2;i<=5;i++)add(`drill_bit_upgrade_${i}`,'machine_upgrade',root+`improvements/drill_bit_upgrade_${i}.png`,`new LabMachineUpgrade(Upgrade.SPEED,${Math.min(8,2**(i-1))})`);
// Mining luck uses actual vanilla Fortune enchantments on compatible powered tools.
for(let i=1;i<=3;i++)add(`drill_luck_upgrade_${i}`,'tool_upgrade',root+`improvements/drill_luck_upgrade_${i}.png`,`new LabFortuneUpgrade(${i})`);
for(const name of ['copper','iridium'])add(name+'_radiator','material',root+`radiator/${name}.png`,'new Item(new Item.Properties())');
for(let i=1;i<=3;i++)add('thermal_insulator'+i,'material',root+`thermal_insulator${i}.png`,'new Item(new Item.Properties())');
for(let i=0;i<=4;i++)add('wind_rotor'+i,'material',root+`rotors/wind_rotor${i}.png`,'new Item(new Item.Properties())');
for(let i=0;i<=2;i++)add('grinding_ball'+i,'material',root+`grinding_ball${i}.png`,'new Item(new Item.Properties())');
for(const e of entries.filter(e=>/^assets\/lolienergistics\/textures\/items\/singularity\/molten_/.test(e.name)&&e.name.endsWith('.png'))){let name=path.basename(e.name,'.png');add('fluid_singularity_'+name,'material',e.name,'new Item(new Item.Properties())');}
add('fluid_singularity','material',root+'singularity/fluid_singularity.png','new Item(new Item.Properties())','Жидкостная сингулярность');
for(const e of entries.filter(e=>/^assets\/lolienergistics\/textures\/items\/laser_focus\/.+\.png$/.test(e.name))){let color=path.basename(e.name,'.png');add('laser_focus_'+color,'material',e.name,'new Item(new Item.Properties())');}
add('neutron_star','battery',root+'neutron_star.png','new LabLargeBattery(1000000000L)','Нейтронная звезда');
add('charged_neutron_star','battery',root+'charged_neutron_star.png','new LabLargeBattery(1000000000000L)','Заряженная нейтронная звезда');
for(const [name,capacity] of [['astrolion',1000000000],['sun',100000000000],['sirius',50000000000000],['zodius',1000000000000000]]) {
 for(const [suffix,state] of [['_micro_star','normal'],['_core','core'],['_dead','dead']])add(name+suffix,state==='dead'?'material':'stellar_battery',`assets/lolienergistics/textures/models/microstar/${name}/${state}/base.png`,state==='dead'?'new Item(new Item.Properties())':`new LabLargeBattery(${capacity}L)`);
}
const ids=new Set(added.map(x=>x.id));fs.writeFileSync(catalogFile,JSON.stringify([...catalog.filter(x=>!ids.has(x.id)),...added],null,2));
fs.writeFileSync(path.join(project,'src/tmr/lolilabs/LegacyRemaining.java'),`package tmr.lolilabs;\nimport net.minecraft.world.item.Item;\nimport mekanism.api.Upgrade;\nimport mekanism.common.lib.transmitter.TransmissionType;\npublic final class LegacyRemaining { public static void register(){\n${added.map(x=>`LabItems.CATALOG.put("${x.id}",LabItems.ITEMS.register("${x.id}",()->${x.supplier}));`).join('\n')}\n} }`);
en['tooltip.loli_labs.machine_upgrade']='Sneak-use a machine: install up to %s native upgrades. Machine limits apply.';
ru['tooltip.loli_labs.machine_upgrade']='Shift+ПКМ по механизму: установить до %s улучшений. Действуют лимиты механизма.';
fs.writeFileSync(enFile,JSON.stringify(en,null,2));fs.writeFileSync(ruFile,JSON.stringify(ru,null,2));console.log(`Imported ${added.length} remaining variants and adapted upgrade/tool types.`);
