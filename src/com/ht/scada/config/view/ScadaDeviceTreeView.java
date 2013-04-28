package com.ht.scada.config.view;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ht.scada.common.tag.entity.AcquisitionChannel;
import com.ht.scada.common.tag.entity.AcquisitionDevice;
import com.ht.scada.common.tag.entity.AreaMinorTag;
import com.ht.scada.common.tag.service.AcquisitionChannelService;
import com.ht.scada.common.tag.service.AreaMinorTagService;
import com.ht.scada.config.scadaconfig.Activator;
import com.ht.scada.config.util.FirePropertyConstants;
import com.ht.scada.config.util.ViewPropertyChange;
import com.ht.scada.config.view.tree.RootTreeModel;
import com.ht.scada.config.view.tree.ScadaDeviceTreeContentProvider;
import com.ht.scada.config.view.tree.ScadaDeviceTreeLabelProvider;

public class ScadaDeviceTreeView extends ViewPart {
	
	private static final Logger log = LoggerFactory.getLogger(ScadaDeviceTreeView.class);
	
	private AcquisitionChannelService acquisitionChannelService = (AcquisitionChannelService) Activator.getDefault()
			.getApplicationContext().getBean("acquisitionChannelService");

	
	public ScadaDeviceTreeView() {
	}

	public static final String ID = "com.ht.scada.config.view.ScadaDeviceTreeView";

	private MenuManager menuMng;
	private TreeViewer treeViewer;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite filterComposite = new Composite(parent, SWT.NONE);
		filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label label = new Label(filterComposite, SWT.NONE);
		label.setBounds(334, 5, 94, 17);
		label.setText("第1页/共4页");
		
		Button btnNewButton = new Button(filterComposite, SWT.NONE);
		btnNewButton.setBounds(145, 0, 80, 27);
		btnNewButton.setText("上一页");
		
		Button btnNewButton_1 = new Button(filterComposite, SWT.NONE);
		btnNewButton_1.setBounds(248, 0, 80, 27);
		btnNewButton_1.setText("下一页");
		
		Label label_1 = new Label(filterComposite, SWT.NONE);
		label_1.setBounds(10, 5, 61, 17);
		label_1.setText("每页显示：");
		
		Combo combo = new Combo(filterComposite, SWT.NONE);
		combo.setBounds(77, 0, 51, 25);
		combo.setText("100");
		
		Composite treeComposite = new Composite(parent, SWT.NONE);
		treeComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
		GridData gd_treeComposite = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_treeComposite.heightHint = 119;
		treeComposite.setLayoutData(gd_treeComposite);
		
		treeViewer = new TreeViewer(treeComposite, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		treeViewer.setAutoExpandLevel(3);
		
		treeViewer.setContentProvider(new ScadaDeviceTreeContentProvider());
		treeViewer.setLabelProvider(new ScadaDeviceTreeLabelProvider());
		treeViewer.setInput("channel");

		Tree tree = treeViewer.getTree();
		menuMng = new MenuManager();
		menuMng.setRemoveAllWhenShown(true);
		
		menuMng.addMenuListener(new MenuListener(treeViewer));
		tree.setMenu(menuMng.createContextMenu(tree));
		
				// 点击打开编辑页面
				tree.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseDown(MouseEvent e) {
						if (e.button == 1) { // 右键
							IStructuredSelection sel = ((IStructuredSelection) treeViewer
									.getSelection());
							if (!sel.isEmpty()) {
								final Object obj = ((IStructuredSelection) treeViewer
										.getSelection()).getFirstElement();
								Display.getDefault().timerExec(
										Display.getDefault().getDoubleClickTime(),
										new Runnable() {
											public void run() {
												edit(obj);
											}
										});
							}
						}
					}
		
				});

		
	}

	private class MenuListener implements IMenuListener {
		private TreeViewer treeViewer;

		public MenuListener(TreeViewer treeViewer) {
			this.treeViewer = treeViewer;
		}

		@Override
		public void menuAboutToShow(IMenuManager manager) {
			IStructuredSelection selection = (IStructuredSelection) treeViewer
					.getSelection();
			if (!selection.isEmpty()) {
				createContextMenu(selection.getFirstElement());
			}
		}

		/**
		 * 右键菜单内容
		 * 
		 * @param selectedObject
		 */
		private void createContextMenu(final Object selectedObject) {
			if (selectedObject instanceof String) {
				final String str = (String) selectedObject;

				if (str.equals("采集通道")) {// 采集通道
					Action objectIndex = new Action() {
						public void run() {
							try {
								PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow()
										.getActivePage()
										.showView(ScadaChannelIndexView.ID);
							} catch (PartInitException e) {
								e.printStackTrace();
							}
							ViewPropertyChange
									.getInstance()
									.firePropertyChangeListener(
											FirePropertyConstants.ACQUISITIONCHANNEL_ADD,
											selectedObject);

						}
					};
					objectIndex.setText("添加采集通道(&A)");
					menuMng.add(objectIndex);
				}
			} else if (selectedObject instanceof AcquisitionChannel) { // 采集通道
				final AcquisitionChannel acquisitionChannel = (AcquisitionChannel) selectedObject;

				// ===============添加设备=======================
				Action objectIndex = new Action() {
					public void run() {
						try {
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.showView(ScadaDeviceIndexView.ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
						ViewPropertyChange.getInstance()
								.firePropertyChangeListener(
										FirePropertyConstants.ACQUISITIONDEVICE_ADD,
										selectedObject);

					}
				};
				objectIndex.setText("添加设备(&A)");
				menuMng.add(objectIndex);

				// ===============修改采集通道(E)=======================
				objectIndex = new Action() {
					public void run() {
						try {
							PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage()
									.showView(ScadaChannelIndexView.ID);
						} catch (PartInitException e) {
							e.printStackTrace();
						}
						ViewPropertyChange.getInstance()
								.firePropertyChangeListener(
										FirePropertyConstants.ACQUISITIONCHANNEL_EDIT,
										selectedObject);

					}
				};
				objectIndex.setText("修改采集通道(&E)");
				menuMng.add(objectIndex);

				// ===============删除采集通道(D)=======================
				objectIndex = new Action() {
					public void run() {
						if (MessageDialog.openConfirm(treeViewer.getTree()
								.getShell(), "删除", "确认要删除吗？")) {
							acquisitionChannelService.deleteById(acquisitionChannel
									.getId().intValue());

							treeViewer.remove(acquisitionChannel);
						}
					}
				};
				objectIndex.setText("删除采集通道(&D)");
				menuMng.add(objectIndex);
			}
		}

	}
	
	private void edit(Object object) {
		if (object instanceof AcquisitionChannel) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(ScadaChannelIndexView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			ViewPropertyChange.getInstance().firePropertyChangeListener(
					FirePropertyConstants.ACQUISITIONCHANNEL_EDIT, object);
		} else if (object instanceof AcquisitionDevice) {
			try {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().showView(ScadaDeviceIndexView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			ViewPropertyChange.getInstance().firePropertyChangeListener(
					FirePropertyConstants.ACQUISITIONDEVICE_EDIT, object);
		}
		
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}
}