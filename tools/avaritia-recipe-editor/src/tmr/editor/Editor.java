package tmr.editor;

import com.google.gson.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public final class Editor extends JFrame {
    private final RecipeStore store;
    private Catalog catalog;
    private RecipeModel model=new RecipeModel();
    private String loadedId=null,brush="minecraft:iron_ingot";
    private boolean dirty,loading;
    private final JTextField id=new JTextField(model.id),result=new JTextField(),ingredient=new JTextField(brush),search=new JTextField(),recipeSearch=new JTextField(),nbt=new JTextField();
    private final JComboBox<String> tier=new JComboBox<>(new String[]{"Уровень 1 · 3 × 3","Уровень 2 · 5 × 5","Уровень 3 · 7 × 7","Уровень 4 · 9 × 9"});
    private final JComboBox<String> catalogType=new JComboBox<>(new String[]{"Предметы","Теги"});
    private final JCheckBox shapeless=new JCheckBox("Без формы"),compatible=new JCheckBox("Совместимость Avaritia"),onlyAvaritia=new JCheckBox("Только Avaritia",true);
    private final JSpinner count=new JSpinner(new SpinnerNumberModel(1,1,64,1));
    private final DefaultListModel<Catalog.Entry> paletteModel=new DefaultListModel<>();
    private final JList<Catalog.Entry> palette=new JList<>(paletteModel);
    private final DefaultListModel<String> savedModel=new DefaultListModel<>();
    private final JList<String> saved=new JList<>(savedModel);
    private final DefaultListModel<Catalog.Recipe> sourceModel=new DefaultListModel<>();
    private final JList<Catalog.Recipe> source=new JList<>(sourceModel);
    private final JPanel grid=new JPanel(),gridHost=new JPanel(new GridBagLayout());
    private final JTextArea preview=new JTextArea();
    private final JLabel status=new JLabel("Загрузка каталога…"),brushLabel=new JLabel(),hint=new JLabel("ЛКМ — поставить · ПКМ — очистить · Ctrl+клик — взять ингредиент");
    private final JButton product=new JButton("Результат"),undoButton=new JButton("Отменить сетку"),redoButton=new JButton("Повторить");
    private final Deque<RecipeModel> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    private final Path instance;
    private final javax.swing.Timer searchTimer;
    private static final Color BG=new Color(24,29,38),PANEL=new Color(34,41,53),TEAL=new Color(92,222,204),TEXT=new Color(226,238,245);

    public Editor(Path instance)throws Exception{
        super("Avaritia Recipe Editor — "+instance.getFileName());this.instance=instance;store=new RecipeStore(instance);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);setMinimumSize(new Dimension(1100,760));setSize(1420,900);setLocationRelativeTo(null);
        searchTimer=new javax.swing.Timer(180,e->filterPalette());searchTimer.setRepeats(false);
        var root=new JPanel(new BorderLayout(12,12));root.setBorder(new EmptyBorder(14,14,10,14));setContentPane(root);
        var toolbar=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        toolbar.add(button("Новый",this::newRecipe));toolbar.add(button("Сохранить · Ctrl+S",()->saveCurrent(true)));
        toolbar.add(button("Импорт JSON…",this::importJson));toolbar.add(button("Экспорт JSON…",this::exportJson));toolbar.add(button("Папка рецептов",this::openFolder));toolbar.add(button("Обновить каталог",this::loadCatalog));
        var title=new JPanel(new BorderLayout());var label=new JLabel("AVARITIA  /  RECIPE EDITOR");label.setForeground(TEAL);label.setFont(label.getFont().deriveFont(Font.BOLD,18));title.add(label,BorderLayout.NORTH);title.add(toolbar,BorderLayout.SOUTH);root.add(title,BorderLayout.NORTH);
        var left=new JPanel(new BorderLayout(5,8));left.setPreferredSize(new Dimension(290,500));var searchBox=new JPanel(new BorderLayout(5,6));searchBox.add(new JLabel("Предметы и теги"),BorderLayout.NORTH);searchBox.add(search,BorderLayout.CENTER);searchBox.add(catalogType,BorderLayout.SOUTH);search.setToolTipText("Имя, modid или часть ID; несколько слов через пробел");left.add(searchBox,BorderLayout.NORTH);
        palette.setFixedCellHeight(45);palette.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);palette.setCellRenderer((list,value,index,selected,focus)->itemLabel(value.id(),value.name(),selected));left.add(new JScrollPane(palette));
        var paint=new JPanel(new BorderLayout(4,6));paint.add(new JLabel("Кисть — предмет, #тег или JSON ингредиента"),BorderLayout.NORTH);paint.add(ingredient);var buttons=new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));buttons.add(button("Взять кисть",this::selectBrush));buttons.add(button("Ластик",()->{brush="";ingredient.setText("");updateBrush();}));paint.add(buttons,BorderLayout.SOUTH);left.add(paint,BorderLayout.SOUTH);
        var center=new JPanel(new BorderLayout(8,8));var fields=new JPanel(new GridLayout(0,1,3,5));fields.add(field("ID рецепта",id));var options=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));tier.setSelectedIndex(3);options.add(tier);options.add(shapeless);compatible.setToolTipText("Флаг compatible исходного формата Avaritia; не меняет рецепты других модов.");fields.add(options);center.add(fields,BorderLayout.NORTH);
        gridHost.add(grid);var recipeArea=new JPanel(new BorderLayout(4,6));recipeArea.add(brushLabel,BorderLayout.NORTH);recipeArea.add(gridHost);var gridButtons=new JPanel(new FlowLayout(FlowLayout.CENTER,6,0));undoButton.addActionListener(e->history(true));redoButton.addActionListener(e->history(false));gridButtons.add(undoButton);gridButtons.add(redoButton);gridButtons.add(button("Зеркало",()->transform(false)));gridButtons.add(button("Поворот 90°",()->transform(true)));gridButtons.add(button("Очистить сетку",()->{snapshot();Arrays.fill(model.cells,null);changed();renderGrid();}));recipeArea.add(gridButtons,BorderLayout.SOUTH);center.add(recipeArea);
        var lower=new JPanel(new BorderLayout(5,6));var output=new JPanel(new BorderLayout(6,4));product.setPreferredSize(new Dimension(92,48));product.setToolTipText("Поставить выбранный предмет в результат; также можно перетащить предмет");product.addActionListener(e->setProduct(brush));output.add(product,BorderLayout.WEST);output.add(result);output.add(count,BorderLayout.EAST);lower.add(output,BorderLayout.NORTH);
        var details=new JPanel(new BorderLayout(4,5));details.add(field("NBT результата (SNBT, необязательно)",nbt),BorderLayout.NORTH);preview.setEditable(false);preview.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));preview.setLineWrap(false);var scroll=new JScrollPane(preview);scroll.setPreferredSize(new Dimension(400,180));details.add(scroll);lower.add(details);center.add(lower,BorderLayout.SOUTH);
        var right=new JPanel(new BorderLayout(5,7));right.setPreferredSize(new Dimension(290,500));var tabs=new JTabbedPane();var mine=new JPanel(new BorderLayout(4,5));mine.add(new JScrollPane(saved));var mineButtons=new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));mineButtons.add(button("Открыть",this::openSaved));mineButtons.add(button("Копия",this::duplicate));mineButtons.add(button("Удалить",this::removeSaved));mine.add(mineButtons,BorderLayout.SOUTH);tabs.add("Мои рецепты",mine);
        var original=new JPanel(new BorderLayout(4,6));var find=new JPanel(new BorderLayout(3,4));find.add(recipeSearch);find.add(onlyAvaritia,BorderLayout.SOUTH);original.add(find,BorderLayout.NORTH);original.add(new JScrollPane(source));original.add(button("Импортировать выбранный",this::openSource),BorderLayout.SOUTH);tabs.add("Рецепты сборки",original);right.add(tabs);
        source.setCellRenderer((list,value,index,selected,focus)->{String item="";try{var r=value.json().get("result");item=r.isJsonObject()?r.getAsJsonObject().get("item").getAsString():r.getAsString();}catch(Exception ignored){}return itemLabel(item,value.id(),selected);});source.setFixedCellHeight(45);
        var split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,center);split.setResizeWeight(0);split.setBorder(null);split.setDividerLocation(285);var all=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,split,right);all.setResizeWeight(1);all.setBorder(null);all.setDividerLocation(1060);root.add(all);
        var footer=new JPanel(new BorderLayout());footer.add(status);var reload=new JLabel("После сохранения: /reload в игре");reload.setForeground(TEAL);footer.add(reload,BorderLayout.EAST);footer.setToolTipText(store.script.toString());root.add(footer,BorderLayout.SOUTH);
        ingredient.addActionListener(e->selectBrush());search.getDocument().addDocumentListener(listener(()->searchTimer.restart()));catalogType.addActionListener(e->filterPalette());recipeSearch.getDocument().addDocumentListener(listener(this::filterSources));onlyAvaritia.addActionListener(e->filterSources());
        palette.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&palette.getSelectedValue()!=null){brush=palette.getSelectedValue().id();ingredient.setText(brush);updateBrush();}});
        palette.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.isShiftDown()||e.getClickCount()==2)setProduct(brush);}});
        palette.setDragEnabled(true);palette.setTransferHandler(new TransferHandler(){@Override public int getSourceActions(JComponent c){return COPY;}@Override protected Transferable createTransferable(JComponent c){var selected=palette.getSelectedValue();return selected==null?null:new StringSelection(selected.id());}});
        product.setTransferHandler(drop(this::setProduct));
        source.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)openSource();}});saved.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)openSaved();}});
        for(var text:List.of(id,result,nbt))text.getDocument().addDocumentListener(listener(()->{if(!loading){dirty=true;updatePreview();}}));count.addChangeListener(e->{if(!loading){dirty=true;updatePreview();}});
        shapeless.addActionListener(e->{if(!loading){model.shapeless=shapeless.isSelected();dirty=true;updatePreview();}});compatible.addActionListener(e->{if(!loading){dirty=true;updatePreview();}});
        tier.addActionListener(e->{if(loading)return;int chosen=tier.getSelectedIndex()+1,size=chosen*2+1;boolean cuts=false;for(int y=0;y<9;y++)for(int x=0;x<9;x++)if((x>=size||y>=size)&&model.cells[y*9+x]!=null)cuts=true;if(cuts&&JOptionPane.showConfirmDialog(this,"Ингредиенты вне новой сетки будут убраны. Продолжить?","Изменить размер",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION){loading=true;tier.setSelectedIndex(model.tier-1);loading=false;return;}snapshot();model.tier=chosen;for(int y=0;y<9;y++)for(int x=0;x<9;x++)if(x>=size||y>=size)model.cells[y*9+x]=null;changed();renderGrid();});
        getRootPane().registerKeyboardAction(e->saveCurrent(true),KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(e->history(true),KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_DOWN_MASK|InputEvent.SHIFT_DOWN_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){if(leaveDraft()){dispose();System.exit(0);}}});
        ToolTipManager.sharedInstance().setDismissDelay(15000);refreshSaved();loadModel(model,null);loadCatalog();
    }
    private JButton button(String text,Runnable action){var b=new JButton(text);b.addActionListener(e->action.run());return b;}
    private JPanel field(String label,JComponent component){var p=new JPanel(new BorderLayout(8,0));var l=new JLabel(label);l.setPreferredSize(new Dimension(125,24));p.add(l,BorderLayout.WEST);p.add(component);return p;}
    private DocumentListener listener(Runnable action){return new DocumentListener(){public void insertUpdate(DocumentEvent e){action.run();}public void removeUpdate(DocumentEvent e){action.run();}public void changedUpdate(DocumentEvent e){action.run();}};}
    private TransferHandler drop(java.util.function.Consumer<String> action){return new TransferHandler(){@Override public boolean canImport(TransferSupport support){return support.isDataFlavorSupported(DataFlavor.stringFlavor);}@Override public boolean importData(TransferSupport support){try{action.accept((String)support.getTransferable().getTransferData(DataFlavor.stringFlavor));return true;}catch(Exception e){error(e);return false;}}};}
    private JLabel itemLabel(String item,String title,boolean selected){var label=new JLabel("<html>"+html(title)+"<br><small>"+html(item)+"</small></html>");label.setBorder(new EmptyBorder(3,6,3,4));label.setOpaque(true);label.setBackground(selected?new Color(47,87,91):PANEL);label.setForeground(TEXT);if(catalog!=null)label.setIcon(catalog.icon(item));return label;}
    private static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    private void renderGrid(){
        grid.removeAll();int size=model.size();grid.setLayout(new GridLayout(size,size,3,3));grid.setBorder(new EmptyBorder(5,5,5,5));int cell=size==9?42:size==7?48:56;grid.setPreferredSize(new Dimension(size*(cell+3)+10,size*(cell+3)+10));
        for(int y=0;y<size;y++)for(int x=0;x<size;x++){int index=y*9+x;var value=model.cells[index];String text=RecipeModel.display(value);var b=new JButton();b.setMargin(new Insets(0,0,0,0));b.setBackground(new Color(46,55,70));b.setFocusable(false);b.setToolTipText(value==null?"Пусто":html(text)+(catalog==null?"":" — "+catalog.name(text)));if(catalog!=null&&!text.startsWith("#"))b.setIcon(catalog.icon(text.replace(" | …","")));if(b.getIcon()==null&&!text.isBlank())b.setText(text.startsWith("#")?"#":text.substring(text.indexOf(':')+1,Math.min(text.length(),text.indexOf(':')+5)));
            b.addMouseListener(new MouseAdapter(){public void mousePressed(MouseEvent e){if(e.isControlDown()){brush=model.cells[index]==null?"":model.cells[index].isJsonArray()||model.cells[index].getAsJsonObject().size()>1?model.cells[index].toString():RecipeModel.display(model.cells[index]);ingredient.setText(brush);updateBrush();return;}paint(index,SwingUtilities.isRightMouseButton(e));}public void mouseEntered(MouseEvent e){if((e.getModifiersEx()&InputEvent.BUTTON1_DOWN_MASK)!=0)paint(index,false);else if((e.getModifiersEx()&InputEvent.BUTTON3_DOWN_MASK)!=0)paint(index,true);}});
            b.setTransferHandler(drop(s->{try{var ingredient=RecipeModel.ingredient(s);snapshot();model.cells[index]=ingredient;changed();renderGrid();}catch(Exception e){error(e);}}));grid.add(b);
        }
        grid.revalidate();grid.repaint();undoButton.setEnabled(!undo.isEmpty());redoButton.setEnabled(!redo.isEmpty());
    }
    private void paint(int index,boolean erase){try{JsonElement value=erase?null:RecipeModel.ingredient(brush);if(Objects.equals(value,model.cells[index]))return;snapshot();model.cells[index]=value;changed();renderGrid();}catch(Exception e){error(e);}}
    private void selectBrush(){try{var value=RecipeModel.ingredient(ingredient.getText());brush=value==null?"":ingredient.getText().trim();updateBrush();}catch(Exception e){error(e);}}
    private void updateBrush(){brushLabel.setText("Кисть: "+(brush.isBlank()?"ластик":brush));brushLabel.setToolTipText(hint.getText());}
    private void setProduct(String brush){try{var v=RecipeModel.ingredient(brush);if(v==null||!v.isJsonObject()||!v.getAsJsonObject().has("item"))throw new IllegalArgumentException("Результатом должен быть конкретный предмет, не тег.");result.setText(v.getAsJsonObject().get("item").getAsString());}catch(Exception e){error(e);}}
    private void snapshot(){undo.push(model.copy());while(undo.size()>100)undo.removeLast();redo.clear();}
    private void history(boolean back){var from=back?undo:redo;var to=back?redo:undo;if(from.isEmpty())return;to.push(model.copy());model=from.pop();loading=true;tier.setSelectedIndex(model.tier-1);loading=false;changed();renderGrid();}
    private void transform(boolean rotate){snapshot();var copy=model.cells.clone();int size=model.size();for(int y=0;y<size;y++)for(int x=0;x<size;x++)model.cells[(rotate?x:y)*9+(rotate?size-1-y:size-1-x)]=copy[y*9+x];changed();renderGrid();}
    private void changed(){dirty=true;updatePreview();}
    private void readFields(){model.id=id.getText().trim();model.result=result.getText().trim();model.count=(int)count.getValue();model.nbt=nbt.getText().trim();model.compatible=compatible.isSelected();model.shapeless=shapeless.isSelected();}
    private void updatePreview(){readFields();try{preview.setText(RecipeModel.JSON.toJson(model.recipe()));preview.setForeground(TEXT);}catch(Exception e){preview.setText(e.getMessage());preview.setForeground(new Color(244,190,128));}preview.setCaretPosition(0);product.setIcon(catalog==null?null:catalog.icon(model.result));setTitle((dirty?"● ":"")+"Avaritia Recipe Editor — "+instance.getFileName());}
    private void loadModel(RecipeModel next,String savedId){loading=true;model=next.copy();loadedId=savedId;id.setText(model.id);result.setText(model.result);count.setValue(model.count);tier.setSelectedIndex(model.tier-1);nbt.setText(model.nbt);shapeless.setSelected(model.shapeless);compatible.setSelected(model.compatible);loading=false;dirty=false;undo.clear();redo.clear();renderGrid();updateBrush();updatePreview();}
    private boolean leaveDraft(){if(!dirty)return true;int answer=JOptionPane.showConfirmDialog(this,"Сохранить текущий рецепт?","Несохранённые изменения",JOptionPane.YES_NO_CANCEL_OPTION);return answer==JOptionPane.NO_OPTION||answer==JOptionPane.YES_OPTION&&saveCurrent(false);}
    private boolean saveCurrent(boolean show){try{readFields();var recipe=model.recipe();if(!confirmIds())return false;if(!Objects.equals(model.id,loadedId)&&store.recipes.containsKey(model.id)&&JOptionPane.showConfirmDialog(this,"Заменить сохранённый рецепт "+model.id+"?","ID уже существует",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return false;var old=new TreeMap<>(store.recipes);try{if(loadedId!=null&&!loadedId.equals(model.id))store.recipes.remove(loadedId);store.recipes.put(model.id,recipe);store.save();}catch(Exception e){store.recipes.clear();store.recipes.putAll(old);throw e;}loadedId=model.id;dirty=false;refreshSaved();updatePreview();status.setText("Сохранено: "+model.id+"  ·  /reload в игре");if(show)JOptionPane.showMessageDialog(this,"Рецепт сохранён.\nВ игре выполните /reload.\n\n"+store.script,"Готово",JOptionPane.INFORMATION_MESSAGE);return true;}catch(Exception e){error(e);return false;}}
    private boolean confirmIds(){
        if(catalog==null)return true;var missing=new TreeSet<String>();if(!catalog.names.containsKey(model.result))missing.add(model.result);for(var cell:model.cells)checkCatalog(cell,missing);
        return missing.isEmpty()||JOptionPane.showConfirmDialog(this,"Эти ID не найдены в каталоге. Проверьте опечатки; новые предметы KubeJS могут отсутствовать.\n\n"+String.join("\n",missing)+"\n\nСохранить всё равно?","Проверка ID",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    private void checkCatalog(JsonElement cell,Set<String> missing){if(cell==null)return;if(cell.isJsonArray()){for(var entry:cell.getAsJsonArray())checkCatalog(entry,missing);return;}var obj=cell.getAsJsonObject();if(obj.has("item")){String value=obj.get("item").getAsString();if(!catalog.names.containsKey(value))missing.add(value);}else if(obj.has("tag")){String value="#"+obj.get("tag").getAsString();if(catalog.tags.stream().noneMatch(t->t.id().equals(value)))missing.add(value);}}
    private void refreshSaved(){savedModel.clear();store.recipes.keySet().forEach(savedModel::addElement);}
    private void newRecipe(){if(!leaveDraft())return;var fresh=new RecipeModel();for(int n=1;store.recipes.containsKey(fresh.id);n++)fresh.id="loli_labs:editor/new_recipe_"+n;loadModel(fresh,null);}
    private void openSaved(){String selected=saved.getSelectedValue();if(selected==null||!leaveDraft())return;try{loadModel(RecipeModel.from(selected,store.recipes.get(selected)),selected);}catch(Exception e){error(e);}}
    private void duplicate(){if(!leaveDraft())return;String selected=saved.getSelectedValue();if(selected==null)return;var copy=RecipeModel.from(selected,store.recipes.get(selected));copy.id+="_copy";while(store.recipes.containsKey(copy.id))copy.id+="_copy";loadModel(copy,null);dirty=true;updatePreview();}
    private void removeSaved(){String selected=saved.getSelectedValue();if(selected==null)return;if(JOptionPane.showConfirmDialog(this,"Убрать "+selected+" из проекта?\nИсходный рецепт из мода снова появится после /reload.","Удалить рецепт",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;var old=store.recipes.remove(selected);try{store.save();refreshSaved();if(selected.equals(loadedId))loadModel(new RecipeModel(),null);}catch(Exception e){store.recipes.put(selected,old);error(e);}}
    private void openSource(){var selected=source.getSelectedValue();if(selected==null||!leaveDraft())return;try{loadModel(RecipeModel.from(selected.id(),selected.json()),store.recipes.containsKey(selected.id())?selected.id():null);dirty=true;updatePreview();status.setText("Импорт: "+selected.source()+". Изменения применятся после сохранения.");}catch(Exception e){error(e);}}
    private void importJson(){if(!leaveDraft())return;var chooser=new JFileChooser();chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Рецепт JSON","json"));if(chooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;try{var path=chooser.getSelectedFile().toPath();String newId=JOptionPane.showInputDialog(this,"ID рецепта", "loli_labs:editor/"+path.getFileName().toString().replaceFirst("\\.json$",""));if(newId==null)return;loadModel(RecipeModel.from(newId,JsonParser.parseString(Files.readString(path)).getAsJsonObject()),null);dirty=true;updatePreview();}catch(Exception e){error(e);}}
    private void exportJson(){try{readFields();String text=RecipeModel.JSON.toJson(model.recipe());var chooser=new JFileChooser();chooser.setSelectedFile(new java.io.File(model.id.substring(model.id.indexOf(':')+1).replace('/','_')+".json"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;Path path=chooser.getSelectedFile().toPath();if(Files.exists(path)&&JOptionPane.showConfirmDialog(this,"Заменить файл "+path+"?","Экспорт",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;Files.writeString(path,text+"\n");}catch(Exception e){error(e);}}
    private void openFolder(){try{Files.createDirectories(store.script.getParent());Desktop.getDesktop().open(store.script.getParent().toFile());}catch(Exception e){error(e);}}
    private void filterPalette(){if(catalog==null)return;String[] terms=search.getText().toLowerCase(Locale.ROOT).trim().split("\\s+");paletteModel.clear();int total=0;for(var e:catalogType.getSelectedIndex()==0?catalog.items:catalog.tags){String hay=(e.id()+" "+e.name()).toLowerCase(Locale.ROOT);if(Arrays.stream(terms).allMatch(hay::contains)){total++;if(paletteModel.size()<600)paletteModel.addElement(e);}}status.setText("Найдено: "+total+(total>600?" · уточните поиск":"")+"  ·  Shift+клик по предмету — результат");}
    private void filterSources(){if(catalog==null)return;String text=recipeSearch.getText().toLowerCase(Locale.ROOT);sourceModel.clear();for(var r:catalog.recipes)if((!onlyAvaritia.isSelected()||r.json().get("type").getAsString().startsWith("avaritia:"))&&(r.id()+" "+r.json().get("result")).toLowerCase(Locale.ROOT).contains(text))sourceModel.addElement(r);}
    private void loadCatalog(){status.setText("Загрузка предметов и рецептов…");new SwingWorker<Catalog,Void>(){protected Catalog doInBackground()throws Exception{return new Catalog(instance);}protected void done(){try{catalog=get();filterPalette();filterSources();renderGrid();updatePreview();}catch(Exception e){error(e);}}}.execute();}
    private void error(Exception e){JOptionPane.showMessageDialog(this,e.getMessage()==null?e.toString():e.getMessage(),"Не удалось выполнить действие",JOptionPane.ERROR_MESSAGE);}
    public static void main(String[] args)throws Exception{
        if(args.length>0&&args[0].equals("--self-test")){SelfTest.run(Path.of(args[1]));return;}
        if(args.length>0&&args[0].equals("--catalog-check")){SelfTest.catalog(Path.of(args[1]));return;}
        Path instance=args.length==0?Path.of(".").toAbsolutePath():Path.of(args[0]).toAbsolutePath();
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        for(String key:List.of("Panel.background","Viewport.background","List.background","TextArea.background","TextField.background","ComboBox.background","Spinner.background","TabbedPane.background","ScrollPane.background","Button.background","CheckBox.background","OptionPane.background","FormattedTextField.background"))UIManager.put(key,PANEL);
        for(String key:List.of("Label.foreground","List.foreground","TextArea.foreground","TextField.foreground","ComboBox.foreground","Button.foreground","CheckBox.foreground","TabbedPane.foreground","Spinner.foreground","FormattedTextField.foreground","OptionPane.messageForeground"))UIManager.put(key,TEXT);
        UIManager.put("TextField.caretForeground",TEAL);UIManager.put("TextArea.caretForeground",TEAL);UIManager.put("List.selectionBackground",new Color(47,87,91));UIManager.put("TabbedPane.selected",new Color(47,87,91));UIManager.put("Button.gradient",List.of(0f,0f,PANEL,PANEL,PANEL));UIManager.put("control",PANEL);UIManager.put("text",TEXT);
        SwingUtilities.invokeLater(()->{try{new Editor(instance).setVisible(true);}catch(Exception e){e.printStackTrace();JOptionPane.showMessageDialog(null,e.toString(),"Avaritia Recipe Editor",JOptionPane.ERROR_MESSAGE);}});
    }
}
