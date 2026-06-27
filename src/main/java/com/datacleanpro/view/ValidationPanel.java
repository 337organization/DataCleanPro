package com.datacleanpro.view;

import com.datacleanpro.model.DataFile;
import com.datacleanpro.model.ValidationResult;
import com.datacleanpro.model.ValidationRule;
import com.datacleanpro.service.DataImportService;
import com.datacleanpro.service.ValidationService;
import com.datacleanpro.util.LogUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 数据验证面板
 * 处理数据验证规则管理
 */
public class ValidationPanel extends JPanel {

    private JTable rulesTable;
    private DefaultTableModel rulesTableModel;
    private JComboBox<FileItem> fileComboBox;
    private JButton refreshButton;
    private JButton addRuleButton;
    private JButton editRuleButton;
    private JButton deleteRuleButton;
    private JButton validateButton;
    private JTextArea resultArea;
    private DataImportService importService;
    private List<DataFile> cachedFiles;
    private List<ValidationRule> cachedRules;

    public ValidationPanel() {
        importService = new DataImportService();
        cachedFiles = new ArrayList<>();
        cachedRules = new ArrayList<>();
        initComponents();
        setupLayout();
        setupEventHandlers();
        loadInitialData();
    }

    private void initComponents() {
        rulesTableModel = new DefaultTableModel(new String[]{"ID", "规则名称", "规则类型", "目标列", "状态", "错误提示"}, 0);
        rulesTable = new JTable(rulesTableModel);
        rulesTable.removeColumn(rulesTable.getColumnModel().getColumn(0));

        fileComboBox = new JComboBox<>();
        refreshButton = new JButton("刷新");
        addRuleButton = new JButton("添加规则");
        editRuleButton = new JButton("编辑规则");
        deleteRuleButton = new JButton("删除规则");
        validateButton = new JButton("执行验证");
        resultArea = new JTextArea();

        resultArea.setEditable(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(new JLabel("文件:"));
        buttonPanel.add(fileComboBox);
        buttonPanel.add(refreshButton);
        buttonPanel.add(addRuleButton);
        buttonPanel.add(editRuleButton);
        buttonPanel.add(deleteRuleButton);
        buttonPanel.add(validateButton);

        JScrollPane tableScrollPane = new JScrollPane(rulesTable);

        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(800, 220));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                            tableScrollPane, resultScrollPane);
        splitPane.setDividerLocation(300);

        add(buttonPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadInitialData());
        addRuleButton.addActionListener(e -> showRuleDialog(null));
        editRuleButton.addActionListener(e -> editSelectedRule());
        deleteRuleButton.addActionListener(e -> deleteSelectedRule());
        validateButton.addActionListener(e -> validateSelectedFile());
    }

    /**
     * 加载文件和规则
     */
    private void loadInitialData() {
        loadFiles();
        loadRules();
    }

    private void loadFiles() {
        SwingWorker<List<DataFile>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DataFile> doInBackground() {
                return importService.getAllFiles();
            }

            @Override
            protected void done() {
                try {
                    cachedFiles = get();
                    fileComboBox.removeAllItems();
                    for (DataFile file : cachedFiles) {
                        fileComboBox.addItem(new FileItem(file));
                    }
                } catch (Exception e) {
                    LogUtil.error("加载验证文件列表失败", e);
                    appendResult("加载文件失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void loadRules() {
        SwingWorker<List<ValidationRule>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ValidationRule> doInBackground() {
                return ValidationService.getAllRules();
            }

            @Override
            protected void done() {
                try {
                    cachedRules = get();
                    rulesTableModel.setRowCount(0);
                    for (ValidationRule rule : cachedRules) {
                        rulesTableModel.addRow(new Object[]{
                                rule.getId(),
                                rule.getRuleName(),
                                rule.getRuleType(),
                                rule.getTargetColumn(),
                                rule.isActive() ? "启用" : "禁用",
                                rule.getErrorMessage()
                        });
                    }
                    appendResult("已加载 " + cachedRules.size() + " 条验证规则");
                } catch (Exception e) {
                    LogUtil.error("加载验证规则失败", e);
                    appendResult("加载规则失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 显示规则编辑对话框
     * @param existingRule 已有规则
     */
    private void showRuleDialog(ValidationRule existingRule) {
        JTextField nameField = new JTextField(existingRule != null ? existingRule.getRuleName() : "", 20);
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"REQUIRED", "EMAIL", "PHONE", "NUMBER_RANGE", "REGEX"});
        JTextField targetField = new JTextField(existingRule != null ? nullToEmpty(existingRule.getTargetColumn()) : "", 20);
        JTextField expressionField = new JTextField(existingRule != null ? nullToEmpty(existingRule.getExpression()) : "", 20);
        JTextField errorField = new JTextField(existingRule != null ? nullToEmpty(existingRule.getErrorMessage()) : "", 20);
        JCheckBox activeCheckBox = new JCheckBox("启用", existingRule == null || existingRule.isActive());

        if (existingRule != null) {
            typeComboBox.setSelectedItem(existingRule.getRuleType());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("规则名称:"));
        panel.add(nameField);
        panel.add(new JLabel("规则类型:"));
        panel.add(typeComboBox);
        panel.add(new JLabel("目标列:"));
        panel.add(targetField);
        panel.add(new JLabel("表达式/范围:"));
        panel.add(expressionField);
        panel.add(new JLabel("错误提示:"));
        panel.add(errorField);
        panel.add(new JLabel("状态:"));
        panel.add(activeCheckBox);

        int result = JOptionPane.showConfirmDialog(this, panel,
                existingRule == null ? "添加规则" : "编辑规则",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        ValidationRule rule = existingRule != null ? existingRule : new ValidationRule();
        rule.setRuleName(nameField.getText().trim());
        rule.setRuleType((String) typeComboBox.getSelectedItem());
        rule.setTargetColumn(emptyToNull(targetField.getText()));
        rule.setExpression(emptyToNull(expressionField.getText()));
        rule.setErrorMessage(emptyToNull(errorField.getText()));
        rule.setActive(activeCheckBox.isSelected());

        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() {
                return ValidationService.saveRule(rule);
            }

            @Override
            protected void done() {
                try {
                    appendResult("规则已保存，ID: " + get());
                    loadRules();
                } catch (Exception e) {
                    LogUtil.error("保存验证规则失败", e);
                    appendResult("保存规则失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void editSelectedRule() {
        ValidationRule rule = getSelectedRule();
        if (rule == null) {
            JOptionPane.showMessageDialog(this, "请先选择一条规则", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showRuleDialog(rule);
    }

    private void deleteSelectedRule() {
        ValidationRule rule = getSelectedRule();
        if (rule == null) {
            JOptionPane.showMessageDialog(this, "请先选择一条规则", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, "确定删除规则: " + rule.getRuleName() + "？",
                "确认", JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return ValidationService.deleteRule(rule.getId());
            }

            @Override
            protected void done() {
                try {
                    appendResult(get() ? "规则已删除" : "规则删除失败");
                    loadRules();
                } catch (Exception e) {
                    LogUtil.error("删除验证规则失败", e);
                    appendResult("删除规则失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * 执行文件验证
     */
    private void validateSelectedFile() {
        FileItem item = (FileItem) fileComboBox.getSelectedItem();
        if (item == null || item.file == null) {
            JOptionPane.showMessageDialog(this, "请先导入并选择一个文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        appendResult("开始验证文件: " + item.file.getFileName());
        SwingWorker<List<ValidationResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ValidationResult> doInBackground() {
                return ValidationService.validateFile(item.file.getId());
            }

            @Override
            protected void done() {
                try {
                    List<ValidationResult> results = get();
                    Map<String, Object> stats = ValidationService.getValidationStatistics(item.file.getId());
                    appendResult("验证完成，总项: " + stats.get("totalResults") +
                            "，通过: " + stats.get("passedResults") +
                            "，失败: " + stats.get("failedResults") +
                            "，通过率: " + String.format("%.2f%%", stats.get("passRate")));

                    int shown = 0;
                    for (ValidationResult result : results) {
                        if (!result.isPassed()) {
                            appendResult("行 " + result.getRowIndex() + " / " + result.getColumnName() +
                                    " / 值: " + result.getCellValue() +
                                    " / 错误: " + result.getErrorMessage());
                            shown++;
                            if (shown >= 30) {
                                appendResult("失败项较多，仅显示前 30 条。");
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LogUtil.error("执行数据验证失败", e);
                    appendResult("验证失败: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private ValidationRule getSelectedRule() {
        int selectedRow = rulesTable.getSelectedRow();
        if (selectedRow < 0) {
            return null;
        }

        int modelRow = rulesTable.convertRowIndexToModel(selectedRow);
        Long id = (Long) rulesTableModel.getValueAt(modelRow, 0);
        for (ValidationRule rule : cachedRules) {
            if (rule.getId() != null && rule.getId().equals(id)) {
                return rule;
            }
        }
        return null;
    }

    private void appendResult(String message) {
        resultArea.append(message + "\n");
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 文件下拉项
     */
    private static class FileItem {
        private final DataFile file;

        private FileItem(DataFile file) {
            this.file = file;
        }

        @Override
        public String toString() {
            return file == null ? "" : file.getFileName() + " (" + file.getRowCount() + " 行)";
        }
    }
}
