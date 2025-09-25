// ===== 1. FileTextExtractor.java =====
package com.luanjining.rag.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 文件文本提取器
 * 支持多种文件格式的文本提取
 *
 * 支持格式: TXT, MD, PDF, DOC, DOCX, XLS, XLSX, CSV
 */
@Component
public class FileTextExtractor {

    private static final Logger logger = LoggerFactory.getLogger(FileTextExtractor.class);

    /**
     * 根据文件类型提取文本内容
     * @param file 要提取文本的文件
     * @return 提取出的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    public String extractText(File file) throws Exception {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("文件不存在: " + (file != null ? file.getPath() : "null"));
        }

        String fileName = file.getName().toLowerCase();
        logger.debug("开始提取文件文本: {}", fileName);

        try {
            if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                return extractFromTextFile(file);
            } else if (fileName.endsWith(".pdf")) {
                return extractFromPdf(file);
            } else if (fileName.endsWith(".docx")) {
                return extractFromDocx(file);
            } else if (fileName.endsWith(".doc")) {
                return extractFromDoc(file);
            } else if (fileName.endsWith(".xlsx")) {
                return extractFromXlsx(file);
            } else if (fileName.endsWith(".xls")) {
                return extractFromXls(file);
            } else if (fileName.endsWith(".csv")) {
                return extractFromCsv(file);
            } else {
                // 默认当作文本文件处理
                logger.warn("未识别的文件类型，尝试按文本文件处理: {}", fileName);
                return extractFromTextFile(file);
            }
        } catch (Exception e) {
            logger.error("文件文本提取失败: {}", fileName, e);
            throw e;
        }
    }

    /**
     * 提取纯文本文件内容
     */
    private String extractFromTextFile(File file) throws IOException {
        logger.debug("提取文本文件: {}", file.getName());
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * 提取PDF文本内容
     */
    private String extractFromPdf(File file) throws IOException {
        logger.debug("提取PDF文件: {}", file.getName());
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    /**
     * 提取DOCX文本内容
     */
    private String extractFromDocx(File file) throws IOException {
        logger.debug("提取DOCX文件: {}", file.getName());
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 提取DOC文本内容
     */
    private String extractFromDoc(File file) throws IOException {
        logger.debug("提取DOC文件: {}", file.getName());
        try (FileInputStream fis = new FileInputStream(file);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 提取XLSX文本内容
     */
    private String extractFromXlsx(File file) throws IOException {
        logger.debug("提取XLSX文件: {}", file.getName());
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                text.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    boolean hasContent = false;
                    StringBuilder rowText = new StringBuilder();

                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.trim().isEmpty()) {
                            rowText.append(cellValue).append("\t");
                            hasContent = true;
                        }
                    }

                    if (hasContent) {
                        text.append(rowText.toString().trim()).append("\n");
                    }
                }
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * 提取XLS文本内容
     */
    private String extractFromXls(File file) throws IOException {
        logger.debug("提取XLS文件: {}", file.getName());
        StringBuilder text = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                text.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    boolean hasContent = false;
                    StringBuilder rowText = new StringBuilder();

                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (!cellValue.trim().isEmpty()) {
                            rowText.append(cellValue).append("\t");
                            hasContent = true;
                        }
                    }

                    if (hasContent) {
                        text.append(rowText.toString().trim()).append("\n");
                    }
                }
                text.append("\n");
            }
        }
        return text.toString();
    }

    /**
     * 提取CSV文本内容
     */
    private String extractFromCsv(File file) throws IOException {
        logger.debug("提取CSV文件: {}", file.getName());
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * 获取Excel单元格的字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        // 格式化数字，避免科学计数法
                        double numericValue = cell.getNumericCellValue();
                        if (numericValue == (long) numericValue) {
                            return String.valueOf((long) numericValue);
                        } else {
                            return String.valueOf(numericValue);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    try {
                        // 尝试获取公式计算结果
                        return getCellValueAsString(cell);
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                case BLANK:
                    return "";
                case ERROR:
                    return "#ERROR#";
                default:
                    return "";
            }
        } catch (Exception e) {
            logger.warn("读取单元格值失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 检查文件是否为支持的类型
     * @param file 要检查的文件
     * @return true if supported, false otherwise
     */
    public boolean isSupportedFileType(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".txt") ||
                fileName.endsWith(".md") ||
                fileName.endsWith(".pdf") ||
                fileName.endsWith(".docx") ||
                fileName.endsWith(".doc") ||
                fileName.endsWith(".xlsx") ||
                fileName.endsWith(".xls") ||
                fileName.endsWith(".csv");
    }

    /**
     * 检查文件是否为PDF或DOCX格式（API文档指定支持的格式）
     * @param file 要检查的文件
     * @return true if PDF or DOCX, false otherwise
     */
    public boolean isApiSupportedFileType(File file) {
        if (file == null || !file.exists()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".pdf") || fileName.endsWith(".docx");
    }

    /**
     * 提取文件中的文本内容（主要方法）
     * 这是对外提供的主要接口方法
     *
     * @param file 要提取文本的文件
     * @return 提取出的文本内容
     * @throws Exception 提取失败时抛出异常
     */
    public String getExtractedText(File file) throws Exception {
        // 1. 检查文件是否存在
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + (file != null ? file.getPath() : "null"));
        }

        // 2. 检查文件大小（防止过大文件）
        long fileSizeInMB = file.length() / (1024 * 1024);
        if (fileSizeInMB > 50) { // 限制50MB
            throw new IllegalArgumentException("文件过大: " + fileSizeInMB + "MB，最大支持50MB");
        }

        // 3. 检查是否为支持的文件类型
        if (!isSupportedFileType(file)) {
            throw new UnsupportedOperationException("不支持的文件类型: " + file.getName());
        }

        logger.info("开始提取文件文本: {} (大小: {}KB)", file.getName(), file.length() / 1024);

        // 4. 提取文本
        String extractedText = extractText(file);

        // 5. 检查提取的文本是否为空
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new IllegalArgumentException("文件内容为空或无法提取文本: " + file.getName());
        }

        // 6. 清理和优化文本
        extractedText = cleanText(extractedText);

        logger.info("文本提取完成: {} (提取文本长度: {}字符)", file.getName(), extractedText.length());

        return extractedText;
    }

    /**
     * 清理和优化提取的文本
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 1. 去除多余的空白字符，但保留必要的换行
        text = text.replaceAll("[ \\t]+", " "); // 多个空格/tab替换为单个空格
        text = text.replaceAll("\\n\\s*\\n", "\n"); // 多个换行替换为单个换行
        text = text.replaceAll("\\r\\n", "\n"); // 统一换行符
        text = text.replaceAll("\\r", "\n"); // Mac格式换行符

        // 2. 去除首尾空白
        text = text.trim();

        // 3. 限制文本长度（防止过长文本）
        if (text.length() > 100000) { // 限制10万字符
            logger.warn("文本过长，截取前10万字符");
            text = text.substring(0, 100000) + "...[文本被截断]";
        }

        return text;
    }

    /**
     * 获取文件类型描述
     * @param file 文件
     * @return 文件类型描述
     */
    public String getFileTypeDescription(File file) {
        if (file == null || !file.exists()) {
            return "未知";
        }

        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) return "PDF文档";
        if (fileName.endsWith(".docx")) return "Word文档(DOCX)";
        if (fileName.endsWith(".doc")) return "Word文档(DOC)";
        if (fileName.endsWith(".xlsx")) return "Excel文档(XLSX)";
        if (fileName.endsWith(".xls")) return "Excel文档(XLS)";
        if (fileName.endsWith(".txt")) return "纯文本文件";
        if (fileName.endsWith(".md")) return "Markdown文档";
        if (fileName.endsWith(".csv")) return "CSV文件";

        return "未知格式";
    }
}
