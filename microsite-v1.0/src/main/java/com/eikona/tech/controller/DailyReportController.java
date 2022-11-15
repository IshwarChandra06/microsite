package com.eikona.tech.controller;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.DailyReport;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.service.DailyAttendanceService;
import com.eikona.tech.util.ExportDailyReports;

@Controller
public class DailyReportController {

	@Autowired
	private DailyAttendanceService dailyAttendanceService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ExportDailyReports exportDailyReports;
	
	@GetMapping("/daily-reports")
	public String viewHomePage(Model model) {
		return "reports/daily_report";
	}

	@RequestMapping(value = "/generate/daily-reports", method = RequestMethod.GET)
	public String seeDataByOrganization(Model model, Principal principal) {
		
		User userObj = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = userObj.getOrganization().getName();
		model.addAttribute("organizationList", orgName);
		return "reports/generate_daily_report";
	}
	
	@RequestMapping(value = "/api/search/daily-reports", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_view')")
	public @ResponseBody PaginationDto<DailyReport> search(Long id, String sDate,String eDate, String employeeId, String employeeName, String office, String department, String designation,
			String status,int pageno, String sortField, String sortDir, Principal principal) {
		
		User userObj = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == userObj.getOrganization()? null : userObj.getOrganization().getName());
		PaginationDto<DailyReport> dtoList = dailyAttendanceService.searchByField(id, sDate, eDate, employeeId, employeeName, office, department, designation,status, pageno, sortField, sortDir, orgName);
		
		return dtoList;
	}
	
	@RequestMapping(value = "/get/data-by-organization", method = RequestMethod.GET)
	public @ResponseBody List<DailyReport> getDataForDailyReportMap(String sDate, String eDate, Principal principal) {
		
		User userObj = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == userObj.getOrganization()? null : userObj.getOrganization().getName());
		
		return dailyAttendanceService.generateDailyAttendance(sDate, eDate, orgName);
		
	}
	
	@RequestMapping(value="/api/daily-attendance/export-to-file",method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('dailyreport_export')")
	public void exportToFile(HttpServletResponse response, Long id, String sDate, String eDate, String employeeName,String employeeId, 
			String designation, String department,String status, String flag, Principal principal) {
		User userObj = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == userObj.getOrganization()? null : userObj.getOrganization().getName());
		 response.setContentType("application/octet-stream");
			DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
			String currentDateTime = dateFormat.format(new Date());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename=Daily_Report" + currentDateTime + "."+flag;
			response.setHeader(headerKey, headerValue);
		try {
			exportDailyReports.fileExportBySearchValue(response, id, sDate, eDate, employeeName,employeeId, designation, department, status, flag, orgName );
		} catch (Exception  e) {
			e.printStackTrace();
		}
	}
}
