package org.intellij.plugins.ui.pathlisteditor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;
import org.intellij.plugins.ui.common.CheckBoxTableCellRenderer;
import org.intellij.plugins.ui.common.SimpleScrollPane;
import org.intellij.plugins.ui.common.SingleRowSelectionTable;
import org.intellij.plugins.util.FileUtil;
import org.intellij.plugins.util.testing.MockFileUtil;
import org.intellij.openapi.testing.MockProject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PathListEditorPanel extends EditableListPanel {
   private ExcludedPathsFromVcsConfiguration configuration;
   private String pathWhereUserLeftOffLastTime;
   private SingleRowSelectionTable table;
   private ArrayList/*<PathListElement>*/ pathListElements = new ArrayList/*<PathListElement>*/();
   private JButton addFileButton;
   private JButton addDirButton;
   private JButton removeButton;
   private static final Logger LOG = Logger.getInstance(PathListEditorPanel.class.getName());
   private FileUtil fileUtil;

   public PathListEditorPanel(String initialSelection, ExcludedPathsFromVcsConfiguration configuration) {
      this(initialSelection, configuration, new FileUtil());
   }

   public PathListEditorPanel(String initialSelection, ExcludedPathsFromVcsConfiguration configuration, FileUtil fileUtil) {
      this.configuration = configuration;
      this.fileUtil = fileUtil;
      this.pathWhereUserLeftOffLastTime = initialSelection;
      initUI();
   }

   protected String getTitle() {
      return null;
   }

   protected JButton[] getButtons() {
      addDirButton = new JButton("Add Directory...");
      addDirButton.setMnemonic('D');
      addDirButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent event) { addDirectory(); }
      });
      addFileButton = new JButton("Add File...");
      addFileButton.setMnemonic('F');
      addFileButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent event) { addFile(); }
      });
      removeButton = new JButton("Remove");
      removeButton.setMnemonic('R');
      removeButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent event) { removeSelection(); }

      });
      return (new JButton[]{
         addDirButton, addFileButton, removeButton
      });
   }

   private void addFile() {
      addPath(true);
   }

   private void addDirectory() {
      addPath(false);
   }

   private void addPath( boolean isFile) {
      int selectedRow = table.getSelectedRow() + 1;
      if (selectedRow < 0)
         selectedRow = pathListElements.size();
      int savedSelectedRow = selectedRow;
      File[] selectedFiles = askForAdditionalPathsToExclude(isFile);

      VirtualFile[] chosenPaths = convertFilesToVirtualFiles(selectedFiles);

      for (int i = 0; i < chosenPaths.length; i++) {
         VirtualFile chosenPath = chosenPaths[i];
         debug("adding path = " + chosenPath);
         if (!isAlreadyAddedToPaths(chosenPath)) {
            PathListElement newElt;
            if (isFile)
               newElt = new PathListElement(chosenPath, false, true);
            else
               newElt = new PathListElement(chosenPath, true, false);
            pathListElements.add(selectedRow, newElt);
            selectedRow++;
         }
      }

      if (selectedRow > savedSelectedRow) {
         AbstractTableModel model = (AbstractTableModel) table.getModel();
         model.fireTableRowsInserted(savedSelectedRow, selectedRow - 1);
         table.setRowSelectionInterval(savedSelectedRow, selectedRow - 1);
      }
   }

   private VirtualFile[] convertFilesToVirtualFiles(File[] selectedFiles) {
      VirtualFile chosenPaths[] = new VirtualFile[selectedFiles.length];
      for (int i = 0; i < selectedFiles.length; i++) {
         File selectedFile = selectedFiles[i];
         chosenPaths[i] = fileUtil.ioFileToVirtualFile(selectedFile);
      }
      return chosenPaths;
   }

    private File[] askForAdditionalPathsToExclude(boolean isFile) {
      JFileChooser fileChooser = new JFileChooser(pathWhereUserLeftOffLastTime);
      fileChooser.setMultiSelectionEnabled(true);
      String approveButtonText;
      if (isFile) {
         approveButtonText = "Add File";
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      } else {
         approveButtonText = "Add Directory";
         fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      }
      int result = fileChooser.showDialog(this, approveButtonText);
      File[] selectedFiles = new File[0] ;
      if (result == JFileChooser.APPROVE_OPTION)
      {
         selectedFiles = fileChooser.getSelectedFiles();
         if (selectedFiles.length > 0) {
            pathWhereUserLeftOffLastTime = selectedFiles[0].getParent();
         }
      }

      return selectedFiles;
   }

   private boolean isAlreadyAddedToPaths(VirtualFile virtualFile) {
      for (Iterator/*<PathListElement>*/ iterator = pathListElements.iterator(); iterator.hasNext();) {
         PathListElement elt = (PathListElement) iterator.next();
         VirtualFile virtualFileInPaths = elt.getVirtualFile();
         if (virtualFileInPaths != null && virtualFile.equals(virtualFileInPaths))
            return true;
      }

      return false;
   }

   private void removeSelection() {
      int rowSelected = table.getSelectedRow();
      if (rowSelected < 0)
         return;
      if (table.isEditing()) {
         TableCellEditor cellEditor = table.getCellEditor();
         if (cellEditor != null)
            cellEditor.stopCellEditing();
      }
      pathListElements.remove(rowSelected);
      AbstractTableModel abstracttablemodel = (AbstractTableModel) table.getModel();
      abstracttablemodel.fireTableRowsDeleted(rowSelected, rowSelected);
      if (rowSelected >= pathListElements.size())
         rowSelected--;
      if (rowSelected >= 0)
         table.setRowSelectionInterval(rowSelected, rowSelected);
   }

   protected JComponent getListPanel() {
      String columnLabels[] = {
         "Path", "Recursively"
      };
      PathListTableModel model = new PathListTableModel(this,columnLabels);
      table = new SingleRowSelectionTable(model);
      table.setPreferredScrollableViewportSize(new Dimension(300, table.getRowHeight() * 6));
      table.setDefaultRenderer(Boolean.class, new CheckBoxTableCellRenderer());
      table.setDefaultRenderer(Object.class, new PathListElementTableCellRenderer());
      table.getColumn(columnLabels[0]).setPreferredWidth(350);
      table.getColumn(columnLabels[1]).setPreferredWidth(140);
      table.getSelectionModel().setSelectionMode(0);
      TableCellEditor editor = table.getDefaultEditor(String.class);
      if (editor instanceof DefaultCellEditor)
         ((DefaultCellEditor) editor).setClickCountToStart(1);
      SimpleScrollPane scrollPane = new SimpleScrollPane(table);
      return scrollPane;
   }

   public void reset() {
      List paths = getConfiguration().getExcludedPaths();
      pathListElements.clear();
      for (Iterator iterator = paths.iterator(); iterator.hasNext();) {
         PathListElement element = (PathListElement) iterator.next();
         pathListElements.add(element.cloneMyself());
      }
   }

   public void apply() {
      getConfiguration().resetExcludedPaths(pathListElements);
   }

   public boolean isModified() {
      return !pathListElements.equals(getConfiguration().getExcludedPaths());
   }

   private ExcludedPathsFromVcsConfiguration getConfiguration() {
      return configuration;
   }

   public ArrayList/*<PathListElement>*/ getPathListElements() {
      return pathListElements;
   }

   static public void main(String[] args) {
      JFrame frame = new JFrame();
      PathListEditorPanel r = new PathListEditorPanel("c:/",
                                                      new ExcludedPathsFromVcsConfiguration(new MockProject()),
                                                      new MockFileUtil());
      r.setBorder(BorderFactory.createCompoundBorder(createEtchedTitleBorder("Exclude from Compile"),
                                                     BorderFactory.createEmptyBorder(2, 2, 2, 2)));
      frame.getContentPane().setLayout(new GridBagLayout());
      frame.getContentPane().add(r, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, 10, 1, new Insets(2, 2, 2, 2), 0, 0));
      frame.pack();
      frame.setVisible(true);
   }

   public static TitledBorder createEtchedTitleBorder(String s) {
      return BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), s);
   }

   private void debug(String message) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(message);
      }
   }

}
