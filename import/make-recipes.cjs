const fs=require('node:fs'),path=require('node:path');
const project=path.resolve(__dirname,'..'),catalog=JSON.parse(fs.readFileSync(path.join(project,'resources/assets/loli_labs/legacy-items.json')));
const ids=new Set(catalog.map(x=>x.id)),recipes=new Map(),signatures=new Map(),notes=[];
const L=id=>'loli_labs:'+id;
const colors=['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black'];
function craft(id,ingredients,count=1){
 if(!ids.has(id))throw new Error('Unknown output '+id);
 if(ingredients.length>9 || ingredients.length<1)throw new Error('Invalid ingredient count '+id);
 const signature=ingredients.slice().sort().join('|');
 if(signatures.has(signature)&&signatures.get(signature)!==id) return false;
 signatures.set(signature,id);
 recipes.set(id,{type:'minecraft:crafting_shapeless',ingredients:ingredients.map(item=>({item})),result:{item:L(id),count}});return true;
}
function unique(id,ingredients,count=1){
 if(craft(id,ingredients,count))return;
 for(const color of colors)if(craft(id,[...ingredients,'minecraft:'+color+'_dye'],count))return;
 for(let a=0;a<16;a++)for(let b=a;b<16;b++)if(craft(id,[...ingredients,'minecraft:'+colors[a]+'_dye','minecraft:'+colors[b]+'_dye'],count))return;
 throw new Error('Conflicting recipe '+id);
}
const bases={asp:'minecraft:glowstone_dust',core:'mekanism:advanced_control_circuit',consumable:'mekanism:alloy_infused',thaumcraft:'botania:manasteel_ingot',forestry:'minecraft:honeycomb',botania:'botania:mana_pearl',divinerpg:'minecraft:echo_shard',galacticraft:'ae2:singularity',nevermine:'botania:dragonstone',bloodmagic:'botania:rune_mana',mekanism:'mekanism:energy_tablet',thermal:'minecraft:blaze_powder',matters:'ae2:matter_ball',improvements:'mekanism:basic_control_circuit'};
Object.assign(bases,{base:'mekanism:basic_control_circuit',quant:'mekanism:advanced_control_circuit',integral:'mekanism:elite_control_circuit',laser_focus:'botania:lens_normal',singularity:'ae2:singularity',rotors:'mekanism:ingot_steel',radiator:'minecraft:copper_ingot',items:'mekanism:alloy_atomic',dead:'avaritia:neutronium_ingot'});
// Unavailable legacy integrations are supplied through colored circuit/magic-component recipes.
// Each group's selector pair is unique; every ingredient exists in this modpack's current tech/magic stack.
const selectors=new Map();
function componentRecipe(item){
 const group=item.group??item.resource.split('/').at(-2);
 let index=selectors.get(group)??0;selectors.set(group,index+1);
 const pairs=[];for(let a=0;a<16;a++)for(let b=a;b<16;b++)pairs.push([a,b]);
 const pair=pairs[index];if(!pair)throw new Error('Too many variants in '+group);
 let base=bases[group]??'mekanism:alloy_reinforced';
 unique(item.id,[base,'minecraft:'+colors[pair[0]]+'_dye','minecraft:'+colors[pair[1]]+'_dye']);
 notes.push({id:item.id,adaptation:'Current-modpack component recipe',group});
}
for(const item of catalog.filter(x=>x.kind==='material'))componentRecipe(item);
// Electronic tiers and actual progression replace the fallback component recipes for shared parts.
const partInputs={microchip:'minecraft:redstone',transistor:'minecraft:copper_ingot',capacitor:'minecraft:gold_ingot',diode:'minecraft:quartz',resistor:'minecraft:iron_ingot',conductive_contour:'minecraft:gold_nugget'};
for(const [part,input] of Object.entries(partInputs)) {
 unique('base_'+part,['mekanism:basic_control_circuit',input],2);
 unique('quant_'+part,[L('base_'+part),'ae2:fluix_crystal','mekanism:alloy_reinforced'],2);
 unique('integral_'+part,[L('quant_'+part),'ae2:engineering_processor','mekanism:alloy_atomic'],2);
}
// Metal shapes use their own ingot and keep a consistent recovery ratio.
for(const item of catalog.filter(x=>x.kind==='material')) {
 let stem=item.id.replace(/_(plate|thin_sheet|sheet|wire|gear)$/,'');
 if(stem===item.id||!ids.has(stem+'_ingot'))continue;
 let suffix=item.id.slice(stem.length+1),amount=suffix==='gear'?4:1;
 unique(item.id,[...Array(amount).fill(L(stem+'_ingot')),suffix==='wire'?'minecraft:redstone':'mekanism:basic_control_circuit'],suffix==='wire'?4:suffix==='gear'?1:2);
}
const tools={
 atomic_disassembler:['mekanism:atomic_disassembler',L('vajra_core')],
 vajra:['mekanism:atomic_disassembler',L('quantum_core')],
 legendary_vajra:[L('vajra'),L('sparkling_core'),'avaritia:infinity_ingot'],
 advanced_drill:['mekanism:atomic_disassembler',L('gravitation_engine')],
 advanced_chainsaw:['mekanism:atomic_disassembler',L('cooling_core')],
 infinity_pickaxe_ultra:['avaritia:infinity_pickaxe',L('neutronium_catalyst')],
 infinity_ultra_hammer:[L('infinity_pickaxe_ultra'),'minecraft:obsidian'],
 gravi_tool:['mekanism:configurator',L('cooling_core')],
 quantum_assembly_tool:['mekanism:configurator',L('quantum_core')],
 manual_pump:['minecraft:bucket','minecraft:iron_ingot','mekanism:basic_control_circuit'],
 relocator:['minecraft:ender_pearl',L('gravitation_engine'),L('energy_crystal'),'ae2:engineering_processor'],
 electric_scribing_tool:['botania:twig_wand','mekanism:energy_tablet','minecraft:paper'],
 focus_energy_ball:['botania:mana_gun','botania:lens_damage',L('energy_crystal')],
 seal_of_sealing:['minecraft:ender_eye','botania:mana_pearl',L('coding_plate_purple')]
};
for(const [id,ingredients] of Object.entries(tools))unique(id,ingredients);
unique('flask',['minecraft:glass_bottle','minecraft:iron_nugget']);
unique('advanced_cell',['minecraft:bucket','mekanism:basic_control_circuit',L('base_capacitor')]);
unique('ultimate_cell',[L('advanced_cell'),L('quantum_core'),L('quant_capacitor')]);
unique('quantum_cell',[L('ultimate_cell'),L('sparkling_core'),L('integral_capacitor')]);
unique('portable_charging_module',['mekanism:energy_tablet',L('energy_crystal'),L('integral_capacitor')]);
unique('advanced_lappack',['mekanism:energy_tablet',L('base_capacitor'),'minecraft:leather']);
unique('ultimate_lappack',[L('advanced_lappack'),L('quant_capacitor'),L('quantum_core')]);
const styles=['king1','king2','king3','king4','king5','king6','king7','king8','gold','dark','satan','hero','oson','leon'];
for(const [index,style] of styles.entries())for(const [part,native] of [['head','helmet'],['chest','chestplate'],['legs','leggings'],['feet','boots']]) {
 const id=`electric_${part}_${style}`;
 if(index===0)unique(id,['minecraft:netherite_'+native,L('armor_crafting_core'),L('quantum_core')]);
 else unique(id,[L('electric_'+part+'_king1'),'minecraft:'+colors[index]+'_dye']);
}
for(const item of catalog.filter(x=>x.kind==='sword')) {
 const index=Number(item.id.replace('sword',''))-1;
 unique(item.id,['minecraft:netherite_sword',L('sword_crafting_core'),L('energy_crystal'),'minecraft:'+colors[index%16]+'_dye',...(index>=16?['minecraft:'+colors[Math.floor(index/16)]+'_dye']:[])]);
}
const armorBase={
 advanced_solar_helmet:['head',L('advanced_light_absorbing_plate')],hybrid_solar_helmet:['head',L('mt_core')],ultimate_solar_helmet:['head',L('quantum_core')],
 advanced_jetpack:['chest',L('engine_boost')],gravi_chestplate:['chest',L('gravitation_engine')],advanced_nano_chestplate:['chest',L('cooling_core')],
 electric_boots_traveller:['feet','botania:manasteel_ingot'],nano_boots_traveller:['feet','botania:elementium_ingot'],quantum_boots_traveller:['feet','botania:terrasteel_ingot'],
 infused_quantum_chest:['chest','botania:terrasteel_ingot'],electric_goggles:['head','botania:mana_pearl'],nano_thaumic_helmet:['head','botania:elementium_ingot'],
 quantum_thaumic_helmet:['head','botania:terrasteel_ingot'],solar_helmet_revealing:['head','botania:dragonstone']
};
for(const [id,[part,ingredient]] of Object.entries(armorBase))unique(id,[L('electric_'+part+'_king1'),ingredient]);
for(const [id,style] of [['box_nano','king1'],['box_quantum','king8']])unique(id,['minecraft:chest',...['head','chest','legs','feet'].map(p=>L('electric_'+p+'_'+style)),L('energy_crystal')]);
const upgradeIngredients={LabFire:'minecraft:blaze_powder',LabVampirism:'botania:rune_lust',LabPoison:'minecraft:spider_eye',LabConfusion:'botania:rune_sloth',LabBlindness:'minecraft:ink_sac',LabWeakness:'minecraft:fermented_spider_eye',LabWither:'minecraft:wither_skeleton_skull',LabKnockback:'minecraft:piston',LabRadius:'botania:rune_air',LabDamage:'mekanism:alloy_reinforced',LabPowerLevel:L('energy_crystal'),LabNightVision:'minecraft:golden_carrot',LabClearEffects:'minecraft:milk_bucket',LabSolar:L('advanced_light_absorbing_plate'),LabWaterBreathing:'minecraft:pufferfish',LabSpeed:'minecraft:sugar',LabJump:'minecraft:rabbit_foot',LabExtinguish:'minecraft:magma_cream',LabFallProtection:'minecraft:feather',LabFlight:L('engine_boost'),LabFood:'minecraft:golden_apple',LabRegen:'minecraft:ghast_tear',LabFastFlight:L('gravitation_engine')};
for(const item of catalog.filter(x=>x.kind==='upgrade')) {
 const key=item.upgradeKey??'LabFlight',value=item.upgradeValue??0;
 const previous=value>1?catalog.find(x=>x.kind==='upgrade'&&x.upgradeKey===key&&x.upgradeValue===value-1):null;
 unique(item.id,[previous?L(previous.id):L('base_microchip'),upgradeIngredients[key],'minecraft:'+colors[value%16]+'_dye']);
}
for(const item of catalog.filter(x=>x.kind==='machine_upgrade')) {
 const type=item.supplier.match(/Upgrade\.(\w+)/)[1];
 const native={SPEED:'mekanism:upgrade_speed',ENERGY:'mekanism:upgrade_energy',STONE_GENERATOR:'mekanism:upgrade_stone_generator'}[type];
 unique(item.id,[native,L('quant_microchip'),'minecraft:'+colors[Number(item.id.match(/\d+$/)[0])%16]+'_dye']);
}
for(const item of catalog.filter(x=>x.kind==='tool_upgrade'))unique(item.id,['minecraft:lapis_lazuli',L('quant_microchip'),...Array(Number(item.id.match(/\d+$/)[0])).fill('minecraft:diamond')]);
for(const [id,ingredients] of Object.entries({upgrade_gas_puller:['mekanism:configurator',L('base_transistor')],upgrade_gas_ejector:['mekanism:configurator',L('base_diode')],upgrade_liquid_tightener:['mekanism:configurator',L('base_capacitor')]}))unique(id,ingredients);
unique('neutron_star',['avaritia:neutronium_ingot',L('integral_capacitor'),'ae2:singularity']);
unique('charged_neutron_star',[L('neutron_star'),'avaritia:infinity_ingot',L('sparkling_core')]);
const starTiers=['astrolion','sun','sirius','zodius'];
for(const [tier,name] of starTiers.entries()){
 unique(name+'_core',[tier===0?L('neutron_star'):L(starTiers[tier-1]+'_micro_star'),L('integral_microchip'),L('sparkling_core')]);
 unique(name+'_micro_star',[L(name+'_core'),'botania:terrasteel_ingot','avaritia:neutronium_ingot']);
 unique(name+'_dead',[L(name+'_micro_star'),'minecraft:coal']);
}
unique('upgrade_quantum_generator',[L('zodius_micro_star'),L('charged_neutron_star'),'avaritia:infinity_ingot','avaritia:infinity_ingot',L('sparkling_core')]);
for(const item of catalog.filter(x=>x.kind==='bucket')) {
 const name=item.fluid,stem=name.replace(/^molten_/,'');
 const candidates=[stem+'_ingot',stem,stem.replace('sannarium','sunnarium'),stem.replace('super_alloy','superalloy')+'_ingot'];
 const material=candidates.find(x=>ids.has(x));
 unique(item.id,['minecraft:bucket',material?L(material):'mekanism:alloy_atomic','minecraft:'+colors[Array.from(name).reduce((n,c)=>n+c.charCodeAt(0),0)%16]+'_dye']);
}
for(const item of catalog)if(!recipes.has(item.id))throw new Error('Missing recipe '+item.id);
const output=path.join(project,'resources/data/loli_labs/recipes');fs.mkdirSync(output,{recursive:true});
for(const [name,recipe]of recipes)fs.writeFileSync(path.join(output,name+'.json'),JSON.stringify(recipe));
fs.writeFileSync(path.join(project,'import/recipe-adaptations.json'),JSON.stringify(notes,null,2));
// Keep custom materials usable in other mods' tag-based recipes.
for(const item of catalog.filter(x=>x.kind==='material')) {
 for(const [suffix,group]of [['_ingot','ingots'],['_dust','dusts'],['_plate','plates'],['_gear','gears'],['_nugget','nuggets']])if(item.id.endsWith(suffix)){
  const name=item.id.slice(0,-suffix.length),file=path.join(project,'resources/data/forge/tags/items',group,name+'.json');fs.mkdirSync(path.dirname(file),{recursive:true});fs.writeFileSync(file,JSON.stringify({replace:false,values:[L(item.id)]}));
 }
}
console.log(`Generated ${recipes.size} distinct adapted recipes and shared material tags.`);
