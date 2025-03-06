package com.linkedin.jobSearch.linkedin_job_finder.service;

import com.linkedin.jobSearch.linkedin_job_finder.model.JobListing;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service for exporting job listings to Excel format
 */
@Service
public class ExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExcelExportService.class);

    private static final String[] HEADERS = {
            "Title", "Company", "Location", "Posted Date", "Salary",
            "Job Type", "Description", "Job URL", "Source"
    };

    /**
     * Exports a list of job listings to an Excel file
     *
     * @param jobListings The job listings to export
     * @return Byte array containing the Excel file
     * @throws IOException If there's an error creating the Excel file
     */
    public byte[] exportToExcel(List<JobListing> jobListings) throws IOException {
        logger.info("Exporting {} job listings to Excel", jobListings.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Job Listings");

            // Create header row with styles
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Create the header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (JobListing job : jobListings) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(job.getTitle() != null ? job.getTitle() : "");
                row.createCell(1).setCellValue(job.getCompany() != null ? job.getCompany() : "");
                row.createCell(2).setCellValue(job.getLocation() != null ? job.getLocation() : "");
                row.createCell(3).setCellValue(job.getPostedDate() != null ? job.getPostedDate() : "");
                row.createCell(4).setCellValue(job.getSalary() != null ? job.getSalary() : "");
                row.createCell(5).setCellValue(job.getJobType() != null ? job.getJobType() : "");
                row.createCell(6).setCellValue(job.getDescription() != null ? job.getDescription() : "");
                row.createCell(7).setCellValue(job.getJobUrl() != null ? job.getJobUrl() : "");
                row.createCell(8).setCellValue(job.getSource() != null ? job.getSource() : "");
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
                // If column is too wide (for URLs or descriptions), cap it
                if (sheet.getColumnWidth(i) > 10000) {
                    sheet.setColumnWidth(i, 10000);
                }
            }

            // Write the workbook to a byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            logger.info("Successfully exported job listings to Excel");
            return outputStream.toByteArray();
        }
    }
}