// Narrow the current build to the user's requested testing scope, preserving work.
const fs=require('fs'),path=require('path');
const root=__dirname, archive=path.join(root,'deferred');
if(fs.existsSync(path.join(archive,'resources')))throw Error('Snapshot already exists; do not overwrite');
fs.mkdirSync(archive,{recursive:true});
fs.cpSync(path.join(root,'resources'),path.join(archive,'resources'),{recursive:true});
fs.cpSync(path.join(root,'src'),path.join(archive,'src'),{recursive:true});
fs.cpSync(path.join(root,'import'),path.join(archive,'import'),{recursive:true});
const previous=path.join(archive,'resources'),next=path.join(root,'resources-testing');
const read=p=>JSON.parse(fs.readFileSync(path.join(previous,p),'utf8'));
const write=(p,v)=>{const file=path.join(next,p);fs.mkdirSync(path.dirname(file),{recursive:true});fs.writeFileSync(file,JSON.stringify(v,null,2));};
const copy=p=>{const dst=path.join(next,p);fs.mkdirSync(path.dirname(dst),{recursive:true});fs.cpSync(path.join(previous,p),dst,{recursive:true});};
const full=read('assets/loli_labs/legacy-items.json');
// Dead stars belong to the deferred reactor/battery lifecycle.
const items=full.filter(x=>x.kind==='material'&&!x.id.endsWith('_dead'));
const ids=new Set(items.map(x=>x.id));
write('assets/loli_labs/legacy-items.json',items);
fs.writeFileSync(path.join(archive,'pending-items.json'),JSON.stringify(full.filter(x=>!ids.has(x.id)),null,2));
copy('META-INF');copy('pack.mcmeta');
copy('assets/loli_labs/models/block');
copy('assets/loli_labs/textures/block');copy('assets/loli_labs/textures/gui');
function texture(ref){if(!ref.startsWith('loli_labs:'))return;const p='assets/loli_labs/textures/'+ref.split(':')[1]+'.png';copy(p);if(fs.existsSync(path.join(previous,p+'.mcmeta')))copy(p+'.mcmeta');}
for(const x of items){
 const p='assets/loli_labs/models/item/'+x.id+'.json',model=read(p);copy(p);
 if(model.loader)throw Error('Material unexpectedly needs custom model: '+x.id);
 for(const t of Object.values(model.textures||{}))texture(t);
 const recipe=read('data/loli_labs/recipes/'+x.id+'.json');
 for(const i of recipe.ingredients)if(i.item?.startsWith('loli_labs:')&&!ids.has(i.item.split(':')[1]))throw Error('Deferred dependency in '+x.id);
 write('data/loli_labs/recipes/'+x.id+'.json',recipe);
}
for(const lang of ['en_us','ru_ru']){
 const old=read('assets/loli_labs/lang/'+lang+'.json'),out={};
 for(const [k,v] of Object.entries(old))if(!k.startsWith('item.loli_labs.')&&!k.startsWith('tooltip.loli_labs.')||ids.has(k.replace('item.loli_labs.','')))out[k]=v;
 const en=read('assets/loli_labs/lang/en_us.json');
 for(const id of ids)out['item.loli_labs.'+id]??=en['item.loli_labs.'+id];
 write('assets/loli_labs/lang/'+lang+'.json',out);
}
const tagdir=path.join(previous,'data/forge/tags/items');
function walk(dir,prefix=''){for(const e of fs.readdirSync(dir,{withFileTypes:true})){const rel=prefix+e.name;if(e.isDirectory())walk(path.join(dir,e.name),rel+'/');else{const p='data/forge/tags/items/'+rel,t=read(p);t.values=t.values.filter(v=>typeof v==='string'&&!v.startsWith('loli_labs:')||typeof v==='string'&&ids.has(v.split(':')[1]));if(t.values.length)write(p,t);}}}
walk(tagdir);
fs.writeFileSync(path.join(root,'src/tmr/lolilabs/LegacyMaterials.java'),'package tmr.lolilabs;\npublic final class LegacyMaterials { public static final String[] IDS = {\n'+items.map(x=>'"'+x.id+'"').join(',\n')+'\n}; }\n');
console.log('Testing scope: '+items.length+' plain crafting items; '+(full.length-items.length)+' deferred items.');
