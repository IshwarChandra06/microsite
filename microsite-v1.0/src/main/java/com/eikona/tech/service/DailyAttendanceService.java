package com.eikona.tech.service;

import java.util.List;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.DailyReport;

public interface DailyAttendanceService {
    
	List<DailyReport> generateDailyAttendance(String sDate, String eDate, String orgName);

	PaginationDto<DailyReport> searchByField(Long id, String sDate, String eDate, String employeeId, String employeeName,
			String office, String department, String designation,String status, int pageno, String sortField, String sortDir, String orgName);

	List<DailyReport> getAllDailyReport();

}

