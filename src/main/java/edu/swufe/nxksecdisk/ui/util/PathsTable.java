package edu.swufe.nxksecdisk.ui.util;

import edu.swufe.nxksecdisk.ui.pojo.FileSystemPath;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathsTable extends JTable
{
    /**
     * 表头;
     */
    private static final String[] COLUMN_ARRAY = new String[]{"类型（编号）", "路径"};

    /**
     * 设置每个列的宽度百分比;
     */
    private double[] columnWidthPercentage = {20.0, 80.0};

    /**
     * 记录当前显示的内容;
     */
    private static final Map<Integer, Short> shownFileSystemPath = new HashMap<>();
    /**
     *
     */
    private static final long serialVersionUID = -3436472714356711024L;

    public PathsTable()
    {
        super(new Object[][]{}, COLUMN_ARRAY);
        addComponentListener(new ComponentListener()
        {

            @Override
            public void componentResized(ComponentEvent e)
            {
                resizeColumns();
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
            }

            @Override
            public void componentHidden(ComponentEvent e)
            {
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
        // TODO 自动生成的方法存根
        return false;
    }

    public void updateValues(List<FileSystemPath> paths)
    {
        Runnable doUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                // TODO 自动生成的方法存根
                shownFileSystemPath.clear();
                try
                {
                    setModel(new TableModel()
                    {
                        @Override
                        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
                        {
                            // TODO 自动生成的方法存根
                        }

                        @Override
                        public void removeTableModelListener(TableModelListener l)
                        {
                            // TODO 自动生成的方法存根
                        }

                        @Override
                        public boolean isCellEditable(int rowIndex, int columnIndex)
                        {
                            // TODO 自动生成的方法存根
                            return false;
                        }

                        @Override
                        public Object getValueAt(int rowIndex, int columnIndex)
                        {
                            // TODO 自动生成的方法存根
                            switch (columnIndex)
                            {
                                case 0:
                                    shownFileSystemPath.put(rowIndex, paths.get(rowIndex).getIndex());
                                    return paths.get(rowIndex).getType() + "（" + paths.get(rowIndex).getIndex() + "）";
                                case 1:
                                    return paths.get(rowIndex).getPath().getAbsoluteFile();
                                default:
                                    return "--";
                            }
                        }

                        @Override
                        public int getRowCount()
                        {
                            // TODO 自动生成的方法存根
                            return paths.size();
                        }

                        @Override
                        public String getColumnName(int columnIndex)
                        {
                            // TODO 自动生成的方法存根
                            return COLUMN_ARRAY[columnIndex];
                        }

                        @Override
                        public int getColumnCount()
                        {
                            // TODO 自动生成的方法存根
                            return COLUMN_ARRAY.length;
                        }

                        @Override
                        public Class<?> getColumnClass(int columnIndex)
                        {
                            // TODO 自动生成的方法存根
                            return String.class;
                        }

                        @Override
                        public void addTableModelListener(TableModelListener l)
                        {
                            // TODO 自动生成的方法存根
                        }
                    });
                    resizeColumns();
                    validate();
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
        };
        // 避免操作过快导致的异常
        Thread t = new Thread(() ->
        {
            SwingUtilities.invokeLater(doUpdate);
        });
        t.start();
    }

    public short getSelectFileSystemIndex()
    {
        if (getSelectedRow() >= 0)
        {
            return shownFileSystemPath.get(getSelectedRow());
        }
        return -1;
    }

    private void resizeColumns()
    {
        int tW = getWidth();
        TableColumn column;
        TableColumnModel jTableColumnModel = getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++)
        {
            column = jTableColumnModel.getColumn(i);
            int pWidth = (int) Math.round(columnWidthPercentage[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }
}
