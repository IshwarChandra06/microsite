	package com.eikona.tech.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Employee;
import com.eikona.tech.entity.Transaction;
import com.eikona.tech.entity.User;
import com.eikona.tech.repository.TransactionRepository;
import com.eikona.tech.repository.UserRepository;
import com.eikona.tech.service.DepartmentService;
import com.eikona.tech.service.DesignationService;
import com.eikona.tech.service.OrganizationService;
import com.eikona.tech.service.TransactionService;
import com.eikona.tech.service.impl.model.TransactionServiceImpl;
import com.eikona.tech.util.ImageProcessingUtil;

@Controller
public class TransactionController {
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private ImageProcessingUtil imageProcessingUtil;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private TransactionServiceImpl transactionserviceImpl;
	
	@Autowired
	private OrganizationService organizationService;
	
	@Autowired
	private DepartmentService departmentService;
	
	@Autowired
	private DesignationService designationService;
	
	@Autowired
	private TransactionRepository transactionRepository;
	
	@GetMapping("/transaction")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String transactionList(Model model) {
		return "transaction/transaction_list";
	}
	
	@GetMapping("/add/employee-from-transaction/{id}")
	@PreAuthorize("hasAuthority('transaction_view')")
	public String editEmployee(@PathVariable(value = "id") long id, Model model, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());

		model.addAttribute("listOrganization", organizationService.getById(user.getOrganization().getId()));
		model.addAttribute("listDepartment", departmentService.getAllByOrganization(user.getOrganization()));
		model.addAttribute("listDesignation", designationService.getAllByOrganization(user.getOrganization()));
		
		Transaction trans= transactionRepository.findById(id).get();

		Employee employee = new Employee();
		employee.setEmpId(trans.getEmpId());
		employee.setName(trans.getName());
		
		model.addAttribute("employee", employee);
		model.addAttribute("title", "New Employee");
		return "employee/employee_new";
	}

	//time log
	@GetMapping("/upload/timelog-transaction")
	public String indexTimeLog() {
		return "multipartfile/upload_timelog";
	}

	@PostMapping("/import/timelog-transaction/excel")
	public String uploadMultipartFileTimeLog(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeTimeLog(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/upload_timelog";
	}

	//temperature
	@GetMapping("/upload/temp-transaction")
	public String indextemp() {
		return "multipartfile/upload_temperature";
	}

	@PostMapping("/import/temp-transaction/excel")
	public String uploadMultipartFileTemp(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeTemp(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/upload_temperature";
	}

	// biosecurity
	@GetMapping("/upload/biosecurity")
	public String indexbio() {
		return "multipartfile/upload_biosecurity";
	}

	@PostMapping("/import/biosecurity-transaction/excel")
	public String uploadMultipartFileBio(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeBio(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/uploadbio";
	}

	//Employee Details
	@GetMapping("/upload/employee-details")
	public String indexEmp() {
		return "multipartfile/upload_employeedetails";
	}

	@PostMapping("/import/employeedetails-transaction/excel")
	public String uploadMultipartFileEmp(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeEmp(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/upload_employeedetails";
	}

	// Easy Time Pro
	@GetMapping("/upload/easy-time-pro")
	public String indexEtp() {
		return "multipartfile/upload_easytimepro";
	}

	@PostMapping("/import/easytimepro-transaction/excel")
	public String uploadMultipartFileEtp(@RequestParam("uploadfile") MultipartFile file, Model model) {
		try {
			transactionserviceImpl.storeEtp(file);
			model.addAttribute("message", "File uploaded successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("message", "Fail! -> uploaded filename: " + file.getOriginalFilename());
		}
		return "multipartfile/upload_easytimepro";
	}
	
	//search data
	@RequestMapping(value = "/api/search/transaction", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('transaction_view')")
	public @ResponseBody PaginationDto<Transaction> search(String employee, Long id, String sDate,String eDate, String employeeId, String employeeName, String device, String department, String designation,
			int pageno, String sortField, String sortDir, Principal principal) {
		
		User user = userRepository.findByUserNameAndIsDeletedFalse(principal.getName());
		String orgName = (null == user.getOrganization()?null : user.getOrganization().getName());
		PaginationDto<Transaction> dtoList = transactionService.searchByField(employee, id, sDate, eDate, employeeId, employeeName, device, department, designation, pageno, sortField, sortDir, orgName);
		
		List<Transaction> eventsList = dtoList.getData();
		List<Transaction> transactionList = new ArrayList<Transaction>();
		for (Transaction trans : eventsList) {
			byte[] image = imageProcessingUtil.searchTransactionImage(trans);
			trans.setCropImageByte(image);
			transactionList.add(trans);
		}
		dtoList.setData(transactionList);
		return dtoList;
	}
}
