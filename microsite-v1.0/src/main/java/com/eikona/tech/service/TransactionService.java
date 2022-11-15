package com.eikona.tech.service;

import com.eikona.tech.dto.PaginationDto;
import com.eikona.tech.entity.Transaction;

public interface TransactionService {

	PaginationDto<Transaction> searchByField(String employee, Long id, String sDate, String eDate, String employeeId,
			String employeeName, String office, String device, String department, int pageno,
			String sortField, String sortDir, String orgName);

}
