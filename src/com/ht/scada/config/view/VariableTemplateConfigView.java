package com.ht.scada.config.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ht.scada.common.tag.entity.TagCfgTpl;
import com.ht.scada.common.tag.service.TagCfgTplService;
import com.ht.scada.common.tag.type.entity.VarSubType;
import com.ht.scada.common.tag.type.entity.VarType;
import com.ht.scada.common.tag.type.service.TypeService;
import com.ht.scada.common.tag.util.DataType;
import com.ht.scada.config.scadaconfig.Activator;
import com.ht.scada.config.util.GridViewerColumnSorter;
import com.ht.scada.config.util.PinyinComparator;

/**
 * 变量模板配置
 * 
 * @author 赵磊
 * 
 */
public class VariableTemplateConfigView extends ViewPart {

	private static final Logger log = LoggerFactory
			.getLogger(VariableTemplateConfigView.class);

	// private boolean isAdded = false; // 是否是新增变量标志
	private String selectedTplName = null; // 选定的变量模板名字
	private String addedTplName = null; // 新增的变量模板名字
	private String[] varTypeArray; // 变量类型
	private String[] varSubTypeArray;	//变量子类型
	private String[] varDataTypeArray;	//值类型	

	public VariableTemplateConfigView() {
		tplNameList = tagCfgTplService.findAllTplName();
		
		List<VarType> varTypeList = typeService.getAllVarType();
		List<VarSubType> varSubTypeList = typeService.getAllVarSubType();

		int length = varTypeList.size();
		varTypeArray = new String[length + 1];
		varTypeArray[0] = "";
		for (int i = 1; i <= length; i++) {
			varTypeArray[i] = varTypeList.get(i - 1).getValue();
		}
		
		int len = varSubTypeList.size();
		varSubTypeArray = new String[len + 1];
		varSubTypeArray[0] = "";
		for (int i = 1; i <= len; i++) {
			varSubTypeArray[i] = varSubTypeList.get(i - 1).getValue();
		}
		
		int len1 = DataType.values().length;
		varDataTypeArray = new String[len1 + 1];
		varDataTypeArray[0] = "";
		for (int i = 1; i <= len1; i++) {
			varDataTypeArray[i] = DataType.values()[i - 1].getValue();
		}

	}

	private TagCfgTplService tagCfgTplService = (TagCfgTplService) Activator
			.getDefault().getApplicationContext().getBean("tagCfgTplService");
	private TypeService typeService = (TypeService) Activator.getDefault()
			.getApplicationContext().getBean("typeService");

	private MenuManager menuMng;

	public static final String ID = "com.ht.scada.config.view.VariableTemplateConfigView";
	private Text text_tpl_name;
	private ListViewer listViewer_1;
	private List<String> tplNameList; // 所有变量模板名字
	private List<TagCfgTpl> tagCfgTplList = new ArrayList<>(); // 当前模板所有变量
	private GridTableViewer gridTableViewer;
	private List<TagCfgTpl> deletedTplList = new ArrayList<>(); // 要删除的变量模板

	// private List<TagCfgTpl> addedTagTplList = new ArrayList<>(); //新增的变量模板

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout(SWT.HORIZONTAL));

		SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH);
		sashForm.setSashWidth(1);

		Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));

		menuMng = new MenuManager();
		menuMng.setRemoveAllWhenShown(true);

		Group group = new Group(composite, SWT.NONE);
		group.setText("变量模板");
		group.setLayout(new FillLayout(SWT.HORIZONTAL));

		listViewer_1 = new ListViewer(group, SWT.BORDER | SWT.V_SCROLL
				| SWT.MULTI);
		listViewer_1
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						if (!selection.isEmpty()) {
							String tplName = (String) selection
									.getFirstElement();
							selectedTplName = tplName;
							addedTplName = null;
							initTplInfo(tplName);
							deletedTplList.clear();
						}

					}
				});
		org.eclipse.swt.widgets.List list = listViewer_1.getList();
		menuMng.addMenuListener(new MenuListener(listViewer_1));

		list.setMenu(menuMng.createContextMenu(list)); // 添加菜单
		listViewer_1.setLabelProvider(new ViewerLabelProvider());

		listViewer_1.setContentProvider(ArrayContentProvider.getInstance());
		listViewer_1.setInput(tplNameList);

		Composite composite_1 = new Composite(sashForm, SWT.NONE);
		composite_1.setLayout(new GridLayout(1, false));

		Group group_2 = new Group(composite_1, SWT.NONE);
		group_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));
		group_2.setText("模板信息");
		group_2.setBounds(0, 0, 70, 84);

		Label label = new Label(group_2, SWT.NONE);
		label.setBounds(10, 24, 56, 17);
		label.setText("模板名：");

		text_tpl_name = new Text(group_2, SWT.BORDER);
		text_tpl_name.setBounds(79, 21, 96, 23);

		Group group_1 = new Group(composite_1, SWT.NONE);
		group_1.setLayout(new GridLayout(1, false));
		group_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		group_1.setText("变量信息");

		Composite composite_3 = new Composite(group_1, SWT.NONE);
		composite_3.setLayout(new GridLayout(6, false));
		composite_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
								
										Label lblNewLabel = new Label(composite_3, SWT.NONE);
										lblNewLabel.setText("变量类型：");
						
								Combo combo = new Combo(composite_3, SWT.NONE);
				
						Label label_1 = new Label(composite_3, SWT.NONE);
						label_1.setText("变量分组：");
		
				Combo combo_1 = new Combo(composite_3, SWT.NONE);

		Button btnNewButton_1 = new Button(composite_3, SWT.NONE);
		btnNewButton_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnNewButton_1.setText("  导入变量词典  ");

		Button button_2 = new Button(composite_3, SWT.NONE);
		button_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		button_2.setText("导出变量词典 ");

		Composite composite_4 = new Composite(group_1, SWT.NONE);
		composite_4.setLayout(new FillLayout(SWT.HORIZONTAL));
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));

		gridTableViewer = new GridTableViewer(composite_4, SWT.BORDER
				| SWT.V_SCROLL | SWT.H_SCROLL);

		final Grid grid = gridTableViewer.getGrid();
		grid.setHeaderVisible(true);
		grid.setColumnScrolling(true);
		grid.setCellSelectionEnabled(true);
		grid.setRowHeaderVisible(true);

		GridViewerColumn gridViewerColumn = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		gridViewerColumn.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getTagName();
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "变量名不能为空！");
					return;
				}
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setTagName((String) value);
				gridTableViewer.update(tct, null);
			}
		});
		GridColumn gridColumn = gridViewerColumn.getColumn();
		gridColumn.setText("变量名");
		gridColumn.setWidth(70);
		new GridViewerColumnSorter(gridViewerColumn) {
			@Override
			protected int doCompare(Viewer viewer, Object e1, Object e2) {
				TagCfgTpl v1 = (TagCfgTpl) e1;
				TagCfgTpl v2 = (TagCfgTpl) e2;

				return PinyinComparator.INSTANCE.compare(v1.getTagName(),
						v2.getTagName());
			}
		};

		GridViewerColumn gridViererColumn_6 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_6 = gridViererColumn_6.getColumn();
		gridColumn_6.setText("变量类型");
		gridColumn_6.setWidth(60);
		gridViererColumn_6
				.setEditingSupport(new EditingSupport(gridTableViewer) {
					protected boolean canEdit(Object element) {
						return true;
					}

					protected CellEditor getCellEditor(Object element) {
						CellEditor ce = new ComboBoxCellEditor(grid,
								varTypeArray);
						return ce;
					}

					protected Object getValue(Object element) {
						TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
						if (tagCfgTpl.getVarType() != null) {
							for (int i = 1; i < varTypeArray.length; i++) {
								if (tagCfgTpl.getVarType().equals(
										VarType.getByValue(varTypeArray[i]))) {
									return i;
								}
							}
						}
						return 0;
					}

					protected void setValue(Object element, Object value) {
						TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
						int index = (int)value;
						if (index<=0) {
							tagCfgTpl.setVarType(null);
						} else {
							tagCfgTpl.setVarType(VarType
									.getByValue(varTypeArray[index]));
						}

						gridTableViewer.update(tagCfgTpl, null);
					}
				});

		GridViewerColumn gridViererColumn_1 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_1 = gridViererColumn_1.getColumn();
		gridColumn_1.setText("子类型");
		gridColumn_1.setWidth(70);
		gridViererColumn_1
		.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new ComboBoxCellEditor(grid,
						varSubTypeArray);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
				if (tagCfgTpl.getSubType() != null) {
					for (int i = 1; i < varSubTypeArray.length; i++) {
						if (tagCfgTpl.getSubType().equals(
								VarSubType.getByValue(varSubTypeArray[i]))) {
							return i;
						}
					}
				}
				return 0;
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
				int index = (int)value;
				if (index<=0) {
					tagCfgTpl.setSubType(null);
				} else {
					tagCfgTpl.setSubType(VarSubType
							.getByValue(varSubTypeArray[index]));
				}

				gridTableViewer.update(tagCfgTpl, null);
			}
		});

		GridViewerColumn gridViererColumn_2 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_2 = gridViererColumn_2.getColumn();
		gridColumn_2.setText("变量分组");
		gridColumn_2.setWidth(70);

		GridViewerColumn gridViererColumn_3 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_3 = gridViererColumn_3.getColumn();
		gridColumn_3.setText("变量标志");
		gridColumn_3.setWidth(70);

		GridViewerColumn gridViewerColumn_5 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_5 = gridViewerColumn_5.getColumn();
		gridColumn_5.setText("功能码");
		gridColumn_5.setWidth(50);
		gridViewerColumn_5.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return String.valueOf(tct.getFunCode());
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "功能码不能为空！");
					return;
				}
				int myValue;
				try {
					myValue = Integer.valueOf((String)value);
				} catch (NumberFormatException e) {
					MessageDialog.openError(grid.getShell(), "错误", "功能码应该为整形！");
					e.printStackTrace();
					return;
				}
				
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setFunCode(myValue);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_7 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_7 = gridViewerColumn_7.getColumn();
		gridColumn_7.setText("数据地址");
		gridColumn_7.setWidth(60);
		gridViewerColumn_7.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return String.valueOf(tct.getDataId());
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "数据地址不能为空！");
					return;
				}
				int myValue;
				try {
					myValue = Integer.valueOf((String)value);
				} catch (NumberFormatException e) {
					MessageDialog.openError(grid.getShell(), "错误", "数据地址应该为整形！");
					e.printStackTrace();
					return;
				}
				
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setDataId(myValue);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_8 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_8 = gridViewerColumn_8.getColumn();
		gridColumn_8.setText("字节长度");
		gridColumn_8.setWidth(60);
		gridViewerColumn_8.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return String.valueOf(tct.getByteLen());
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "字节长度不能为空！");
					return;
				}
				int myValue;
				try {
					myValue = Integer.valueOf((String)value);
				} catch (NumberFormatException e) {
					MessageDialog.openError(grid.getShell(), "错误", "字节长度应该为整形！");
					e.printStackTrace();
					return;
				}
				
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setByteLen(myValue);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_9 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_9 = gridViewerColumn_9.getColumn();
		gridColumn_9.setText("字节偏移量");
		gridColumn_9.setWidth(75);
		gridViewerColumn_9.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return String.valueOf(tct.getByteOffset());
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "字节偏移量不能为空！");
					return;
				}
				int myValue;
				try {
					myValue = Integer.valueOf((String)value);
				} catch (NumberFormatException e) {
					MessageDialog.openError(grid.getShell(), "错误", "字节偏移量应该为整形！");
					e.printStackTrace();
					return;
				}
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setByteOffset(myValue);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_10 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_10 = gridViewerColumn_10.getColumn();
		gridColumn_10.setText("位偏移量");
		gridColumn_10.setWidth(70);
		gridViewerColumn_10.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return String.valueOf(tct.getBitOffset());
			}

			protected void setValue(Object element, Object value) {
				if("".equals((String) value)) {
					MessageDialog.openError(grid.getShell(), "错误", "位偏移量不能为空！");
					return;
				}
				int myValue;
				try {
					myValue = Integer.valueOf((String)value);
				} catch (NumberFormatException e) {
					MessageDialog.openError(grid.getShell(), "错误", "位偏移量应该为整形！");
					e.printStackTrace();
					return;
				}
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setBitOffset(myValue);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_4 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_4 = gridViewerColumn_4.getColumn();
		gridColumn_4.setText("值类型");
		gridColumn_4.setWidth(70);
		gridViewerColumn_4
		.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new ComboBoxCellEditor(grid,
						varDataTypeArray);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
				if (tagCfgTpl.getDataType() != null) {
					for (int i = 1; i < varDataTypeArray.length; i++) {
						if (tagCfgTpl.getDataType().equals(
								DataType.getByValue(varDataTypeArray[i]))) {
							return i;
						}
					}
				}
				return 0;
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tagCfgTpl = (TagCfgTpl) element;
				int index = (int)value;
				if (index<=0) {
					tagCfgTpl.setDataType(null);
				} else {
					tagCfgTpl.setDataType(DataType
							.getByValue(varDataTypeArray[index]));
				}

				gridTableViewer.update(tagCfgTpl, null);
			}
		});

		GridViewerColumn gridViewerColumn_11 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_11 = gridViewerColumn_11.getColumn();
		gridColumn_11.setText("基数");
		gridColumn_11.setWidth(40);
		gridViewerColumn_11.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getBaseValue()==null?"":String.valueOf(tct.getBaseValue());
			}

			protected void setValue(Object element, Object value) {
				
				TagCfgTpl tct = (TagCfgTpl) element;
				String myValue = (String)value;
				tct.setBaseValue("".equals(myValue)?null:Float.valueOf(myValue));
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_12 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_12 = gridViewerColumn_12.getColumn();
		gridColumn_12.setText("系数");
		gridColumn_12.setWidth(40);
		gridViewerColumn_12.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getCoefValue()==null?"":String.valueOf(tct.getCoefValue());
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				String myValue = (String)value;
				tct.setCoefValue("".equals(myValue)?null:Float.valueOf(myValue));
				gridTableViewer.update(tct, null);
			}
		});

		// GridColumnGroup gridColumnGroup_2 = new GridColumnGroup(grid,
		// SWT.TOGGLE);
		// gridColumnGroup_2.setText("扩展信息");

		GridViewerColumn gridViewerColumn_13 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_13 = gridViewerColumn_13.getColumn();
		gridColumn_13.setText("存储规则");
		gridColumn_13.setWidth(65);
		gridViewerColumn_13.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getStorage()==null?"":tct.getStorage();
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setStorage("".equals((String)value)?null:(String)value);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_14 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_14 = gridViewerColumn_14.getColumn();
		gridColumn_14.setText("触发规则");
		gridColumn_14.setWidth(65);
		gridViewerColumn_14.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getTriggerName()==null?"":tct.getTriggerName();
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setTriggerName("".equals((String)value)?null:(String)value);
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_15 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_15 = gridViewerColumn_15.getColumn();
		gridColumn_15.setText("最大值");
		gridColumn_15.setWidth(60);
		gridViewerColumn_15.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getMaxValue()==null?"":String.valueOf(tct.getMaxValue());
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setMaxValue("".equals((String)value)?null:Double.valueOf((String)value));
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_16 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_16 = gridViewerColumn_16.getColumn();
		gridColumn_16.setWidth(60);
		gridColumn_16.setText("最小值");
		gridViewerColumn_16.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getMinValue()==null?"":String.valueOf(tct.getMinValue());
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setMinValue("".equals((String)value)?null:Double.valueOf((String)value));
				gridTableViewer.update(tct, null);
			}
		});

		GridViewerColumn gridViewerColumn_17 = new GridViewerColumn(
				gridTableViewer, SWT.NONE);
		GridColumn gridColumn_17 = gridViewerColumn_17.getColumn();
		gridColumn_17.setWidth(65);
		gridColumn_17.setText("脉冲单位");
		gridViewerColumn_17.setEditingSupport(new EditingSupport(gridTableViewer) {
			protected boolean canEdit(Object element) {
				return true;
			}

			protected CellEditor getCellEditor(Object element) {
				CellEditor ce = new TextCellEditor(grid);
				return ce;
			}

			protected Object getValue(Object element) {
				TagCfgTpl tct = (TagCfgTpl) element;
				return tct.getUnitValue()==null?"":String.valueOf(tct.getUnitValue());
			}

			protected void setValue(Object element, Object value) {
				TagCfgTpl tct = (TagCfgTpl) element;
				tct.setUnitValue("".equals((String)value)?null:Integer.valueOf((String)value));
				gridTableViewer.update(tct, null);
			}
		});

		Menu menu = new Menu(grid);
		grid.setMenu(menu);

		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) listViewer_1
						.getSelection();
				if (addedTplName == null && selection.isEmpty()) {// 非新增变量且未选择
					MessageDialog.openWarning(grid.getShell(), "提示", "未选择模板！");
					return;
				}

				TagCfgTpl tagCfgTpl = new TagCfgTpl();
				tagCfgTpl.setTagName("新增的变量");

				tagCfgTplList.add(tagCfgTpl);

				gridTableViewer.setInput(tagCfgTplList);
				gridTableViewer.refresh();
				// gridTableViewer.setSelection(selection);
			}
		});
		menuItem.setText("添加变量");

		MenuItem menuItem_1 = new MenuItem(menu, SWT.NONE);
		menuItem_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) gridTableViewer
						.getSelection();
				if (selection.isEmpty()) {
					MessageDialog.openWarning(grid.getShell(), "提示", "未选择变量！");
					return;
				}

				GridItem gridItems[] = grid.getSelection();
				for (GridItem gi : gridItems) {
					TagCfgTpl selectedTpl = (TagCfgTpl) gi.getData();
					tagCfgTplList.remove(selectedTpl);

					if (selectedTpl.getId() != null) {
						deletedTplList.add(selectedTpl);
					}
				}
				gridTableViewer.refresh();

			}
		});
		menuItem_1.setText("删除变量");

		gridTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		gridTableViewer.setLabelProvider(new ViewerLabelProvider_1());
		gridTableViewer.setInput(tagCfgTplList);

		Composite composite_2 = new Composite(composite_1, SWT.NONE);
		composite_2.setLayout(new GridLayout(1, false));
		composite_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		composite_2.setBounds(0, 0, 64, 64);

		Button button_1 = new Button(composite_2, SWT.NONE);
		button_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		button_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if ("".equals(text_tpl_name.getText().trim())) {
					MessageDialog.openError(grid.getShell(), "错误", "模板名不能为空！");
					return;
				}

				if (tagCfgTplList == null || tagCfgTplList.isEmpty()) {
					MessageDialog.openError(grid.getShell(), "错误",
							"模板无变量，无法保存！");
					return;
				}

				for (TagCfgTpl tct : deletedTplList) {
					tagCfgTplService.deleteById(tct.getId());
				}

				for (TagCfgTpl tct : tagCfgTplList) {
					tct.setTplName(text_tpl_name.getText().trim());//更新模板名字
					//更新变量分组与变量Key
					if(tct.getSubType() == null) {
						tct.setVarGroup(null);
						tct.setVarName(null);
					} else {
						tct.setVarGroup(tct.getSubType().getVarGroup());
						tct.setVarName(tct.getSubType().toString().toLowerCase());
					}
					
					
					tagCfgTplService.update(tct);
				}

				tplNameList = tagCfgTplService.findAllTplName();
				listViewer_1.setInput(tplNameList);
				listViewer_1.refresh();
				deletedTplList.clear();
				
				gridTableViewer.refresh();
			}
		});
		button_1.setText("  保 存 模 板  ");
		sashForm.setWeights(new int[] { 78, 953 });

	}

	private class MenuListener implements IMenuListener {
		private ListViewer listViewer;

		public MenuListener(ListViewer listViewer) {
			this.listViewer = listViewer;
		}

		@Override
		public void menuAboutToShow(IMenuManager manager) {
			IStructuredSelection selection = (IStructuredSelection) listViewer
					.getSelection();
			if (!selection.isEmpty()) {
				createContextMenu(selection.getFirstElement());
			} else {// 新建
				Action action = new Action() {
					public void run() {
						addTpl();
					}
				};
				action.setText("新建变量模板");
				menuMng.add(action);
			}
		}

		/**
		 * 右键菜单内容
		 * 
		 * @param
		 */
		private void createContextMenu(final Object selectedObject) {
			if (selectedObject instanceof String) {
				Action action = new Action() {
					public void run() {
						addTpl();
					}
				};
				action.setText("新建变量模板");
				menuMng.add(action);

				action = new Action() {
					public void run() {
						String tplNames[] = listViewer_1.getList()
								.getSelection();
						for (String name : tplNames) {
							tagCfgTplService.deleteAllVariablesByTplName(name);
						}

						tplNameList = tagCfgTplService.findAllTplName();
						listViewer_1.setInput(tplNameList);
						listViewer_1.refresh();

						tagCfgTplList.clear();
						deletedTplList.clear();
						gridTableViewer.setInput(tagCfgTplList);
						gridTableViewer.refresh();

					}
				};
				action.setText("删除变量模板");
				menuMng.add(action);

			}
		}

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private static class ViewerLabelProvider_1 extends LabelProvider implements
			ITableLabelProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			TagCfgTpl tagCfgTpl = (TagCfgTpl) element;

			switch (columnIndex) {
			case 0:// 变量名
				return tagCfgTpl.getTagName();
			case 1:// 变量类型
				return tagCfgTpl.getVarType() == null ? null : tagCfgTpl
						.getVarType().getValue();
			case 2:// 变量子类型
				return tagCfgTpl.getSubType() == null ? null : tagCfgTpl
						.getSubType().getValue();
			case 3:// 变量分组
				return tagCfgTpl.getVarGroup() == null ? null : tagCfgTpl
						.getVarGroup().getValue();
			case 4:// 变量key
				return tagCfgTpl.getVarName();
			case 5:// 功能码
				return String.valueOf(tagCfgTpl.getFunCode());
			case 6:// 数据地址
				return String.valueOf(tagCfgTpl.getDataId());
			case 7:// 字节长度
				return String.valueOf(tagCfgTpl.getByteLen());
			case 8:// 字节偏移量
				return String.valueOf(tagCfgTpl.getByteOffset());
			case 9:// 位偏移量
				return String.valueOf(tagCfgTpl.getBitOffset());
			case 10:// 值类型
				return tagCfgTpl.getDataType() == null ? null : tagCfgTpl
						.getDataType().getValue();
			case 11:// 基数
				return tagCfgTpl.getBaseValue() == null ? "" : String
						.valueOf(tagCfgTpl.getBaseValue());
			case 12:// 系数
				return tagCfgTpl.getCoefValue() == null ? "" : String
						.valueOf(tagCfgTpl.getCoefValue());
			case 13:// 存储规则
				return tagCfgTpl.getStorage();
			case 14:// 触发规则
				return tagCfgTpl.getTriggerName();
			case 15:// 最大值
				return tagCfgTpl.getMaxValue() == null ? "" : String
						.valueOf(tagCfgTpl.getMaxValue());
			case 16:// 最小值
				return tagCfgTpl.getMinValue() == null ? "" : String
						.valueOf(tagCfgTpl.getMinValue());
			case 17:// 脉冲单位
				return tagCfgTpl.getUnitValue() == null ? "" : String
						.valueOf(tagCfgTpl.getUnitValue());

			default:
				break;
			}

			return null;
		}
	}

	private static class ViewerLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return super.getImage(element);
		}

		public String getText(Object element) {
			if (element instanceof String) {
				return (String) element;
			}
			return super.getText(element);
		}
	}

	/**
	 * 初始化模板信息
	 * 
	 * @param tplName
	 */
	private void initTplInfo(String tplName) {
		text_tpl_name.setText(tplName);
		if (!"".equals(tplName)) {
			initVariableByTplName(tplName);
		}
	}

	private void initVariableByTplName(String tplName) {
		tagCfgTplList.clear();

		tagCfgTplList = tagCfgTplService.findVariablesByTplName(tplName);
		log.debug("变量个数：" + tagCfgTplList.size());

		gridTableViewer.setInput(tagCfgTplList);
		gridTableViewer.refresh();

	}

	/**
	 * 新建变量模板
	 */
	private void addTpl() {
		text_tpl_name.setText("新增变量模板");
		addedTplName = text_tpl_name.getText();
		selectedTplName = null;

		listViewer_1.setSelection(null);

		tagCfgTplList.clear();
		deletedTplList.clear();
		gridTableViewer.setInput(tagCfgTplList);
		gridTableViewer.refresh();

	}
}
